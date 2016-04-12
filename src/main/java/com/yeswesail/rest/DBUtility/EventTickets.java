package com.yeswesail.rest.DBUtility;


public class EventTickets extends DBInterface
{	
	private static final long serialVersionUID = -643395528484801051L;

	protected int idEventTickets;
	protected int eventId;
	protected int available;
	protected int booked;
	protected int price;
	
	private void setNames()
	{
		tableName = "Events";
		idColName = "idEvents";
	}

	public EventTickets() throws Exception
	{
		setNames();
	}

	public EventTickets(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
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
	
}
