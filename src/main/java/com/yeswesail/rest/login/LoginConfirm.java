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
import com.yeswesail.rest.ResponseEntityCreator;
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
		String uri = prop.getWebHost() + "/" + prop.getRedirectHome() + "?token=" + token;

		if (email == null)
		{
			try 
			{
				String query = "SELECT * FROM RegistrationConfirm WHERE token = '" + token + "'";
				rc = new RegistrationConfirm();
				rc.populateObject(query, rc);
			}
			catch (Exception e) 
			{
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.confirmTokenInvalid")).build();
			}
		}
		else
		{
			if (email.compareTo("") == 0)
			{
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.invalidEmail")).build();
			}
			try 
			{
				UsersAuth ua = UsersAuth.findToken(token);
				ua.setLastRefreshed(new Date());
				ua.update("idUsersAuth");
				Users u = new Users(ua.getUserId());
				u.setEmail(email);
				u.setStatus("A");
				u.update("idUsers");
				return Response.status(Response.Status.OK).build();
			}
			catch(Exception e)
			{
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(ResponseEntityCreator.formatEntity(prop.getDefaultLang(), "auth.confirmTokenInvalid")).build();
			}
		}
			
		try 
		{
			location = new URI(uri);
			Users u = new Users(rc.getUserId());
			u.setStatus("A");
			u.update("idUsers");
			rc.setStatus("I");
			rc.update("idRegistrationConfirm");
			UsersAuth ua = new UsersAuth();
			ua.setCreated(new Date());
			ua.setLastRefreshed(ua.getCreated());
			ua.setUserId(u.getIdUsers());
			ua.setToken(rc.getToken());
			ua.insert("idUsersAuth", ua);
			return Response.seeOther(location).build();
		} 
		catch (URISyntaxException e) 
		{
			log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage());
		}
		catch (Exception e) {
			log.error("Exception " + e.getMessage() + " updating user and registration token");
		}
		return Response.status(Response.Status.OK).build();
	}
}
