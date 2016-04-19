package com.yeswesail.rest;

import java.text.SimpleDateFormat;
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

import com.yeswesail.rest.DBUtility.CategoriesLanguages;
import com.yeswesail.rest.DBUtility.EventTicketsDescription;
import com.yeswesail.rest.DBUtility.EventTypes;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.jsonInt.MapsJson;


@Path("/maps")
public class Maps {
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	JsonHandler jh = new JsonHandler();

	@SuppressWarnings("unchecked")
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMaps(MapsJson jsonIn, @HeaderParam("Language") String language)
	{
		String json = null;
		
		ArrayList<Object> map = new ArrayList<>();
		try {
			switch(jsonIn.mapName.toUpperCase())
			{
			case "CATEGORIES":
				map = (ArrayList<Object>) 
					CategoriesLanguages.populateCollection("SELECT * " +
														   "FROM CategoriesLanguages " +
														   "WHERE languageId = " + 
														   		Constants.getLanguageCode(language), CategoriesLanguages.class);
				break;
			
			case "ROLES":
				map = (ArrayList<Object>) 
					Roles.populateCollection("SELECT * " +
											 "FROM RolesLanguages " +
											 "WHERE languageId = " + Constants.getLanguageCode(language), Roles.class);
					break;
				
			case "EVENTTYPES":
				map = (ArrayList<Object>) 
					EventTypes.populateCollection("SELECT * " +
											 	  "FROM EventTypes " +
											 	  "WHERE languageId = " + Constants.getLanguageCode(language), EventTypes.class);
				break;
				
			case "TICKETTYPES":
				map = (ArrayList<Object>) 
						EventTicketsDescription.populateCollection("SELECT * " +
											 	  "FROM EventTicketsDescription " +
											 	  "WHERE languageId = " + Constants.getLanguageCode(language), EventTypes.class);
				break;

			case "LOCATION":
				SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
				ArrayList<Events> events = 
						(ArrayList<Events>) Events.populateCollection(
												"SELECT DISTINCT location " +
												"FROM Events " +
												"WHERE dateEnd > " + sdf.format(new Date()), Events.class);
				map = new ArrayList<Object>();
				for (Events e : events)
				{
					map.add(e.getLocation());
				}
				break;
			}
		} 
		catch (Exception e) {
			log.error("Error jasonizing the map " + jsonIn.mapName + " (" + e.getMessage() + ")");
			json = LanguageResources.getResource(
					Constants.getLanguageCode(language), "generic.execError") + " (" + 
					e.getMessage() + ")";
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(json).build();
		}
		// No record found. return an empty object

		if (map == null)
		{
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		
		if (jh.jasonize(map, language) != Response.Status.OK)
		{
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(jh.json).build();
		}
		
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}
}
