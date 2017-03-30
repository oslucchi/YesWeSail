package com.yeswesail.rest.login;

import java.net.MalformedURLException;
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
import javax.ws.rs.core.Response.Status;

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
import com.yeswesail.rest.DBUtility.PasswordHandler;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.Roles;
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
	
	protected Response populateUsersTable(AuthJson jsonIn, boolean accessByExternalAuth, String language)
	{
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			u = new Users();
			u.setEmail(jsonIn.username);
			u.setName(jsonIn.firstName);
			u.setSurname(jsonIn.lastName);
			u.setStatus(Constants.STATUS_D);
			u.setIsShipOwner(false);
			u.setFacebook(jsonIn.facebookId);
			u.setConnectedVia("P");
			u.setRoleId(Roles.TRAVELLER);
			u.setIdUsers(u.insertAndReturnId(conn, "idUsers", u));
			PasswordHandler pw = new PasswordHandler();
			if (jsonIn.password != null)
			{
				pw.setIdUsers(u.getIdUsers());
				pw.setPassword(jsonIn.password);
				pw.updatePassword(conn, false);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				if (!accessByExternalAuth)
					return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.alreadyRegistered");
			}
			else
			{
				log.warn("Exception " + e.getMessage(), e);
				return Utils.jsonizeResponse(Response.Status.FORBIDDEN, e, language, "generic.execError");
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		return null;
	}
	
	protected Response populateRegistrationConfirmTable(AuthJson jsonIn, String language)
	{
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			RegistrationConfirm rc = new RegistrationConfirm();
			if (rc.findActiveRecordById(conn, u.getIdUsers()) == null)
			{
				token = jsonIn.token;
				rc.setCreated(new Date());
				rc.setStatus(Constants.STATUS_ACTIVE);
				rc.setUserId(u.getIdUsers());
				rc.setToken(token);
				rc.setIdRegistrationConfirm(rc.insertAndReturnId(conn, "idRegistrationConfirm", rc));
			}
			else
			{
				token = rc.getToken();
			}
			rc.setToken(token);
			rc.setPasswordChange(jsonIn.password);
			rc.update (conn, "idRegistrationConfirm");
		}
		catch(Exception e) 
		{
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.alreadyRegistered");
			}
			else
			{
				log.warn("Exception " + e.getMessage(), e);
				return Utils.jsonizeResponse(Response.Status.FORBIDDEN, e, language, "generic.execError");
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return null;
	}
	
	protected Response populateUsersAuthTable(String token, int userId, String language)
	{
		DBConnection conn = null;
		UsersAuth ua = null;
		try 
		{
			conn = DBInterface.connect();
			log.debug("Looking up the token '" + token + "' to check if it exists already in the DB");
			ua = UsersAuth.findToken(token);
			if (ua == null)
			{
				log.debug("Not found, checking if the user " + userId + " already has one...");
				ua = UsersAuth.findUserId(userId);
			}
			if (ua == null)
			{
				log.debug("Neither token or user were found, create a new entry in the table");
				ua = new UsersAuth();
				ua.setCreated(new Date());
				ua.setLastRefreshed(ua.getCreated());
				ua.setUserId(userId);
				ua.setToken(token);
				ua.setIdUsersAuth(ua.insertAndReturnId(conn, "idUsersAuth", ua));
			}
			else
			{
				log.debug("An entry was already present, updating it with new data");
				ua.setLastRefreshed(new Date());
				ua.setToken(token);
				ua.setUserId(userId);
				ua.update(conn, "idUsersAuth");
			}
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.FORBIDDEN, e, language, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		// Updating the session data. Any user should have maximum one entry in SD
		SessionData sa = SessionData.getInstance();
		Object[] userProfile = sa.getSessionData(userId);
		if (userProfile == null)
		{
			userProfile = new Object[SessionData.SESSION_ELEMENTS];
			userProfile[SessionData.LANGUAGE] = new Integer(Utils.setLanguageId(language));
		}
		try 
		{
			userProfile[SessionData.BASIC_PROFILE] = (u == null ? new Users(userId) : u);
			userProfile[SessionData.WHOLE_PROFILE] = AddressInfo.findUserId(userId);
		}
		catch(Exception e) 
		{
			;
		}
		
		sa.updateSession(userId, userProfile, token);
		return null;		
	}

	private Response prepareAndSendMail(String bodyProperty, String subjectProperty, 
									  String confirmLink, String language, AuthJson jsonIn)
	{
		int languageId = Utils.setLanguageId(language);
		
		Response response = populateRegistrationConfirmTable(jsonIn, language);
		if (response != null)
			return response;

		try
		{
			String httpLink = prop.getWebHost() + "/rest/auth/" + confirmLink + "/" + token;
	        String htmlText = LanguageResources.getResource(languageId, bodyProperty);
	        htmlText = htmlText.replaceAll("CNFMLINK", httpLink);
	        String subject = LanguageResources.getResource(Constants.getLanguageCode(language), subjectProperty);
			URL url = null;
			url = prop.getContext().getResource("/images/application/mailLogo.png");
			String imagePath = url.getPath();
			Mailer.sendMail(jsonIn.username, subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, language, "mailer.sendError");
		} 
		catch(MalformedURLException e)
		{
			log.warn("Exception " + e.getMessage(), e);
		}
		return null;
	}

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(AuthJson jsonIn, @HeaderParam("Language") String language) 
	{
		int languageId = Utils.setLanguageId(language);
		if (jsonIn.username == null)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "users.badMail");
		}
		if ((jsonIn.password == null) || (jsonIn.password.length() == 0))
		{
			return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.badPassword");
		}
		jsonIn.token = UUID.randomUUID().toString();

		Response response;
		if ((response = populateUsersTable(jsonIn, false, language)) != null)
			return response;

		log.debug("Sending a registration confirm email to '" + u.getEmail() + "'");
		if ((response = prepareAndSendMail("mail.body", "mail.subject", "confirmUser", language, jsonIn)) != null)
			return response;
		log.debug("Sent");
		
		Utils ut = new Utils();
		ut.addToJsonContainer("responseMessage", LanguageResources.getResource(languageId, "auth.registerRedirectMsg"), true);
		return Response.status(Response.Status.OK).entity(ut.jsonize()).build();
	}

	@GET
	@Path("/isAuthenticated")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response isAtuhenticated(@HeaderParam("Language") String language,
									@HeaderParam("Authorization") String token) 
	{
		SessionData sa = SessionData.getInstance();
		Utils ut = new Utils();
		if (sa.getBasicProfile(token) != null)
		{
			ut.addToJsonContainer("authorized", "true", true);
			return Response.status(Status.OK).entity(ut.jsonize()).build();
		}
		
		if (UsersAuth.findToken(token) == null)
		{
			ut.addToJsonContainer("authorized", "false", true);
			return Response.status(Status.UNAUTHORIZED).entity(ut.jsonize()).build();
		}
		ut.addToJsonContainer("authorized", "true", true);
		return Response.status(Status.OK).entity(ut.jsonize()).build();
	}

	
	@GET
	@Path("/fbAppId")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response fbAppId() 
	{
		Utils ut = new Utils();
		ut.addToJsonContainer("fbAppId", prop.getFbApplicationId(), true);
		return Response.status(Status.OK).entity(ut.jsonize()).build();
	}

	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(AuthJson jsonIn, 
						  @HeaderParam("Language") String language,
						  @QueryParam("fromState") String fromState) 
	{ 
		String username = jsonIn.username; 
		String password = jsonIn.password; 
		Users u;

		/*
		 * A new login always requires a new token to be generated
		 */
		String token = UUID.randomUUID().toString();
		log.debug("Login called for user '" + username + "'");
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users();
			log.debug("Get user by email");
			u.findByEmail(conn, username);

			PasswordHandler pw = new PasswordHandler();
			pw.userPassword(conn, u.getIdUsers());
			
			log.debug("Found. Password in database is '" + pw.getPassword() + "'");
			if ((pw.getPassword() == null) || (pw.getPassword().compareTo(password) != 0))
			{
				log.debug("Wrong password, returning UNAUTHORIZED");
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.wrongCredentials");
			}
		}
		catch(Exception e)
		{
			DBInterface.disconnect(conn);
			if (e.getMessage().compareTo("No record found") == 0)
			{
				log.debug("Email not found, returning UNAUTHORIZED");
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.mailNotRegistered");
			}
			else
			{
				log.debug("Generic error " + e.getMessage(), e);
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "generic.execError");
			}
		}
		populateUsersAuthTable(token, u.getIdUsers(), language);

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("token", token);
		jsonResponse.put("user", u);
		jsonResponse.put("toState", fromState);
		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	@POST
	@Path("/loginByToken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginByToken(@HeaderParam("Authorization") String token, 
								 @HeaderParam("Language") String language)
	{
		UsersAuth ua = null;
		SessionData sa = SessionData.getInstance();

		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			if ((ua = UsersAuth.findToken(token)) == null)
			{
				sa.removeUser(token);
				DBInterface.disconnect(conn);
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, new Exception("Token not found"), language, "auth.sessionExpired");
			}
			if (prop.getSessionExpireTime() != 0)
			{
				if (ua.getLastRefreshed().getTime() + prop.getSessionExpireTime() * 1000 < new Date().getTime())
				{
					ua.delete(conn, ua.getIdUsersAuth());
					sa.removeUser(token);
					DBInterface.disconnect(conn);
					return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.sessionExpired");
				}
			}
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			sa.removeUser(token);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, language, "auth.sessionExpired");
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
		catch(Exception e) 
		{
			log.error("Exception " + e.getMessage() + " setting up sessionData", e);
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
	@Path("/changePassword")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(AuthJson jsonIn, 
								   @HeaderParam("Language") String language)
	{
		jsonIn.token = UUID.randomUUID().toString();		
		if (jsonIn.username == null)
		{
			return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.badMail");
		}
		DBConnection conn = null;
		RegistrationConfirm rc;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, jsonIn.username);
			rc = new RegistrationConfirm();
			rc.setCreated(new Date());
			rc.setPasswordChange(jsonIn.password);
			rc.setStatus(Constants.STATUS_ACTIVE);
			rc.setToken(jsonIn.token);
			rc.setUserId(u.getIdUsers());
			rc.insert(conn, "idRegistrationConfirm", rc);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.FORBIDDEN, e, language, "users.badMail");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		Response response;
		if ((response = prepareAndSendMail("mail.passwordChange", "mail.passwordChangeSubject", 
									  "confirmPasswordChange", language, jsonIn)) != null)
			return response;
		
		return Response.status(Response.Status.OK)
				.entity(ResponseEntityCreator.formatEntity(language, "auth.registerRedirectMsg")).build();
	}
	
	@GET
	@Path("confirmPasswordChange/{token}")
	@Consumes(MediaType.TEXT_HTML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmPasswordChange(@PathParam("token") String token) 
	{
		String language = prop.getDefaultLang();
		URI location = null;
		String uri = prop.getWebHost() + "/" + prop.getRedirectHome() + "?token=" + token;

		RegistrationConfirm rc = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			rc = new RegistrationConfirm();
			rc.findActiveRecordByToken(conn, token);
			PasswordHandler pw = new PasswordHandler();
			pw.setPassword(rc.getPasswordChange());
			pw.setIdUsers(rc.getUserId());
			pw.updatePassword(conn, true);
			populateUsersAuthTable(token, rc.getUserId(), language);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, language, "auth.confirmTokenInvalid");
		}
		
		try 
		{
			rc.setStatus(Constants.STATUS_COMPLETED);
			rc.update(conn, "idRegistrationConfirm");
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			log.error("Exception updating the registration confirm record. (" + e.getMessage() + ")");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		
		try 
		{
			location = new URI(uri);
			return Response.seeOther(location).build();
		}
		catch(URISyntaxException e) 
		{
			log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage(), e);
		}
		catch(Exception e) {
			log.error("Exception " + e.getMessage() + " updating user and registration token", e);
		}
		return Response.status(Response.Status.OK)
				.entity("").build();
	}

    
	@GET
	@Path("/fbLogin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fbLogin(@QueryParam("state") String state, 
							@QueryParam("code") String code)
	{
		log.debug("Authenticating via facebook on code '" + code + "' state '" + state + "'");
        FacebookHandler fbh = new FacebookHandler();
        Response response = null;
        if ((response = fbh.getFacebookAccessToken(code, state)) != null)
        {
    		log.warn("Can't get the FB token. returning UNAUTHORIZED");
			return response;
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
			UsersAuth ua = UsersAuth.findToken(token);
			if (ua != null)
				ua.delete(conn, ua.getIdUsersAuth());
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, language, "auth.tokenNotFound");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		SessionData.getInstance().removeUser(token);
		return Response.status(Response.Status.OK)
				.entity("{}")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}
	
	public Users getUser()
	{
		return u;
	}
}
