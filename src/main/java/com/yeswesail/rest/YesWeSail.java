package com.yeswesail.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Path;

import com.yeswesail.rest.ApplicationProperties;

@Path("/")
public class YesWeSail implements ServletContextListener {
	ApplicationProperties prop;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		;
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		prop = ApplicationProperties.getInstance();
		prop.setContext(arg0.getServletContext());		
	}
}
