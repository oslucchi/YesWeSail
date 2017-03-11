package com.yeswesail.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.EventTickets;
import com.yeswesail.rest.DBUtility.Events;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.RegistrationConfirm;
import com.yeswesail.rest.DBUtility.TicketLocks;
import com.yeswesail.rest.DBUtility.Users;

public class TicketReleaser extends Thread {
	final long DAY_DURATION = 60 * 60 * 24 * 1000;
	final Logger log = Logger.getLogger(this.getClass());
	ApplicationProperties prop = ApplicationProperties.getInstance();
	private DBConnection conn;

	private void release(TicketLocks tl, DBConnection transaction) throws Exception
	{
		EventTickets[] ticketsToRelease;

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
			DBInterface.TransactionStart(transaction);
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
	
    @SuppressWarnings("unchecked")
	public void run() {
    	HashMap<Integer, TicketLocks> pendingBuy = new HashMap<>();
		Date now;
		int count = 0;
		log.trace("Ticket releaser Started");
    	try
    	{
    		conn = DBInterface.connect();
        	while(true)
        	{
        		now = new Date();
        		TicketLocks[] tList = TicketLocks.findAll(false);
        		for(TicketLocks tl : tList)
        		{
        			if (tl.getLockTime() == null)
        				continue;
        			switch(tl.getStatus())
        			{
        			case "P":
        				if (now.getTime() - tl.getLockTime().getTime() > prop.getReleaseTicketLocksAfter() * 1000)
            			{
            				log.debug("Ticket lockId " + tl.getIdTicketLocks() + 
            						  " eventTicketId " + tl.getEventTicketId() + 
            		 				  " expired will be removed");
            				release(tl, conn);
            			}
        				break;

        			case "W":
        				if (!pendingBuy.containsKey(tl.getIdTicketLocks()) && 
        					(now.getTime() - tl.getLockTime().getTime() > prop.getSendMailOnTicketinWState() * 1000))
            			{
        					pendingBuy.put(tl.getIdTicketLocks(), tl);
            				log.debug("Ticket lockId " + tl.getIdTicketLocks() + 
            						  " eventTicketId " + tl.getEventTicketId() + 
            		 				  " is in pending state since too long. Sending email to admin");
            				EventTickets  et = new EventTickets(conn, tl.getEventTicketId());
            				Events e = (et != null ? e = new Events(conn, et.getEventId()) : null);
            				Users u = new Users(conn, tl.getUserId());
            				
            				String body =  "L'utente " + 
		    							   u.getSurname() + " " + u.getName() +
		        						   " ha tentato l'acquistato di un biglietto senza completare la procedura in " +
		    							   (prop.getSendMailOnTicketinWState() / 60) + " minuti.<br/>" +
		        						   "I riferimenti del cliente per il contatto sono:\n" +
		    							   "<ul>" +
		    							   "<li>email: " + u.getEmail() + "</li>" +
		        						   "<li>telefono: " + u.getPhone1() + "</li>" +
		    							   "</ul><br>";
            				if (e != null)
            				{
            					Users so = new Users(conn, e.getShipOwnerId());
            					SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy");
            					body += "L'evento che aveva prenotato e' il seguente:<br>" + 
            							"<ul>" +
            							"<li>Id evento: " + e.getIdEvents() + "</li>" +
            							"<li>Da: " + sf.format(e.getDateStart()) + " a: " + sf.format(e.getDateEnd()) + "</li>" +
            							"<li>Imbarco: " + e.getLocation() + "</li>" +
            							"<li>Armatore: " + so.getSurname() + " " + so.getName() + "</li>" +
            							"</ul><br>";
            				}
            				body += "Contattare il cliente per controllare lo stato dell'acquisto e successivamente " +
            						"il supporto tecnico, per ripristinare il corretto stato del biglietto";
            				Mailer.sendMail(prop.getAdminEmail(), 
            								"Biglietto in attesa completamento acquisto da troppo tempo", 
            								body, null);

            				PendingActions pa = new PendingActions();
            				pa.setActionType("ticket lost");
            				pa.setUserId(tl.getUserId());
            				pa.setLink("rest/requests/ticketLost/" + tl.getIdTicketLocks());
            				pa.setCreated(new Date());
            				pa.setUpdated(pa.getCreated());
            				pa.setStatus("P");
            				pa.insert(conn, "idPendingActions", pa);

            				log.debug("Mail sent");
            			}
        				break;
        			}
        		}
        		
        		ArrayList<RegistrationConfirm> rcList = 
        				(ArrayList<RegistrationConfirm>) RegistrationConfirm.populateCollection(
    										"SELECT * FROM RegistrationConfirm " +
    										"WHERE status = 'A'", RegistrationConfirm.class);
        		for(RegistrationConfirm rc : rcList)
        		{
    				if ((now.getTime() - rc.getCreated().getTime()) > 5 * DAY_DURATION)
    				{
    	        		log.debug("marked confirmation id " + rc.getIdRegistrationConfirm());
    					rc.setStatus("E");
    					rc.update(conn, "idRegistrationConfirm");
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
