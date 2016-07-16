package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

@Path("/auth/confirmUser")
public class LoginConfirm {
	final Logger log = Logger.getLogger(this.getClass());
	
	@GET
	@Path("/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(@PathParam("token") String token, @QueryParam("email") String email)
	{
		ApplicationProperties prop = ApplicationProperties.getInstance();
		URI location;
		RegistrationConfirm rc = null;
		Users u = null;
		DBConnection conn = null;
		try {
			conn = DBInterface.connect();
		} 
		catch (Exception e1) {
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e1, 
										 prop.getDefaultLang(), "generic.execError");
		};
		if (email == null)
		{
			try 
			{
				String query = "SELECT * FROM RegistrationConfirm WHERE token = '" + token + "'";
				rc = new RegistrationConfirm();
				rc.populateObject(conn, query, rc);
			}
			catch (Exception e) 
			{
				DBInterface.disconnect(conn);
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, 
						 prop.getDefaultLang(), "auth.confirmTokenInvalid");
			}
		}
		else
		{
			if (email.compareTo("") == 0)
			{
				DBInterface.disconnect(conn);
				return Utils.jsonizeResponse(Response.Status.BAD_REQUEST, null, 
						 prop.getDefaultLang(), "auth.invalidEmail");
			}
			try 
			{
				UsersAuth ua = UsersAuth.findToken(token);
				ua.setLastRefreshed(new Date());
				ua.update(conn, "idUsersAuth");
				u = new Users(conn, ua.getUserId());
				u.setEmail(email);
				u.setStatus("A");
				u.update(conn, "idUsers");
				return Response.status(Response.Status.OK).build();
			}
			catch(Exception e)
			{
				return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, e, 
						 prop.getDefaultLang(), "auth.confirmTokenInvalid");
			}
		}
		String uri = null;
		try 
		{
			u = new Users(conn,rc.getUserId());
			u.setStatus("A");
			u.update(conn, "idUsers");
			// Redirect on the profile page
			uri = prop.getWebHost() + "/#/users/" + u.getIdUsers() + "/personal-info?token=" + token + "&profileIncomplete=true";
			location = new URI(uri);
			
			rc.setStatus("I");
			rc.update(conn, "idRegistrationConfirm");
			UsersAuth ua = new UsersAuth();
			ua.setCreated(new Date());
			ua.setLastRefreshed(ua.getCreated());
			ua.setUserId(u.getIdUsers());
			ua.setToken(rc.getToken());
			ua.insert(conn, "idUsersAuth", ua);
			return Response.seeOther(location).build();
		} 
		catch (URISyntaxException e) 
		{
			log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage());
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " updating user and registration token");
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}
}
