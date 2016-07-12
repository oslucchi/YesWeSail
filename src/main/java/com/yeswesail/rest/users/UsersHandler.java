package com.yeswesail.rest.users;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.JSONArray;
import org.json.JSONObject;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.UploadFiles;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.Boats;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.DocumentTypes;
import com.yeswesail.rest.DBUtility.Documents;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.AddressInfoJson;
import com.yeswesail.rest.jsonInt.BoatsJson;
import com.yeswesail.rest.jsonInt.ShipownerRequestJson;
import com.yeswesail.rest.jsonInt.UsersJson;

@Path("/users")
public class UsersHandler {
	String contextPath = null;

	Utils utils = new Utils();
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
					utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "auth.loginTokenNotExist"), true);
					errMsg = utils.jsonize();
				}
				else
				{
					utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
					errMsg = utils.jsonize();
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
				utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
				errMsg = utils.jsonize();
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
			// u.setPassword("******");
			json = mapper.writeValueAsString(u);
		} 
		catch (IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")");
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
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
			uWiped.setIsShipOwner(u.getIsShipOwner());
			uWiped.setIdUsers(u.getIdUsers());
			uWiped.setAbout(u.getAbout());
			uWiped.setExperiences(u.getExperiences());
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}

		return Response.status(Response.Status.OK).entity(json).build();
	}

	private Response fillWholeProfile(int userId, int languageId, boolean trustedRequestor)
	{
		Users u = null;
		AddressInfo[] ai = null;
		Documents[] docs;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			u = new Users(conn, userId);
			// u.setPassword("******");
			ai = AddressInfo.findUserId(u.getIdUsers());
			if (ai.length == 0)
			{
				ai = new AddressInfo[2];
				ai[0] = new AddressInfo();
				ai[0].setType("D");
				ai[1] = new AddressInfo();
				ai[1].setType("I");
			}
			u.setPersonalInfo(ai[0]);
			u.setBillingInfo(ai[1]);
			docs = Documents.findAllUsersDoc(languageId, u.getIdUsers());
		}
		catch (Exception e) 
		{
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		utils.addToJsonContainer("user", u, true);
		if (trustedRequestor && (docs != null))
		{
			for(Documents doc : docs)
			{
				doc.setImages(
						UploadFiles.getExistingFilesPath("docs_" + u.getIdUsers() + "_" + doc.getIdDocuments() + "_", "/images/shipowner"));
			}
			utils.addToJsonContainer("docs", docs, false);
		}
		return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
	}

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response userProfileById(@PathParam("userId") int userId, @HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		if (!Utils.userSelfOrAdmin(token, userId, languageId))
		{
			return fillBasicProfile(userId, languageId, false);
		}
		else		
		{
			return fillWholeProfile(userId, languageId, true);
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
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
				usersList.get(i).setPersonalInfo(ai[0]);
				usersList.get(i).setBillingInfo(ai[1]);
			}
		}
		catch (Exception e) 
		{
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
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
	@POST
    @Path("/{userId}/profilePic")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadProfilePic(FormDataMultiPart form,
						   @HeaderParam("Authorization") String token,
						   @HeaderParam("Language") String language,
						   @PathParam("userId") int userId)
    {
		int languageId = Utils.setLanguageId(language);
		List<BodyPart> parts = form.getBodyParts();
		
		String[] acceptableTypes = {
				"image/gif",
				"image/png",
				"image/jpeg",
				"image/jpg"
		};

		String prefix = "us_" + userId + "_";

		Response response = UploadFiles.uploadFromRestRequest(
								parts, token, "/images/users", 
								prefix, acceptableTypes, languageId, true);
		
		try {
			contextPath = prop.getContext().getResource("/images/users").getPath();
		}
		catch (MalformedURLException e) 
		{
			contextPath = null;
			log.warn("Exception " + e.getMessage() + " retrieving context path");	
		}
		Utils jsonizer = new Utils();

		ArrayList<String> imagePath = UploadFiles.getExistingFilesPath(prefix, contextPath);
		jsonizer.addToJsonContainer("images", imagePath, true);
		
		StatusType status = Response.Status.OK;
		if (response.getStatusInfo() != Response.Status.OK)
		{
			status = Response.Status.PARTIAL_CONTENT;
			JSONObject jo = new JSONObject((String)response.getEntity());			

			jsonizer.addToJsonContainer("rejectionMessage", jo.get("rejectionMessage"), false);
			
			JSONArray rejected = (JSONArray)jo.get("rejectedList");
			String[] s = new String[rejected.length()];
			for(int i = 0; i < rejected.length(); i++)
			{
				s[i] = rejected.getString(i);
			}
			jsonizer.addToJsonContainer("rejectedList", s, false);
		}
		
		Users u = null;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.connect();
			u = new Users(conn, userId);
			u.setImageURL(imagePath.get(0));
			u.update(conn, "idUsers");
			SessionData sd = SessionData.getInstance();
			sd.addUser(u.getIdUsers(), languageId);;
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage() + " setting user's image URL");
		}
		String jsonResponse = jsonizer.jsonize();
		return Response.status(status).entity(jsonResponse).build();
    }

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(UsersJson jsonIn, @HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		boolean retVal = true;
		if ((jsonIn.billingInfo != null) && (jsonIn.billingInfo.taxCode != null))
		{
			retVal = TaxcodeChecker.checkTaxcode(jsonIn.billingInfo.country, 
												 jsonIn.billingInfo.taxCode, TaxcodeChecker.COMPANY);
		}
		else if ((jsonIn.personalInfo != null) && (jsonIn.personalInfo.taxCode != null))
		{
			retVal = TaxcodeChecker.checkTaxcode(jsonIn.personalInfo.country, 
												 jsonIn.personalInfo.taxCode, TaxcodeChecker.PERSONAL);
		}
		if (retVal == false)
		{
			return Utils.jsonizeResponse(Status.BAD_REQUEST, null, languageId, "users.taxcodeIncorrect");
		}
		
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
			if (sd.getBasicProfile(token).getRoleId() == Roles.ADMINISTRATOR)
				u.setStatus(jsonIn.status);
			if (jsonIn.birthday != null)
				u.setBirthday(jsonIn.birthday, "yyyy-MM-dd");
			u.setRoleId(jsonIn.roleId);
			AddressInfoJson[] aiJson = new AddressInfoJson[2];
			aiJson[0] = jsonIn.personalInfo;
			aiJson[1] = jsonIn.billingInfo;
			AddressInfo ai = new AddressInfo();
			String sql = "DELETE FROM AddressInfo WHERE userId = " + jsonIn.idUsers;
			AddressInfo.executeStatement(conn, sql, false);
			for(int i = 0; i < 2; i++)
			{
				ai.setAddress1(aiJson[i].address1);
				ai.setAddress2(aiJson[i].address2);
				ai.setCompanyName(aiJson[i].companyName);
				ai.setCity(aiJson[i].city);
				ai.setCountry(aiJson[i].country);
				ai.setProvince(aiJson[i].province);
				ai.setZip(aiJson[i].zip);
				ai.setTaxCode(aiJson[i].taxCode);
				ai.setType(aiJson[i].type);
				ai.setUserId(jsonIn.idUsers);
				ai.insert(conn, "idAddressInfo", ai);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
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
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/shipowners")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response requestUpgrade(FormDataMultiPart form,
										@HeaderParam("Authorization") String token,
										@HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);
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
		log.trace("Got shipowner info: " + sh.usersId + " - " + sh.navigationLicense + " - " + sh.sailingLicense);
		
		DBConnection conn = null;
		SessionData sd = SessionData.getInstance();
		String[] acceptableTypes = {
				"application/pdf",
				"image/png",
				"image/jpeg",
				"image/jpg"
		};

		ArrayList<String> uploadedList = new ArrayList<>();
		ArrayList<String> rejectedList = new ArrayList<>();
		ArrayList<String> rejectionMessage = new ArrayList<>();
		try 
		{
			conn = DBInterface.TransactionStart();

			Documents doc = new Documents();
			doc.setUserId(sh.usersId);
			doc.setDocumentTypesId(DocumentTypes.SAILING_LICENSE);
			doc.setNumber(sh.sailingLicense);
			int docId = doc.insertAndReturnId(conn, "idDocuments", doc);
			String prefix = "docs_" + sh.usersId + "_" + docId + "_";
			Object[] results = null;
			results = UploadFiles.uploadBodyPart(parts, "filesSailingDocs", token, "/images/shipowner", 
												 prefix, acceptableTypes, languageId, false);
			uploadedList = (ArrayList<String>) results[0];
			rejectedList = (ArrayList<String>) results[1];
			rejectionMessage = (ArrayList<String>) results[2];
			if (rejectionMessage.size() != 0)
			{
				DBInterface.TransactionRollback(conn);
				Utils u = new Utils();
				u.addToJsonContainer("uploadedList", uploadedList, true);
				u.addToJsonContainer("rejectedList", rejectedList, false);
				u.addToJsonContainer("rejectionMessage", rejectionMessage, false);
				return Response.status(Status.NOT_ACCEPTABLE).entity(u.jsonize()).build();
			}
			
			doc.setUserId(sh.usersId);
			doc.setDocumentTypesId(DocumentTypes.NAVIGATION_CERTIFICATE);
			doc.setNumber(sh.navigationLicense);
			docId = doc.insertAndReturnId(conn, "idDocuments", doc);
			prefix = "docs_" + sh.usersId + "_" + docId + "_";
			results = UploadFiles.uploadBodyPart(parts, "filesNavigationDocs", token, "/images/shipowner", 
												 prefix, acceptableTypes, languageId, false);

			uploadedList.addAll((ArrayList<String>) results[0]);
			rejectedList.addAll((ArrayList<String>) results[1]);
			rejectionMessage.addAll((ArrayList<String>) results[2]);
			if (rejectionMessage.size() != 0)
			{
				DBInterface.TransactionRollback(conn);
				Utils u = new Utils();
				u.addToJsonContainer("uploadedList", uploadedList, true);
				u.addToJsonContainer("rejectedList", rejectedList, false);
				u.addToJsonContainer("rejectionMessage", rejectionMessage, false);
				return Response.status(Status.NOT_ACCEPTABLE).entity(u.jsonize()).build();
			}
			String destPath = prop.getContext().getResource("/images/shipowner").getPath();
			UploadFiles.moveFiles( destPath + "/" + token, destPath, "docs_" + sh.usersId + "_" , true);
			
			PendingActions pa = new PendingActions();
			pa.setActionType("statusUpgrade");
			pa.setUserId(sd.getBasicProfile(token).getIdUsers());
			pa.setLink("rest/requests/statusUpgrade/" + sd.getBasicProfile(token).getIdUsers());
			pa.setCreated(new Date());
			pa.setUpdated(pa.getCreated());
			pa.setStatus("P");
			pa.insert(conn, "idPendingActions", pa);

			log.trace("User's status upgrade request added");
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on insert");
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}

		Utils u = new Utils();
		u.addToJsonContainer("uploadedList", uploadedList, true);
		return Response.status(Status.OK).entity(u.jsonize()).build();
	}	
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/shipowners/{id}/boats")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addBoat(FormDataMultiPart form,
							@PathParam("id") int shipownerId,
							@HeaderParam("Authorization") String token,
							@HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);
		// int shipownerId = 2;
		// Getting sailorInfo JSON object
		List<BodyPart> parts = form.getBodyParts();
		BoatsJson boat = new BoatsJson();
		for(BodyPart part : parts)
		{
			if ((part.getMediaType().getType().compareTo("text") == 0) &&
				(part.getContentDisposition().getParameters().get("name").compareTo("boatInfo") == 0))
			{
				Utils.populateObjectFromJSON(part.getEntityAs(String.class), boat);
				break;
			}
		}
		log.trace("Got boat info: " + 
					"engineType " + boat.engineType + " - " + 
					"model " + boat.model + " - " + 
					"name " + boat.name + " - " + 
					"length " + boat.length + " - " + 
					"plate " + boat.plate + " - " + 
					"year " + boat.year + " - " + 
					"cabinsWithBathroom " + boat.cabinsWithBathroom + " - " + 
					"cabinsNoBathroom " + boat.cabinsNoBathroom + " - " + 
					"bunks " + boat.bunks + " - " + 
					"sharedBathrooms " + boat.sharedBathrooms + " - " + 
					"insurance " + boat.insurance + " - " + 
					"securityCertification " + boat.securityCertification + " - " + 
					"RTFLicense " + boat.RTFLicense 
				);

		DBConnection conn = null;
		Boats bo = null;
		try 
		{
			bo = new Boats();
			conn = DBInterface.connect();
			
			bo.setBunks(boat.bunks);
			bo.setCabinsNoBathroom(boat.cabinsNoBathroom);
			bo.setCabinsWithBathroom(boat.cabinsWithBathroom);
			bo.setEngineType(boat.engineType);
			bo.setLength(boat.length);
			bo.setModel(boat.model);
			bo.setName(boat.name);
			bo.setOwnerId(shipownerId);
			bo.setPlate(boat.plate);
			bo.setSharedBathrooms(boat.sharedBathrooms);
			bo.setYear(boat.year);
			bo.setInsurance(boat.insurance);
			bo.setSecurityCertification(boat.securityCertification);
			bo.setRTFLicense(boat.RTFLicense);
			bo.setIdBoats(bo.insertAndReturnId(conn, "idBoats", bo));
			
			log.trace("User's status upgrade request added");
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on insert");
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		String prefix = null;
		String[] acceptableTypes = {
				"image/gif",
				"image/png",
				"image/jpeg",
				"image/jpg"
		};
		ArrayList<String> uploadedList = new ArrayList<>();
		ArrayList<String> rejectedList = new ArrayList<>();
		ArrayList<String> rejectionMessage = new ArrayList<>();
		Object[] results = null;
		prefix = "bo_" + shipownerId + "_" + bo.getIdBoats() + "_img_";
		results = UploadFiles.uploadBodyPart(parts, "other", token, "/images/boats", 
											 prefix, acceptableTypes, languageId, false);
		uploadedList = (ArrayList<String>) results[0];
		rejectedList = (ArrayList<String>) results[1];
		rejectionMessage = (ArrayList<String>) results[2];
		
		prefix = "bo_" + shipownerId + "_" + bo.getIdBoats() + "_bp_";
		results = UploadFiles.uploadBodyPart(parts, "bluePrints", token, "/images/boats", 
											 prefix, acceptableTypes, languageId, false);
		uploadedList.addAll((ArrayList<String>) results[0]);
		rejectedList.addAll((ArrayList<String>) results[1]);
		rejectionMessage.addAll((ArrayList<String>) results[2]);
		
		acceptableTypes = new String[] {
				"application/pdf",
				"image/gif",
				"image/png",
				"image/jpeg",
				"image/jpg"
		};
		prefix = "bo_" + shipownerId + "_" + bo.getIdBoats() + "_doc_";
		results = UploadFiles.uploadBodyPart(parts, "docs", token, "/images/boats", 
				 							 prefix, acceptableTypes, languageId, false);
		uploadedList.addAll((ArrayList<String>) results[0]);
		rejectedList.addAll((ArrayList<String>) results[1]);
		rejectionMessage.addAll((ArrayList<String>) results[2]);
		
		String destPath = null;
		try {
			destPath = prop.getContext().getResource("/images/boats").getPath();
		}
		catch (MalformedURLException e) 
		{
			destPath = "";
			log.warn("Exception " + e.getMessage() + " retrieving context path");
			return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		UploadFiles.moveFiles(destPath + "/" + token, destPath, 
							  "bo_" + shipownerId + "_" + bo.getIdBoats() + "_", true);

		utils.addToJsonContainer("uploadedList", uploadedList, true);
		if (rejectionMessage.size() != 0)
		{
			utils.addToJsonContainer("rejectedList", rejectedList, false);
			utils.addToJsonContainer("rejectionMessage", rejectionMessage.get(0), false);
			return Response.status(Status.PARTIAL_CONTENT).entity(utils.jsonize()).build();
		}
		return Response.status(Status.OK).entity(utils.jsonize()).build();
	}

	@GET
	@Path("/shipowners/{id}/boats")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBoats(@PathParam("id") int shipownerId,
							 @HeaderParam("Authorization") String token,
							 @HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);

		DBConnection conn = null;
		Boats[] boats = null;
		try 
		{
			conn = DBInterface.connect();			
			boats = Boats.findAll(languageId, shipownerId);
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on insert");
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		for(Boats boat : boats)
		{
			boat.setImages(
					UploadFiles.getExistingFilesPath(
							"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_img_", "/images/boats"));
			boat.setDocs(
					UploadFiles.getExistingFilesPath(
							"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_doc_", "/images/boats"));
		}

		utils.addToJsonContainer("boats", boats, true);
				String jsonResponse = utils.jsonize();
		return Response.status(Response.Status.OK).entity(jsonResponse).build();
	}
}