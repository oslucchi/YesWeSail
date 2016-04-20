package com.yeswesail.rest.events;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventDescription;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.EventDescriptionJson;
import com.yeswesail.rest.jsonInt.EventJson;
import com.yeswesail.rest.jsonInt.ImageUploadJson;
import com.yeswesail.rest.jsonInt.TicketJson;

@Path("/events")
public class EventsHandler {
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
	
	@POST
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventList(EventJson jsonIn, @HeaderParam("Language") String language)
	{
		Events[] eventsFiltered = null;
		String where = " WHERE ";
		String and = "";
		if (jsonIn.categoryId != 0)
		{
			where += " categoryId = " + jsonIn.categoryId;
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

		where += " ORDER BY dateStart ASC";
		
		try 
		{
			eventsFiltered = Events.findByFilter(where, Constants.getLanguageCode(language));
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

		if (jh.jasonize(event, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(jh.json).build();
		}

		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("event", jh.json);
		
		EventTickets[] tickets = null;
		try
		{
			tickets = EventTickets.findByEventId(event.getIdEvents(), languageId);
			jsonResponse.put("tickets", tickets);
		}
		catch(Exception e)
		{
			return Response.status(Response.Status.SERVICE_UNAVAILABLE)
					.entity(LanguageResources.getResource(
								Constants.getLanguageCode(language), "generic.execError") + " (" + 
								e.getMessage() + ")").build();
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
		event.setStatus("P");
		int eventId = -1;
		try
		{
			eventId = event.insertAndReturnId("idEvents", event);
			for (EventDescriptionJson anchor : jsonIn.eventDescription)
			{
				EventDescription ed = new EventDescription();
				ed.setEventId(eventId);
				ed.setLanguageId(languageId);
				ed.setAnchorZone(anchor.anchorZone);
				ed.setDescription(anchor.description);
				ed.insert("idEVentDescription", ed);
			}

			for (TicketJson ticket : jsonIn.tickets)
			{
				EventTickets et = new EventTickets();
				et.setEventId(eventId);
				et.setAvailable(ticket.onSale);
				et.setTicketType(ticket.ticketType);
				et.setPrice(ticket.price);
				et.setBooked(0);
				et.insert("idEventTickets", et);
			}
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
	
	@POST
	@Path("/uploadMultiImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadMultipleFile(EventJson jsonIn, 
			 @FormDataParam("image") InputStream imageBuf, @HeaderParam("Language") String language)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultipartStream multipartStream;
		try {
			multipartStream = new MultipartStream(imageBuf, jsonIn.boundary.getBytes(), 1024, null);
		    boolean nextPart = multipartStream.skipPreamble();
		    while (nextPart) {
		        String header = multipartStream.readHeaders();
		        log.debug("Received header: '" + header + "'");
		        multipartStream.readBodyData(baos);
		        // TODO Save file
		        nextPart = multipartStream.readBoundary(); 
		        log.trace("Received body");
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).entity("").build();
	}
	
	@POST
	@Path("/uploadSingleImage")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadMultipleFile(ImageUploadJson jsonIn, 
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
	        	imagePath = "/images/events/ev_" + jsonIn.referenceId + "_" + jsonIn.imageName;
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
			DBInterface.TransactionRollback();
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
}
