package com.yeswesail.rest.users;

import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.yeswesail.rest.Constants;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.UsersJson;

@Path("/users")
public class Profile {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Users u = null;
	UsersAuth ua = null;
	AddressInfo[] ai = new AddressInfo[2];

	private String getUserData(UsersJson jsonIn, String language)
	{
		String errMsg = null;
		u = SessionData.getInstance().getBasicProfile(jsonIn.token);
		
		if (u == null)
		{
			try 
			{
				ua = new UsersAuth();
				String query = "SELECT * FROM UsersAuth WHERE token = '" + jsonIn.token + "'";
				ua.populateObject(query, ua);
			}
			catch (Exception e) 
			{
				if (e.getMessage().compareTo("No record found") == 0)
				{
					errMsg = LanguageResources.getResource(
								Constants.getLanguageCode(language), "auth.loginTokenNotExist");
				}
				else
				{
					errMsg = LanguageResources.getResource(Constants.getLanguageCode(language), "generic.execError") + " (" +
							 e.getMessage() + ")";
				}
				log.error("Error getting user from UsersAuth: " + errMsg);
			}

			try 
			{
				SessionData.getInstance().addUser(jsonIn.token, Constants.getLanguageCode(language));
				u = SessionData.getInstance().getBasicProfile(jsonIn.token);
			}
			catch(Exception e)
			{
				errMsg = LanguageResources.getResource(Constants.getLanguageCode(language), "generic.execError") + " (" +
						 e.getMessage() + ")";
			}
		}
		
		return errMsg;
	}
	
	@POST
	@Path("/basic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response basicProfile(@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		UsersJson jsonIn = new UsersJson();
		jsonIn.token = token;
		String errMsg = getUserData(jsonIn, language);
		if (errMsg != null)
		{
			return Response.status(Response.Status.UNAUTHORIZED).entity(errMsg).build();
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try {
			u.setPassword("");
			json = mapper.writeValueAsString(u);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}
	
	@POST
	@Path("/whole")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response wholeProfile(@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		UsersJson jsonIn = new UsersJson();
		jsonIn.token = token;
		String errMsg = null;
		try 
		{
			ai = AddressInfo.findUserId(u.getIdUsers());
		}
		catch (Exception e) {
			log.error("Error retrieving AddressInfo for user " + u.getIdUsers() + " (" + e.getMessage() + ")");
			errMsg = LanguageResources.getResource(
						Constants.getLanguageCode(language), "users.addressInfoException") + " (" +
						e.getMessage() + ")";
			return Response.status(Response.Status.UNAUTHORIZED).entity(errMsg).build();
		}
		
		u.setPassword("");
		ArrayList<Object> toJson = new ArrayList<>();
		toJson.add(u);
		toJson.add(ai);
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try {
			json = mapper.writeValueAsString(toJson);
		} 
		catch (IOException e) {
			log.error("Error jasonizing whole profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(
							Constants.getLanguageCode(language), "generic.execError") + " (" + 
							e.getMessage() + ")").build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}
}
