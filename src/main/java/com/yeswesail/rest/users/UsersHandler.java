package com.yeswesail.rest.users;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.UploadFiles;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.ShipownerRequestJson;
import com.yeswesail.rest.jsonInt.UsersJson;

@Path("/users")
public class UsersHandler {
	@Context
	private ServletContext context;
	String contextPath = null;

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
					Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "auth.loginTokenNotExist"), true);
					errMsg = Utils.jsonize();
				}
				else
				{
					Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
					errMsg = Utils.jsonize();
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
				Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
				errMsg = Utils.jsonize();
			}
		}
		
		return errMsg;
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
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(Utils.jsonize())
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	private Response fillBasicProfile(int userId, int languageId, boolean requestorAuthenticated)
	{
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
			uWiped.setLanguagesSpoken(u.getLanguagesSpoken());
			if (requestorAuthenticated)
			{
				uWiped.setFacebook(u.getFacebook());
				uWiped.setEmail(u.getEmail());
				uWiped.setGoogle(u.getGoogle());
				uWiped.setTwitter(u.getTwitter());
			}
		}
		catch (Exception e) 
		{
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
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
			log.error("Error jsonizing basic profile (" + e.getMessage() + ")");
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}
	
	@GET
	@Path("/basic/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response basicPublicProfile(@PathParam("userId") int userId, 
									   @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		return fillBasicProfile(userId, languageId, false);
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
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
					.build();
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try 
		{
			json = mapper.writeValueAsString(uList.toArray());
		} 
		catch (IOException e) {
			log.error("Error jsonizing basic profile (" + e.getMessage() + ")");
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	private Response fillWholeProfile(int userId, int languageId)
	{
		Users u = null;
		AddressInfo[] ai = new AddressInfo[2];
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, userId);
			u.setPassword("******");
			ai = AddressInfo.findUserId(u.getIdUsers());
			u.setAddressInfo(ai);
		}
		catch (Exception e) 
		{
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
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
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
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
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		if (sd.getBasicProfile(token).getIdUsers() != userId)
		{
			return fillBasicProfile(userId, languageId, true);
		}
		else		
		{
			return fillWholeProfile(userId, languageId);
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchAll(@HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(Utils.jsonize())
					.build();
		}
		
		ArrayList<Users> usersList = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			usersList = (ArrayList<Users>) Users.populateCollection("SELECT * FROM Users", Users.class);
			for(int i = 0; i < usersList.size(); i++)
			{
				AddressInfo[] ai = AddressInfo.findUserId(usersList.get(i).getIdUsers());
				usersList.get(i).setAddressInfo(ai);
			}
		}
		catch (Exception e) 
		{
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
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
			u.setStatus(jsonIn.status);
			u.setBirthday(jsonIn.birthday, "yyyy-MM-dd");
			u.setRoleId(jsonIn.roleId);
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
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	@POST
	@Path("/create/dummy")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createOnTheFly(UsersJson jsonIn, @HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(Utils.jsonize())
					.build();
		}
		
		DBConnection conn = null;
		Users u = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, jsonIn.idUsers);
			u.setEmail(jsonIn.email);
			u.setName(jsonIn.name);
			u.setSurname(jsonIn.surname);
			u.setStatus("A");
			u.setRoleId(Roles.DUMMY);
			u.insert(conn, "idUsers", u);
		}
		catch(Exception e)
		{
			Utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}
	
	@POST
	@Path("/shipowners-OK")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFileWithData(
			@FormDataParam("files[0]") InputStream inputStream,
			@FormDataParam("files[0]") FormDataContentDisposition uploadedInputStream,
			@FormDataParam("sailorInfo") String sailorInfo
			)
	{
		System.out.println(uploadedInputStream.getFileName());
		System.out.println(sailorInfo);
		return Response.status(Response.Status.OK).entity("{}").build();

	}

	@POST
	@Path("/shipowners-2")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFileWithData2(
			@FormDataParam("files") InputStream inputStream,
			@FormDataParam("files") FormDataContentDisposition uploadedInputStream,
			@FormDataParam("sailorInfo") String sailorInfo
			)
	{
		System.out.println(uploadedInputStream.getFileName());
		System.out.println(sailorInfo);
		return Response.status(Response.Status.OK).entity("{}").build();

	}
	

	@POST
	@Path("/shipowners")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFileWithData3(FormDataMultiPart form,
										@HeaderParam("Authorization") String token,
										@HeaderParam("Language") String language)

	{
		List<BodyPart> parts = form.getBodyParts();
		// Getting sailorInfo JSON object
		ShipownerRequestJson sh = new ShipownerRequestJson();
		for(BodyPart part : parts)
		{
			if(part.getMediaType().getType().compareTo("text") == 0)
			{
				Utils.populateObjectFromJSON(part.getEntityAs(String.class), sh);
				break;
			}
		}
		System.out.println(sh.usersId + " - " + sh.navigationLicense + " - " + sh.sailingLicense);

		// getting destination path from context
		String destPath = null;
		try 
		{
			destPath = context.getResource("/images/shipowner").getPath();
		}
		catch (MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving context path");
			// TODO return error
		}

		String prefix = "sh_" + sh.usersId + "_licenses_";
		int lastIndex = -1;
		// Checking if any existing file to preserve and marking the starting sequence number for 
		// this file set to upload
		ArrayList<String> images = UploadFiles.getExistingFilesPath(prefix, destPath);
		int pos;
		int a = 0;
		for(String fName : images)
		{
			pos = fName.lastIndexOf("_") + 1;
			fName = fName.substring(pos);
			a = Integer.parseInt(fName.substring(0, fName.lastIndexOf(".")));
			if (lastIndex < a)
				lastIndex = a;
		}
		lastIndex++;
		// lastIndex = 0;
		
		// uploading the files into temp dir
		for(BodyPart part : parts)
		{
			if(part.getMediaType().getType().compareTo("image") == 0)
			{
				UploadFiles.uploadFiles(part, destPath, prefix, token, lastIndex++);
			}
		}
		
		// moving to the final destination
		UploadFiles.newMoveFiles(destPath + File.separator + token, destPath, prefix, false);
		return Response.status(Response.Status.OK).entity("{}").build();
	}	
}
