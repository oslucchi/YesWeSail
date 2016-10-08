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
		DBConnection transaction = null;

		EventTickets et = new EventTickets(conn, tl.getEventTicketId());
		if (et.getTicketType() == EventTickets.WHOLE_BOAT)
		{
			log.debug("WHOLE_BOAT ticket required to be released");
			ticketsToRelease = EventTickets.getAllTicketByEventId(et.getEventId(), 1);
		}
		else
		{
			ticketsToRelease = new EventTickets[1];
			ticketsToRelease[0] = et;
		}
		try
		{
			transaction = DBInterface.TransactionStart();
			for(EventTickets item : ticketsToRelease)
			{
				try
				{
					TicketLocks ticket = TicketLocks.findByEventTicketId(transaction, item.getIdEventTickets());
					log.debug("Deleting lock id " + ticket.getIdTicketLocks());
					ticket.delete(transaction, ticket.getIdTicketLocks());
				}
				catch(Exception e)
				{
					if (e.getMessage().compareTo("No record found") != 0)
					{
						log.warn("Exception " + e.getMessage() + " retrieving and deleting ticket lock for eventTicket " +
								 item.getIdEventTickets());
						DBInterface.TransactionRollback(transaction);
						throw e;
					}
				}
				if (item.getBooked() > 0)
				{
					log.debug("Updating booked status for event ticket " + item.getIdEventTickets());
					item.setBooked(item.getBooked() - 1);
					item.update(transaction, "idEventTickets");
				}
			}			
		}
		catch(Exception e)
		{
			DBInterface.TransactionRollback(transaction);
			return;
		}
		finally
		{
			DBInterface.TransactionCommit(transaction);
		}
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
        		now = new Date();
        		TicketLocks[] tList = TicketLocks.findAll();
        		for(TicketLocks tl : tList)
        		{
        			if ((tl.getLockTime() != null) && 
        				(tl.getStatus().compareTo("P") == 0) &&
        				(now.getTime() - tl.getLockTime().getTime() > prop.getReleaseTicketLocksAfter() * 1000))
        			{
        				log.debug("Ticket lockId " + tl.getIdTicketLocks() + 
        						  " eventTicketId " + tl.getEventTicketId() + 
        		 				  " expired will be removed");
        				release(tl);
        			}
        		}
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
