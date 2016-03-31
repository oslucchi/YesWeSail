package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;


/*
 * Tra i parametri della funzione
 * 						   @Context HttpServletRequest httpRequest,
 */
@Path("/auth")
public class Auth {
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(@QueryParam("email") String email, 
						     @QueryParam("password") String password)
	{
		ApplicationProperties prop = new ApplicationProperties();
		String errorMsg = "";
		String token = UUID.randomUUID().toString();
		try 
		{
			Users u = new Users();
			u.setEmail(email);
			u.setPassword(password);
			u.setStatus("D");
			u.setIsShipOwner("F");
			u.setConnectedVia("P");
			u.setRoleId(1);
			int id = u.insertAndReturnId("idUsers", u);
			RegistrationConfirm rc = new RegistrationConfirm();
			rc.setUserId(id);
			rc.setToken(token);
			rc.setStatus("A");
			id = rc.insertAndReturnId("idRegistrationConfirm", rc);
		}
		catch (Exception e) {
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				errorMsg = LanguageResources.getResource("users.alreadyRegistered");
			}
			else
			{
				errorMsg = LanguageResources.getResource("generic.execError") + " (" + e.getMessage() + ")";
			}
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(errorMsg).build();
		}
		try
		{
			String httpLink = prop.getWebHost() + "/rest/auth/confirmUser/" + token;
	        String htmlText = LanguageResources.getResource("mail.body");
	        htmlText.replaceAll("%CNFMLINK%", httpLink);
	        String subject = LanguageResources.getResource("mail.subject");
			URL url = getClass().getResource("/images/mailLogo.png");
			String imagePath = url.getPath();
			Mailer.sendMail(email, subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			errorMsg = LanguageResources.getResource("mailer.sendError") + " (" + e.getMessage() + ")";
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
		}
		
		return Response.status(Response.Status.OK).entity(LanguageResources.getResource("auth.registerRedirectMsg")).build();
	}
	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("application/json")
	public Response login(@FormParam("username") String username, 
						  @FormParam("password") String password)
	{
		String errorMsg = "";
		String token = UUID.randomUUID().toString();
		Users u;
		String query = null;
		log.debug("Login called. Parameters: '" + username + "|" + password + "'");
		try 
		{
			query = "SELECT * FROM Users WHERE email = '" + username + "'";
			u = new Users();
			log.debug("Select user by email");
			u.populateObject(query, u);
			log.debug("Found. Password in database is '" + u.getPassword() + "'");
			if (u.getPassword().compareTo(password) != 0)
			{
				log.debug("Wrong password, returning UNAUTHORIZED");
				errorMsg = LanguageResources.getResource("auth.wrongCredentials");
				return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
			}
		}
		catch (Exception e) {
			if (e.getMessage().compareTo("No record found") == 0)
			{
				log.debug("Email not found, returning UNAUTHORIZED");
				errorMsg = LanguageResources.getResource("auth.mailNotRegistered");
			}
			else
			{
				log.debug("Generic error " + e.getMessage());
				errorMsg = LanguageResources.getResource("generic.execError") + " (" + e.getMessage() + ")";
			}
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}
		// Check if this user already has a token
		UsersAuth ua = null;
		try
		{
			log.trace("Searching for a valid token for the user in DB");
			ua = new UsersAuth();
			query = "SELECT * FROM UsersAuth WHERE userId = " + u.getIdUsers();
			ua.populateObject(query, ua);
			ua.setLastRefreshed(new Date());
			log.trace("Refreshing the last access");
			ua.update("idUsersAuth");
		}
		catch (Exception e) {
			if (e.getMessage().compareTo("No record found") == 0)
			{
				log.debug("token not valid in DB, creating new one");
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
			log.trace("Redirect to " + uri);
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
