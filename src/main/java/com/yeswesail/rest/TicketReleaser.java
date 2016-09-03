package com.yeswesail.rest;

import java.util.Date;

import org.apache.log4j.Logger;

import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.TicketLocks;

public class TicketReleaser extends Thread {
	final Logger log = Logger.getLogger(this.getClass());
	ApplicationProperties prop = ApplicationProperties.getInstance();
	private DBConnection conn;

	private void release(TicketLocks tl) throws Exception
	{
		EventTickets[] ticketsToRelease;

		return;
//		DBInterface.TransactionStart();
//		EventTickets et = new EventTickets(conn, tl.getEventTicketId());
//		if (et.getTicketType() == EventTickets.WHOLE_BOAT)
//		{
//			ticketsToRelease = EventTickets.getAllTicketByEventId(et.getEventId(), 1);
//		}
//		else
//		{
//			ticketsToRelease = new EventTickets[1];
//			ticketsToRelease[0] = et;
//		}
//		for(EventTickets item : ticketsToRelease)
//		{
//			try
//			{
//				TicketLocks ticket = TicketLocks.findByEventTicketId(conn, item.getIdEventTickets());
//				log.debug("Deleting lock id " + ticket.getIdTicketLocks());
//				ticket.delete(conn, ticket.getIdTicketLocks());
//			}
//			catch(Exception e)
//			{
//				if (e.getMessage().compareTo("No record found") != 0)
//				{
//					throw e;
//				}
//			}
//			finally
//			{
//				DBInterface.TransactionRollback(conn);
//			}
//			if (item.getBooked() > 0)
//			{
//				log.debug("Updating booked status for event ticket " + item.getIdEventTickets());
//				item.setBooked(item.getBooked() - 1);
//				item.update(conn, "idEventTickets");
//			}
//		}
//		DBInterface.TransactionCommit(conn);
	}
	
	public void doShutdown()
	{
		log.trace("Requested to shutdown");
		Thread.currentThread().interrupt();
	}
	
    public void run() {
    	
		Date now;
		int count = 0;
		log.trace("Started");
    	try
    	{
    		conn = DBInterface.connect();
        	while(true)
        	{
//        		now = new Date();
//        		TicketLocks[] tList = TicketLocks.findAll();
//        		for(TicketLocks tl : tList)
//        		{
//        			if (now.getTime() - tl.getLockTime().getTime() > prop.getReleaseTicketLocksAfter() * 1000)
//        			{
//        				log.debug("Ticket lockId " + tl.getIdTicketLocks() + 
//        						  " eventTicketId " + tl.getEventTicketId() + 
//        		 				  " expired will be removed");
//        				release(tl);
//        			}
//        		}
        		Thread.sleep(1000);
        		if (++count == 60)
        		{
        			log.trace("Thread ticketReleaser is running");
        			count = 0;
        		}
        	}
    	}
    	catch(Exception e)
    	{
			log.trace("Exception " + e.getMessage() + " aborting");
    		Thread.currentThread().interrupt();
    	}
    }
}
