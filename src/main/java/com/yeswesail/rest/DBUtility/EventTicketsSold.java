package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

public class EventTicketsSold extends DBInterface
{	
	private static final long serialVersionUID = -643395528484801051L;

	protected int idEventTicketsSold;
	protected int eventTicketId;
	protected int userId;
	protected String transactionId;
	protected Date timestamp;

	private void setNames()
	{
		tableName = "EventTicketsSold";
		idColName = "idEventsTicketsSold";
	}

	public EventTicketsSold() throws Exception
	{
		setNames();
	}

	public EventTicketsSold(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
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

	public int getEventTicketId() {
		return eventTicketId;
	}

	public void setEventTicketId(int eventTicketId) {
		this.eventTicketId = eventTicketId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
