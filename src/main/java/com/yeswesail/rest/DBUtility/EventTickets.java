package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

public class EventTickets extends DBInterface
{	
	private static final long serialVersionUID = -643395528484801051L;
	public static final int BUNK = 1;
	public static final int CABIN_NO_BATH = 2;
	public static final int CABIN_BATH = 3;
	public static final int CABIN_VIP = 4;
	
	protected int idEventTickets;
	protected int eventId;
	protected int ticketType;
	protected int available;
	protected int booked;
	protected int price;
	protected int cabinRef;
	protected String bookedTo;
	protected String description;
	
	private void setNames()
	{
		tableName = "EventTickets";
		idColName = "idEventTickets";
	}

	public EventTickets() throws Exception
	{
		setNames();
	}

	public EventTickets(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}
	
	public EventTickets(DBConnection conn, int id, int languageId) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description " +
			 	 "FROM EventTickets a INNER JOIN EventTicketsDescription b ON " +
			 	 "     a.ticketType = b.ticketType AND " +
			 	 "     b.languageId = " + languageId + " " +
				 "WHERE eventId = " + id + " AND " +
			 	 "      available > booked ";
		this.populateObject(conn, sql, this);
	}

	public static EventTickets[] findByEventId(int eventId, int languageId) throws Exception
	{
		String sql = "SELECT a.*, b.description " +
				 	 "FROM EventTickets a INNER JOIN EventTicketsDescription b ON " +
				 	 "     a.ticketType = b.ticketType AND " +
				 	 "     b.languageId = " + languageId + " " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      available > booked " + 
					 "ORDER BY ticketType, price ASC";
		@SuppressWarnings("unchecked")
		ArrayList<EventTickets> tickets=
			(ArrayList<EventTickets>) EventTickets.populateCollection(sql, EventTickets.class);
		return(tickets.toArray(new EventTickets[tickets.size()]));
	}

	public int getIdEventTickets() {
		return idEventTickets;
	}

	public void setIdEventTickets(int idEventTickets) {
		this.idEventTickets = idEventTickets;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

	public int getBooked() {
		return booked;
	}

	public void setBooked(int booked) {
		this.booked = booked;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getTicketType() {
		return ticketType;
	}

	public void setTicketType(int ticketType) {
		this.ticketType = ticketType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCabinRef() {
		return cabinRef;
	}

	public void setCabinRef(int cabinRef) {
		this.cabinRef = cabinRef;
	}

	public void bookATicket() {
		booked ++;
	}

	public void releaseATicket() {
		booked --;
	}

	public String getBookedTo() {
		return bookedTo;
	}

	public void setBookedTo(String bookedTo) {
		this.bookedTo = bookedTo;
	}
	
	
}
