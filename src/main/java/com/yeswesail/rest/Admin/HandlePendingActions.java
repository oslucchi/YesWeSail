package com.yeswesail.rest.Admin;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.Roles;

@Path("/pending")
public class HandlePendingActions {
	@Context
	private ServletContext context;

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	JsonHandler jh = new JsonHandler();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveAll(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		PendingActions[] actions = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving pending actions");
			conn = DBInterface.connect();
			actions = PendingActions.getActives(conn);
			log.trace("Retrieval completed");
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Reviews.search");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		if (jh.jasonize(actions, language) != Response.Status.OK)
		{
			log.error("Error '" + jh.json + "' jsonizing the actions object");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@PUT
	@Path("/{idPendingActions}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authorize(@PathParam("idPendingActions") int idPendingActions, 
							  @HeaderParam("Language") String language,
							  @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}
		
		PendingActions action = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving review to update. Id " + idPendingActions);
			conn = DBInterface.TransactionStart();
			action = new PendingActions(conn, idPendingActions);
			action.setStatus("C");
			action.setUpdated(new Date());
			DBInterface.TransactionCommit(conn);
			log.trace("Status changed");
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on Reviews.search");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		finally
		{
			DBInterface.TransactionRollback(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}
}
