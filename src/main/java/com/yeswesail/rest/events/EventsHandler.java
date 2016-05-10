package com.yeswesail.rest.events;


import java.io.File;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.Boats;
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
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		Events[] hot = null;
		try 
		{
			hot = Events.findHot(Constants.getLanguageCode(language));
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
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
		Events[] eventsFiltered = null;
		try 
		{
			eventsFiltered = Events.findByFilter(buildWhereCondition(jsonIn), 
												 Constants.getLanguageCode(language));
		}
		catch (Exception e) {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
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
	public Response eventDetails(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		Genson genson = new Genson();

		int languageId;
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		languageId = Constants.getLanguageCode(language);

		Events event = null;
		try
		{
			event = new Events(jsonIn.eventId, languageId);
		}
		catch (Exception e) 
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
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
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
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
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
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
			String path = context.getResource("/images/events").getPath();
			File directory = new File(path);
	        File[] fList = directory.listFiles();
	        ArrayList<String> imageURLs = new ArrayList<>();
	        for (File file : fList)
	        {
	            if (!file.isFile() || (!file.getName().startsWith("ev_" + event.getIdEvents() + "_")))
	            	continue;
	            
	            imageURLs.add(prop.getWebHost() + file.getPath().substring(file.getPath().indexOf("/images/")));
	        }
			jsonResponse.put("images", imageURLs);
		} 
		catch (MalformedURLException e1) 
		{
			log.warn("Unable to get images as resources for the required event (" + e1.getMessage() + ")");
		}

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

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventCreate(EventJson jsonIn, @HeaderParam("Language") String language, @HeaderParam("Authorization") String token)
	{
		int languageId;
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		languageId = Constants.getLanguageCode(language);

		Events event = null;
		try
		{
			DBInterface.TransactionStart();
			event = new Events();
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback();
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
		event.setCategoryId(jsonIn.categoryId);
		try 
		{
			event.setDateStart(sdf.parse(jsonIn.dateStart));
			event.setDateStart(sdf.parse(jsonIn.dateEnd));
		}
		catch (ParseException e) {
			DBInterface.TransactionRollback();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		
		event.setEventType(jsonIn.eventType);
		event.setLocation(jsonIn.location);
		event.setShipId(jsonIn.shipId);
		event.setShipownerId(SessionData.getInstance().getBasicProfile(token).getIdUsers());
		event.setEventRef(jsonIn.eventRef);
		event.setStatus("P");
		event.setEarlyBooking("N");
		event.setLastMinute("N");
		int eventId = -1;
		try
		{
			eventId = event.insertAndReturnId("idEvents", event);
			EventDescription ed = new EventDescription();
			ed.setEventId(eventId);
			ed.setLanguageId(languageId);
			ed.setAnchorZone(0);
			ed.setDescription(jsonIn.title);
			ed.insert("idEVentDescription", ed);

			ed.setAnchorZone(1);
			ed.setDescription(jsonIn.description);
			ed.insert("idEVentDescription", ed);

			ed.setAnchorZone(2);
			ed.setDescription(jsonIn.logistics);
			ed.insert("idEVentDescription", ed);

			ed.setAnchorZone(3);
			ed.setDescription(jsonIn.includes);
			ed.insert("idEVentDescription", ed);

			ed.setAnchorZone(4);
			ed.setDescription(jsonIn.excludes);
			ed.insert("idEVentDescription", ed);

			DBInterface.TransactionCommit();
		} 
		catch (Exception e) {
			DBInterface.TransactionRollback();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		
		return Response.status(Response.Status.OK).entity("").build();
	}
	
/*	
	@POST
	@Path("/uploadMultiImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadMultipleFile(EventJson jsonIn, 
			 @FormDataParam("image") InputStream imageBuf, @HeaderParam("Language") String language)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = null;
        MultipartStream multipartStream;
		try 
		{
			multipartStream = new MultipartStream(imageBuf, jsonIn.boundary.getBytes(), 1024, null);
		    boolean nextPart = multipartStream.skipPreamble();
		    boolean isFirst = true;
		    String imagePath = null;
		    int counter = 0;
		    while (nextPart) 
		    {
		        String header = multipartStream.readHeaders();
		        log.debug("Received header: '" + header + "'");
		        if (isFirst)
		        {
		        	imagePath = "/images/events/ev_" + counter++ + 
		        				"_main." + header.substring(header.lastIndexOf("."));
		        	isFirst = true;
		        }
		        else
		        {
		        	imagePath = "/images/events/ev_" + counter++ + "_" + 
		        				header.substring(header.lastIndexOf("."));
		        }
		        multipartStream.readBodyData(baos);
		        log.trace("Received body");
	        	os = new FileOutputStream(imagePath);
	            os.write(baos.toByteArray(), 0, baos.size());
	            os.close();
		        nextPart = multipartStream.readBoundary(); 
		    }
		} 
		catch (IOException e) 
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}
	
	@POST
	@Path("/uploadSingleImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadSingleFile(ImageUploadJson jsonIn, 
			 @FormDataParam("image") InputStream imageBuf, @QueryParam("imageName") String imageName,
			 @HeaderParam("Language") String language)
	{
		OutputStream os = null;
		String imagePath = null;
		try
		{
	        if (jsonIn.isMainPicture)
	        {
	        	imagePath = "/images/events/ev_" + jsonIn.referenceId + 
	        				"_main." + jsonIn.imageName.substring(jsonIn.imageName.lastIndexOf("."));
	        }
	        else
	        {
	        	imagePath = "/images/events/ev_" + jsonIn.referenceId + "_" + 
	        				jsonIn.imageName.substring(jsonIn.imageName.lastIndexOf("."));
	        }
	        os = new FileOutputStream(imagePath);
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while((bytesRead = imageBuf.read(buffer)) !=-1)
	        {
	            os.write(buffer, 0, bytesRead);
	        }
	        if (jsonIn.isMainPicture)
	        {
	        	Events ev = new Events(jsonIn.referenceId);
	    		ev.setImageURL(imagePath);
	    		ev.update("idEvents");
	        }
		}
		catch(Exception e)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
		}
		finally
		{
	        try 
	        {
				imageBuf.close();
		        os.flush();
		        os.close();
			} 
	        catch (IOException e) 
	        {
	        	;
			}
		}
		return Response.status(Response.Status.OK).entity("").build();
	}
*/
}
