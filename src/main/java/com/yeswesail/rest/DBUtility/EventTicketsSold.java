package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

public class EventTicketsSold extends DBInterface
{	
	private static final long serialVersionUID = -643395528484801051L;

	protected int idEventTicketsSold;
	protected int eventId;
	protected int userId;
	
	private void setNames()
	{
		tableName = "EventTicketsSold";
		idColName = "idEventsTicketsSold";
	}

	public EventTicketsSold() throws Exception
	{
		setNames();
	}

	public EventTicketsSold(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public static Users[] findParticipants(int eventId, int languageId) throws Exception
	{

		String sql = "SELECT DISTINCT c.name, c.surname, c.idUsers, c.imageURL " +
					 "FROM Users c INNER JOIN ( " +
					 "      EventTicketsSold a INNER JOIN EventTickets b ON " +
					 "         a.eventTicketId = b.idEventTickets AND " +
					 "         b.eventId = " + eventId + " " +
					 "      ) ON " +
					 "	 a.userId = c.idUsers";
		@SuppressWarnings("unchecked")
		ArrayList<Users> tickets = (ArrayList<Users>) Users.populateCollection(sql, Users.class);
		return(tickets.toArray(new Users[tickets.size()]));
	}

	public int getIdEventTicketsSold() {
		return idEventTicketsSold;
	}

	public void setIdEventTicketsSold(int idEventTicketsSold) {
		this.idEventTicketsSold = idEventTicketsSold;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

}
