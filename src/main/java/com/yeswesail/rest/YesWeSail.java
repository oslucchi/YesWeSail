package com.yeswesail.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;

@Path("/")
public class YesWeSail implements ServletContextListener {
	ApplicationProperties prop;
	TicketReleaser releaser;
	final Logger log = Logger.getLogger(this.getClass());

	@Override
    public void contextDestroyed(ServletContextEvent sce){
        try {
            releaser.doShutdown();
            releaser.interrupt();
        } 
        catch(Exception e) {
    		log.warn("Exception " + e.getMessage(), e);    		
        }
    }

	@Override
	public void contextInitialized(ServletContextEvent arg0) 
	{
		prop = ApplicationProperties.getInstance();
		log.debug("arg0 servlet context " + arg0.getServletContext());
		prop.setContext(arg0.getServletContext());

        if (prop.isStartReleaser() && ((releaser == null) || !releaser.isAlive()))
        {
    		log.debug("starting ticket releaser");
        	releaser = new TicketReleaser();
            releaser.start();
        }
    }

}
