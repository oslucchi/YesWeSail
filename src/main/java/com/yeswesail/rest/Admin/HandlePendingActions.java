package com.yeswesail.rest.Admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.UploadFiles;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.Reviews;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.Users;

@Path("/requests")
public class HandlePendingActions {

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	JsonHandler jh = new JsonHandler();
	Utils utils = new Utils();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveAll(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

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
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.unauthorized");
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

	@GET 
	@Path("/user/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveAll(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token,
								@PathParam("userId") int userId)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if ((sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR) &&
			(sd.getBasicProfile(token).getIdUsers() != userId))
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		PendingActions[] actions = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving pending actions");
			conn = DBInterface.connect();
			actions = PendingActions.getPendingOnUser(conn, userId);
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

	@GET 
	@Path("/reviews/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getReview(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token,
								@PathParam("id") int id)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		Reviews review = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving pending actions");
			conn = DBInterface.connect();
			review = new Reviews(conn, id, false);
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
		utils.addToJsonContainer("review", review, true);
		String json = utils.jsonize();
		return Response.status(Response.Status.OK).entity(json).build();
	}

	@GET 
	@Path("/ticketLost/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getTicketLost(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token,
								@PathParam("id") int id)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		TicketLocks tl = null;
		EventTickets et = null;
		Events ev = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving pending actions");
			conn = DBInterface.connect();
			tl = new TicketLocks(conn, id);
			et = new EventTickets(conn, tl.getEventTicketId());
			ev = new Events(conn, et.getEventId());
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
		utils.addToJsonContainer("ticketLock", tl, true);
		utils.addToJsonContainer("ticketDetails", et, false);
		utils.addToJsonContainer("event", ev, false);
		String json = utils.jsonize();
		return Response.status(Response.Status.OK).entity(json).build();
	}

	@PUT 
	@Path("/ticketLost/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeTicketLock(@HeaderParam("Language") String language,
									@HeaderParam("Authorization") String token,
									@PathParam("id") int id)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		EventTickets[] ticketsToRelease;

		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
		}
		catch(Exception e)
		{
			utils.addToJsonContainer("error", 
					LanguageResources.getResource(languageId, "ticket.cannotDeleteLocks") +
					" (exception '" + e.getMessage() + "')", true);
			String json = utils.jsonize();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json).build();
		}

		try
		{
			TicketLocks tl = new TicketLocks(conn, id);
	
			EventTickets et = new EventTickets(conn, tl.getEventTicketId());
			if (et.getTicketType() == EventTickets.WHOLE_BOAT)
			{
				log.debug("WHOLE_BOAT ticket required to be released");
				ticketsToRelease = EventTickets.getAllTicketByEventId(et.getEventId(), 1);
			}
			else
			{
				ticketsToRelease = new EventTickets[1];
				ticketsToRelease[0] = et;
			}
	
			for(EventTickets item : ticketsToRelease)
			{
				try
				{
					TicketLocks ticket = TicketLocks.findByEventTicketId(conn, item.getIdEventTickets());
					log.debug("Deleting lock id " + ticket.getIdTicketLocks());
					ticket.delete(conn, ticket.getIdTicketLocks());
				}
				catch(Exception e)
				{
					if (e.getMessage().compareTo("No record found") != 0)
					{
						log.warn("Exception " + e.getMessage() + " retrieving and deleting ticket lock for eventTicket " +
								 item.getIdEventTickets());
						DBInterface.TransactionRollback(conn);
						DBInterface.disconnect(conn);
						utils.addToJsonContainer("error", 
								LanguageResources.getResource(languageId, "ticket.cannotDeleteLocks") +
								" (exception '" + e.getMessage() + "')", true);
						String json = utils.jsonize();
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json).build();
					}
				}
				if (item.getBooked() > 0)
				{
					log.debug("Updating booked status for event ticket " + item.getIdEventTickets());
					item.setBooked(item.getBooked() - 1);
					item.update(conn, "idEventTickets");
				}
			}
			PendingActions pa = new PendingActions();
			// TODO mark the action as 'C'ompleted once the id will be received
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e1)
		{
			DBInterface.TransactionRollback(conn);
			utils.addToJsonContainer("error", 
					LanguageResources.getResource(languageId, "ticket.cannotDeleteLocks") +
					" (exception '" + e1.getMessage() + "')", true);
			String json = utils.jsonize();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json).build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@PUT 
	@Path("/reviews/{id}/{idPendingActions}/{command}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response actOnReview(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token,
								@PathParam("id") int id,
								@PathParam("idPendingActions") int idPendingActions,
								@PathParam("command") String command)
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
		Reviews review = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			review = new Reviews(conn, id, false);
			log.trace("changing review stauts");
			if (command.compareTo("approve") == 0)
			{
				review.setStatus("A");
			}
			else 
			{
				review.setStatus("R");
			}
			review.setUpdated(new Date());
			review.update(conn, "idReviews");
			log.trace("changed");
			
			action = new PendingActions(conn, idPendingActions);
			action.setStatus("C");
			action.update(conn, "idPendingActions");
			log.trace("marked request as complete");
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on status upgrade action");
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
	@Path("/statusUpgrade/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getStatusUpgrade(@HeaderParam("Language") String language,
									 @HeaderParam("Authorization") String token,
									 @PathParam("id") int id)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		Users u = null;
		DBConnection conn = null;
		ArrayList<String> docs = new ArrayList<>();
		try 
		{
			log.trace("Retrieving pending actions");
			conn = DBInterface.connect();
			u = new Users(conn, id);
			docs = UploadFiles.getExistingFilesPathAsURL("docs_" + u.getIdUsers() + "_", "/images/shipowner").get(UploadFiles.LARGE);
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
		
		utils.addToJsonContainer("user", u, true);
		utils.addToJsonContainer("docs", docs, false);
		return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
	}
	

	@PUT 
	@Path("/statusUpgrade/{id}/{idPendingActions}/{command}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response actOnStatusUpgrade(@HeaderParam("Language") String language,
									   @HeaderParam("Authorization") String token,
									   @PathParam("id") int id,
									   @PathParam("idPendingActions") int idPendingActions,
									   @PathParam("command") String command)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(LanguageResources.getResource(languageId, "generic.unauthorized"))
					.build();
		}

		Users u = null;
		DBConnection conn = null;
		PendingActions action = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			if (command.compareTo("approve") == 0)
			{
				u = new Users(conn, id);
				log.trace("changing user's role");
				u.setRoleId(Roles.SHIP_OWNER);
				u.setIsShipOwner(true);
				log.trace("changed");
				u.update(conn, "idUsers");
				if (sd.getBasicProfile(u.getIdUsers()) != null)
				{
					sd.getBasicProfile(u.getIdUsers()).setRoleId(Roles.ADMINISTRATOR);
				}
			}

			action = new PendingActions(conn, idPendingActions);
			action.setStatus("C");
			action.update(conn, "idPendingActions");
			log.trace("marked request as complete");
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on status upgrade action");
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
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
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
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.unauthorized");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}
}
