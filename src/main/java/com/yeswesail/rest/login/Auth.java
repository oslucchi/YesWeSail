package com.yeswesail.rest.login;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.Users;


/*
 * Tra i parametri della funzione
 * 						   @Context HttpServletRequest httpRequest,
 */
@Path("/auth/register")
public class Auth {
	DBConnection conn;
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(@QueryParam("email") String email, 
						   @QueryParam("password") String password)
	{
		ApplicationProperties prop = new ApplicationProperties();
		String errorMsg = "";
		String token = UUID.randomUUID().toString();
		try 
		{
			conn = DBConnection.getInstance();
			Users u = new Users();
			u.setEmail(email);
			u.setPassword(password);
			u.setStatus("D");
			u.setIsShipOwner("F");
			u.setConnectedVia("P");
			u.setRoleId(1);
			int id = u.insertAndReturnId("idUsers", u);
			RegistrationConfirm rc = new RegistrationConfirm();
			rc.setUserId(id);
			rc.setToken(token);
			rc.setStatus("A");
			id = rc.insertAndReturnId("idRegistrationConfirm", rc);
		}
		catch (Exception e) {
			if (e.getCause().getMessage().substring(0, 15).compareTo("Duplicate entry") == 0)
			{
				errorMsg = LanguageResources.getResource("users.alreadyRegistered");
			}
			else
			{
				errorMsg = LanguageResources.getResource("generic.execError") + " (" + e.getMessage() + ")";
			}
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(errorMsg).build();
		}
		try
		{
			String httpLink = prop.getWebHost() + "/rest/auth/confirmUser/" + token;
	        String htmlText = LanguageResources.getResource("mail.body");
	        htmlText.replaceAll("%CNFMLINK%", httpLink);
	        String subject = LanguageResources.getResource("mail.subject");
			URL url = getClass().getResource("/images/mailLogo.png");
			String imagePath = url.getPath();
			Mailer.sendMail(email, subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			errorMsg = LanguageResources.getResource("mailer.sendError") + " (" + e.getMessage() + ")";
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMsg).build();
		}
		
		return Response.status(Response.Status.OK).entity(LanguageResources.getResource("auth.registerRedirectMsg")).build();
	}
}
