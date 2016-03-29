package com.yeswesail.rest;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.DBUtility.DBConnection;

@Path("/")
public class YesWeSail {
	ApplicationProperties prop;
	DBConnection conn;

	@GET
	@Produces("text/html")
	public Response getStartingPage()
	{
		String output = "<h1>Hello World!<h1>" +
				"<p>RESTful Service is running ... <br>Ping @ " + new Date().toString() + "</p<br>";
		return Response.status(200).entity(output).build();
	}

	public YesWeSail()
	{
		prop = new ApplicationProperties();
		try 
		{
			conn = DBConnection.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
