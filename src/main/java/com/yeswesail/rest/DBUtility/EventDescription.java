package com.yeswesail.rest.DBUtility;

public class EventDescription extends DBInterface 
{
	private static final long serialVersionUID = -1022667590563399335L;

	protected int idEventDescription;
	protected int languageId;
	protected int eventId;
	protected int anchorZone;
	protected String description;

	private void setNames()
	{
		tableName = "EventDescription";
		idColName = "idEventDescription";
	}

	public EventDescription() throws Exception
	{
		setNames();
	}

	public EventDescription(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public int getIdEventDescription() {
		return idEventDescription;
	}

	public void setIdEventDescription(int idEventDescription) {
		this.idEventDescription = idEventDescription;
	}

	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getAnchorZone() {
		return anchorZone;
	}

	public void setAnchorZone(int anchorZone) {
		this.anchorZone = anchorZone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}