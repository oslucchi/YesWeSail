package com.yeswesail.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.DBUtility.CategoriesLanguages;
import com.yeswesail.rest.DBUtility.EventTicketsDescription;
import com.yeswesail.rest.DBUtility.EventTypes;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.RolesLanguages;
import com.yeswesail.rest.jsonInt.MapsJson;


@Path("/maps")
public class Maps {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	JsonHandler jh = new JsonHandler();
	SimpleDateFormat sdf =new SimpleDateFormat("YYYY-MM-d");
	String[] mapNames = {
			"CATEGORIES",
			"ROLES",
			"EVENTTYPES",
			"TICKETTYPES",
			"LOCATIONS"
	};
	ArrayList<Object> categories = null;;
	ArrayList<Object> roles = null;;
	ArrayList<Object> eventTypes = null;;
	ArrayList<Object> eventTicketDescription = null;;
	ArrayList<Events> events = null;;

	private ArrayList<Object> getCategories(int languageId) throws Exception
	{
		if (categories == null)
		{
			CategoriesLanguages c = new CategoriesLanguages();
			categories = (ArrayList<Object>) c.getAll(languageId);
		}
		for(Object e : categories)
		{
			((CategoriesLanguages)e).setDescription(((CategoriesLanguages)e).getDescription().toUpperCase());
		}
		return categories;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Object> getRoles(int languageId) throws Exception
	{
		if (roles == null)
		{
			Roles r = new Roles();
			roles = (ArrayList<Object>) r.populateCollectionOnCondition(
							"WHERE languageId = " + languageId, RolesLanguages.class);
		}
		for(Object e : roles)
		{
			((RolesLanguages)e).setDescription(((RolesLanguages)e).getDescription().toUpperCase());
		}
		return roles;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Object> getEventTypes(int languageId) throws Exception
	{
		if (eventTypes == null)
		{
			EventTypes e = new EventTypes();
			eventTypes = (ArrayList<Object>) e.populateCollectionOnCondition(
					"WHERE languageId = " + languageId, EventTypes.class);
		}
		for(Object e : eventTypes)
		{
			((EventTypes)e).setDescription(((EventTypes)e).getDescription().toUpperCase());
		}
		return eventTypes;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Object> getTicketTypes(int languageId) throws Exception
	{
		if (eventTicketDescription == null)
		{
			EventTicketsDescription e = new EventTicketsDescription();
			eventTicketDescription = (ArrayList<Object>) e.populateCollectionOnCondition(
						"WHERE languageId = " + languageId, EventTicketsDescription.class);
		}
		for(Object e : eventTicketDescription)
		{
			((EventTicketsDescription)e).setDescription(((EventTicketsDescription)e).getDescription().toUpperCase());
		}
		return eventTicketDescription;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Events> getEvents() throws Exception
	{
		if (events == null)
		{
			Events e = new Events();
			events = (ArrayList<Events>) e.populateCollectionOfDistinctsOnCondition(
					"WHERE dateEnd > '" + sdf.format(new Date()) + "' AND " +
					"      location != 'TBD'", "location" , Events.class);
		}
		for(Events e : events)
		{
			e.setLocation(e.getLocation().toUpperCase());
		}
		return events;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@SuppressWarnings("unchecked")
	public Response getMaps(MapsJson jsonIn, @HeaderParam("Language") String language)
	{
		String json = null;
		ArrayList<Object>[] mapArray = new ArrayList[5];
		int languageId = Constants.getLanguageCode(language);
		try {
			switch(jsonIn.mapName.toUpperCase())
			{
			case "CATEGORIES":
				mapArray[0] = getCategories(languageId);
				break;
			
			case "ROLES":
				mapArray[0] = getRoles(languageId);
				break;
				
			case "EVENTTYPES":
				mapArray[0] = getEventTypes(languageId);
				break;
				
			case "TICKETTYPES":
				mapArray[0] = getTicketTypes(languageId);
				break;

			case "LOCATIONS":
				mapArray[0] = new ArrayList<Object>();
				for (Events e : getEvents())
				{
					mapArray[0].add(e.getLocation());
				}
				break;
			
			default:
				mapArray[0] = getCategories(languageId);
				mapArray[1] = getRoles(languageId);
				mapArray[2] = getEventTypes(languageId);
				mapArray[3] = getTicketTypes(languageId);
				mapArray[4] = new ArrayList<Object>();
				for (Events e : getEvents())
				{
					mapArray[4].add(e.getLocation());
				}
			}
		} 
		catch (Exception e) {
			log.error("Error jasonizing the map " + jsonIn.mapName + " (" + e.getMessage() + ")");
			json = LanguageResources.getResource(
					Constants.getLanguageCode(language), "generic.execError") + " (" + 
					e.getMessage() + ")";
			return Response.status(Response.Status.OK)
					.entity(json).build();
		}
		// No record found. return an empty object
		
		HashMap<String, Object> jsonResponse = new HashMap<>();
		for(int i = 0; i < 5 && mapArray[i] != null; i++)
		{
			jsonResponse.put(mapNames[i], mapArray[i]);
		}
		JsonHandler jh = new JsonHandler();
		if (jh.jasonize(jsonResponse, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
}
