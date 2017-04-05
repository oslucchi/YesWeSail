package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

import com.yeswesail.rest.Constants;

public class EventDescription extends DBInterface 
{
	private static final long serialVersionUID = -1022667590563399335L;
	public static final int MAX_ANCHOR_ZONE = 5;
	public static final String[] zones = {
										"title",
										"description",
										"logistics", 
										"includes", 
										"excludes"
									};
	
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
	public static EventDescription[] findByEventId(DBConnection conn, int eventId, int languageId) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + languageId + " " +
					 "ORDER BY anchorZone";
		ArrayList<EventDescription> eventDescriptions =
			(ArrayList<EventDescription>) EventDescription.populateCollection(conn, sql, EventDescription.class);
		if (eventDescriptions.size() == 0)
		{
			sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + Constants.getLanguageCode("en_US") + " " +
					 "ORDER BY anchorZone";
			eventDescriptions =
					(ArrayList<EventDescription>) EventDescription.populateCollection(conn, sql, EventDescription.class);
		}
		if (eventDescriptions.size() == 0)
		{
			sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + Constants.getLanguageCode("it_IT") + " " +
					 "ORDER BY anchorZone";
			eventDescriptions =
					(ArrayList<EventDescription>) EventDescription.populateCollection(conn, sql, EventDescription.class);
		}
		return(eventDescriptions.toArray(new EventDescription[eventDescriptions.size()]));
	}

	public static EventDescription findByEventId(DBConnection conn, int eventId, int anchorZone, int languageId) throws Exception
	{
		EventDescription ed = new EventDescription();
		String sql = "SELECT * " +
				 	 "FROM EventDescription " +
					 "WHERE eventId = " + eventId + " AND " +
				 	 "      languageId = " + languageId + " AND " +
					 "      anchorZone = " + anchorZone;
		try
		{
			ed.populateObject(conn, sql, ed);
		}
		catch(Exception e)
		{
			if (e.getMessage().equals("No record found"))
			{
				return null;
			}
			throw e;
		}
		return ed;
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
		try
		{
			conn.executeQuery("DELETE FROM EventDescription " + whereClause, true);
		}
    	catch(Exception e)
		{
			DBInterface.disconnect(conn);
			throw e;
		}
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