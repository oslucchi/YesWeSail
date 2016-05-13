package com.yeswesail.rest;

import javax.ws.rs.Path;

import com.yeswesail.rest.ApplicationProperties;

@Path("/")
public class YesWeSail {
	ApplicationProperties prop;

	public YesWeSail()
	{
		prop = ApplicationProperties.getInstance();
	}
}
