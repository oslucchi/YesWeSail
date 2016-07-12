package com.yeswesail.rest.events;


import java.io.File;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.json.JSONArray;
import org.json.JSONObject;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.UploadFiles;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.Boats;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventDescription;
import com.yeswesail.rest.DBUtility.EventRoute;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.EventDescriptionJson;
import com.yeswesail.rest.jsonInt.EventJson;
import com.yeswesail.rest.jsonInt.EventRouteJson;
import com.yeswesail.rest.jsonInt.TicketJson;

@Path("/events")
public class EventsHandler {
	@Context
	private ServletContext context;

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Users u = null;
	Events e = null;
	JsonHandler jh = new JsonHandler();
	String contextPath = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@POST
	@Path("/hotEvents")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response hotEvents(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		/*
		 * TODO 
		 * define search criteria. 
		 * For now next 4 expiring with no preference criteria.
		 * It might be smart to present the one with only few tickets remaining. it just need to change the query
		 */
		int languageId = Utils.setLanguageId(language);

		Events[] hot = null;
		try 
		{
			log.trace("Getting hot events via findHot method");
			hot = Events.findHot(Constants.getLanguageCode(language));
			log.trace("Retrieval completed");
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Events.findHot with language " + language);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		// No record found. return an empty object
		if (hot == null)
		{
			log.trace("No record found");
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		ArrayList<ArrayList<Events>> hotList = organizeEvents(hot);
		
		if (jh.jasonize(hotList, language) != Response.Status.OK)
		{
			log.error("Error '" + jh.json + "' jsonizing the hot event object");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	private ArrayList<ArrayList<Events>> organizeEvents(ArrayList<Events> events) 
	{
		return organizeEvents(events.toArray(new Events[events.size()]));
	}
	
	private ArrayList<ArrayList<Events>> organizeEvents(Events[] events) 
	{
		ArrayList<ArrayList<Events>> hotList = new ArrayList<>();
		int i;
		for(Events e : events)
		{
			for(i = 0; i < hotList.size(); i++)
			{
				if ((hotList.get(i) != null) && (e.getAggregateKey() != null) && 
					(hotList.get(i).get(0).getAggregateKey() != null) &&
					(hotList.get(i).get(0).getAggregateKey().compareTo(e.getAggregateKey()) == 0))
				{
					hotList.get(i).add(e);
					break;
				}
			}
			if ((hotList.size() < prop.getMaxNumHotOffers()) && (i == hotList.size()))
			{
				ArrayList<Events> item = new ArrayList<>();
				item.add(e);
				hotList.add(item);
			}
		}
		return hotList;
	}

	private String buildWhereCondition(EventJson jsonIn)
	{
		String where = " WHERE ";
		String and = "";
				
		if (jsonIn.eventType != 0)
		{
			where += and + " eventType = " + jsonIn.eventType;
			and = " AND ";
		}
		if (jsonIn.location != null)
		{
			where += and + " location = '" + jsonIn.location + "'";
			and = " AND ";
		}
		if (jsonIn.categoryId != 0)
		{
			where += and + " categoryId = " + jsonIn.categoryId;
			and = " AND ";
		}
		if (jsonIn.dateStart != null)
		{
			where += and + " dateStart > '" + jsonIn.dateStart + "'";
			and = " AND ";
		}
		if (jsonIn.dateEnd != null)
		{
			where += and + " dateEnd < '" + jsonIn.dateEnd + "'";
			and = " AND ";
		}
		if (jsonIn.activeOnly)
		{
			where += and + "a.status = 'A' ";
			and = " AND ";
		}
		if (jsonIn.labels != null)
		{
			String or = "";
			where += and + "(";
			for(String label : jsonIn.labels)
			{
				where += or + " labels LIKE '%" + label + "%'";
				and = " OR ";
			}
		}
		if (where.trim().compareTo("WHERE") == 0)
		{
			where = "";
		}
		where += " ORDER BY dateStart ASC";
		return(where);
	}

	private Response handleSearch(EventJson jsonIn, int languageId, boolean activeOnly)
	{
		Events[] events= null;
		try 
		{
			events = Events.findByFilter(buildWhereCondition(jsonIn), languageId);
		}
		catch (Exception e) {
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}
		if (events == null)
		{
			return Response.status(Response.Status.NO_CONTENT)
					.entity("{}")
					.build();
		}
		
		ArrayList<Events> eventsFiltered = new ArrayList<>();
		if (activeOnly)
		{
			for(Events ev : events)
			{
				if (ev.getMinPrice() != 0)
					eventsFiltered.add(ev);
			}
		}
		else
		{
			eventsFiltered = new ArrayList<Events>(Arrays.asList(events));
		}
		// No record found. return an empty object
		if (eventsFiltered.size() == 0)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}

		if (activeOnly)
		{
			ArrayList<ArrayList<Events>> eventsList = organizeEvents(eventsFiltered);
			if (jh.jasonize(eventsList, languageId) != Response.Status.OK)
			{
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(jh.json).build();
			}
		}
		else
		{
			if (jh.jasonize(eventsFiltered, languageId) != Response.Status.OK)
			{
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(jh.json).build();
			}

		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@POST
	@Path("/search/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchAll(EventJson jsonIn, 
							  @HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		return handleSearch(jsonIn, languageId, false);
	}

	@POST
	@Path("/search/actives")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchActives(EventJson jsonIn, 
								  @HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		return handleSearch(jsonIn, languageId, true);
	}
	
	@POST
	@Path("/clone")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventClone(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		Events ev = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			ev = new Events(conn, jsonIn.idEvents);
			
			if ((jsonIn.aggregateKey != null) && (jsonIn.aggregateKey.toLowerCase().compareTo("true") == 0))
			{
				if (ev.getAggregateKey() != null)
				{
					jsonIn.aggregateKey = ev.getAggregateKey();
				}
				else
				{
					jsonIn.aggregateKey = UUID.randomUUID().toString();
					ev.setAggregateKey(jsonIn.aggregateKey);
					ev.update(conn, "idEvents");
				}
			}
			ev = fillEvent(jsonIn, ev, languageId);
			int idEvents = ev.insertAndReturnId(conn, "idEvents", ev);
			ev.setIdEvents(idEvents);
			String sql = 
					"INSERT INTO EventDescription " +
				    "  SELECT 0, languageId, " + idEvents + ", anchorZone, description" +
					"  FROM EventDescription " +
				    "  WHERE eventId = " + jsonIn.idEvents;
			EventDescription.executeStatement(conn, sql, true);

			sql = 
					"INSERT INTO EventTickets " +
				    "  SELECT 0, " + idEvents + ", ticketType, available, " +
				    "         booked, price, cabinRef, bookedTo " + 
					"  FROM EventEventTickets " +
				    "  WHERE eventId = " + jsonIn.idEvents;
			EventTickets.executeStatement(conn, sql, true);
			
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) {
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}	
		finally
		{
			DBInterface.disconnect(conn);
		}

		if (jh.jasonize(ev, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@POST
	@Path("/details")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventDetails(EventJson jsonIn, 
								 @HeaderParam("Language") String language, @HeaderParam("Edit-Mode") boolean editMode)
	{
		Genson genson = new Genson();
		int languageId = Utils.setLanguageId(language);

		Events event = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			if (editMode)
			{
				event = new Events(conn, jsonIn.eventId, languageId, false);
			}
			else
			{
				event = new Events(conn, jsonIn.eventId, languageId);
			}
		}
		catch (Exception e) 
		{
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("event", event);
		
		EventTickets[] allTickets = null;
		try
		{
			allTickets = EventTickets.findByEventId(event.getIdEvents(), languageId);
			ArrayList<ArrayList<EventTickets>> tickets = new ArrayList<>();
			int ticketType = -1;
			int index = -1;
			for (int i = 0; i < allTickets.length; i++)
			{
				if ((allTickets[i].getTicketType() == EventTickets.ALL_BOAT) && Utils.anyTicketAlreadySold(event.getIdEvents()))
				{
					continue;
				}
				if (allTickets[i].getTicketType() != ticketType)
				{
					ticketType = allTickets[i].getTicketType();
					index++;
					tickets.add(new ArrayList<EventTickets>());
				}
				tickets.get(index).add(allTickets[i]);
			}
			jsonResponse.put("tickets", tickets);
		}
		catch(Exception e)
		{
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}

		Users[] participants = null;
		try
		{
			participants = EventTicketsSold.findParticipants(event.getIdEvents(), languageId);
			jsonResponse.put("participants", participants);
		}
		catch(Exception e)
		{
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}
		
		try {
			EventDescription[] ed = EventDescription.findByEventId(event.getIdEvents(), languageId);
			for(int i = 0; i < ed.length; i++)
			{
				switch(ed[i].getAnchorZone())
				{
				case 1:
					jsonResponse.put("description", ed[i]);
					break;
				case 2:
					jsonResponse.put("logistics", ed[i]);
					break;
				case 3:
					jsonResponse.put("includes", ed[i]);
					break;
				case 4:
					jsonResponse.put("excludes", ed[i]);
					break;
				}
			}
		}
		catch(Exception e)
		{
			log.warn("Unable to populate descriptions (" + e.getMessage() + ")");
		}

		try {
			contextPath = context.getResource("/images/events").getPath();
		}
		catch (MalformedURLException e) 
		{
			contextPath = null;
			log.warn("Exception " + e.getMessage() + " retrieving context path");	
		}
		jsonResponse.put("images", 
						 UploadFiles.getExistingFilesPath("ev_"+ event.getIdEvents() + "_",contextPath));

		try
		{
			Users u = new Users(conn, event.getShipOwnerId());
			jsonResponse.put("shipOwner", u);
		}
		catch (Exception e) {
			jsonResponse.put("shipOwner", "{}");
		}

		try
		{
			EventRoute[] r = EventRoute.getRoute(conn, event.getIdEvents());
			jsonResponse.put("route", r);
		}
		catch (Exception e) {
			jsonResponse.put("route", "{}");
		}

		try
		{
			Boats b = new Boats(conn, event.getShipId());
			jsonResponse.put("boat", b);
		}
		catch (Exception e) {
			jsonResponse.put("boat", "{}");
		}

		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	private Events fillEvent(EventJson jsonIn, Events event, int languageId)
	{
		event.setCategoryId(jsonIn.categoryId);
		if (jsonIn.dateStart == null)
		{
			jsonIn.dateStart = "1970-01-01";
			jsonIn.dateEnd = "1970-01-01";
		}
		try
		{
			event.setDateStart(new Date(Long.valueOf(jsonIn.dateStart).longValue()));
		}
		catch(NumberFormatException e)
		{
			try 
			{
				event.setDateStart(sdf.parse(jsonIn.dateStart));
			}
			catch (ParseException e1)
			{
				jsonIn.dateStart = "1970-01-01";
				log.warn("Exception parsing jsonIn.dateStart = " + jsonIn.dateStart + ". Date set to epoch");
			}
		}
		try
		{
			event.setDateEnd(new Date(Long.valueOf(jsonIn.dateEnd).longValue()));
		}
		catch(NumberFormatException e)
		{
			try 
			{
				event.setDateEnd(sdf.parse(jsonIn.dateEnd));
			}
			catch (ParseException e1)
			{
				jsonIn.dateEnd = "1970-01-01";
				log.warn("Exception parsing jsonIn.dateEnd = " + jsonIn.dateEnd + ". Date set to epoch");
			}
		}
		event.setEventType(jsonIn.eventType);
		if (jsonIn.location == null)
			jsonIn.location = "TBD";
		event.setLocation(jsonIn.location);
		event.setShipId(jsonIn.shipId);
		event.setShipOwnerId(jsonIn.shipOwnerId);
		if (jsonIn.status == null)
		{
			event.setStatus("P");
		}
		else
		{
			event.setStatus(jsonIn.status);
		}
		event.setEarlyBooking(jsonIn.earlyBooking);
		event.setLastMinute(jsonIn.lastMinute);
		event.setHotEvent(jsonIn.hotEvent);
		if (jsonIn.imageURL == null)
		{
			event.setImageURL(LanguageResources.getResource(languageId, "events.placeholder.url"));
		}
		else
		{
			event.setImageURL(jsonIn.imageURL);
		}
		return event;
	}

	private Events handleInsertUpdate(EventJson jsonIn, DBConnection conn, 
									  int userId, int languageId) throws Exception // 0 Insert - 1 Update
	{
		Events event = null;
		int actionType = 0;
		try
		{
			event = new Events(conn, jsonIn.eventId);
			actionType = 1;
		}
		catch(Exception e)
		{
			if (e.getMessage().compareTo("No record found") == 0)
			{
				event = new Events();
				event.setCreatedBy(userId);
				event.setCreatedOn(new Date());
				actionType = 0;
			}
			else
			{
				throw e;
			}
		}

		int eventId = -1;
		event = fillEvent(jsonIn, event, languageId);
		if (actionType == 0)
		{
			eventId = event.insertAndReturnId(conn, "idEvents", event);
			event.setIdEvents(eventId);
		}
		else
		{
			event.update(conn, "idEvents");
			eventId = event.getIdEvents();
		}
		
		EventDescription eventDetails = new EventDescription();
		try
		{
			eventDetails.findEventTitleyId(conn, eventId, languageId);
			eventDetails.setDescription(jsonIn.title);
			eventDetails.update(conn, "idEventDescription");
		}
		catch(Exception e)
		{
			if (e.getMessage().compareTo("No record found") == 0)
			{
				eventDetails.setAnchorZone(0);
				eventDetails.setEventId(eventId);
				eventDetails.setLanguageId(languageId);
				eventDetails.setDescription(jsonIn.title);
				eventDetails.insert(conn, "idEventDescription", eventDetails);
			}
			else
			{
				throw e;
			}
		}
		return event;
	}

	private void handleInsertUpdateDetails(EventJson jsonIn, int languageId, DBConnection conn) throws Exception // 0 Insert - 1 Update
	{
		EventDescriptionJson[] eventDetails = new EventDescriptionJson[4];
		eventDetails[0] = jsonIn.description; 
		eventDetails[1] = jsonIn.logistics; 
		eventDetails[2] = jsonIn.includes; 
		eventDetails[3] = jsonIn.excludes; 

		EventDescription ed =  new EventDescription();
		try
		{
			ed.deleteOnWhere(conn, "WHERE eventId = " + jsonIn.eventId + " AND " +
										   "      languageId = " + languageId);
		}
		catch(Exception e)
		{
			;
		}

		int zone = 1;
		for(EventDescriptionJson item : eventDetails)
		{
			if (item == null)
				continue;
			if (item.description != null)
			{
				ed.setEventId(jsonIn.idEvents);
				ed.setLanguageId(languageId);
				ed.setAnchorZone(zone++);
				ed.setDescription(item.description);
				ed.insert(conn, "idEventDescription", ed);
			}
		}
	}

	private void handleInsertUpdateRoute(EventJson jsonIn, int languageId, DBConnection conn) throws Exception // 0 Insert - 1 Update
	{
		EventRoute er =  new EventRoute();
		try
		{
			er.deleteOnWhere(conn, "WHERE eventId = " + jsonIn.eventId);
		}
		catch(Exception e)
		{
			;
		}

		int seq = 0;
		for(EventRouteJson item : jsonIn.route)
		{
			if (item == null)
				continue;
			er.setDescription(item.description);
			er.setEventId(jsonIn.idEvents);
			er.setLat(item.lat);
			er.setLng(item.lng);
			er.setSeq(seq++);
			er.insert(conn, "idEventRoute", er);
		}

	}

	private Response eventHandler(EventJson jsonIn, String language, String token)
	{
		int languageId = Utils.setLanguageId(language);
		DBConnection conn = null;
		Events event = null;
		try
		{
			conn = DBInterface.TransactionStart();
			event = handleInsertUpdate(jsonIn, conn, 
									   SessionData.getInstance().getBasicProfile(token).getIdUsers(), languageId);
			jsonIn.idEvents = jsonIn.eventId = event.getIdEvents();
			
			if ((jsonIn.route != null) && (jsonIn.route.length != 0))
			{
				handleInsertUpdateRoute(jsonIn, languageId, conn);
			}

			if ((jsonIn.description != null) || (jsonIn.logistics != null) ||
				(jsonIn.includes != null) || (jsonIn.excludes != null))
			{
				handleInsertUpdateDetails(jsonIn, languageId, conn);
			}
			if (jsonIn.tickets != null)
			{
				handleInsertTicket(jsonIn.tickets, conn);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}

		if (jh.jasonize(event, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}

		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
	
	private Response eventDetailsHandler(EventJson jsonIn, String language, int actionType)
	{
		int languageId = Utils.setLanguageId(language);
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			handleInsertUpdateDetails(jsonIn, languageId, conn);
			handleInsertUpdateRoute(jsonIn, languageId, conn);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@DELETE
	@Path("/delete/{idEvents}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventDelete(@PathParam("idEvents") int idEvents, 
								@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		DBConnection conn = null;
		Events ev = null;
		EventDescription ed = null;
		try
		{
			conn = DBInterface.TransactionStart();
			ed = new EventDescription();
			ed.delete(conn, "DELETE FROM EventDescription WHERE eventId = " + idEvents);
			ev = new Events(conn, idEvents);
			ev.delete(conn, idEvents);
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
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@POST
	@Path("/passengers")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addPassenger(TicketJson jsonIn, @HeaderParam("Language") String language, 
							   @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

		DBConnection conn = null;
		EventTickets et = null;
		EventTicketsSold ets = null;
		try 
		{
			conn = DBInterface.TransactionStart();
			et = new EventTickets(conn, jsonIn.idEventTickets);
			et.bookATicket();
			et.update(conn, "idEventTickets");
			if (jsonIn.usersId == 0)
			{
				Users u = new Users();
				u.setName(jsonIn.userName);
				u.setSurname(jsonIn.userSurname);
				u.setEmail(jsonIn.userEmail);
				u.setRoleId(Roles.DUMMY);
				u.setConnectedVia("X");
				u.setStatus("A");
				u.setIsShipOwner(false);
				try
				{
					jsonIn.usersId = u.insertAndReturnId(conn, "idUsers", u);
					// TODO 
					// Send a mail upon completion to the users
					// it should be a change password link with some statement on "Check your vacations details"
				}
				catch(Exception e)
				{
					if (e.getMessage().toLowerCase().startsWith("duplicate entry"))
					{
						u.findByEmail(conn, jsonIn.userEmail);
						jsonIn.usersId = u.getIdUsers();
					}
				}
			}
			ets = new EventTicketsSold();
			ets.setEventTicketId(jsonIn.idEventTickets);
			ets.setTimestamp(new Date());
			ets.setUserId(jsonIn.usersId);
			ets.setTransactionId("Direct Shipowner Sell");
			ets.insert(conn, "idEventTicketsSold", ets);
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
		
	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventCreate(EventJson jsonIn, 
								@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userSelfOrAdmin(token, jsonIn.shipOwnerId, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		return eventHandler(jsonIn, language, token); // Create event
	}
	
	@PUT
	@Path("/{eventId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventUpdate(EventJson jsonIn, @PathParam("eventId") int eventId, 
								@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		jsonIn.eventId = eventId;
		return eventHandler(jsonIn, language, token); // Update event
	}
	
	@POST
	@Path("/addDescriptionLanguage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventAddLanguage(EventJson jsonIn, 
									 @HeaderParam("Language") String language)
	{
		return eventDetailsHandler(jsonIn, language, 0);
	}

	
	@POST
	@Path("/updateDescriptionLanguage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventUpdateLanguage(EventJson jsonIn, 
										@HeaderParam("Language") String language)
	{
		return eventDetailsHandler(jsonIn, language, 1);
	}

	private void handleInsertTicket(TicketJson[] jsonIn, DBConnection conn) throws Exception
	{
		EventTickets et = null;
		for(TicketJson t : jsonIn)
		{
			et = new EventTickets();
			et.setAvailable(t.available);
			et.setBooked(0);
			et.setCabinRef(t.cabinRef);
			et.setEventId(t.eventId);
			et.setPrice(t.price);
			et.setTicketType(t.ticketType);
			et.insert(conn, "eventTicketId", t);
		}
	}
	
	@POST
	@Path("/addTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTickets(TicketJson[] jsonIn, @HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		DBConnection conn = null;
		try//					savedFile.write(item.data);
//		savedFile.close();

		{
			conn = DBInterface.TransactionStart();
			handleInsertTicket(jsonIn, conn);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.TransactionRollback(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();

	}	
	
	
	@POST
	@Path("/updateTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTicket(TicketJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		EventTickets et = null;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			et = new EventTickets(conn, jsonIn.idEventTickets);
			et.setAvailable(jsonIn.available);
			et.setBooked(jsonIn.booked);
			et.setCabinRef(jsonIn.cabinRef);
			et.setEventId(jsonIn.eventId);
			et.setPrice(jsonIn.price);
			et.setTicketType(jsonIn.ticketType);
			et.update(conn, "idEventTickets");
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
		return Response.status(Response.Status.OK).entity("{}").build();
	}	

	
	@POST
	@Path("/deleteTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteTickets(TicketJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			new EventTickets().delete(conn, jsonIn.idEventTickets);
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
		return Response.status(Response.Status.OK).entity("{}").build();
	}	

	@DELETE
    @Path("/{eventId}/{imageName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response deleteImage(@PathParam("imageName") String imageName)
	{
		String eventsPath = null;
		try {
			eventsPath = context.getResource("/images/events").getPath();
		}
		catch (MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving images/events path");	
		}
		File toRemove = new File(eventsPath + File.separator + imageName);
		toRemove.delete();
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@POST
    @Path("/{eventId}/upload")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(FormDataMultiPart form,
						   @HeaderParam("Authorization") String token,
						   @HeaderParam("Language") String language,
						   @PathParam("eventId") int eventId)
    {
		int languageId = Utils.setLanguageId(language);
		List<BodyPart> parts = form.getBodyParts();
		
		String[] acceptableTypes = {
				"image/png",
				"image/jpeg",
				"image/jpg"
		};

		String prefix = "ev_" + eventId + "_";

		Response response = UploadFiles.uploadFromRestRequest(
								parts, token, "/images/events", 
								prefix, acceptableTypes, languageId, false);
		
		try {
			contextPath = context.getResource("/images/events").getPath();
		}
		catch (MalformedURLException e) 
		{
			contextPath = null;
			log.warn("Exception " + e.getMessage() + " retrieving context path");	
		}
		Utils jsonizer = new Utils();

		jsonizer.addToJsonContainer("images", 
								 UploadFiles.getExistingFilesPath(prefix, contextPath), true);
		
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
		String jsonResponse = jsonizer.jsonize();
		return Response.status(status).entity(jsonResponse).build();
    }
}
