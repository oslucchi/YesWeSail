package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.DBUtility.UsersAuth;

@Path("/auth/logout")
public class Logout {
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());

	@GET
	@Path("/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginByToken(@PathParam("token") String token)
	{
		try 
		{
			String query = "SELECT * FROM UserAuth WHERE token = '" + token + "'";
			UsersAuth ua = new UsersAuth();
			ua.populateObject(query, ua);
			ua.delete(ua.getIdUsersAuth());
		}
		catch (Exception e) {
			// Silently accept
			;
		}
		URI location;
		try {
			location = new URI(prop.getWebHost());
			return Response.seeOther(location).build();
		} 
		catch (URISyntaxException e) {
			log.error("exception on redirect: " + e.getMessage());
		}
		return Response.status(Response.Status.OK).build();
	}
	
	
}
