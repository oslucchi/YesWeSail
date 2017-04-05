package com.yeswesail.rest.events;

import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.EventTicketsJson;
import com.yeswesail.rest.jsonInt.TicketJson;

@Path("/tickets")
public class TicketsHandler {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Users u = null;
	Events e = null;
	JsonHandler jh = new JsonHandler();

	private Response getTikectsFromDB(TicketJson jsonIn, String language)
	{
		int languageId = Utils.setLanguageId(language);

		EventTickets[] tickets = null;
		try 
		{
			tickets = EventTickets.getAllTicketByEventId(null, jsonIn.eventId, languageId);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}

		// No record found. return an empty object
		if (tickets == null)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}

		int ticketType = -1;
		int index = -1;
		ArrayList<ArrayList<EventTickets>> toReturn = new ArrayList<>();

		boolean wholeBoatAvailable = true;
		for (int i = 0; i < tickets.length; i++)
		{
			if (tickets[i].getAvailable() - tickets[i].getBooked() <= 0)
			{
				wholeBoatAvailable = false;
				continue;
			}
			if (tickets[i].getTicketType() != ticketType)
			{
				ticketType = tickets[i].getTicketType();
				index++;
				toReturn.add(new ArrayList<EventTickets>());
			}
			toReturn.get(index).add(tickets[i]);
		}
		if (!wholeBoatAvailable)
		{
			for(int i = 0; i < toReturn.size(); i++)
			{
				if (toReturn.get(i).get(0).getTicketType() == EventTickets.WHOLE_BOAT)
				{
					toReturn.remove(i);
					break;
				}
			}
		}

		if (jh.jasonize(toReturn, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}

		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@POST
	@Path("/eventTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventTickets(TicketJson jsonIn, @HeaderParam("Language") String language)
	{
		return getTikectsFromDB(jsonIn, language);
	}

	@POST
	@Path("/bookTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response bookTicket(TicketJson[] jsonIn, @HeaderParam("Language") String language, 
			@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		EventTickets et = null;
		DBConnection conn = null;
		Users u = SessionData.getInstance().getBasicProfile(token);

		try 
		{
			conn = DBInterface.TransactionStart();
			for (TicketJson t : jsonIn)
			{
				if (t.ticketType == EventTickets.WHOLE_BOAT) 
				{
					if (Utils.anyTicketAlreadySold(conn, t.eventId))
					{
						DBInterface.TransactionRollback(conn);
						return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, null, languageId, "ticket.fareNotAvailable");
					}
					EventTickets[] tickets = EventTickets.findByEventId(conn, t.eventId, languageId);
					for (EventTickets item : tickets)
					{
						while(item.getBooked() != item.getAvailable())
						{
							item.bookATicket();
						}
						item.update(conn, "idEventTickets");
					}
				}
				else
				{
					et = new EventTickets(conn, t.idEventTickets);
					if (et.getAvailable() - et.getBooked() <= 0)
					{
						DBInterface.TransactionRollback(conn);
						return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, null, languageId, "ticket.fareNotAvailable");
					}

					et.bookATicket();
					et.update(conn, "idEventTickets");
				}
				TicketLocks tl = new TicketLocks();
				tl.setEventTicketId(t.idEventTickets);
				tl.setLockTime(new Date());
				tl.setBookedTo((t.bookedTo != null ? t.bookedTo : 
					SessionData.getInstance().getBasicProfile(token).getEmail()));
				tl.setUserId(u.getIdUsers());
				tl.setStatus(Constants.STATUS_PENDING_APPROVAL);
				tl.insert(conn, "idTicketLocks", tl);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			if (e.getMessage().compareTo("No record found") == 0)
			{
				return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, e, languageId, "ticket.fareNotAvailable");
			}
			else
			{
				return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, e, languageId, "generic.execError");
			}
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		return(getTikectsFromDB(jsonIn[0], language));
	}

	public boolean freeTicket(int idEventTickets)
	{
		EventTickets et = null;
		TicketLocks tl = null;
		DBConnection conn = null;

		try 
		{
			conn = DBInterface.TransactionStart();
			tl = new TicketLocks(conn, idEventTickets);
			et = new EventTickets(conn, tl.getEventTicketId());
			if (et.getTicketType() == EventTickets.WHOLE_BOAT) 
			{
				EventTickets[] tickets = EventTickets.findByEventId(conn, et.getEventId(), 1);
				for (EventTickets item : tickets)
				{
					item.setBooked(0);
					item.update(conn, "idEventTickets");
				}
			}
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return false;
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return true;
	}
		
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventTickets(@HeaderParam("Language") String language,
			@QueryParam("eventId") int eventId)
	{
		int languageId = Utils.setLanguageId(language);

		DBConnection conn = null;

		EventTickets[] tickets = null;
		try
		{
			conn = DBInterface.connect();
			tickets = EventTickets.findByEventId(conn, eventId, languageId);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		Utils jsonizer = new Utils();

		jsonizer.addToJsonContainer("tickets", tickets, true);
		return Response.status(Response.Status.OK)
				.entity(jsonizer.jsonize())
				.build();
	}

	@GET
	@Path("/{eventId}/ticketsSold")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ticketsSold(@HeaderParam("Language") String language, 
			@HeaderParam("Authorization") String token,
			@PathParam("eventId") int eventId)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

		DBConnection conn = null;

		Utils jsonizer = new Utils();
		EventTicketsSold[] tickets = null;
		try
		{
			conn = DBInterface.connect();
			tickets = EventTicketsSold.getTicketSold(conn, eventId, languageId);
			EventTicketsJson[] participants = new EventTicketsJson[tickets.length];
			int idx = 0;
			for(EventTicketsSold item : tickets)
			{
				participants[idx] = new EventTicketsJson();
				participants[idx].description = item.getDescription();
				participants[idx].idEventTicketsSold = item.getIdEventTicketsSold();
				participants[idx].price = item.getPrice();
				participants[idx].user = new Users(conn, item.getUserId());
				idx++;
			}
			jsonizer.addToJsonContainer("tickets", participants, true);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK)
				.entity(jsonizer.jsonize())
				.build();
	}

	@DELETE
	@Path("/{idEventTicketsSold}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removePassenger(@PathParam("idEventTicketsSold") int idEventTicketsSold, 
			@HeaderParam("Language") String language, 
			@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

		DBConnection conn = null;
		EventTicketsSold ets = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			ets = new EventTicketsSold(conn, idEventTicketsSold);
			EventTickets et = new EventTickets(conn, ets.getEventTicketId());
			et.releaseATicket();
			et.update(conn, "idEventTickets");
			ets.delete(conn, idEventTicketsSold);
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		return Response.status(Response.Status.OK)
				.entity("{}").build();
	}

}
