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
			u.setStatus("D");
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
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				if (!accessByExternalAuth)
					return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.alreadyRegistered");
			}
			else
			{
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
				rc.setStatus("A");
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
		catch (Exception e) 
		{
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.alreadyRegistered");
			}
			else
			{
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
		catch (Exception e) 
		{
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				return Utils.jsonizeResponse(Response.Status.FORBIDDEN, null, language, "users.tokenAlreadyExistent");
			}
			else
			{
				return Utils.jsonizeResponse(Response.Status.FORBIDDEN, e, language, "generic.execError");
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
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
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, language, "mailer.sendError");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		if ((response = prepareAndSendMail("mail.body", "mail.subject", "confirmUser", language, jsonIn)) != null)
			return response;
		
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
		
		try 
		{
			UsersAuth.findToken(token);
		}
		catch (Exception e) {
			ut.addToJsonContainer("authorized", "false", true);
			return Response.status(Status.UNAUTHORIZED).entity(ut.jsonize()).build();
		}
		ut.addToJsonContainer("authorized", "true", true);
		return Response.status(Status.OK).entity(ut.jsonize()).build();
	}

	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(AuthJson jsonIn, @HeaderParam("Language") String language) 
	{ 
		String username = jsonIn.username; 
		String password = jsonIn.password; 
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

			PasswordHandler pw = new PasswordHandler();
			pw.userPassword(conn, u.getIdUsers());
			
			log.debug("Found. Password in database is '" + pw.getPassword() + "'");
			if ((pw.getPassword() == null) || (pw.getPassword().compareTo(password) != 0))
			{
				log.debug("Wrong password, returning UNAUTHORIZED");
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.wrongCredentials");
			}
		}
		catch (Exception e)
		{
			DBInterface.disconnect(conn);
			if (e.getMessage().compareTo("No record found") == 0)
			{
				log.debug("Email not found, returning UNAUTHORIZED");
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "auth.mailNotRegistered");
			}
			else
			{
				log.debug("Generic error " + e.getMessage());
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, language, "generic.execError");
			}
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
		catch (Exception e) 
		{
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
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		SessionData sa = SessionData.getInstance();
		Object[] userProfile = sa.getSessionData(token);
		if (userProfile == null)
		{
			userProfile = new Object[SessionData.SESSION_ELEMENTS];
			userProfile[SessionData.LANGUAGE] = new Integer(Utils.setLanguageId(language));
		}
		try 
		{
			userProfile[SessionData.BASIC_PROFILE] = u;
			userProfile[SessionData.WHOLE_PROFILE] = AddressInfo.findUserId(u.getIdUsers());
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
				}
			}
		}
		catch (Exception e) 
		{
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
			rc.setStatus("A");
			rc.setToken(jsonIn.token);
			rc.setUserId(u.getIdUsers());
			rc.insert(conn, "idRegistrationConfirm", rc);
		}
		catch (Exception e) 
		{
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
			UsersAuth ua = UsersAuth.findUserId(rc.getUserId());
			if (ua == null)
			{
				ua = new UsersAuth();
				ua.setCreated(new Date());
				ua.setLastRefreshed(ua.getCreated());
				ua.setUserId(rc.getUserId());
				ua.setToken(token);
				ua.insert(conn, "idUsersAuth", ua);
			}
			else
			{
				SessionData.getInstance().removeUser(ua.getToken());
				ua.setToken(token);
				ua.update(conn, "idUsersAuth");
			}
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, language, "auth.confirmTokenInvalid");
		}
		
		try 
		{
			rc.setStatus("C");
			rc.update(conn, "idRegistrationConfirm");
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e)
		{
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
		catch (URISyntaxException e) 
		{
			log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage());
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " updating user and registration token");
		}
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
        Response response = null;
        if ((response = fbh.getFacebookAccessToken(code)) != null)
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
			String query = "SELECT * FROM UsersAuth WHERE token = '" + token + "'";
			UsersAuth ua = new UsersAuth();
			ua.populateObject(conn, query, ua);
			ua.delete(conn, ua.getIdUsersAuth());
		}
		catch (Exception e) 
		{
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
