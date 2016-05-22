package com.yeswesail.rest.events;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.Cart;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.TicketsInCart;

@Path("/cart")
public class CartHandler {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	JsonHandler jh = new JsonHandler();

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
}
