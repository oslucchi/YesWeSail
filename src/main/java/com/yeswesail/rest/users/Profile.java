package com.yeswesail.rest.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.UsersJson;

@Path("/users")
public class Profile {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Users u = null;
	UsersAuth ua = null;
	JsonHandler jh = new JsonHandler();

	private String getUserData(UsersJson jsonIn, int languageId)
	{
		String errMsg = null;
		u = SessionData.getInstance().getBasicProfile(jsonIn.token);
		
		if (u == null)
		{
			DBConnection conn = null;
			try 
			{
				conn = DBInterface.connect();
				ua = new UsersAuth();
				String query = "SELECT * FROM UsersAuth WHERE token = '" + jsonIn.token + "'";
				ua.populateObject(conn, query, ua);
			}
			catch (Exception e) 
			{
				if (e.getMessage().compareTo("No record found") == 0)
				{
					errMsg = LanguageResources.getResource(
								languageId, "auth.loginTokenNotExist");
				}
				else
				{
					errMsg = LanguageResources.getResource(languageId, "generic.execError") + " (" +
							 e.getMessage() + ")";
				}
				log.error("Error getting user from UsersAuth: " + errMsg);
			}
			finally
			{
				DBInterface.disconnect(conn);
			}

			try 
			{
				SessionData.getInstance().addUser(jsonIn.token, languageId);
				u = SessionData.getInstance().getBasicProfile(jsonIn.token);
			}
			catch(Exception e)
			{
				errMsg = LanguageResources.getResource(languageId, "generic.execError") + " (" +
						 e.getMessage() + ")";
			}
		}
		
		return errMsg;
	}

	@GET
	@Path("/basic/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response basicPublicProfile(@PathParam("userId") int userId, 
									   @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		Users u = null;
		Users uWiped = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, userId);
			uWiped = new Users();
			uWiped.setName(u.getName());
			uWiped.setSurname(u.getSurname());
			uWiped.setRoleId(u.getRoleId());
			uWiped.setImageURL(u.getImageURL());
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try 
		{
			json = mapper.writeValueAsString(uWiped);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	@POST
	@Path("/basic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response basicProfile(@HeaderParam("Authorization") String token)
	{
		UsersJson jsonIn = new UsersJson();
		jsonIn.token = token;
		int languageId = SessionData.getInstance().getLanguage(token);
		String errMsg = getUserData(jsonIn, languageId);
		if (errMsg != null)
		{
			log.error("Error " + errMsg + " retrieving userData");
			return Response.status(Response.Status.UNAUTHORIZED).entity(errMsg).build();
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try {
			u.setPassword("******");
			json = mapper.writeValueAsString(u);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(
							languageId, "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	@GET
	@Path("/suggestion/{roleId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response usersSuggestion(@PathParam("roleId") int roleId, @HeaderParam("Authorization") String token)
	{
		int languageId = SessionData.getInstance().getLanguage(token);
		ArrayList<Users> uList = null;
		try 
		{
			uList = Users.findUsersbyRole(roleId);
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try 
		{
			json = mapper.writeValueAsString(uList.toArray());
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response userProfileById(@PathParam("userId") int userId, @HeaderParam("Authorization") String token)
	{
		int languageId = SessionData.getInstance().getLanguage(token);
		Users u = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, userId);
			u.setPassword("******");
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try 
		{
			json = mapper.writeValueAsString(u);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	@GET
	@Path("/whole")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response wholeProfile(@HeaderParam("Authorization") String token, @HeaderParam("Language") String language)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = Utils.setLanguageId(language);

		AddressInfo[] ai = new AddressInfo[2];
		DBConnection conn = null;

		String errMsg = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, sd.getBasicProfile(token).getIdUsers());
			ai = AddressInfo.findUserId(u.getIdUsers());
		}
		catch (Exception e) {
			log.error("Error retrieving AddressInfo for user " + u.getIdUsers() + " (" + e.getMessage() + ")");
			errMsg = LanguageResources.getResource(
						languageId, "users.addressInfoException") + " (" +
						e.getMessage() + ")";
			return Response.status(Response.Status.UNAUTHORIZED).entity(errMsg).build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		u.setPassword("");
		u.setAddressInfo(ai);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try 
		{
			json = mapper.writeValueAsString(u);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("search/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchAll(@HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}
		
		ArrayList<Users> usersList = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			usersList = (ArrayList<Users>) Users.populateCollection("SELECT * FROM Users", Users.class);
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		if (jh.jasonize(usersList, languageId) != Response.Status.OK)
		{
			log.error("Error '" + jh.json + "' jsonizing the usersLis object");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(UsersJson jsonIn, @HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		DBConnection conn = null;
		Users u = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			u = new Users(conn, jsonIn.idUsers);
			u.setEmail(jsonIn.email);
			u.setExperiences(jsonIn.experiences);
			u.setImageURL(jsonIn.imageURL);
			u.setInterests(jsonIn.interests);
			u.setLanguagesSpoken(jsonIn.languagesSpoken);
			u.setName(jsonIn.name);
			u.setSurname(jsonIn.surname);
			u.setPhone1(jsonIn.phone1);
			u.setPhone2(jsonIn.phone2);
			if (jsonIn.addressInfo != null)
			{
				AddressInfo ai = new AddressInfo();
				String sql = "DELETE FROM AddressInfo WHERE userId = " + jsonIn.idUsers;
				AddressInfo.executeStatement(conn, sql, false);
				for(int i = 0; i < jsonIn.addressInfo.length; i++)
				{
					ai.setAddress1(jsonIn.addressInfo[i].address1);
					ai.setAddress2(jsonIn.addressInfo[i].address2);
					ai.setCompanyName(jsonIn.addressInfo[i].companyName);
					ai.setCity(jsonIn.addressInfo[i].city);
					ai.setCountry(jsonIn.addressInfo[i].country);
					ai.setProvince(jsonIn.addressInfo[i].province);
					ai.setTaxCode(jsonIn.addressInfo[i].taxCode);
					ai.setType(jsonIn.addressInfo[i].type);
					ai.setUserId(jsonIn.idUsers);
					ai.insert(conn, "idAddressInfo", ai);
				}
			}
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
}
