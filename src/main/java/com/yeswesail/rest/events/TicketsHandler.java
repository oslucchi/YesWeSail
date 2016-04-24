package com.yeswesail.rest.events;

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

	@POST
	@Path("/eventTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventTickets(TicketJson jsonIn, @HeaderParam("Language") String language)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		int languageId = Constants.getLanguageCode(language);
		
		EventTickets[] tickets = null;
		try 
		{
			tickets = EventTickets.findByEventId(jsonIn.eventId, languageId);
		}
		catch (Exception e)
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		
		// No record found. return an empty object
		if (tickets == null)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		if (jh.jasonize(tickets, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@POST
	@Path("/bookTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response bookTicket(TicketJson jsonIn, @HeaderParam("Language") String language, 
							   @HeaderParam("Authorization") String token)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}

		String errMsg = null;
		EventTickets et = null;
		try 
		{
			DBInterface.TransactionStart();
			et = new EventTickets(jsonIn.eventTicketId);
			if (et.getAvailable() - et.getBooked() < jsonIn.quantity)
			{
				DBInterface.TransactionRollback();
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.entity(LanguageResources.getResource(
									Constants.getLanguageCode(language), "ticket.fareNotAvailable"))
						.build();
			}
			et.bookATicket();
			TicketLocks tl = new TicketLocks();
			tl.setEventTicketId(jsonIn.eventTicketId);
			tl.setLockTime(new Date());
			tl.setBookedTo((jsonIn.bookedTo != null ? jsonIn.bookedTo : 
							SessionData.getInstance().getBasicProfile(token).getEmail()));
			tl.insert("idTicketLocks", tl);
			DBInterface.TransactionCommit();
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback();
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
		
		return Response.status(Response.Status.OK).entity("").build();
	}

	@POST
	@Path("/buyTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response buyTicket(TicketJson jsonIn, @HeaderParam("Language") String language)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}

		try 
		{
			DBInterface.TransactionStart();
			TicketLocks tl = new TicketLocks();
			tl.delete(jsonIn.eventTicketId);
			DBInterface.TransactionCommit();
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(Constants.getLanguageCode(language), "generic.execError"))
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("").build();
	}
}
