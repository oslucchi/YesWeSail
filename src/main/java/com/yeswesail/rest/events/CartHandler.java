package com.yeswesail.rest.events;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
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
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.Cart;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.TicketsInCart;
import com.yeswesail.rest.DBUtility.TicketsInCart.Tickets;
import com.yeswesail.rest.jsonInt.CartJson;
import com.yeswesail.rest.jsonInt.TicketJson;

@Path("/cart")
public class CartHandler {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	JsonHandler jh = new JsonHandler();
	static private BraintreeGateway gateway = null;
	
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
	@Path("paypal/process/{userId}")
	public Response paypalProcess(@QueryParam("paymentId") String paymentId,
								  @QueryParam("PayerID") String payerId,
								  @QueryParam("token") String token,
								  @PathParam("userId") int userId)
	{
		log.trace("Paypal processs called back on userId " + userId + " paypal token '" + token + "'" +
				  "' paymnet Id '" + paymentId + "' payer id '" + payerId + "'");
		URI location = null;

		APIContext context = new APIContext(prop.getPaypalClientId(), prop.getPaypalClientSecret(), "sandbox");
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
        	TicketLocks[] tickets = TicketLocks.findByUserId(conn, userId);
        	processTicketsPaid(conn, tickets, payment.getId());
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
	@Path("paypal/cancel/{userId}")
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
	

	private Payment getPPgateway(int userId)
	{
        APIContext context = new APIContext(prop.getPaypalClientId(), prop.getPaypalClientSecret(), "sandbox");

        RedirectUrls paypalUrl = new RedirectUrls();
        paypalUrl.setReturnUrl(prop.getWebHost() + "/rest/cart/paypal/process/" + userId);
        paypalUrl.setCancelUrl(prop.getWebHost() + "/rest/cart/paypal/cancel/" + userId);
        List<com.paypal.api.payments.Transaction> transactions = new ArrayList<com.paypal.api.payments.Transaction>();
        com.paypal.api.payments.Transaction t = new com.paypal.api.payments.Transaction();
        t.setAmount(
        		new Amount()
    			.setCurrency("EUR")
    			.setTotal("100"))
        	.setDescription("Osvaldo's payment");
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
	
	private void getBTGateway()
	{
		if (gateway == null)
		{
			gateway = new BraintreeGateway(Environment.SANDBOX, 
										   prop.getBraintreeMerchantId(), 
										   prop.getBraintreePublicKey(), 
										   prop.getBraintreePrivateKey());
		}
	}

	@POST
	@Path("generateToken")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateToken(CartJson[] ticketList, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		getBTGateway();
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			for(CartJson ticketListItem : ticketList)
			{
				for(TicketJson ticket : ticketListItem.tickets)
				{
					TicketLocks t = new TicketLocks(conn, ticket.idTicketLocks);
					if (!ticket.toBuy)
					{
						EventTickets et = new EventTickets(conn, t.getEventTicketId());
						et.releaseATicket();
						et.update(conn, "idEventTickets");
						t.delete(conn, t.getIdTicketLocks());
					}
				}
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
		
		String token = gateway.clientToken().generate();
		if (jh.jasonize(token, languageId) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	private void processTicketsPaid(DBConnection conn, TicketLocks[] tickets, String transactionId) 
			throws Exception
	{
		for(TicketLocks t : tickets)
		{
			EventTicketsSold ev;
			ev = new EventTicketsSold();
			ev.setEventTicketId(t.getEventTicketId());
			ev.setUserId(t.getUserId());
			ev.setTransactionId(transactionId);
			ev.insert(conn, "idEventTicketsSold", ev);
			t.delete(conn, t.getIdTicketLocks());
		}		
	}
	
	private int calculateTransactionAmount(DBConnection conn, int userId, TicketLocks[] tickets)
	{
		int amount = 0;
		try
		{
			for(TicketLocks t : tickets)
			{
				EventTickets et = new EventTickets(conn, t.getEventTicketId());
				amount = amount + et.getPrice();
			}
			log.trace("Payment for user " + userId + ". Amount requested " + amount);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage() + " retrieving tickets");
		}
		return amount;
	}

	private Response payViaBraintree(DBConnection conn, String nonce, String method,  
					int amount, int userId, int languageId, TicketLocks[] tickets)
	{
		Utils jsonizer = new Utils();

		if ((method.toUpperCase().compareTo("CC") != 0) &&
			    (method.toUpperCase().compareTo("PP") != 0))
		{
			jsonizer.addToJsonContainer("responseCode", "Bad method request", true);
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity(jsonizer.jsonize())
					.build();
		}

		getBTGateway();
		
		TransactionRequest request = new TransactionRequest()
			    .amount(new BigDecimal(amount))
			    .paymentMethodNonce(nonce)
			    .options()
			    .submitForSettlement(true)
			    .done();
		Result<Transaction> result = gateway.transaction().sale(request);


		try
		{
			if (result.isSuccess()) 
	        {
		        Transaction transaction = result.getTarget();
		        log.trace("Payment completed. Transaction id " + transaction.getId());
		        processTicketsPaid(conn, tickets, transaction.getId());
				DBInterface.TransactionCommit(conn);
		        
				if (method.toUpperCase().compareTo("PP") == 0)
				{
					jsonizer.addToJsonContainer("transactionId", transaction.getId(), true);
					return Response
							.status(Response.Status.OK)
							.entity(jsonizer.jsonize())
							.build();
				}
				else
				{
					URI location = new URI(prop.getWebHost() + "/#/cart/success?transactionId=" + transaction.getId());
					return Response.seeOther(location).build();
				}
	        }
	        else if (result.getTransaction() != null) 
	        {
				DBInterface.TransactionRollback(conn);
	            Transaction transaction = result.getTransaction();
	            String errorMsg = "Transaction rejected. Status: " + transaction.getStatus() + 
	            				  " Code: " + transaction.getProcessorResponseCode() + 
	            				  " Text: " + transaction.getProcessorResponseText(); 
	            log.warn(errorMsg);
				if (method.toUpperCase().compareTo("PP") == 0)
				{
					jsonizer.addToJsonContainer("responseCode", transaction.getProcessorResponseCode(), true);
					return Response
							.status(Response.Status.FORBIDDEN)
							.entity(jsonizer.jsonize())
							.build();
				}
				else
				{
		            String url = prop.getWebHost() + "/#/cart/error?responseCode=" + 
		            		URLEncoder.encode(transaction.getProcessorResponseCode(),"UTF-8");
					URI location = new URI(url);
					return Response.seeOther(location).build();
				}
	        }
	        else 
	        {
				DBInterface.TransactionRollback(conn);
	        	String multipleErrors = "Multiple errors occurred during transaction:<br>";
	            for (ValidationError error : result.getErrors().getAllDeepValidationErrors())
	            {
	            	multipleErrors += "Attribute: " + error.getAttribute() +
			            			  "  Code: " + error.getCode() + 
			            			  "  Message: " + error.getMessage();
	            	multipleErrors += "\n";
	            }
	            log.warn(multipleErrors);
				if (method.toUpperCase().compareTo("PP") == 0)
				{
					jsonizer.addToJsonContainer("responseCode", result.getMessage(), true);
					return Response
							.status(Response.Status.FORBIDDEN)
							.entity(jsonizer.jsonize())
							.build();
				}
				else
				{
		            String url = prop.getWebHost() + "/#/cart/error?responseCode=" + 
	            			 result.getErrors().getAllDeepValidationErrors().get(0).getCode();
					URI location = new URI(url);
					return Response.seeOther(location).build();
				}
	        }
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
	}

	private Response payViaPaypal(DBConnection conn, int amount, int userId, 
								  int languageId, TicketLocks[] tickets)
	{
		Payment payment = getPPgateway(userId);
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

	
	@GET
	@Path("checkout/{method}/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkout(@QueryParam("payment_method_nonce") String nonce,
							 @PathParam("userId") int userId,
							 @PathParam("method") String method,
							 @HeaderParam("Authorization") String token)
	{
		int languageId;
		SessionData sd = SessionData.getInstance();

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
		
		TicketLocks[] tickets = null;
		try {
			conn = DBInterface.TransactionStart();
			tickets = TicketLocks.findByUserId(conn, userId);
			for(TicketLocks ticket : tickets)
			{
				ticket.setStatus("W");
				ticket.update(conn, "idTicketLocks");
			}
		}
		catch (Exception e) 
		{
			log.error("Unable to process tickets. Exception " + e.getMessage());
			DBInterface.TransactionRollback(conn);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, languageId, "generic.error");
		}

		int amount = calculateTransactionAmount(conn, userId, tickets);
		if (amount == 0)
		{
			DBInterface.TransactionRollback(conn);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, null, languageId, "payment.cantfindtickets");
		}
		
		try
		{
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, languageId, "generic.error");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		if (nonce.equalsIgnoreCase("null"))
		{
			return payViaPaypal(conn, amount, userId, languageId, tickets);
		}
		else
		{
			return payViaBraintree(conn, nonce, method, amount, userId, languageId, tickets);
		}
	}
	
}
