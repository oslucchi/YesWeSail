package com.yeswesail.rest.login;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.AuthJson;


@Path("/auth")
public class Auth {
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());
	Genson genson = new Genson();

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(AuthJson jsonIn) 
	{
		ApplicationProperties prop = new ApplicationProperties();
		String errorMsg = "";
		String token = UUID.randomUUID().toString();
		try 
		{
			Users u = new Users();
			u.setEmail(jsonIn.username);
			u.setPassword(jsonIn.password);
			u.setName(jsonIn.firstName);
			u.setSurname(jsonIn.lastName);
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
			Mailer.sendMail(jsonIn.username, subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			errorMsg = LanguageResources.getResource("mailer.sendError") + " (" + e.getMessage() + ")";
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
		}
		
		return Response.status(Response.Status.OK)
				.entity(LanguageResources.getResource("auth.registerRedirectMsg")).build();
	}

	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(AuthJson jsonIn) 
	{ 
		String username = jsonIn.username; 
		String password = jsonIn.password; 
		String errorMsg = "";
		Users u;
		String query = null;

		/*
		 * A new login always requires a new token to be generated
		 */
		String token = UUID.randomUUID().toString();
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
			log.trace("Setting up the new token for the user in DB");
			ua = new UsersAuth();
			query = "SELECT * FROM UsersAuth WHERE userId = " + u.getIdUsers();
			ua.populateObject(query, ua);
			ua.setToken(token);
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
		SessionData sa = SessionData.getInstance();
		Object[] userProfile = sa.getWholeProfile(u.getIdUsers());
		if (userProfile == null)
		{
			userProfile = new Object[2];
		}
		userProfile[0] = u;
		try 
		{
			userProfile[1] = AddressInfo.findUserId(u.getIdUsers());
		}
		catch (Exception e) 
		{
			;
		}
		sa.updateSession(u.getIdUsers(), userProfile, token);

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("token", token);
		jsonResponse.put("user", u);
		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}
	
	@POST
	@Path("/loginByToken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginByToken(AuthJson jsonIn)
	{
		String errorMsg = "";
		UsersAuth ua = null;
		SessionData sa = SessionData.getInstance();
		
		try 
		{
			ua = UsersAuth.findToken(jsonIn.token);
			if (prop.getSessionExpireTime() != 0)
			{
				if (ua.getLastRefreshed().getTime() + prop.getSessionExpireTime() * 1000 < new Date().getTime())
				{
					ua.delete(ua.getIdUsersAuth());
					sa.removeUser(jsonIn.token);
					errorMsg = LanguageResources.getResource("auth.sessionExpired");
					return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
				}
			}
		}
		catch (Exception e) {
			sa.removeUser(jsonIn.token);
			errorMsg = LanguageResources.getResource("auth.sessionExpired");
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}

		Object[] userProfile = sa.getWholeProfile(jsonIn.token);
		try 
		{
			ua.setLastRefreshed(new Date());
			ua.update("idUsersAuth");
			if (userProfile == null)
			{
				sa.addUser(ua.getUserId());
				userProfile = new Object[2];
				userProfile = sa.getWholeProfile(ua.getToken());
			}
			else
			{
				userProfile[0] = new Users(ua.getUserId());
				userProfile[1] = AddressInfo.findUserId(ua.getUserId());
				sa.updateSession(jsonIn.token, userProfile);
			}
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " setting up sessionData");
		}
		
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("token", jsonIn.token);
		jsonResponse.put("user", userProfile[0]);
		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(AuthJson jsonIn)
	{
		try 
		{
			String query = "SELECT * FROM UsersAuth WHERE token = '" + jsonIn.token + "'";
			UsersAuth ua = new UsersAuth();
			ua.populateObject(query, ua);
			ua.delete(ua.getIdUsersAuth());
		}
		catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).
					entity(LanguageResources.getResource("auth.tokenNotFound")).build();
		}
		
		SessionData.getInstance().removeUser(jsonIn.token);
		return Response.status(Response.Status.OK)
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}
}
