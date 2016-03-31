package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.DBUtility.Users;

@Path("/auth/login")
public class Login {
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());
	
	@GET
	@Path("/{email}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@PathParam("email") String email, 
						  @PathParam("password") String password)
	{
		String errorMsg = "";
		String token = UUID.randomUUID().toString();
		Users u;
		String query = null;
		try 
		{
			query = "SELECT * FROM Users WHERE email = '" + email + "'";
			u = new Users();
			u.populateObject(query, u);
			password = u.getPassword();
			if (u.getPassword().compareTo(password) != 0)
			{
				errorMsg = LanguageResources.getResource("auth.wrongCredentials");
				return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
			}
		}
		catch (Exception e) {
			if (e.getMessage().compareTo("No record found") == 0)
			{
				errorMsg = LanguageResources.getResource("auth.mailNotRegistered");
			}
			else
			{
				errorMsg = LanguageResources.getResource("generic.execError") + " (" + e.getMessage() + ")";
			}
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}
		// Check if this user already has a token
		UsersAuth ua = null;
		try
		{
			ua = new UsersAuth();
			query = "SELECT * FROM UsersAuth WHERE userId = " + u.getIdUsers();
			ua.populateObject(query, ua);
			ua.setLastRefreshed(new Date());
			ua.update("idUsersAuth");
		}
		catch (Exception e) {
			if (e.getMessage().compareTo("No record found") == 0)
			{
				ua.setUserId(u.getIdUsers());
				ua.setToken(token);
				ua.setCreated(new Date());
				ua.setLastRefreshed(ua.getCreated());
				try 
				{
					ua.insert("idUsersAuth", ua);
				} 
				catch (Exception e1) {
					log.error("Error inserting token for user id " + u.getIdUsers());
				}
			}
			else
			{
				errorMsg = LanguageResources.getResource("generic.execError") +  " (" + e.getMessage() + ")";
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
			}
		}

		String uri = prop.getWebHost() + "/" + prop.getRedirectHome() + "?token=" + token;
		try {
			URI location;
			location = new URI(uri);
			return Response.seeOther(location).build();
		} 
		catch (URISyntaxException e) 
		{
			log.error("Exception setting redirection URL '" + uri + "': " + e.getMessage());
		}
		return Response.status(Response.Status.OK).build();
	}

	
	@GET
	@Path("/token/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginByToken(@PathParam("token") String token)
	{
		String errorMsg = "";
		try 
		{
			String query = "SELECT * FROM UserAuth WHERE token = '" + token + "'";
			UsersAuth ua = new UsersAuth();
			ua.populateObject(query, ua);
			if (prop.getSessionExpireTime() != 0)
			{
				if (ua.getLastRefreshed().getTime() + prop.getSessionExpireTime() * 1000 < new Date().getTime())
				{
					errorMsg = LanguageResources.getResource("auth.sessionExpired");
					return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
				}
			}
		}
		catch (Exception e) {
			errorMsg = LanguageResources.getResource("auth.sessionExpired");
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}
		String uri = prop.getWebHost() + "/" + prop.getRedirectHome() + "?token=" + token;
		try {
			URI location;
			location = new URI(uri);
			return Response.temporaryRedirect(location).build();
		} 
		catch (URISyntaxException e) {
			log.error("Exception setting redirection URL '" + uri + "': " + e.getMessage());
		}
		return Response.status(Response.Status.OK).build();
	}
	
	
}
