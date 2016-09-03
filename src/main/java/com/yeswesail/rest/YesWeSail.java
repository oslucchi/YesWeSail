package com.yeswesail.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Path;

import com.yeswesail.rest.ApplicationProperties;

@Path("/")
public class YesWeSail implements ServletContextListener {
	ApplicationProperties prop;
	TicketReleaser releaser;

	@Override
    public void contextDestroyed(ServletContextEvent sce){
        try {
            releaser.doShutdown();
            releaser.interrupt();
        } catch (Exception ex) {
        }
    }

	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		prop = ApplicationProperties.getInstance();
		prop.setContext(arg0.getServletContext());
        if ((releaser == null) || (!releaser.isAlive())) {
        	releaser = new TicketReleaser();
            releaser.start();
        }
    }

}
