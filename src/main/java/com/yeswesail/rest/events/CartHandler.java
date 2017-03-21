package com.yeswesail.rest.events;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.Cart;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.TicketsInCart;
import com.yeswesail.rest.DBUtility.TicketsInCart.Tickets;
import com.yeswesail.rest.jsonInt.TicketJson;

@Path("/cart")
public class CartHandler {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	JsonHandler jh = new JsonHandler();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCart(@HeaderParam("Authorization") String token)
	{
		Genson genson = new Genson();
		int languageId = SessionData.getInstance().getLanguage(token);
		TicketsInCart[] cart = null;
		DBConnection conn = null;
		HashMap<String, Object> jsonResponse = new HashMap<>();
		try
		{
			conn = DBInterface.connect();
			cart = Cart.getCartItems(conn, SessionData.getInstance().getBasicProfile(token).getIdUsers(), languageId);
			jsonResponse.put("tickets", cart);
			Date minLockTime = null;
			int ticketsCount = 0;
			for(TicketsInCart item : cart)
			{
				ticketsCount += item.getTickets().size();
				for(Tickets ticket : item.getTickets())
				{
					if ((minLockTime == null) ||
						(minLockTime.getTime() > ticket.getLockTime().getTime()))
					{
						minLockTime = ticket.getLockTime();
					}
				}
			}
			jsonResponse.put("ticketsCount", ticketsCount);
			jsonResponse.put("expiring", minLockTime);
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response emptyCart(@HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			TicketLocks[] tickets = TicketLocks.findByUserId(conn, sd.getBasicProfile(token).getIdUsers()); 
			for(TicketLocks ticket : tickets)
			{
				EventTickets ev = new EventTickets(conn, ticket.getEventTicketId());
				if (ev.getTicketType() == EventTickets.WHOLE_BOAT)
				{
					EventTickets[] et = EventTickets.getAllTicketByEventId(ev.getEventId(), languageId);
					for (EventTickets item : et)
					{
						if (item.getTicketType() == EventTickets.WHOLE_BOAT)
							continue;
						while(item.getBooked() != 0)
						{
							item.releaseATicket();
						}
						item.update(conn, "idEventTickets");
					}
				}
				ev.releaseATicket();
				ev.update(conn, "idEventTickets");
				ticket.delete(conn, ticket.getIdTicketLocks());
			}
			
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e)
		{
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/paypal/process/{userId}/{paymentRef}")
	public Response paypalProcess(@QueryParam("paymentId") String paymentId,
								  @QueryParam("PayerID") String payerId,
								  @QueryParam("token") String token,
								  @PathParam("userId") int userId,
								  @PathParam("paymentRef") String paymentRef)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(userId);
		
		log.trace("Paypal processs called back on userId " + userId + " paypal token '" + token + "'" +
				  "' paymnet Id '" + paymentId + "' payer id '" + payerId + "'");
		URI location = null;

		APIContext context = new APIContext(prop.getPaypalClientId(), prop.getPaypalClientSecret(), prop.getPaypalMode());
        Payment payment = new Payment();
        payment.setId(paymentId);
        
        PaymentExecution pe = new PaymentExecution();
        pe.setPayerId(payerId);
        
        try {
			payment.execute(context, pe);
		} 
        catch (PayPalRESTException e) {
            try 
            {
            	location = new URI(prop.getWebHost() + "/#/cart/error?responseCode=" + 
      				  					URLEncoder.encode(e.getMessage(),"UTF-8"));
            }
            catch(Exception e1)
            {
    			return Response
    					.status(Response.Status.UNAUTHORIZED)
    					.entity("{responseCode : 'Error executing PayPal payment. Please contact YWS administrators'}")
    					.build();
            }
			return Response.seeOther(location).build();
		}

        log.trace("Payment completed. Transaction id " + payment.getId());
        
        DBConnection conn = null;
        try
        {
        	conn = DBInterface.TransactionStart();
        	TicketLocks[] tickets = TicketLocks.findByPaymentRef(conn, paymentRef);
        	processTicketsPaid(conn, tickets, payment, languageId);
			DBInterface.TransactionCommit(conn);
        }
        catch(Exception e)
        {
        	// TODO should send the admin an email to advise
        	log.error("Exception " + e.getMessage() + " updating tickets for user " + userId);
    		DBInterface.TransactionRollback(conn);
        }
        finally
        {
        	// TODO the message to the user should be consistent with the fact the payment is done
        	// but the tickets have not been received
        	DBInterface.disconnect(conn);
        }

        try 
		{
			location = new URI(prop.getWebHost() + "/#/cart/success?transactionId=" + payment.getId());
		} 
		catch (URISyntaxException e) {
			return Response
					.status(Response.Status.OK)
					.entity("{}")
					.build();
		}
		return Response.seeOther(location).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/paypal/cancel/{userId}")
	public Response paypalCancel(@PathParam("userId") int userId,
								 @QueryParam("token") String ppToken)
	{
		log.trace("Paypal cancel called back on userId " + userId + " paypal token '" + ppToken + "'");
		URI location = null;
		int languageId;
		SessionData sd = SessionData.getInstance();

		languageId = sd.getLanguage(userId);

		String url = null;
		try 
		{
			url = prop.getWebHost() + "/#/cart/error?responseCode=" + 
				  URLEncoder.encode(
						  LanguageResources.getResource(languageId, "payment.paypal.requestCancelled"),
						  "UTF-8");

			location = new URI(url);
		} 
		catch (URISyntaxException | UnsupportedEncodingException e) {
			log.warn("Exception " + e.getMessage() + " On redirecting to '" + url + "'");
					
			return Response
					.status(Response.Status.OK)
					.entity("{}")
					.build();
		} 
		log.debug("redirecting to " + location.getPath());
		return Response.seeOther(location).build();
	}
	

	private Payment getPPgateway(int userId, ArrayList<TicketLocks> tickets, DBConnection conn, int languageId)
	{
		log.debug("Connecting to PayPal context " + prop.getPaypalMode());
        APIContext context = new APIContext(prop.getPaypalClientId(), prop.getPaypalClientSecret(), prop.getPaypalMode());

        RedirectUrls paypalUrl = new RedirectUrls();
        
        paypalUrl.setReturnUrl(prop.getWebHost() + "/rest/cart/paypal/process/" + userId + "/" + tickets.get(0).getPaymentRef());
        paypalUrl.setCancelUrl(prop.getWebHost() + "/rest/cart/paypal/cancel/" + userId + "/" + tickets.get(0).getPaymentRef());
        List<com.paypal.api.payments.Transaction> transactions = new ArrayList<com.paypal.api.payments.Transaction>();

        int amount = 0;
        try
        {
	        for(TicketLocks ticket : tickets)
	        {
	        	EventTickets ev = new EventTickets(conn, ticket.getEventTicketId());
	        	amount += ev.getPrice();
	        }
        }
        catch(Exception e)
        {
			log.error("Unable to create ticket list and calculate amount. Exception '" + 
					  e.getMessage() + "' while creating a PayPal payment");
			return null;        	
        }
        
		com.paypal.api.payments.Transaction t = new com.paypal.api.payments.Transaction();
        t.setAmount(
        		new Amount()
    			.setCurrency("EUR")
    			.setTotal(Integer.toString(amount)))
        		.setDescription(LanguageResources.getResource(languageId, "ticket.payment.description"))
//        		.setCustom(Integer.toString(ticket.getIdTicketLocks()))
        		;
        transactions.add(t);
        
        Payment payment = new Payment()
        			.setIntent("sale")
        			.setRedirectUrls(paypalUrl)
        			.setPayer(new Payer().setPaymentMethod("paypal"))
        			.setTransactions(transactions);
        
        Payment returnVal = null;
        try {
			returnVal = payment.create(context);
		} 
        catch (PayPalRESTException e) {
			log.error("Exception '" + e.getMessage() + "' while creating a PayPal payment");
			return null;
		}
        return returnVal;
	}

	private void processTicketsPaid(DBConnection conn, TicketLocks[] tickets, Payment payment, int languageId) 
			throws Exception
	{
		String payload = "";
		String sep = "";
		int userId = -1;
		int amount = 0;
		for(TicketLocks t : tickets)
		{
			userId = t.getUserId();
			EventTicketsSold ev;
			ev = new EventTicketsSold();
			ev.setEventTicketId(t.getEventTicketId());
			ev.setUserId(t.getUserId());
			ev.setTransactionId(payment.getId());
			ev.insert(conn, "idEventTicketsSold", ev);
			
			EventTickets et = new EventTickets();
			et.getEventTickets(conn, t.getEventTicketId(), languageId);
			Events e = new Events();
			e.getEvents(conn, et.getEventId(), languageId);
			
			t.delete(conn, t.getIdTicketLocks());
			payload += sep + "{" +
							 "\"eventTicketId\" : " + et.getIdEventTickets() + ", " +
							 "\"eventTicketDescription\" : \"" + et.getDescription() + "\", " +
							 "\"eventDescription\" : \"" + e.getTitle() + "\"," +
							 "\"amount\" : " + et.getPrice() +  
							 "}";
			sep = ",";
			amount += et.getPrice();
		}

		payload = "{" +
					"\"userId\" : " + userId + ", " +
					"\"paymentId\" : \"" + payment.getId() + "\", " +
					"\"tickets\" : [" + payload + "], " +
					"\"totalAmount\" : " + amount + 
				  "}";
        
		Mailer.sendMail(prop.getAdminEmail(), "Passenger " + userId + " just bought a ticket", 
				"<p>Go to the requests admin page and confirm it</p>", null);

		PendingActions pa = new PendingActions();
		pa.setActionType(PendingActions.CONFIRM_TICKET);
		pa.setLink("rest/requests/" + PendingActions.CONFIRM_TICKET + "/" + userId);
		pa.setPayload(payload);
		pa.setCreated(new Date());
		pa.setStatus(Constants.STATUS_PENDING_APPROVAL);
		pa.setUserId(userId);
		pa.insert(conn, "idPendingActions", pa);
	}
	
	private Response payViaPaypal(DBConnection conn, int userId, int languageId, ArrayList<TicketLocks> tickets)
	{
		Payment payment = getPPgateway(userId, tickets, conn, languageId);
		Utils jsonizer = new Utils();
		if ((payment == null) || (payment.getLinks() == null))
		{
			log.error("payment didn't get through. Failure reason '" + payment.getFailureReason() + "'");
			DBInterface.TransactionRollback(conn);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.PRECONDITION_FAILED, null, 
										 languageId, "payment.paypal.unableToCreatePayment");
		}

		Iterator<Links> iter = payment.getLinks().iterator();
		while(iter.hasNext())
		{
			Links link = iter.next();
			if (link.getRel().equalsIgnoreCase("approval_url"))
			{
				jsonizer.addToJsonContainer("approval_url", link.getHref(), true);
				return Response
						.status(Response.Status.OK)
						.entity(jsonizer.jsonize())
						.build();
			}
		}
		return Utils.jsonizeResponse(Response.Status.PRECONDITION_FAILED, null, 
				 languageId, "payment.paypal.unableToCreatePayment");
	}

	
	@POST
	@Path("/checkout/{method}/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkout(@QueryParam("payment_method_nonce") String nonce,
							 @PathParam("userId") int userId,
							 @PathParam("method") String method,
							 @HeaderParam("Authorization") String token,
							 TicketJson[] ticketsToHandle)
	{
		int languageId;
		Response response = null;
		SessionData sd = SessionData.getInstance();
		String paymentRef = UUID.randomUUID().toString();
		
		if(token != null)
		{
			languageId = sd.getLanguage(token);
			userId = sd.getBasicProfile(token).getIdUsers();
		}
		else
		{
			languageId = sd.getLanguage(userId);
		}
		DBConnection conn = null;
		
		TicketLocks ticket = null;
		ArrayList<TicketLocks> tickets = new ArrayList<>();
		try {
			conn = DBInterface.TransactionStart();
			for(TicketJson t : ticketsToHandle)
			{
				ticket = new TicketLocks(conn, t.idTicketLocks);
				ticket.setPaymentRef(paymentRef);
				ticket.setStatus(Constants.STATUS_WAITING_FOR_TRANSACTION);
				ticket.update(conn, "idTicketLocks");
				tickets.add(ticket);
			}
			DBInterface.TransactionCommit(conn);
			response = payViaPaypal(conn, userId, languageId, tickets);
		}
		catch (Exception e) 
		{
			log.error("Unable to process tickets. Exception " + e.getMessage());
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, languageId, "generic.error");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		log.trace("proceeding with the effective payment");
		return response;
	}
	
}
