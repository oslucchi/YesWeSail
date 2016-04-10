package com.yeswesail.rest.events;


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
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.EventJson;

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
			return Response.status(Response.Status.UNAUTHORIZED)
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
}
