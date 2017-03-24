package com.yeswesail.rest.Admin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;

@Path("/adminActions")
public class AdminActions {

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());

	private Response prepareAndSendMail(String bodyProperty, String subjectProperty, String language, Users user)
	{
		int languageId = Utils.setLanguageId(language);
		try
		{
			String httpLink = prop.getWebHost() + "#/reset-password?email=" + user.getEmail();
			String htmlText = LanguageResources.getResource(languageId, bodyProperty);
			htmlText = htmlText.replaceAll("CNFMLINK", httpLink);
			htmlText = htmlText.replaceAll("USERNAME", user.getName());
			String subject = LanguageResources.getResource(Constants.getLanguageCode(language), subjectProperty);
			URL url = null;
			url = prop.getContext().getResource("/images/application/mailLogo.png");
			String imagePath = url.getPath();
			Mailer.sendMail(user.getEmail(), subject, htmlText, imagePath);
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

	@GET 
	@Path("/mailToUsers/{roleId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveAll(@PathParam("roleId") int roleId,
								@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd == null)
		{
			log.error("Unable to retrieve session data");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}
		if (sd.getBasicProfile(token) == null)
		{
			log.error("Unable to retrieve a basic profile for token " + token);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		Response response;
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}
		DBConnection conn = null;
		String bodyRef = null;
		switch(roleId)
		{
		case Roles.SHIP_OWNER:
			bodyRef = "mail.newhome.shipowner.body";
			break;
		default:
			bodyRef = "mail.newhome.body";
			break;
		}
		try 
		{
			conn = DBInterface.connect();
			ArrayList<Users> usersList = Users.findUsersbyRole(roleId);
			log.debug("Found " + usersList.size() + " users");
			for (Users user : usersList)
			{
				log.debug("Evaluating " + user.getEmail() + " with status '" + user.getStatus() + "'");
				if ((user.getEmail() == null) || (user.getStatus().compareTo(Constants.STATUS_PENDING_APPROVAL) != 0))
				{
					continue;
				}
				log.debug("Sending email to: " + user.getEmail());
				if ((response = prepareAndSendMail(bodyRef, "mail.newhome.subject", 
												   "it-IT", user)) != null)
					return response;

			}
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on mailToUsers");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@GET 
	@Path("/dumpSessionData")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response dumpSessionData(@HeaderParam("Language") String language,
									@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd == null)
		{
			log.error("Unable to retrieve session data");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}
		if (sd.getBasicProfile(token) == null)
		{
			log.error("Unable to retrieve a basic profile for token " + token);
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}
		Utils ut = new Utils();
		ut.addToJsonContainer("sessionData", sd.getAllItems(), true);
		return Response.status(Response.Status.OK).entity(ut.jsonize()).build();
	}

}
