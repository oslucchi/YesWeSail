package com.yeswesail.rest.users;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpObject;
import com.ecwid.maleorang.annotation.Field;
import com.ecwid.maleorang.method.v3_0.members.EditMemberMethod;
import com.ecwid.maleorang.method.v3_0.members.MemberInfo;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.UploadFiles;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.Boats;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.DocumentTypes;
import com.yeswesail.rest.DBUtility.Documents;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.AddressInfoJson;
import com.yeswesail.rest.jsonInt.BoatsJson;
import com.yeswesail.rest.jsonInt.Contactsjson;
import com.yeswesail.rest.jsonInt.ShipownerRequestJson;
import com.yeswesail.rest.jsonInt.SubscribeJson;
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
			catch(Exception e) 
			{
				if (e.getMessage().compareTo("No record found") == 0)
				{
					log.error("No record found on token: " + jsonIn.token);
					utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "auth.loginTokenNotExist"), true);
					errMsg = utils.jsonize();
				}
				else
				{
					log.warn("Exception " + e.getMessage(), e);
					utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
					errMsg = utils.jsonize();
				}
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
				log.warn("Exception " + e.getMessage(), e);
				utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
				errMsg = utils.jsonize();
			}
		}
		
		return errMsg;
	}
	

	public static class MergeVars extends MailchimpObject {
	    @Field
	    public String EMAIL, FNAME, LNAME;

	    public MergeVars() { }

	    public MergeVars(String email, String fname, String lname) {
	        this.EMAIL = email;
	        this.FNAME = fname;
	        this.LNAME = lname;
	    }
	}
	
	@POST
	@Path("/subscribe")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response subscribe(SubscribeJson jsonIn, @HeaderParam("Authorization") String token)
	{
	    SessionData session = SessionData.getInstance();
		Users u = null;
		int languageId = Constants.LNG_EN;
		if ((token != null) && !token.trim().isEmpty())
		{
			languageId = session.getLanguage(token);
			u = session.getBasicProfile(token);
			if ((u == null) || (u.getEmail().compareTo(jsonIn.u.email) != 0))
			{
				utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "users.mailSpoofing"), true);
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(utils.jsonize())
						.build();
			}
		}
		else
		{
			u = new Users();
			u.setEmail(jsonIn.u.email);
			u.setName(jsonIn.u.name);
			u.setSurname(jsonIn.u.surname);
		}

		switch(jsonIn.what.toUpperCase())
		{
		case "MAILCHIMP":
			MailchimpClient client = new MailchimpClient(prop.getMailchimpAPIKEY());
			EditMemberMethod.Create method = 
				new EditMemberMethod.Create(prop.getMailchimpListId(), u.getEmail());
			method.status = "subscribed";
			method.merge_fields = new MailchimpObject();
			method.merge_fields.mapping.put("FNAME", u.getName());
			method.merge_fields.mapping.put("LNAME", u.getSurname());
			MemberInfo member = null;
			try
			{
				member = client.execute(method);
			}
			catch(Exception e)
			{
				log.error("Error trying to register " + jsonIn.u.email + " to mailchimp (Exception " + e.getMessage() + ")", e);
				if (e.getMessage().contains("is already a list member"))
				{
					utils.addToJsonContainer("error", 
							LanguageResources.getResource(languageId, "mailchimp.already.registered"), true);
				}
				else
				{
					utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
				}
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(utils.jsonize())
						.build();
			}
			finally
			{
				try 
				{
					client.close();
				} 
				catch(IOException e) {
					log.warn("Unable to close the malchimp client. (Exception " + e.getMessage() + ")", e);
				}
			}
			log.debug("Member subscribed. Status " + member.status + ". Last update " + member.last_changed);
			break;
			
		default:
			break;
		}

		return Response.status(Response.Status.OK).entity("{}").build();
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
		catch(IOException e) {
			log.error("Error jasonizing basic profile (" + e.getMessage() + ")", e);
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
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
		catch(IOException e) {
			log.error("Error jsonizing basic profile (" + e.getMessage() + ")", e);
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
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
		catch(IOException e) {
			log.error("Error jsonizing basic profile (" + e.getMessage() + ")", e);
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
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
				doc.setImages(UploadFiles
									.getExistingFilesPathAsURL(
											"docs_" + u.getIdUsers() + "_" + doc.getIdDocuments() + 
											"_", "/images/shipowner")
									.get(UploadFiles.LARGE));
			}
			utils.addToJsonContainer("docs", docs, false);
			Events[] eventList = null;
			try 
			{
				eventList = Events.findByFilter("shipOwnerId = " + u.getIdUsers(), languageId);
				utils.addToJsonContainer("events", eventList, false);				
			} 
			catch(Exception e) 
			{
				log.debug("Exception " + e.getMessage() + " retrieving events for shipowner " + u.getIdUsers(), e);
			}
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
				if ((ai == null) || (ai.length == 0))
				{
					ai = new AddressInfo[2];
					ai[0] = new AddressInfo();
					ai[1] = ai[0];
				}
				usersList.get(i).setPersonalInfo(ai[0]);
				usersList.get(i).setBillingInfo(ai[1]);
			}
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
		SessionData sd = SessionData.getInstance();
		if ((sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR) &&
			(sd.getBasicProfile(token).getIdUsers() != userId))
		{
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
					.build();
		}

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
		
		Utils jsonizer = new Utils();

		ArrayList<String> imagePath = UploadFiles.getExistingFilesPathAsURL(prefix, "/images/users").get(UploadFiles.LARGE);
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
			String imgPath = imagePath.get(0);
			if (imgPath.startsWith(prop.getWebHost()))
			{
				imgPath = imagePath.get(0).substring(prop.getWebHost().length() + 1);
			}
			u.setImageURL(imgPath);
			u.update(conn, "idUsers");
			sd.addUser(u.getIdUsers(), languageId);;
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage() + " setting user's image URL", e);
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		String jsonResponse = jsonizer.jsonize();
		return Response.status(status).entity(jsonResponse).build();
    }

	@PUT
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(UsersJson jsonIn, @PathParam("userId") int userId, 
						   @HeaderParam("Authorization") String token)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		jsonIn.idUsers = userId;
		log.debug("user id " + userId + " isadmin " + !Utils.userIsAdmin(token, languageId) +
				  " email requested for change " + sd.getBasicProfile(token).getEmail() + 
				  " email in json data " + jsonIn.email);
		
		if (!Utils.userIsAdmin(token, languageId) && (jsonIn.email.compareTo(sd.getBasicProfile(token).getEmail()) != 0))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "users.mailSpoofing");
		}
		
		boolean retVal = true;
		if ((jsonIn.billingInfo != null) && 
			(jsonIn.billingInfo.taxCode != null) &&
			(jsonIn.billingInfo.taxCode.trim().compareTo("") != 0))
		{
			retVal = TaxcodeChecker.checkTaxcode(jsonIn.billingInfo.country, 
												 jsonIn.billingInfo.taxCode, TaxcodeChecker.COMPANY);
		}
		else if ((jsonIn.personalInfo != null) && 
				 (jsonIn.personalInfo.taxCode != null)  &&
				 (jsonIn.personalInfo.taxCode.trim().compareTo("") != 0))
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
			u.setAbout(jsonIn.about);
			if (sd.getBasicProfile(token).getRoleId() == Roles.ADMINISTRATOR)
				u.setStatus(jsonIn.status);
			if (jsonIn.birthday != null)
				u.setBirthday(jsonIn.birthday, "dd/MM/yyyy");
			u.setRoleId(jsonIn.roleId);
			u.update(conn, "idUsers");
			if ((jsonIn.personalInfo.type != null) && (jsonIn.billingInfo.type != null))
			{
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
			}
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
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
			u.setStatus(Constants.STATUS_ACTIVE);
			u.setRoleId(Roles.DUMMY);
			u.insert(conn, "idUsers", u);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
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
		log.trace("Got shipowner info: " + sh.usersId + " - " + sh.sailingLicense);
		
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
			log.trace("uploading file...");
			results = UploadFiles.uploadBodyPart(parts, "filesSailingDocs", token, "/images/shipowner", 
												 prefix, acceptableTypes, languageId, false);
			log.trace("done!");
			uploadedList = (ArrayList<String>) results[0];
			rejectedList = (ArrayList<String>) results[1];
			rejectionMessage = (ArrayList<String>) results[2];
			if (rejectionMessage.size() != 0)
			{
				log.debug("Some files have been rejected");
				DBInterface.TransactionRollback(conn);
				Utils u = new Utils();
				u.addToJsonContainer("uploadedList", uploadedList, true);
				u.addToJsonContainer("rejectedList", rejectedList, false);
				u.addToJsonContainer("rejectionMessage", rejectionMessage, false);
				return Response.status(Status.NOT_ACCEPTABLE).entity(u.jsonize()).build();
			}
			String destPath = prop.getContext().getResource("/images/shipowner").getPath();
			log.trace("move uploaded files in the proper folder");
			UploadFiles.moveFiles( destPath + "/" + token, destPath, "docs_" + sh.usersId + "_" , true);
			
			PendingActions pa = new PendingActions();
			pa.setActionType(PendingActions.STATUS_UPGRADE);
			pa.setUserId(sd.getBasicProfile(token).getIdUsers());
			pa.setLink("rest/requests/" + PendingActions.STATUS_UPGRADE + "/" + 
						sd.getBasicProfile(token).getIdUsers());
			pa.setCreated(new Date());
			pa.setUpdated(pa.getCreated());
			pa.setStatus(Constants.STATUS_PENDING_APPROVAL);
			pa.insert(conn, "idPendingActions", pa);

			log.trace("User's status upgrade request added");
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on insert", e);
			DBInterface.TransactionRollback(conn);
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
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
		SessionData sd = SessionData.getInstance();
		if ((sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR) &&
			(sd.getBasicProfile(token).getIdUsers() != shipownerId))
		{
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
					.build();
		}
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
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on insert", e);
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
		catch(MalformedURLException e) 
		{
			destPath = "";
			log.warn("Exception " + e.getMessage() + " retrieving context path", e);
			return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		UploadFiles.moveFiles(destPath + "/" + token, destPath, 
							  "bo_" + shipownerId + "_" + bo.getIdBoats() + "_", true);

//		// Create images thumbnail
//		prefix = "bo_" + shipownerId + "_" + bo.getIdBoats() + "_";
//		ImageHandler imgHnd = new ImageHandler();
//		for(String image : UploadFiles.getExistingFilesPathAsURL(prefix, "/images/boats"))
//		{
//			imgHnd.scaleImages(image);
//		}

		utils.addToJsonContainer("uploadedList", uploadedList, true);
		if (rejectionMessage.size() != 0)
		{
			utils.addToJsonContainer("rejectedList", rejectedList, false);
			utils.addToJsonContainer("rejectionMessage", rejectionMessage.get(0), false);
			return Response.status(Status.PARTIAL_CONTENT).entity(utils.jsonize()).build();
		}
		return Response.status(Status.OK).entity(utils.jsonize()).build();
	}

	@DELETE
	@Path("/shipowners/{id}/boats/{idBoats}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteBoat(@PathParam("id") int shipownerId,
							   @PathParam("idBoats") int idBoats,
							   @HeaderParam("Authorization") String token,
							   @HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if ((sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR) &&
			(sd.getBasicProfile(token).getIdUsers() != shipownerId))
		{
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
					.build();
		}

		DBConnection conn = null;
		Boats bo = null;
		Boats[] boats = null;
		try 
		{
			bo = new Boats();
			conn = DBInterface.connect();
			log.trace("Deleting boat id " + idBoats);
			bo.delete(conn, idBoats);
			boats = Boats.findAll(languageId, shipownerId);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on boat delete", e);
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
			ArrayList<String> images = UploadFiles.getExistingFilesPathAsURL(
							"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_img_", "/images/boats").get(UploadFiles.LARGE);
			images.addAll(UploadFiles.getExistingFilesPathAsURL(
									"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_bp_", "/images/boats").get(UploadFiles.LARGE));
			boat.setImages(images);
			boat.setDocs(
					UploadFiles.getExistingFilesPathAsURL(
							"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_doc_", "/images/boats").get(UploadFiles.LARGE));
		}

		utils.addToJsonContainer("boats", boats, true);
				String jsonResponse = utils.jsonize();
		return Response.status(Response.Status.OK).entity(jsonResponse).build();
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
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on insert", e);
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
			ArrayList<ArrayList<String>> imagesTemp = UploadFiles.getExistingFilesPathAsURL(
					"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_img_", "/images/boats");
			ArrayList<String> images = new ArrayList<>();
			ArrayList<String> imagesSmall = new ArrayList<>();
			ArrayList<String> imagesMedium = new ArrayList<>();
			ArrayList<String> imagesLarge = new ArrayList<>();

			images.addAll(imagesTemp.get(UploadFiles.ORIGINAL));
			imagesSmall.addAll(imagesTemp.get(UploadFiles.SMALL));
			imagesMedium.addAll(imagesTemp.get(UploadFiles.MEDIUM));
			imagesLarge.addAll(imagesTemp.get(UploadFiles.LARGE));

			imagesTemp = UploadFiles.getExistingFilesPathAsURL(
					"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_bp_", "/images/boats");
			images.addAll(imagesTemp.get(UploadFiles.ORIGINAL));
			imagesSmall.addAll(imagesTemp.get(UploadFiles.SMALL));
			imagesMedium.addAll(imagesTemp.get(UploadFiles.MEDIUM));
			imagesLarge.addAll(imagesTemp.get(UploadFiles.LARGE));

			boat.setImages(images);
			boat.setImagesSmall(imagesSmall);
			boat.setImagesMedium(imagesMedium);
			boat.setImagesLarge(imagesLarge);

			boat.setDocs(
					UploadFiles.getExistingFilesPathAsURL(
							"bo_" + boat.getOwnerId() + "_" + boat.getIdBoats() + "_doc_", "/images/boats").get(UploadFiles.LARGE));
		}

		utils.addToJsonContainer("boats", boats, true);
				String jsonResponse = utils.jsonize();
		return Response.status(Response.Status.OK).entity(jsonResponse).build();
	}


	@POST
	@Path("/contacts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response contacs(Contactsjson jsonIn,
							 @HeaderParam("Authorization") String token,
							 @HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);

		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();			
			Mailer.sendMail(jsonIn.email, prop.getContactsMailTo(), null, 
							jsonIn.subject, jsonIn.message, null);
		}
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on insert", e);
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		utils.addToJsonContainer("message", LanguageResources.getResource(languageId, "mailer.sentOk"), true);
		return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
	}
	
	@GET
	@Path("/{userId}/events")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response usersEvents(@HeaderParam("Authorization") String token,
								@HeaderParam("Language") String language,
								@PathParam("userId") int userId)
	{
		Events[] eventsList = null;
		DBConnection conn = null;
		int languageId = Utils.setLanguageId(language);
		
		try 
		{
			conn = DBInterface.connect();
			Users u = new Users(conn, userId);
			if (!Utils.userSelfOrAdmin(token, userId,languageId) ||
				(u.getRoleId() < Roles.SHIP_OWNER))
			{
				utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(utils.jsonize())
						.build();
			}
		
			eventsList = Events.findByFilter(
							"WHERE shipownerId = " + userId + " " +
							"ORDER BY dateStart DESC", languageId);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		Utils ut = new Utils();
		ut.addToJsonContainer("events", eventsList, true);
		return Response.status(Response.Status.OK).entity(ut.jsonize()).build();
	}

	@GET
	@Path("/{userId}/tickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response usersTickets(@HeaderParam("Authorization") String token,
								 @PathParam("userId") int userId)
	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
		// verifcare test
		if (!Utils.userSelfOrAdmin(token, sd.getBasicProfile(token).getIdUsers(),languageId))
		{
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.unauthorized"), true);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(utils.jsonize())
					.build();
		}
		
		EventTicketsSold[] eventsList = null;
		DBConnection conn = null;
		// int userId = sd.getBasicProfile(token).getIdUsers();
		try 
		{
			conn = DBInterface.connect();
			eventsList = EventTicketsSold.findTicketSoldToUser(userId, languageId);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			utils.addToJsonContainer("error", LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")", true);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(utils.jsonize())
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		Utils ut = new Utils();
		ut.addToJsonContainer("tickets", eventsList, true);
		return Response.status(Response.Status.OK).entity(ut.jsonize()).build();
	}	
}