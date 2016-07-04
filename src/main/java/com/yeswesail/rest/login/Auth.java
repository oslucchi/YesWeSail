package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;
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
import com.yeswesail.rest.ResponseEntityCreator;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
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
	
	protected String populateUsersTable(AuthJson jsonIn, boolean accessByExternalAuth, String language)
	{
		String errorMsg = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users();
			u.setEmail(jsonIn.username);
			u.setPassword(jsonIn.password);
			u.setName(jsonIn.firstName);
			u.setSurname(jsonIn.lastName);
			u.setStatus("D");
			u.setIsShipOwner(false);
			u.setFacebook(jsonIn.facebookId);
			u.setConnectedVia("P");
			u.setRoleId(1);
			u.setIdUsers(u.insertAndReturnId(conn, "idUsers", u));
		}
		catch (Exception e) {
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				if (!accessByExternalAuth)
					errorMsg = ResponseEntityCreator.formatEntity(language, "users.alreadyRegistered");
				else
					errorMsg = null;
			}
			else
			{
				errorMsg = ResponseEntityCreator.formatEntity(language, "generic.execError") + " (" + e.getMessage() + ")";
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return errorMsg;
	}
	
	protected String populateRegistrationConfirmTable(AuthJson jsonIn, String language)
	{
		String errorMsg = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			RegistrationConfirm rc = new RegistrationConfirm();
			rc.setUserId(u.getIdUsers());
			rc.setToken(token);
			rc.setStatus("A");
			rc.setIdRegistrationConfirm(rc.insertAndReturnId(conn, "idRegistrationConfirm", rc));
		}
		catch (Exception e) {
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				errorMsg = ResponseEntityCreator.formatEntity(language, "users.alreadyRegistered");
			}
			else
			{
				errorMsg = ResponseEntityCreator.formatEntity(language, "generic.execError") + " (" + e.getMessage() + ")";
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return errorMsg;
	}
	
	protected String populateUsersAuthTable(String token, int userId, String language)
	{
		String errorMsg = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			UsersAuth ua = new UsersAuth();
			ua.setCreated(new Date());
			ua.setLastRefreshed(ua.getCreated());
			ua.setUserId(userId);
			ua.setToken(token);
			ua.setIdUsersAuth(ua.insertAndReturnId(conn, "idUsersAuth", ua));
		}
		catch (Exception e) {
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				errorMsg = ResponseEntityCreator.formatEntity(language, "users.tokenAlreadyExistent");
			}
			else
			{
				errorMsg = ResponseEntityCreator.formatEntity(language, "generic.execError") + " (" + e.getMessage() + ")";
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return errorMsg;
	}

	private String prepareAndSendMail(String bodyProperty, String subjectProperty, 
									  String confirmLink, String language, AuthJson jsonIn)
	{
		String errorMsg;
		token = UUID.randomUUID().toString();		
		errorMsg = populateRegistrationConfirmTable(jsonIn, language);
		if (errorMsg != null)
			return errorMsg;

		try
		{
			String httpLink = prop.getWebHost() + "/rest/auth/" + confirmLink + "/" + token;
	        String htmlText = ResponseEntityCreator.formatEntity(language, bodyProperty);
	        htmlText = htmlText.replaceAll("CNFMLINK", httpLink);
//	        htmlText = htmlText.substring(0, htmlText.indexOf("CNFMLINK")) + httpLink + 
//	        		   htmlText.substring(htmlText.indexOf("CNFMLINK") + 8);
//	        htmlText = htmlText.substring(0, htmlText.indexOf("CNFMLINK")) + httpLink + 
//	        		   htmlText.substring(htmlText.indexOf("CNFMLINK") + 8);
	        String subject = LanguageResources.getResource(Constants.getLanguageCode(language), "mail.subject");
			URL url = getClass().getResource("/images/mailLogo.png");
			String imagePath = url.getPath();
			Mailer.sendMail(jsonIn.username, subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			errorMsg = ResponseEntityCreator.formatEntity(language, "mailer.sendError") + " (" + e.getMessage() + ")";
		}
		return null;
	}

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(AuthJson jsonIn, @HeaderParam("Language") String language) 
	{
		String errorMsg = null; 
		if (jsonIn.username == null)
		{
			errorMsg = ResponseEntityCreator.formatEntity(language, "users.badMail");
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "users.badMail");
//			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}
		errorMsg = populateUsersTable(jsonIn, false, language);
		if (errorMsg != null)
			return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();

		errorMsg = prepareAndSendMail("mail.body", "mail.subject", "confirmUser", language, jsonIn);
		if (errorMsg != null)
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
		
		return Utils.jsonizeResponse(Response.Status.OK, null, language, "auth.registerRedirectMsg");
//		return Response.status(Response.Status.OK)
//				.entity(ResponseEntityCreator.formatEntity(language, "auth.registerRedirectMsg")).build();
	}

	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(AuthJson jsonIn, @HeaderParam("Language") String language) 
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
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			query = "SELECT * FROM Users WHERE email = '" + username + "'";
			u = new Users();
			log.debug("Select user by email");
			u.populateObject(conn, query, u);
			log.debug("Found. Password in database is '" + u.getPassword() + "'");
			if (u.getPassword().compareTo(password) != 0)
			{
				log.debug("Wrong password, returning UNAUTHORIZED");
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.wrongCredentials");
//				errorMsg = ResponseEntityCreator.formatEntity(language, "auth.wrongCredentials");
//				return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
			}
		}
		catch (Exception e) {
			DBInterface.disconnect(conn);
			if (e.getMessage().compareTo("No record found") == 0)
			{
				log.debug("Email not found, returning UNAUTHORIZED");
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.mailNotRegistered");
//				errorMsg = ResponseEntityCreator.formatEntity(language, "auth.mailNotRegistered");
			}
			else
			{
				log.debug("Generic error " + e.getMessage());
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "generic.execError");
//				errorMsg = ResponseEntityCreator.formatEntity(language, "generic.execError") + " (" + e.getMessage() + ")";
			}
//			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}

		// Check if this user already has a token
		UsersAuth ua = null;
		try
		{
			log.debug("Setting up the new token for the user in DB");
			ua = new UsersAuth();
			query = "SELECT * FROM UsersAuth WHERE userId = " + u.getIdUsers();
			ua.populateObject(conn, query, ua);
			ua.setToken(token);
			ua.setLastRefreshed(new Date());
			log.debug("Refreshing the last access");
			ua.update(conn, "idUsersAuth");
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
					ua.insert(conn, "idUsersAuth", ua);
				} 
				catch (Exception e1) {
					log.error("Error inserting token for user id " + u.getIdUsers());
				}
			}
			else
			{
				return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, language, "generic.execError");
//				errorMsg = ResponseEntityCreator.formatEntity(language, "generic.execError") +  " (" + e.getMessage() + ")";
//				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
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
		return Response.status(Response.Status.OK).entity(entity).build();
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

		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			ua = UsersAuth.findToken(token);
			if (prop.getSessionExpireTime() != 0)
			{
				if (ua.getLastRefreshed().getTime() + prop.getSessionExpireTime() * 1000 < new Date().getTime())
				{
					ua.delete(conn, ua.getIdUsersAuth());
					sa.removeUser(token);
					DBInterface.disconnect(conn);
					return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.sessionExpired");
//					errorMsg = ResponseEntityCreator.formatEntity(language, "auth.sessionExpired");
//					return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
				}
			}
		}
		catch (Exception e) {
			sa.removeUser(token);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, language, "auth.sessionExpired");
//			errorMsg = ResponseEntityCreator.formatEntity(language, "auth.sessionExpired");
//			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
		}

		Object[] userProfile = sa.getWholeProfile(token);
		try  
		{
			ua.setLastRefreshed(new Date());
			ua.update(conn, "idUsersAuth");
			if (userProfile == null)
			{
				sa.addUser(ua.getUserId(), Constants.getLanguageCode(language));
				userProfile = new Object[SessionData.SESSION_ELEMENTS];
				userProfile = sa.getSessionData(ua.getToken());
			}
			else
			{
				userProfile[SessionData.BASIC_PROFILE] = new Users(conn, ua.getUserId());
				userProfile[SessionData.WHOLE_PROFILE] = AddressInfo.findUserId(ua.getUserId());
				userProfile[SessionData.LANGUAGE] = Constants.getLanguageCode(language);
				sa.updateSession(token, userProfile);
			}
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " setting up sessionData");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("token", token);
		jsonResponse.put("user", userProfile[0]);
		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS")
				.build();
	}
    
	@POST
	@Path("/chgPasswd")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(AuthJson jsonIn, @HeaderParam("Language") String language)
	{
		String errorMsg = null;
		if (jsonIn.username == null)
		{
			return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.badMail");
//			errorMsg = ResponseEntityCreator.formatEntity(language, "users.badMail");
//			return Response.status(Response.Status.FORBIDDEN).entity(errorMsg).build();
		}
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, jsonIn.username);
		}
		catch (Exception e) 
		{
			return Utils.jsonizeResponse(Response.Status.FORBIDDEN, e, language, "users.badMail");
//			return Response.status(Response.Status.FORBIDDEN)
//					.entity(ResponseEntityCreator.formatEntity(language, "users.badMail"))
//					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		errorMsg = prepareAndSendMail("mail.passwordChange", "mail.passwordChangeSubject", 
									  "confirmPasswordChange", language, jsonIn);
		if (errorMsg != null)
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
		
		return Response.status(Response.Status.OK)
				.entity(ResponseEntityCreator.formatEntity(language, "auth.registerRedirectMsg")).build();
	}

	@POST
	@Path("/confirmPasswordChange")
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmPasswordChange(AuthJson jsonIn, @PathParam("token") String token, @HeaderParam("Language") String language) 
	{
		ApplicationProperties prop = ApplicationProperties.getInstance();
		URI location = null;
		String uri = prop.getWebHost() + "/" + prop.getRedirectHome() + "?token=" + jsonIn.token;

		RegistrationConfirm rc = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			rc = new RegistrationConfirm();
			rc.findActiveRecordByToken(conn, jsonIn.token);
			Users u = new Users(conn, rc.getUserId());
			u.setPassword(jsonIn.password);
			u.update(conn, "idUsers");
		}
		catch (Exception e) 
		{
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, language, "auth.confirmTokenInvalid");
//			return Response.status(Response.Status.UNAUTHORIZED)
//				.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.confirmTokenInvalid")).build();
		}
		
		try 
		{
			rc.setStatus("I");
			rc.update(conn, "idRegistrationConfirm");
		}
		catch(Exception e)
		{
			log.error("Exception updating the registration confirm record. (" + e.getMessage() + ")");
		}
		
		try 
		{
			location = new URI(uri);
			DBInterface.disconnect(conn);
			return Response.seeOther(location).build();
		}
		catch (URISyntaxException e) 
		{
			log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage());
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " updating user and registration token");
		}
		DBInterface.disconnect(conn);
		return Response.status(Response.Status.OK)
				.entity("").build();
	}

    
	@GET
	@Path("/fbLogin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fbLogin(@QueryParam("code") String code)
	{
		log.debug("Authenticating via facebook on code '" + code + "'");
        FacebookHandler fbh = new FacebookHandler();
        String errorMsg = null;
        if ((errorMsg = fbh.getFacebookAccessToken(code)) != null)
        {
    		log.warn("Got the error '" + errorMsg + "' returning UNAUTHORIZED");
			return Response.status(Response.Status.UNAUTHORIZED).entity(errorMsg).build();
        }
		log.debug("Authenticated. Redirect to '" + fbh.getLocation().getPath() + "'");
		return Response.seeOther(fbh.getLocation()).build();
   }

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@HeaderParam("Authorization") String token, @HeaderParam("Language") String language)
	{
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			String query = "SELECT * FROM UsersAuth WHERE token = '" + token + "'";
			UsersAuth ua = new UsersAuth();
			ua.populateObject(conn, query, ua);
			ua.delete(conn, ua.getIdUsersAuth());
		}
		catch (Exception e) {
			return Utils.jsonizeResponse(Response.Status.NOT_FOUND, e, language, "auth.tokenNotFound");
//			return Response.status(Response.Status.NOT_FOUND).
//					entity(ResponseEntityCreator.formatEntity(language, "auth.tokenNotFound")).build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		SessionData.getInstance().removeUser(token);
		return Response.status(Response.Status.OK)
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}
}
