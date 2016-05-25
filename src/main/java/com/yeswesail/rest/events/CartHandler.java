package com.yeswesail.rest.events;

import java.math.BigDecimal;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
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
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.Cart;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.TicketsInCart;

@Path("/cart")
public class CartHandler {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	JsonHandler jh = new JsonHandler();
	private final static String braintreeMerchantId = "cx874cyfmhr5nd9h";
	private final static String braintreePublicKey = "rgbdtg3ddmsysphk";
	private final static String braintreePrivateKey = "710377eb22e01a14c0bdb8b6e5951fbf";
	static private BraintreeGateway gateway = null;
	
	@GET
	@Path("/")
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
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError"))
					.build();
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

	@GET
	@Path("generateToken")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateToken(@HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		if (gateway == null)
		{
			gateway = new BraintreeGateway(Environment.SANDBOX, braintreeMerchantId, braintreePublicKey, braintreePrivateKey);
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
	@Path("checkout")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkout(@QueryParam("payment_method_nonce") String nonce)
	{
		TransactionRequest request = new TransactionRequest()
			    .amount(new BigDecimal("10.00"))
			    .paymentMethodNonce(nonce)
			    .options()
			    .submitForSettlement(true)
			    .done();

		Result<Transaction> result = gateway.transaction().sale(request);
		
        if (result.isSuccess()) 
        {
            Transaction transaction = result.getTarget();
    		return Response.status(Response.Status.OK).entity("<H2>Welcome on board!!</H2>" + 
    					"Your payment was completed successfully.<br>" +
    					"The payment id for your reference is " + transaction.getId()).build();
        }
        else if (result.getTransaction() != null) 
        {
            Transaction transaction = result.getTransaction();
//            System.out.println("  Status: " + transaction.getStatus());
//            System.out.println("  Code: " + transaction.getProcessorResponseCode());
//            System.out.println("  Text: " + transaction.getProcessorResponseText());
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    				.entity("Error processing transaction '" + transaction.getProcessorResponseText()).build();
        }
        else 
        {
        	String multipleErrors = "Multiple errors occurred during transaction:<br>";
            for (ValidationError error : result.getErrors().getAllDeepValidationErrors())
            {
            	multipleErrors += "Attribute: " + error.getAttribute() +
		            			  "  Code: " + error.getCode() + 
		            			  "  Message: " + error.getMessage();
            	multipleErrors += "<br>";
            }
    		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(multipleErrors).build();
        }
	}
	
}
