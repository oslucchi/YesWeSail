package com.yeswesail.rest.events;

import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.Users;
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
			tickets = EventTickets.findByEventId(jsonIn.eventId, languageId);
		}
		catch (Exception e)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		
		// No record found. return an empty object
		if (tickets == null)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		int ticketType = -1;
		int index = -1;
		ArrayList<ArrayList<EventTickets>> toReturn = new ArrayList<>();

		for (int i = 0; i < tickets.length; i++)
		{
			if (tickets[i].getTicketType() != ticketType)
			{
				ticketType = tickets[i].getTicketType();
				index++;
				toReturn.add(new ArrayList<EventTickets>());
			}
			toReturn.get(index).add(tickets[i]);
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

		String errMsg = null;
		EventTickets et = null;
		DBConnection conn = null;
		Users u = SessionData.getInstance().getBasicProfile(token);

		try 
		{
			conn = DBInterface.TransactionStart();
			for (TicketJson t : jsonIn)
			{
				et = new EventTickets(conn, t.idEventTickets);
				if (et.getAvailable() - et.getBooked() <= 0)
				{
					DBInterface.TransactionRollback(conn);
					return Response.status(Response.Status.NOT_ACCEPTABLE)
							.entity(LanguageResources.getResource(languageId, "ticket.fareNotAvailable"))
							.build();
				}
				et.bookATicket();
				et.update(conn, "idEventTickets");
				TicketLocks tl = new TicketLocks();
				tl.setEventTicketId(t.idEventTickets);
				tl.setLockTime(new Date());
				tl.setBookedTo((t.bookedTo != null ? t.bookedTo : 
								SessionData.getInstance().getBasicProfile(token).getEmail()));
				tl.setUserId(u.getIdUsers());
				tl.setStatus("P");
				tl.insert(conn, "idTicketLocks", tl);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			if (e.getMessage().compareTo("No record found") == 0)
			{
				errMsg = LanguageResources.getResource(
							Constants.getLanguageCode(language), "ticket.fareNotAvailable"); 
			}
			else
			{
				errMsg = LanguageResources.getResource(
							Constants.getLanguageCode(language), "generic.execError"); 
			}
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.entity(errMsg + " (" + e.getMessage() + ")").build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		return(getTikectsFromDB(jsonIn[0], language));
	}

	@POST
	@Path("/buyTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response buyTickets(TicketJson[][] jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		DBConnection conn = null;

		try 
		{
			conn = DBInterface.TransactionStart();
			TicketLocks tl = new TicketLocks();
			for(int i = 0; i < jsonIn.length; i++)
			{
				for(TicketJson t : jsonIn[i])
				{
					tl = new TicketLocks(conn, t.idEventTickets);
					tl.setStatus("S");
					tl.update(conn, "idTicketLocks");
				}
			}
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError"))
					.build();
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		return Response.status(Response.Status.OK).entity("").build();
	}
}
