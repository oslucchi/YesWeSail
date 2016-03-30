package com.yeswesail.rest.users;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

@Path("/users")
public class Profile {
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());
	
	@GET
	@Path("/basic/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response basic(@PathParam("token") String token)
	{
		String errMsg = "";
		Users u = SessionData.getInstance().getBasicProfile(token);
		UsersAuth ua = null;

		if (u == null)
		{
			try 
			{
				ua = new UsersAuth();
				String query = "SELECT * FROM UsersAuth WHERE token = '" + token + "'";
				ua.populateObject(query, ua);
			}
			catch (Exception e) 
			{
				if (e.getMessage().compareTo("No record found") == 0)
				{
					errMsg = LanguageResources.getResource("auth.loginTokenNotExist");
				}
				else
				{
					errMsg = LanguageResources.getResource("generic.execError") + " (" +
							 e.getMessage() + ")";
				}
				log.error("Error getting user from UsersAuth: " + errMsg);
				return Response.status(Response.Status.UNAUTHORIZED).entity(errMsg).build();
			}

			try 
			{
				SessionData.getInstance().addUser(token);
				u = SessionData.getInstance().getBasicProfile(token);
			}
			catch(Exception e)
			{
				errMsg = LanguageResources.getResource("generic.execError") + " (" +
						 e.getMessage() + ")";
			}
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(u);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource("generic.execError") + " (" + e.getMessage() + ")").build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}
}
