package com.yeswesail.rest.DBUtility;


public class EventTicketsDescription extends DBInterface
{	
	private static final long serialVersionUID = -643395528484801051L;

	protected int idEventTicketsDescription;
	protected int eventId;
	protected int ticketId;
	protected int languageId;
	protected String description;
	
	private void setNames()
	{
		tableName = "EventTicketsDescription";
		idColName = "idEventTicketsDescription";
	}

	public EventTicketsDescription() throws Exception
	{
		setNames();
	}

	public EventTicketsDescription(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public int getIdEventTicketsDescription() {
		return idEventTicketsDescription;
	}

	public void setIdEventTicketsDescription(int idEventTicketsDescription) {
		this.idEventTicketsDescription = idEventTicketsDescription;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getTicketId() {
		return ticketId;
	}

	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
