package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.ResponseEntityCreator;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.PasswordHandler;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

@Path("/auth/confirmUser")
public class LoginConfirm {
	final Logger log = Logger.getLogger(this.getClass());
	private Genson genson = new Genson();


	@GET
	@Path("FB/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response connectFBAccount(@HeaderParam("Language") String language,
									 @PathParam("token") String token,
									 @QueryParam("email") String email,
									 @QueryParam("password") String password)
	{
		HashMap<String, Object> jsonResponse = new HashMap<>();
		ApplicationProperties prop = ApplicationProperties.getInstance();

		DBConnection conn = null;
		if (((email == null) || (email.compareTo("") == 0))||
			((password != null) && (password.compareTo("") == 0)))
		{
			log.debug("No valid mail/password passed");
			return Utils.jsonizeResponse(Response.Status.BAD_REQUEST, null, 
					prop.getDefaultLang(), "auth.wrongCredentials");
		}
		
		SessionData sd = SessionData.getInstance();
		UsersAuth ua = null;			
		Users uFB = null;
		Users u = null;
		try 
		{
			conn = DBInterface.connect();
			if ((ua = UsersAuth.findToken(conn, token)) == null)
			{
				DBInterface.disconnect(conn);
				return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, 
						new Exception("App token '" +token + "' not found"), 
						prop.getDefaultLang(), "generic.execError");
			}
			uFB = new Users(conn, ua.getUserId());
		} 
		catch(Exception e1) 
		{
			log.warn("Exception " + e1.getMessage(), e1);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e1, 
					prop.getDefaultLang(), "generic.execError");
		}

		String entity = null;
		if (password == null)
		{
			/*
			 * A request to enter a valid email during a FB login is raised.
			 * Check if the email already exists
			 */
			try 
			{
				u = new Users();
				u.findByEmail(conn, email);
				DBInterface.disconnect(conn);
				return Utils.jsonizeResponse(Response.Status.CONFLICT, null, 
						prop.getDefaultLang(), "auth.emailAlreadyRegistered");
			}
			catch(Exception e)
			{
				if (!e.getMessage().equalsIgnoreCase("No record found"))
				{
					log.warn("Exception " + e.getMessage() + 
							 " searching user by the given email '" + email + "'", e);
					DBInterface.disconnect(conn);
					return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, 
							prop.getDefaultLang(), "generic.execError");
				}
			}
			
			// It does not exists
			try {
				uFB.setEmail(email);
				uFB.update(conn, "idUsers");
				DBInterface.disconnect(conn);
				// Updating the session data with current user data
				sd.removeUser(token);
				sd.addUser(token, Constants.getLanguageCode(language));
			}
			catch(Exception e) 
			{
				log.warn("Exception " + e.getMessage() +
						 " updating user with the given email '" + email + "'", e);
				return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, 
						prop.getDefaultLang(), "generic.execError");
			}
			finally
			{
				DBInterface.disconnect(conn);
			}
			
			// returning to the user
			jsonResponse.put("token", token);
			jsonResponse.put("user", u);
			entity = genson.serialize(jsonResponse);
			return Response.status(Response.Status.OK).entity(entity).build();
		}
		
		/*
		 * The User has another entry by email/password
		 * Let's validate the data passed is correct
		 */
		
		try
		{
			u = new Users();
			u.findByEmail(conn, email);
			PasswordHandler pw = new PasswordHandler();
			pw.userPassword(conn, u.getIdUsers());
			
			if ((pw.getPassword() == null) || (pw.getPassword().compareTo(password) != 0))
			{
				log.debug("Wrong password, returning UNAUTHORIZED");
				DBInterface.disconnect(conn);
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, 
											 prop.getDefaultLang(), "auth.wrongCredentials");
			}
		}
		catch(Exception e)
		{
			log.debug("Exception " + e.getMessage() + ", returning UNAUTHORIZED", e);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, 
										 prop.getDefaultLang(), "auth.wrongCredentials");
		}
		
		try
		{
			log.trace("Credential verified. Club existing entry and the FB one together");
			// email and password passed match. They refer to a different profile though.
			// We so need to club them together and remove the FB newly created
			ua.setUserId(u.getIdUsers());
			ua.setLastRefreshed(new Date());
			ua.update(conn, "idUsersAuth");

			u.setStatus(Constants.STATUS_ACTIVE);
			u.setFacebook(uFB.getFacebook());
			u.update(conn, "idUsers");
			sd.removeUser(token);
			sd.addUser(token, Constants.getLanguageCode(language));
			
			// Delete the entry eventually created during login FB
			uFB.delete(conn, uFB.getIdUsers());
			jsonResponse.put("token", token);
			jsonResponse.put("user", u);
			entity = genson.serialize(jsonResponse);
			return Response.status(Response.Status.OK).entity(entity).build();
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, null, 
					prop.getDefaultLang(), "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}

	@GET
	@Path("{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(@PathParam("token") String token, @QueryParam("email") String email)
	{
		ApplicationProperties prop = ApplicationProperties.getInstance();
		URI location;
		RegistrationConfirm rc = null;
		String uri = prop.getWebHost() + "/" + prop.getRedirectHome() + "?token=" + token;

		DBConnection conn = null;
		try {
			conn = DBInterface.connect();
		} 
		catch(Exception e1) {
			log.warn("Exception " + e1.getMessage(), e1);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(ResponseEntityCreator.formatEntity(
								prop.getDefaultLang(), "generic.execError")).build();
		}
		
		if (email == null)
		{
			try 
			{
				String query = "SELECT * FROM RegistrationConfirm WHERE token = '" + token + "'";
				rc = new RegistrationConfirm();
				rc.populateObject(conn, query, rc);
			}
			catch(Exception e) 
			{
				log.warn("Exception " + e.getMessage(), e);
				DBInterface.disconnect(conn);
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.confirmTokenInvalid")).build();
			}
		}
		else
		{
			if (email.compareTo("") == 0)
			{
				DBInterface.disconnect(conn);
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.invalidEmail")).build();
			}
			UsersAuth ua = null;
			if ((ua = UsersAuth.findToken(conn, token)) == null)
			{
				DBInterface.disconnect(conn);
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.confirmTokenInvalid")).build();
			}
			try 
			{
				ua.setLastRefreshed(new Date());
				ua.update(conn, "idUsersAuth");
				Users u = new Users(conn, ua.getUserId());
				u.setEmail(email);
				u.setStatus(Constants.STATUS_ACTIVE);
				u.update(conn, "idUsers");
				return Response.status(Response.Status.OK).build();
			}
			catch(Exception e)
			{
				log.warn("Exception " + e.getMessage(), e);
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.confirmTokenInvalid")).build();
			}
			finally
			{
				DBInterface.disconnect(conn);
			}
		}
			
		try 
		{
			location = new URI(uri);
			Users u = new Users(conn,rc.getUserId());
			u.setStatus(Constants.STATUS_ACTIVE);
			u.update(conn, "idUsers");
			rc.setStatus(Constants.STATUS_COMPLETED);
			rc.update(conn, "idRegistrationConfirm");
			UsersAuth ua = new UsersAuth();
			ua.setCreated(new Date());
			ua.setLastRefreshed(ua.getCreated());
			ua.setUserId(u.getIdUsers());
			ua.setToken(rc.getToken());
			ua.insert(conn, "idUsersAuth", ua);
			return Response.seeOther(location).build();
		} 
		catch(URISyntaxException e) 
		{
			log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage(), e);
		}
		catch(Exception e) {
			log.error("Exception " + e.getMessage() + " updating user and registration token", e);
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).build();
	}
}
