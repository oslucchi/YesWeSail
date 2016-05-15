package com.yeswesail.rest.events;


import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

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
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.EventTicketsSold;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.EventDescriptionJson;
import com.yeswesail.rest.jsonInt.EventJson;
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
			hot = Events.findHot(Constants.getLanguageCode(language));
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		
		// No record found. return an empty object
		if (hot == null)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		if (jh.jasonize(hot, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
	
		return Response.status(Response.Status.OK).entity(jh.json).build();
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
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventList(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		Events[] eventsFiltered = null;
		try 
		{
			eventsFiltered = Events.findByFilter(buildWhereCondition(jsonIn), 
												 Constants.getLanguageCode(language));
		}
		catch (Exception e) {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}

		// No record found. return an empty object
		if (eventsFiltered == null)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		if (jh.jasonize(eventsFiltered, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
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
		try
		{
			if (editMode)
			{
				event = new Events(jsonIn.eventId, languageId, false);
			}
			else
			{
				event = new Events(jsonIn.eventId, languageId);
			}
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
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
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}

		Users[] participants = null;
		try
		{
			participants = EventTicketsSold.findParticipants(event.getIdEvents(), languageId);
			jsonResponse.put("participants", participants);
		}
		catch(Exception e)
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
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
		File directory = new File(contextPath);
        File[] fList = directory.listFiles();
        ArrayList<String> imageURLs = new ArrayList<>();
        for (File file : fList)
        {
            if (!file.isFile() || (!file.getName().startsWith("ev_" + event.getIdEvents() + "_")))
            	continue;
            
            imageURLs.add(prop.getWebHost() + file.getPath().substring(file.getPath().indexOf("/images/")));
        }
		jsonResponse.put("images", imageURLs);

		try
		{
			Users u = new Users(event.getShipownerId());
			jsonResponse.put("shipOwner", u);
		}
		catch (Exception e) {
			jsonResponse.put("shipOwner", "{}");
		}

		try
		{
			Boats b = new Boats(event.getShipId());
			jsonResponse.put("boat", b);
		}
		catch (Exception e) {
			jsonResponse.put("boat", "{}");
		}

		String entity = genson.serialize(jsonResponse);
		return Response.status(Response.Status.OK).entity(entity).build();
	}
	
	private Events handleInsertUpdate(EventJson jsonIn, int actionType, 
									  DBConnection conn, int userId) throws Exception // 0 Insert - 1 Update
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Events event = null;
		if (actionType == 1)
		{
			event = new Events(jsonIn.eventId);
		}
		else
		{
			event = new Events();
			event.setCreatedBy(userId);
			event.setCreatedOn(new Date());
		}
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
			event.setDateStart(sdf.parse(jsonIn.dateStart));
		}
		try
		{
			event.setDateEnd(new Date(Long.valueOf(jsonIn.dateEnd).longValue()));
		}
		catch(NumberFormatException e)
		{
			event.setDateEnd(sdf.parse(jsonIn.dateEnd));
		}
		event.setEventType(jsonIn.eventType);
		if (jsonIn.location == null)
			jsonIn.location = "TBD";
		event.setLocation(jsonIn.location);
		event.setShipId(jsonIn.shipId);
		event.setShipownerId(jsonIn.shipOwnerId);
		event.setStatus("P");
		event.setEarlyBooking("N");
		event.setLastMinute("N");
		if (jsonIn.imageURL == null)
		{
			event.setImageURL("http://www.placehold.it/1920x600?text=Here goes your event image");
		}
		else
		{
			event.setImageURL(jsonIn.imageURL);
		}
		int eventId = -1;
		if (actionType == 0)
		{
			eventId = event.insertAndReturnId(conn, "idEvents", event);
			event.setIdEvents(eventId);
		}
		else
		{
			event.update("idEvents");
		}
		return event;
	}

	private void handleInsertUpdateDetails(EventJson jsonIn, int languageId, int actionType, DBConnection conn) throws Exception // 0 Insert - 1 Update
	{
		jsonIn.eventDetails = new EventDescriptionJson[5];
		for(int i = 0; i < 5; i++)
		{
			jsonIn.eventDetails[i] = new EventDescriptionJson();
			jsonIn.eventDetails[i].eventId = jsonIn.eventId; 
			jsonIn.eventDetails[i].anchorZone = i; 
			jsonIn.eventDetails[i].languageId = languageId; 
		}
		jsonIn.eventDetails[0].description = jsonIn.title; 
		jsonIn.eventDetails[1].description = jsonIn.description; 
		jsonIn.eventDetails[2].description = jsonIn.logistics; 
		jsonIn.eventDetails[3].description = jsonIn.includes; 
		jsonIn.eventDetails[4].description = jsonIn.excludes; 

		if (actionType == 1)
		{
			EventDescription.deleteOnWhere("WHERE eventId = " + jsonIn.eventDetails[0].eventId + " AND " +
										   "      languageId = " + languageId);
		}

		EventDescription ed =  new EventDescription();
		for(EventDescriptionJson item : jsonIn.eventDetails)
		{
			if (item.description != null)
			{
				ed.setEventId(item.eventId);
				ed.setLanguageId(languageId);
				ed.setAnchorZone(item.anchorZone);
				ed.setDescription(item.description);
				ed.insert(conn, "idEventDescription", ed);
			}
		}
	}

	private Response eventHandler(EventJson jsonIn, String language, String token, int actionType)
	{
		int languageId = Utils.setLanguageId(language);
		DBConnection conn = null;
		Events event = null;
		try
		{
			conn = DBInterface.TransactionStart();
			event = handleInsertUpdate(jsonIn, actionType, conn, 
									   SessionData.getInstance().getBasicProfile(token).getIdUsers());
			jsonIn.eventId = event.getIdEvents();
			handleInsertUpdateDetails(jsonIn, languageId, actionType, conn);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
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
			handleInsertUpdateDetails(jsonIn, languageId, actionType, conn);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}
	
	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventCreate(EventJson jsonIn, 
								@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		return eventHandler(jsonIn, language, token, 0); // Create event
	}
	
	@PUT
	@Path("/{eventId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventUpdate(EventJson jsonIn, @PathParam("eventId") int eventId, 
								@HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		jsonIn.eventId = eventId;
		return eventHandler(jsonIn, language, token, 1); // Update event
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

	@POST
	@Path("/addTickets")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTickets(TicketJson[] jsonIn, @HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		EventTickets et = null;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			for(TicketJson t : jsonIn)
			{
				et = new EventTickets();
				et.setAvailable(t.quantity);
				et.setBooked(0);
				et.setCabinRef(t.cabinRef);
				et.setEventId(t.eventId);
				et.setPrice(t.price);
				et.setTicketType(t.ticketType);
				et.insert(conn, "eventTicketId", t);
			}
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}	
	
	
	@POST
	@Path("/updateTicket")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTickets(TicketJson jsonIn, @HeaderParam("Language") String language)
	{
		int languageId = Utils.setLanguageId(language);

		EventTickets et = null;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.TransactionStart();
			et = new EventTickets(jsonIn.eventTicketId);
			et.setAvailable(jsonIn.quantity);
			et.setBooked(jsonIn.sold);
			et.setCabinRef(jsonIn.cabinRef);
			et.setEventId(jsonIn.eventId);
			et.setPrice(jsonIn.price);
			et.setTicketType(jsonIn.ticketType);
			et.insert("eventTicketId", jsonIn);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		return Response.status(Response.Status.OK).entity("").build();
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
			new EventTickets().delete(jsonIn.eventTicketId);
			DBInterface.TransactionCommit(conn);
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(languageId, "generic.execError") + " (" + e.getMessage() + ")")
					.build();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}	

	@POST
    @Path("/uploadImages")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImages(@Context HttpServletRequest request, @HeaderParam("Language") String language)
    {
		int languageId = Utils.setLanguageId(language);
		boolean errorMoving = false;
		try 
		{
			contextPath = context.getResource("/").getPath();
		}
		catch (MalformedURLException e) 
		{
			contextPath = null;
			log.warn("Exception " + e.getMessage() + " retrieving context path");	
		}
		
		if (ServletFileUpload.isMultipartContent(request))
		{
			String token = UUID.randomUUID().toString();
			UploadFiles up = new UploadFiles();
			try 
			{
				up.uploadMultipartFiles(request, contextPath, "eventId", token);
				errorMoving = up.moveFiles(contextPath, token, "ev_");
			}
			catch (Exception e) 
			{
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(LanguageResources.getResource(languageId, "events.imageUploadError") + 
								" (" + e.getMessage() + ")")
						.build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(LanguageResources.getResource(languageId, "generic.uploadFileNoMultipart")).build();
		}
        return Response.status(Response.Status.OK)
        		.entity((errorMoving ? LanguageResources.getResource(languageId, "events.imageUploadError "): "{}"))
        		.build();
    }
}
