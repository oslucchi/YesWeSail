package com.yeswesail.users;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;

@Path("/users")
public class Profile {
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());
	
	@GET
	@Path("/basic/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@PathParam("token") String token)
	{
		return Response.status(Response.Status.OK).build();
	}

}
