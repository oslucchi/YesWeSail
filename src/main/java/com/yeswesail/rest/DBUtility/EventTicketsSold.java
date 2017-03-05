package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

public class EventTicketsSold extends DBInterface
{	
	private static final long serialVersionUID = -643395528484801051L;

	protected int idEventTicketsSold;
	protected int eventTicketId;
	protected int userId;
	protected int eventId;
	protected String transactionId;
	protected String description;
	protected Date timestamp;
	protected int price;
	protected String title;
	protected Date dateStart;
	protected Date dateEnd;
	protected String location;

	private void setNames()
	{
		tableName = "EventTicketsSold";
		idColName = "idEventTicketsSold";
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

	public static Users[] findParticipants(int eventId) throws Exception
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
	
	public static EventTicketsSold[] getTicketSold(int eventId, int languageId) throws Exception
	{
		String sql = "SELECT *, c.description, b.price " +
					 "FROM EventTicketsDescription c INNER JOIN ( " +
					 "      EventTicketsSold a INNER JOIN EventTickets b ON " +
					 "         a.eventTicketId = b.idEventTickets AND " +
					 "         b.eventId = " + eventId + " " +
					 "      ) ON " +
					 "	 c.ticketType = b.ticketType " +
					 "WHERE c.languageId = " + languageId;
		@SuppressWarnings("unchecked")
		ArrayList<EventTicketsSold> tickets = 
			(ArrayList<EventTicketsSold>) EventTicketsSold.populateCollection(sql, EventTicketsSold.class);
		return(tickets.toArray(new EventTicketsSold[tickets.size()]));
	}

	public static EventTicketsSold[] findByEventId(int eventId) throws Exception
	{
		String sql = "SELECT a.* " +
					 "FROM EventTicketsSold a INNER JOIN EventTickets b ON " +
					 "         a.eventTicketId = b.idEventTickets " +
					 "WHERE b.eventId = " + eventId;
		@SuppressWarnings("unchecked")
		ArrayList<EventTicketsSold> tickets = 
			(ArrayList<EventTicketsSold>) EventTicketsSold.populateCollection(sql, EventTicketsSold.class);
		return(tickets.toArray(new EventTicketsSold[tickets.size()]));
	}

	public static EventTicketsSold[] findTicketSoldToUser(int userId, int languageId) throws Exception
	{
		String sql = "SELECT a.*, b.eventId, c.description, b.price, " +
					 "       d.dateStart, d.dateEnd, d.location, e.description as title " +
					 "FROM (EventTicketsDescription c INNER JOIN ( " +
					 "       EventTicketsSold a INNER JOIN EventTickets b ON " +
					 "         a.eventTicketId = b.idEventTickets " +
					 "     ) ON " +
					 "	      c.ticketType = b.ticketType AND " +
					 "         c.languageId = " + languageId + ")  " +
					 "	INNER JOIN " +
					 "     (Events d INNER JOIN EventDescription e ON " +
					 "        d.idEvents = e.eventId AND " +
					 "        e.anchorZone = 0 AND " +
					 "        e.languageId = " + languageId + ") ON " +
					 "      b.eventId = e.eventId " +
					 "WHERE a.userId = " + userId;
		@SuppressWarnings("unchecked")
		ArrayList<EventTicketsSold> tickets = 
			(ArrayList<EventTicketsSold>) EventTicketsSold.populateCollection(sql, EventTicketsSold.class);
		return(tickets.toArray(new EventTicketsSold[tickets.size()]));
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

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDateStart() {
		return dateStart;
	}

	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}

	public Date getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
