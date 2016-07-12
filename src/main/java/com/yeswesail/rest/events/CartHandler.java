package com.yeswesail.rest.events;

import java.math.BigDecimal;
import java.net.URI;

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
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.Cart;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.TicketsInCart;
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
		int languageId = SessionData.getInstance().getLanguage(token);
		TicketsInCart[] cart = null;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.connect();
			cart = Cart.getCartItems(conn, SessionData.getInstance().getBasicProfile(token).getIdUsers(), languageId);
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		if (jh.jasonize(cart, languageId) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		
		return Response.status(Response.Status.OK).entity(jh.json).build();
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
				if (ev.getTicketType() == EventTickets.ALL_BOAT)
				{
					EventTickets[] et = EventTickets.getAllTicketByEventId(ev.getEventId(), languageId);
					for (EventTickets item : et)
					{
						if (item.getTicketType() == EventTickets.ALL_BOAT)
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
		
		getBTGateway();
		
		Utils jsonizer = new Utils();
		DBConnection conn = null;
		TicketLocks[] tickets = null;
		int amount = 0;
		try
		{
			conn = DBInterface.TransactionStart();
			tickets = TicketLocks.findByUserId(conn, userId);
			for(TicketLocks t : tickets)
			{
				EventTickets et = new EventTickets(conn, t.getEventTicketId());
				amount = amount + et.getPrice();
			}
			log.trace("Requesting payment for user " + userId + ". Amount requested " + amount);
			TransactionRequest request = new TransactionRequest()
				    .amount(new BigDecimal(amount))
				    .paymentMethodNonce(nonce)
				    .options()
				    .submitForSettlement(true)
				    .done();
			Result<Transaction> result = gateway.transaction().sale(request);
	        
			if (result.isSuccess()) 
	        {
	            Transaction transaction = result.getTarget();
				
	            log.trace("Payment completed. Transaction id " + transaction.getId());
				for(TicketLocks t : tickets)
				{
					EventTicketsSold ev = new EventTicketsSold();
					ev.setEventTicketId(t.getEventTicketId());
					ev.setUserId(t.getUserId());
					ev.setTransactionId(transaction.getId());
					ev.insert(conn, "idEventTicketsSold", ev);
					t.delete(conn, t.getIdTicketLocks());
				}
				
				DBInterface.TransactionCommit(conn);
				if (method.toUpperCase().compareTo("PP") == 0)
				{
					jsonizer.addToJsonContainer("transactionId", transaction.getId(), true);
					return Response
							.status(Response.Status.OK)
							.entity(jsonizer.jsonize())
							.build();
				}
				else if (method.toUpperCase().compareTo("CC") == 0)
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
				else if (method.toUpperCase().compareTo("CC") == 0)
				{
		            String url = prop.getWebHost() + "/#/cart/error?responseCode=" + transaction.getProcessorResponseCode();
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
				else if (method.toUpperCase().compareTo("CC") == 0)
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
			if ((method.toUpperCase().compareTo("CC") != 0) &&
			    (method.toUpperCase().compareTo("PP") != 0))
			{
				jsonizer.addToJsonContainer("responseCode", "Bad method request", true);
				return Response
						.status(Response.Status.BAD_REQUEST)
						.entity(jsonizer.jsonize())
						.build();
			}
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity("").build();
	}
	
}
