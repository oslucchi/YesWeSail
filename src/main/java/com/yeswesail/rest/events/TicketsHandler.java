package com.yeswesail.rest.events;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.MessagingException;
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
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.Mailer;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventDescription;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsDescription;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.PendingActions;
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
			tickets = EventTickets.findByEventId(jsonIn.eventId, languageId);
		}
		catch (Exception e)
		{
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
					return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, null, languageId, "ticket.fareNotAvailable");
				}

				if (et.getTicketType() == EventTickets.ALL_BOAT) 
				{
					if (Utils.anyTicketAlreadySold(t.eventId))
					{
						DBInterface.TransactionRollback(conn);
						return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, null, languageId, "ticket.fareNotAvailable");
					}
					EventTickets[] tickets = EventTickets.findByEventId(t.eventId, languageId);
					for (EventTickets item : tickets)
					{
						if (item.getTicketType() == EventTickets.ALL_BOAT)
							continue;
						while(item.getBooked() != item.getAvailable())
						{
							item.bookATicket();
						}
						item.update(conn, "idEventTickets");
					}
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

	@POST
	@Path("/buyTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response buyTickets(TicketJson[][] jsonIn, 
							  @HeaderParam("Language") String language, 
							  @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		if (Utils.userSelfOrAdmin(token, jsonIn[0][0].usersId, languageId))
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		if (!Utils.userHasValidEmail(token))
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "users.profileEmailInvalid");
		}
		if (!Utils.userHasValidTaxcode(token))
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "users.taxcodeInvalid");
		}
		
		String listForApproval = "";
		String sep = "";
		String ticketsList = "<ul>";
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
					EventDescription ed = new EventDescription();
					ed.findEventTitleyId(conn, t.eventId, languageId);
					EventTickets et = new EventTickets(conn, t.idEventTickets);
					EventTicketsDescription etd = new EventTicketsDescription(conn, et.getTicketType(), languageId);
					ticketsList += "<li>" + ed.getDescription() + " - " + etd.getDescription() + "</li>";
					listForApproval += sep + t.idEventTickets;
					sep = ",";
				}
			}
			ticketsList += "</ul>";
			PendingActions pa = new PendingActions();
			pa.setActionType(PendingActions.TICKETS_BUY);
			pa.setCreated(new Date());
			pa.setLink("rest/tickets/confirm?tickets=" + listForApproval);
			pa.setStatus("P");
			pa.setUserId(jsonIn[0][0].usersId);
			pa.insert(conn, "idPendingActions", pa);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		try
		{
	        String htmlText = LanguageResources.getResource(languageId, "mail.ticketsUserOnBuy");
	        htmlText = htmlText.replaceAll("TICKETLIST", ticketsList);
	        String subject = LanguageResources.getResource(Constants.getLanguageCode(language), "mail.ticketsUserOnBuySubject");
			URL url = getClass().getResource("/images/mailLogo.png");
			String imagePath = url.getPath();
			Mailer.sendMail(jsonIn[0][0].userName, prop.getAdminEmail(), subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			log.warn("Couldn't send ticket confirmation message to the user. Exception " + e.getMessage());
		}
		return Response.status(Response.Status.OK).entity("").build();
	}

	@POST
	@Path("confirm")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response confirmTickets(@HeaderParam("Language") String language,
							     @QueryParam("tickets") String ticketsList,
							     @QueryParam("userId") int userId)
	{
		int languageId = Utils.setLanguageId(language);

		String[] tickets = ticketsList.split(",");
		String ticketsHTML = "<ul>";
		DBConnection conn = null;

		try
		{
			conn = DBInterface.connect();
			for(String eventTicketId : tickets)
			{
				EventTickets et = new EventTickets(conn, Integer.parseInt(eventTicketId));
				EventDescription ed = new EventDescription();
				ed.findEventTitleyId(conn, et.getEventId(), languageId);
				EventTicketsDescription etd = new EventTicketsDescription(conn, et.getTicketType(), languageId);
				ticketsHTML += "<li>" + ed.getDescription() + " - " + etd.getDescription() + "</li>";
			}
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		try
		{
	        String htmlText = LanguageResources.getResource(languageId, "mail.ticketsUserOnPaymentConfirmSubject");
	        htmlText = htmlText.replaceAll("TICKETLIST", ticketsHTML);
	        String subject = LanguageResources.getResource(Constants.getLanguageCode(language), "mail.ticketsUserOnPaymentConfirm");
			URL url = getClass().getResource("/images/mailLogo.png");
			String imagePath = url.getPath();			
			Mailer.sendMail(SessionData.getInstance().getBasicProfile(userId).getEmail(), subject, htmlText, imagePath);
		}
		catch(MessagingException e)
		{
			log.warn("Couldn't send ticket confirmation message to the user. Exception " + e.getMessage());
		}
		return Response.status(Response.Status.OK).entity("").build();
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
			tickets = EventTickets.findByEventId(eventId, languageId);
		}
		catch(Exception e)
		{
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
			tickets = EventTicketsSold.getTicketSold(eventId, languageId);
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
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		return Response.status(Response.Status.OK)
				.entity("{}").build();
	}
	
}
