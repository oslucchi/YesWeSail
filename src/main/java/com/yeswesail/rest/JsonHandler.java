package com.yeswesail.rest;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonHandler {
	final Logger log = Logger.getLogger(this.getClass());
	public String json = null;

	public Status jasonize(Object obj, String language) 
	{
		return jasonize(obj, Constants.getLanguageCode(language));
	}

	public Status jasonize(Object obj, int languageId) 
	{
		ObjectMapper mapper = new ObjectMapper();
		
		try
		{
			json = mapper.writeValueAsString(obj);
		}
		catch (IOException e) 
		{
			log.error("Error jasonizing the object (" + e.getMessage() + ")");
			json = LanguageResources.getResource(languageId, "generic.execError") + " (" + 
					e.getMessage() + ")";
			return Response.Status.UNAUTHORIZED;
		}
		return Response.Status.OK;
	}
}
