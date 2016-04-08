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
import com.yeswesail.rest.Constants;
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
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Genson genson = new Genson();
	private String token = null;
	private Users u = null;
	
	protected String populateUsersTable(AuthJson jsonIn)
	{
		String errorMsg = null;
		try 
		{
			u = new Users();
			u.setEmail(jsonIn.username);
			u.setPassword(jsonIn.password);
			u.setName(jsonIn.firstName);
			u.setSurname(jsonIn.lastName);
			u.setStatus("D");
			u.setIsShipOwner("F");
			u.setConnectedVia("P");
			u.setRoleId(1);
			u.insert("idUsers", u);
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
		}
		return errorMsg;
	}
	
	protected String populateRegistrationConfirmTable(AuthJson jsonIn)
	{
		String errorMsg = null;
		try 
		{
			RegistrationConfirm rc = new RegistrationConfirm();
			rc.setUserId(u.getIdUsers());
			rc.setToken(token);
			rc.setStatus("A");
			rc.insert("idRegistrationConfirm", rc);
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
		}
		return errorMsg;
	}
	
	protected String populateUsersAuthTable(String token, int userId)
	{
		String errorMsg = null;
		try 
		{
			UsersAuth ua = new UsersAuth();
			ua.setCreated(new Date());
			ua.setLastRefreshed(ua.getCreated());
			ua.setUserId(userId);
			ua.setToken(token);
			ua.insert("idUsersAuth", ua);
		}
		catch (Exception e) {
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				errorMsg = LanguageResources.getResource("users.tokenAlreadyExistent");
			}
			else
			{
				errorMsg = LanguageResources.getResource("generic.execError") + " (" + e.getMessage() + ")";
			}
		}
		return errorMsg;
	}

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(AuthJson jsonIn) 
	{
		token = UUID.randomUUID().toString();
		String errorMsg = populateUsersTable(jsonIn);
		if (errorMsg == null)
			return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();

		errorMsg = populateRegistrationConfirmTable(jsonIn);
		if (errorMsg == null)
			return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();

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
	public Response loginByToken(@HeaderParam("Authorization") String token, @HeaderParam("Language") String language)
	{
		String errorMsg = "";
		UsersAuth ua = null;
		SessionData sa = SessionData.getInstance();

		try 
		{
			ua = UsersAuth.findToken(token);
			if (prop.getSessionExpireTime() != 0)
			{
				if (ua.getLastRefreshed().getTime() + prop.getSessionExpireTime() * 1000 < new Date().getTime())
				{
					ua.delete(ua.getIdUsersAuth());
					sa.removeUser(token);
					errorMsg = LanguageResources.getResource("auth.sessionExpired");
					return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
				}
			}
		}
		catch (Exception e) {
			sa.removeUser(token);
			errorMsg = LanguageResources.getResource("auth.sessionExpired");
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}

		Object[] userProfile = sa.getWholeProfile(token);
		try  
		{
			ua.setLastRefreshed(new Date());
			ua.update("idUsersAuth");
			if (userProfile == null)
			{
				sa.addUser(ua.getUserId(), Constants.getLanguageCode(language));
				userProfile = new Object[SessionData.SESSION_ELEMENTS];
				userProfile = sa.getSessionData(ua.getToken());
			}
			else
			{
				userProfile[SessionData.BASIC_PROFILE] = new Users(ua.getUserId());
				userProfile[SessionData.WHOLE_PROFILE] = AddressInfo.findUserId(ua.getUserId());
				userProfile[SessionData.LANGUAGE] = Constants.getLanguageCode(language);
				sa.updateSession(token, userProfile);
			}
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " setting up sessionData");
		}
		
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("token", token);
		jsonResponse.put("user", userProfile[0]);
		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}
    
	@GET
	@Path("/fbLogin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fbLogin(@QueryParam("code") String code)
	{
        FacebookHandler fbh = new FacebookHandler();
        String errorMsg = null;
        if ((errorMsg = fbh.getFacebookAccessToken(code)) != null)
        {
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
        }
		
		if (fbh.isNewRegistration())
		{
			return Response.seeOther(fbh.getLocation()).build();
		}
		return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
   }

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@HeaderParam("Authorization") String token)
	{
		try 
		{
			String query = "SELECT * FROM UsersAuth WHERE token = '" + token + "'";
			UsersAuth ua = new UsersAuth();
			ua.populateObject(query, ua);
			ua.delete(ua.getIdUsersAuth());
		}
		catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).
					entity(LanguageResources.getResource("auth.tokenNotFound")).build();
		}
		
		SessionData.getInstance().removeUser(token);
		return Response.status(Response.Status.OK)
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}
}
