package com.yeswesail.rest.events;


import java.io.File;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import com.google.common.io.Files;
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
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.EventDescriptionJson;
import com.yeswesail.rest.jsonInt.EventJson;
import com.yeswesail.rest.jsonInt.EventRouteJson;
import com.yeswesail.rest.jsonInt.TicketJson;

@Path("/events")
public class EventsHandler {
	@Context
	private ServletContext context;
	
	final String[] imageSizeType = {"-large", "-medium", "-small"};

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Users u = null;
	Events e = null;
	JsonHandler jh = new JsonHandler();
	String contextPath = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
/*
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
/*
		int languageId = Utils.setLanguageId(language);

		Events[] hot = null;
		try 
		{
			log.trace("Getting hot events via findHot method");
			hot = Events.findHot(Constants.getLanguageCode(language));
			log.trace("Retrieval completed");
		}
		catch(Exception e) 
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
		
		HashMap<Integer, Users> shipownerList = new HashMap<>();
		for(ArrayList<Events> listItem : hotList)
		{
			for(Events e : listItem)
			{
				try 
				{
					shipownerList.put(e.getShipOwnerId(), new Users(e.getShipOwnerId()));
				}
				catch(Exception e1)
				{
					log.debug("Excption " + e1.getMessage() + " retrieving data for shipowner " + 
							  e.getShipOwnerId());
				}
			}
		}
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("events", hotList);
		jsonResponse.put("shipowners", shipownerList);
		
		Genson genson = new Genson();
		String entity = genson.serialize(jsonResponse);


		return Response.status(Response.Status.OK).entity(entity).build();
	}
*/
	
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
		catch(Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Events.findHot with language " + language, e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		// No record found. return an empty object
		if (hot == null)
		{
			log.trace("No record found");
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		ArrayList<ArrayList<Events>> hotList = organizeEvents(hot);
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("events", hotList);
		jsonResponse.put("shipowners", getShipownerList(hotList));
		Genson genson = new Genson();
		String entity = genson.serialize(jsonResponse);

		return Response.status(Response.Status.OK).entity(entity).build();
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
			if ((prop.getMaxNumHotOffers() != 0) &&
				(hotList.size() >= prop.getMaxNumHotOffers()))
			{
				break;
			}
			e.setImageURL(e.getImageURL().replace("large", "medium"));
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
			if (i == hotList.size())
			{
				ArrayList<Events> item = new ArrayList<>();
				item.add(e);
				hotList.add(item);
			}
		}
		ArrayList<ArrayList<Events>> sorted = new ArrayList<>();
		for(ArrayList<Events> listItem : hotList)
		{
			Collections.sort(listItem, new Comparator<Events>() {
		        @Override 
		        public int compare(Events e1, Events e2) {
		            if (e1.getDateStart().getTime() >  e2.getDateStart().getTime())
		            {
		            	return 1;
		            }
		            else if (e1.getDateStart().getTime() ==  e2.getDateStart().getTime())
		            {
		            	return 0;
		            }
		            else
		            {
		            	return -1;
		            }
		        }
		    });
			sorted.add(listItem);
		}
		return sorted;
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
			where += and + "a.status = '" + Constants.STATUS_ACTIVE + "' ";
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
			jsonIn.activeOnly = activeOnly;
			log.trace("Search for " + (activeOnly ? " active " : " all ") + " events");
			events = Events.findByFilter(buildWhereCondition(jsonIn), languageId);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
		Object list = null;
		ArrayList<ArrayList<Events>> eventsList = null;
		if (activeOnly)
		{
			log.trace("Look for active only. Organizing events");
			eventsList = organizeEvents(eventsFiltered);
		}
		else
		{
			log.trace("All events requested. Simulating through an ArrayList of 1 element only");
			eventsList = new ArrayList<>();
			eventsList.add(eventsFiltered);
		}

		if (!activeOnly)
		{
			list = eventsList.get(0);
		}
		else
		{
			list = eventsList;
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("events", list);
		jsonResponse.put("shipowners", getShipownerList(eventsList));

		Genson genson = new Genson();
		String entity = genson.serialize(jsonResponse);

		return Response.status(Response.Status.OK).entity(entity).build();
	}

	@SuppressWarnings("unchecked")
	private HashMap<Integer, Users> getShipownerList(ArrayList<ArrayList<Events>> eventsList) 
	{
		String soIdList = "";
		String sep = "";
		HashMap<Integer, Users> shipownerList = new HashMap<>();
		log.debug("the eventsList contains " + eventsList.size() + " elements");
		for(ArrayList<Events> listItem : eventsList)
		{
			if (listItem.size() > 0)
			{
				log.debug("Current list has " + listItem.size() + " events. The first item is on event " + 
						listItem.get(0).getIdEvents());
			}
			else
			{
				log.debug("current item contains no elements");
			}
			for(Events e : listItem)
			{
				e.setImageURL(e.getImageURL().replace("large", "medium"));
				if (!shipownerList.containsKey(e.getShipOwnerId()))
				{
					shipownerList.put(e.getShipOwnerId(), null);
					soIdList += sep + e.getShipOwnerId();
					sep = ",";
				}
			}
		}
		ArrayList<Users> soList = null;
		if (soIdList.compareTo("") != 0)
		{
			soIdList = " (" + soIdList + ") ";
			try 
			{
				soList = (ArrayList<Users>) Users.populateCollection(
						"SELECT * FROM Users " +
								"WHERE idUsers IN " + soIdList, Users.class);
				for(Users u : soList)
				{
					if (shipownerList.containsKey(u.getIdUsers()))
					{
						shipownerList.put(u.getIdUsers(), u.wipe());
					}
				}
			}
			catch(Exception e1)
			{
				log.debug("Exception " + e1.getMessage() + " retrieving data for shipowner " + 
						e.getShipOwnerId(), e1);
			}
		}
		return(shipownerList);
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
		if ((sd.getBasicProfile(token) == null) ||
			(sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR))
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
		jsonIn.status = Constants.STATUS_ACTIVE;
		return handleSearch(jsonIn, languageId, true);
	}
	
	@POST
	@Path("/clone")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventClone(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		// Check dates. return error in case of null.
		if ((jsonIn.dateStart == null) || (jsonIn.dateEnd == null))
		{
			return Utils.jsonizeResponse(Response.Status.BAD_REQUEST, null, 
										 languageId, "events.clone.badDates");
		}

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
			ev.setCreatedOn(new Date());
			int idEvents = ev.insertAndReturnId(conn, "idEvents", ev);
			ev.setIdEvents(idEvents);
			ev.setEarlyBooking(false);
			ev.setHotEvent(false);
			ev.setLastMinute(false);
			ev.setStatus(Constants.STATUS_PENDING_APPROVAL);
			ev.setImageURL(ev.getImageURL().replace(new Integer(ev.getIdEvents()).toString(), 
													new Integer(idEvents).toString()));
			ev.update(conn, "idEvents");
			
			String sql = 
					"INSERT INTO EventDescription " +
				    "  SELECT 0, languageId, " + idEvents + ", anchorZone, description" +
					"  FROM EventDescription " +
				    "  WHERE eventId = " + jsonIn.idEvents;
			EventDescription.executeStatement(conn, sql, true);

			sql = 
					"INSERT INTO EventTickets " +
				    "  SELECT 0, " + idEvents + ", ticketType, available, " +
				    "         0, price, cabinRef, '' " + 
					"  FROM EventTickets " +
				    "  WHERE eventId = " + jsonIn.idEvents;
			EventTickets.executeStatement(conn, sql, true);
			
			sql = 
					"INSERT INTO EventRoute " +
				    "  SELECT 0, " + idEvents + ", lat, lng, description, seq " +
					"  FROM EventRoute " +
				    "  WHERE eventId = " + jsonIn.idEvents;
			EventRoute.executeStatement(conn, sql, true);
			
			DBInterface.TransactionCommit(conn);
			log.debug("Copying image files");

			String replace = "ev_"+ jsonIn.idEvents + "_";
			ArrayList<String> imagesList= UploadFiles.getExistingFilesPathOnLocalFilesystem(replace, "/images/events");
			for(String source : imagesList)
			{
				File from = new File(source);
				File to = new File(source.substring(0, source.indexOf(replace)) +
								   "ev_" + idEvents + "_" + 
								   source.substring(source.indexOf(replace) + replace.length()));
				Files.copy(from, to);
			}
			log.debug("...done");
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
	@Path("/images/default")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeEventImageURL(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		Events event = null;
		DBConnection conn = null;
		try 
		{
			conn = DBInterface.connect();
			event = new Events(conn, jsonIn.eventId);
			jsonIn.imageURL = jsonIn.imageURL.replace("small", "large");
			event.setImageURL(jsonIn.imageURL);
			event.update(conn, "idEvents");
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

		return Response.status(Response.Status.OK).entity("{}").build();
	}
	
	private double distance(double lat1, double lon1, double lat2, double lon2, String unit) 
	{
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + 
					  Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") 
		{
			dist = dist * 1.609344;
		}
		else if (unit == "N")
		{
			dist = dist * 0.8684;
		}

		return (dist);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
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
			// Make sure the imageURL is set to large regardless of what is the DB
			event.setImageURL(event.getImageURL()
									.replace("small", "large")
									.replace("medium", "large"));
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("event", event);
		
		EventTickets[] allTickets = null;
		int ticketsAvailable = 0;
		try
		{
			allTickets = EventTickets.getAllTicketByEventId(event.getIdEvents(), languageId);
			ArrayList<ArrayList<EventTickets>> tickets = new ArrayList<>();
			int ticketType = -1;
			int index = -1;
			for (int i = 0; i < allTickets.length; i++)
			{
				if ((allTickets[i].getTicketType() == EventTickets.WHOLE_BOAT) && Utils.anyTicketAlreadySold(event.getIdEvents()))
				{
					continue;
				}
				if (allTickets[i].getTicketType() != ticketType)
				{
					ticketType = allTickets[i].getTicketType();
					for(;index < ticketType - 1; index++)
					{
						tickets.add(new ArrayList<EventTickets>());
					}
				}
				if (allTickets[i].getAvailable() - allTickets[i].getBooked() > 0)
				{
					ticketsAvailable += allTickets[i].getAvailable() - allTickets[i].getBooked();
					tickets.get(index).add(allTickets[i]);
				}
			}
			jsonResponse.put("tickets", tickets);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.disconnect(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}

		Users[] participants = null;
		try
		{
			participants = EventTicketsSold.findParticipants(event.getIdEvents());
			if (ticketsAvailable == 0)
			{
				jsonResponse.put("participantMessage", 
								 LanguageResources.getResource(languageId, "events.nospotsleft"));
			}
			else if (ticketsAvailable <= 3)
			{
				jsonResponse.put("participantMessage", 
						 LanguageResources.getResource(languageId, "events.fewspotsleft"));
			}
			else if (participants.length == 0)
			{
				jsonResponse.put("participantMessage", 
						 LanguageResources.getResource(languageId, "events.noparticipants"));
			}
			jsonResponse.put("participants", participants);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
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
			log.warn("Unable to populate descriptions (" + e.getMessage() + ")", e);
		}

		ArrayList<ArrayList<String>> imagesTemp = UploadFiles.getExistingFilesPathAsURL("ev_"+ event.getIdEvents() + "_", "/images/events");

		ArrayList<String> images = imagesTemp.get(UploadFiles.ORIGINAL);
		ArrayList<String> imagesSmall = imagesTemp.get(UploadFiles.SMALL);
		ArrayList<String> imagesMedium = imagesTemp.get(UploadFiles.MEDIUM);
		ArrayList<String> imagesLarge = imagesTemp.get(UploadFiles.LARGE);

		if (images.isEmpty())
		{
			images.add(event.getImageURL());
		}
		else
		{
			jsonResponse.put("images", images);
		}

		try
		{
			log.debug("Trying to get the shipowner details for id " + event.getShipOwnerId());
			Users u = new Users(conn, event.getShipOwnerId());
			jsonResponse.put("shipOwner", u);
		}
		catch(Exception e) {
			log.warn("Unable to get details, exception (" + e.getMessage() + ")", e);
			jsonResponse.put("shipOwner", "{}");
		}

		EventRoute[] r = null;
		try
		{
			log.debug("Trying to get the route details for event " + event.getIdEvents());
			r = EventRoute.getRoute(conn, event.getIdEvents());
			jsonResponse.put("route", r);
		}
		catch(Exception e) {
			log.warn("Unable to get route details, exception (" + e.getMessage() + ")", e);
			jsonResponse.put("route", "{}");
		}

		try
		{
			log.debug("querying the boat details for this event");
			Boats b = new Boats(conn, event.getBoatId());
			imagesTemp = UploadFiles.getExistingFilesPathAsURL(
								"bo_" + b.getOwnerId() + "_" + b.getIdBoats() + "_img_", "/images/boats");
			images.addAll(imagesTemp.get(UploadFiles.ORIGINAL));
			imagesSmall.addAll(imagesTemp.get(UploadFiles.SMALL));
			imagesMedium.addAll(imagesTemp.get(UploadFiles.MEDIUM));
			imagesLarge.addAll(imagesTemp.get(UploadFiles.LARGE));

			imagesTemp = UploadFiles.getExistingFilesPathAsURL(
								"bo_" + b.getOwnerId() + "_" + b.getIdBoats() + "_bp_", "/images/boats");
			images.addAll(imagesTemp.get(UploadFiles.ORIGINAL));
			imagesSmall.addAll(imagesTemp.get(UploadFiles.SMALL));
			imagesMedium.addAll(imagesTemp.get(UploadFiles.MEDIUM));
			imagesLarge.addAll(imagesTemp.get(UploadFiles.LARGE));
			
			b.setImages(images);
			imagesTemp = UploadFiles.getExistingFilesPathAsURL(
					"bo_" + b.getOwnerId() + "_" + b.getIdBoats() + "_doc_", "/images/boats");
			ArrayList<String> docs = new ArrayList<>();
			docs.addAll(imagesTemp.get(UploadFiles.ORIGINAL));
			docs.addAll(imagesTemp.get(UploadFiles.SMALL));
			docs.addAll(imagesTemp.get(UploadFiles.MEDIUM));
			docs.addAll(imagesTemp.get(UploadFiles.LARGE));
			b.setDocs(docs);

			jsonResponse.put("imagesSmall", imagesSmall);
			jsonResponse.put("imagesMedium", imagesMedium);
			jsonResponse.put("imagesLarge", imagesLarge);
			jsonResponse.put("boat", b);
		}
		catch(Exception e) {
			log.warn("Unable to get the required boat, exception (" + e.getMessage() + ")", e);
			jsonResponse.put("boat", "{}");
		}
		
		Events[] groundEventsAvailable = null;
		try {
			groundEventsAvailable = Events.findByFilter("WHERE eventType = 2", languageId);
		} 
		catch(Exception e) {
			log.warn("Exception " + e.getMessage() + " populating ground events for event " + event.getIdEvents(), e);
		}
		ArrayList<Events> groundEvents = new ArrayList<Events>();
		if(groundEventsAvailable != null)
		{
			for(Events ev : groundEventsAvailable)
			{
				EventRoute start[] = null;
				try {
					start = EventRoute.getRoute(conn, ev.getIdEvents());
					EventRoute er = null;
					for (int i = 0; i < r.length; i++) 
					{
						er = r[i];
						double dist = distance(new Double(er.getLat()), new Double(er.getLng()), 
											   new Double(start[0].getLat()), new Double(start[0].getLng()), "K");
						if (dist < prop.getMaxDistanceForEventsOnTheGround())
						{
							groundEvents.add(ev);
							break;
						}
					}
				} 
				catch(Exception e) {
					log.warn("Exception " + e.getMessage() + " getting routes for ground event " + ev.getIdEvents(), e);
				}
			}
		}
		jsonResponse.put("groundEvents", groundEvents);
			
		DBInterface.disconnect(conn);
		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity).build();
	}

	private Events fillEvent(EventJson jsonIn, Events event, int languageId)
	{
		event.setCategoryId(jsonIn.categoryId);
		if (jsonIn.dateStart == null) 
		{
			jsonIn.dateStart = "1970-01-01";
		}
		if (jsonIn.dateEnd == null)
		{
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
			catch(ParseException e1)
			{
				jsonIn.dateStart = "1970-01-01";
				log.warn("Exception parsing jsonIn.dateStart = " + jsonIn.dateStart + ". Date set to epoch", e);
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
			catch(ParseException e1)
			{
				jsonIn.dateEnd = "1970-01-01";
				log.warn("Exception parsing jsonIn.dateEnd = " + jsonIn.dateEnd + ". Date set to epoch", e1);
			}
		}
		event.setEventType(jsonIn.eventType);
		if (jsonIn.location == null)
			jsonIn.location = "TBD";
		event.setLocation(jsonIn.location);
		event.setBoatId((jsonIn.boatId == 0 ? 1 : jsonIn.boatId));
		event.setShipOwnerId(jsonIn.shipOwnerId);
		if (jsonIn.status == null)
		{
			event.setStatus(Constants.STATUS_PENDING_APPROVAL);
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
		event.setBackgroundOffsetX(jsonIn.backgroundOffsetX);
		event.setBackgroundOffsetY(jsonIn.backgroundOffsetY);
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
								   "      languageId = " + languageId + " AND " +
								   "      anchorZone != 0");
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
		}

		for(int zone = 0; zone < eventDetails.length; zone++)
		{
			if ((eventDetails[zone] == null) || (eventDetails[zone].description == null))
				continue;

			if ((ed = EventDescription.findByEventId(conn, jsonIn.eventId, 
													 zone, languageId)) != null)
			{
				ed.setDescription(eventDetails[zone].description);
				ed.update(conn, "idEventDescription");
			}
			else
			{
				ed = new EventDescription();
				ed.setEventId(jsonIn.idEvents);
				ed.setLanguageId(languageId);
				ed.setAnchorZone(zone + 1);
				ed.setDescription(eventDetails[zone].description);
				ed.insert(conn, "idEventDescription", ed);
			}
		}
	}

	private void handleInsertUpdateRoute(EventJson jsonIn, int languageId, DBConnection conn) throws Exception // 0 Insert - 1 Update
	{
		EventRoute er =  new EventRoute();

		ArrayList<EventRoute> retainPoints = new ArrayList<>();
		EventRoute erTemp = null;
		for(EventRouteJson item : jsonIn.route)
		{
			if (item == null)
				continue;
			erTemp = EventRoute.findByRouteCoordinates(conn, jsonIn.eventId, item.lat, item.lng);
			if (erTemp != null)
			{
				retainPoints.add(erTemp);
			}
		}
		
		String sql = "WHERE eventId = " + jsonIn.eventId;
		String sep = "";
		if (retainPoints.size() > 0)
		{
			sql += " AND " +
				   "      seq NOT IN (";
			for(EventRoute item : retainPoints)
			{
				sql += sep + item.getSeq();
				sep = ", ";
			}
			sql += ")";
		}

		try
		{
			er.deleteOnWhere(conn, sql);
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage(), e);
		}

		for(EventRouteJson item : jsonIn.route)
		{
			if (item == null)
				continue;
			erTemp = EventRoute.findByRouteCoordinates(conn, jsonIn.eventId, item.lat, item.lng);
			if (erTemp != null)
			{
				erTemp.setDescription(item.description);
				erTemp.setSeq(item.seq);
				erTemp.update(conn, "idEventRoute");
			}
			else
			{
				er = new EventRoute();
				er.setDescription(item.description);
				er.setEventId(jsonIn.idEvents);
				er.setLat(item.lat);
				er.setLng(item.lng);
				er.setSeq(item.seq);
				er.insert(conn, "idEventRoute", er);
			}
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
			@SuppressWarnings("unchecked")
			ArrayList<EventRoute> erList = 
					(ArrayList<EventRoute>) EventRoute.populateCollection(
													"SELECT * FROM EventRoute " +
													"WHERE eventId = " + jsonIn.eventId, 
													EventRoute.class);
			if (erList.size() == 0)
			{
				if ((jsonIn.route == null) || (jsonIn.route.length == 0))
				{
					DBInterface.disconnect(conn);
					return Utils.jsonizeResponse(Response.Status.BAD_REQUEST, null, languageId, "events.missingRoutes");
				}
				else
				{
					handleInsertUpdateRoute(jsonIn, languageId, conn);
				}
			}
			else 
			{
				if (jsonIn.route != null)
				{
					handleInsertUpdateRoute(jsonIn, languageId, conn);
				}
			}

		
			if ((jsonIn.description != null) || (jsonIn.logistics != null) ||
				(jsonIn.includes != null) || (jsonIn.excludes != null))
			{
				handleInsertUpdateDetails(jsonIn, languageId, conn);
			}
			if (jsonIn.tickets != null)
			{
				handleInsertTicket(jsonIn.tickets, jsonIn.idEvents, conn);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
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
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.SERVICE_UNAVAILABLE, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
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
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		DBConnection conn = null;
		Events ev = null;
		

		try
		{
			if ((EventTicketsSold.getTicketSold(idEvents, languageId).length > 0) ||
				(TicketLocks.findByEventId(idEvents).length > 0))
			{
				log.warn("Tickets for this event have been sold already. can't delete");
				DBInterface.TransactionRollback(conn);
				return Utils.jsonizeResponse(Response.Status.NOT_ACCEPTABLE, null, 
											 languageId, "events.cannotDelete.ticketsSold");
			}

			conn = DBInterface.TransactionStart();
			EventDescription ed = new EventDescription();
			log.trace("Deleting EventDescriptions");
			ed.delete(conn, "DELETE FROM EventDescription WHERE eventId = " + idEvents);
			EventRoute er = new EventRoute();
			log.trace("Deleting EventRoutes");
			er.delete(conn, "DELETE FROM EventRoute WHERE eventId = " + idEvents);
			EventTickets et = new EventTickets();
			log.trace("Deleting EventTickets");
			et.delete(conn, "DELETE FROM EventTickets WHERE eventId = " + idEvents);
			ev = new Events(conn, idEvents);
			log.trace("Deleting Event");
			ev.delete(conn, idEvents);
			DBInterface.TransactionCommit(conn);
			log.trace("Deleting Images");

			String delete = "ev_"+ idEvents + "_";
			ArrayList<ArrayList<String>> imagesList= UploadFiles.getExistingFilesPathAsURL(delete, "/images/events");
			for(ArrayList<String> sourceList : imagesList)
			{
				for(String source : sourceList)
				{
					File toDelete = new File(source);
					toDelete.delete();
				}
			}
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage() + " raised", e);
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
				u.setStatus(Constants.STATUS_ACTIVE);
				u.setIsShipOwner(false);
				u.setImageURL("images/users/default-icon.png");
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
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
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

	private void handleInsertTicket(TicketJson[][] jsonIn, int eventId, DBConnection conn) throws Exception
	{
		EventTickets et = null;
		EventTickets[] eventTickets = EventTickets.findByEventId(eventId);
		for(int y = 0; y < jsonIn.length; y++)
		{
			if (jsonIn[y] == null)
				continue;
			for(TicketJson t : jsonIn[y])
			{
				if (t == null)
					continue;
				
				et = null;
				for(int i = 0; i < eventTickets.length; i++)
				{
					if (eventTickets[i].getIdEventTickets() == t.idEventTickets)
					{
						et = eventTickets[i];
						break;
					}
				}
				if (et == null)
				{
					et = new EventTickets();
					et.setAvailable(t.available);
					et.setBooked(0);
					et.setCabinRef(t.cabinRef);
					et.setEventId(t.eventId);
					et.setTicketType(t.ticketType);
					et.setPrice(t.price);
					et.insert(conn, "idEventTickets", et);
				}
				else
				{
					et.setPrice(t.price);
					et.update(conn, "idEventTickets");
				}
			}
		}
	}
	
	@POST
	@Path("/addTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTickets(TicketJson[][] jsonIn, 
							   @HeaderParam("Language") String language, 
							   @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			handleInsertTicket(jsonIn, jsonIn[0][0].eventId, conn);
			DBInterface.TransactionCommit(conn);
		}
		catch 
		(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
	@Path("/updateTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTicket(TicketJson jsonIn, 
								  @HeaderParam("Language") String language,
								  @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

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
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
	public Response deleteTickets(TicketJson jsonIn, 
								  @HeaderParam("Language") String language,
								  @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			new EventTickets().delete(conn, jsonIn.idEventTickets);
			DBInterface.TransactionCommit(conn);
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
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
		catch(MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving images/events path", e);	
		}
		String baseName = imageName.substring(0, imageName.indexOf("-"));
//		File toRemove = new File(eventsPath + File.separator + imageName);
//		toRemove.delete();
		File toRemove = null;
		for(String size : imageSizeType)
		{
			toRemove = new File(eventsPath + File.separator + baseName + size + ".jpg");
			toRemove.delete();
		}
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
		
		Utils jsonizer = new Utils();

		ArrayList<ArrayList<String>> images = UploadFiles.getExistingFilesPathAsURL(prefix, "/images/events");

		jsonizer.addToJsonContainer("images", images.get(UploadFiles.ORIGINAL), true);
		jsonizer.addToJsonContainer("imagesSmall", images.get(UploadFiles.SMALL), false);
		jsonizer.addToJsonContainer("imagesMedium", images.get(UploadFiles.MEDIUM), false);
		jsonizer.addToJsonContainer("imagesLarge", images.get(UploadFiles.LARGE), false);
		
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
	
	@PUT
    @Path("/{eventId}/makeHot")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response makeHot(EventJson jsonIn,
    						@HeaderParam("Authorization") String token,
						    @HeaderParam("Language") String language,
						    @PathParam("eventId") int eventId)
    {
		log.debug("Setting event " + eventId + " hot flag");
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		DBConnection conn = null;
		Events event = null;
		try
		{
			log.debug("value from json " + jsonIn.hotEvent);
			conn = DBInterface.TransactionStart();
			event = new Events(conn, eventId);
			event.setHotEvent(jsonIn.hotEvent);
			event.update(conn, "idEvents");
			DBInterface.TransactionCommit(conn);
			log.debug("done");
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}

		Utils jsonizer = new Utils();
		jsonizer.addToJsonContainer("event", event, true);
		return Response.status(Response.Status.OK).entity(jsonizer.jsonize()).build();
    }

	@PUT
    @Path("/{eventId}/makeEarlyBooking")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response makeEarlyBooking(EventJson jsonIn,
    						@HeaderParam("Authorization") String token,
						    @HeaderParam("Language") String language,
						    @PathParam("eventId") int eventId)
    {
		log.debug("Setting event " + eventId + " earlyBooking flag");
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		DBConnection conn = null;
		Events event = null;
		try
		{
			log.debug("value from json " + jsonIn.earlyBooking);
			conn = DBInterface.TransactionStart();
			event = new Events(conn, eventId);
			event.setEarlyBooking(jsonIn.earlyBooking);
			event.update(conn, "idEvents");
			DBInterface.TransactionCommit(conn);
			log.debug("done");
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		Utils jsonizer = new Utils();
		jsonizer.addToJsonContainer("event", event, true);
		return Response.status(Response.Status.OK).entity(jsonizer.jsonize()).build();
    }


	@PUT
    @Path("/{eventId}/makeLastMinute")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response makeLastMinute(EventJson jsonIn,
    						@HeaderParam("Authorization") String token,
						    @HeaderParam("Language") String language,
						    @PathParam("eventId") int eventId)
    {
		log.debug("Setting event " + eventId + " lastMinute flag");
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		DBConnection conn = null;
		Events event = null;
		try
		{
			log.debug("value from json " + jsonIn.lastMinute);
			conn = DBInterface.TransactionStart();
			event = new Events(conn, eventId);
			event.setLastMinute(jsonIn.lastMinute);
			event.update(conn, "idEvents");
			DBInterface.TransactionCommit(conn);
			log.debug("done");
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		Utils jsonizer = new Utils();
		jsonizer.addToJsonContainer("event", event, true);
		return Response.status(Response.Status.OK).entity(jsonizer.jsonize()).build();
    }

	@PUT
    @Path("/{eventId}/changeStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response changeStatus(EventJson jsonIn,
    						@HeaderParam("Authorization") String token,
						    @HeaderParam("Language") String language,
						    @PathParam("eventId") int eventId)
    {
		log.debug("Setting event " + eventId + " status flag");
		int languageId = Utils.setLanguageId(language);
		if (!Utils.userIsAdmin(token, languageId))
		{
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		
		DBConnection conn = null;
		Events event = null;
		try
		{
			log.debug("value from json " + jsonIn.status);
			conn = DBInterface.TransactionStart();
			event = new Events(conn, eventId);
			event.setStatus(jsonIn.status);
			event.update(conn, "idEvents");
			DBInterface.TransactionCommit(conn);
			log.debug("done");
		}
		catch(Exception e) 
		{
			log.warn("Exception " + e.getMessage(), e);
			DBInterface.TransactionRollback(conn);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		Utils jsonizer = new Utils();
		jsonizer.addToJsonContainer("event", event, true);
		return Response.status(Response.Status.OK).entity(jsonizer.jsonize()).build();
    }

}
