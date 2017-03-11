package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

import com.yeswesail.rest.Constants;

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

	public EventDescription(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static EventDescription[] findByEventId(int eventId, int languageId) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + languageId + " " +
					 "ORDER BY anchorZone";
		ArrayList<EventDescription> eventDescriptions =
			(ArrayList<EventDescription>) EventDescription.populateCollection(sql, EventDescription.class);
		if (eventDescriptions.size() == 0)
		{
			sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + Constants.getLanguageCode("en_US") + " " +
					 "ORDER BY anchorZone";
			eventDescriptions =
					(ArrayList<EventDescription>) EventDescription.populateCollection(sql, EventDescription.class);
		}
		if (eventDescriptions.size() == 0)
		{
			sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + Constants.getLanguageCode("it_IT") + " " +
					 "ORDER BY anchorZone";
			eventDescriptions =
					(ArrayList<EventDescription>) EventDescription.populateCollection(sql, EventDescription.class);
		}
		return(eventDescriptions.toArray(new EventDescription[eventDescriptions.size()]));
	}

	public void findEventTitleyId(DBConnection conn, int eventId, int languageId) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM EventDescription a " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + languageId + " AND " +
					 "      anchorZone = 0 ";
		this.populateObject(conn, sql, this);
	}


	public static void deleteOnWhere(String whereClause) throws Exception
	{
		DBConnection conn = null;
		conn = new DBConnection();
		conn.executeQuery("DELETE FROM EventDescription " + whereClause, true);
	}
	
	public void deleteOnWhere(DBConnection conn, String whereClause) throws Exception
	{
		conn.executeQuery("DELETE FROM EventDescription " + whereClause, true);
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