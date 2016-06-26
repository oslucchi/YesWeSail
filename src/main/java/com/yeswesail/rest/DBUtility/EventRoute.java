package com.yeswesail.rest.DBUtility;


import java.util.ArrayList;

import org.apache.log4j.Logger;

public class EventRoute extends DBInterface
{	
	private static final long serialVersionUID = 2658938461796360575L;
	private static final Logger log = Logger.getLogger(EventRoute.class);
	// private static final ApplicationProperties prop = ApplicationProperties.getInstance();
	

	protected int idEventRoute;
	protected int eventId;
	protected String lat;
	protected String lng;
	protected int seq;
	protected String description;
	
	private void setNames()
	{
		tableName = "EventRoute";
		idColName = "idEventRoute";
	}

	public EventRoute() throws Exception
	{
		setNames();
	}

	public EventRoute(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}
	
	@SuppressWarnings("unchecked")
	public static EventRoute[] getRoute(DBConnection conn, int eventId) throws Exception 
	{
		String sql = "SELECT * " +
				 	 "FROM EventRoute " +
				 	 "WHERE eventId = " + eventId + " " +
				 	 "ORDER BY seq ";
	
		log.trace("trying to populate collection with sql '" + sql + "'");
		ArrayList<EventRoute> route = 
				(ArrayList<EventRoute>) EventRoute.populateCollection(sql, EventRoute.class);
		log.trace("Done. There are " + route.size() + " elemets");
		if (route.size() == 0)
			return null;
		
		return(route.toArray(new EventRoute[route.size()]));
	}
	
	public static void deleteOnWhere(String whereClause) throws Exception
	{
		DBConnection conn = null;
		conn = new DBConnection();
		conn.executeQuery("DELETE FROM EventRoute " + whereClause);
	}

	public void deleteOnWhere(DBConnection conn , String whereClause) throws Exception
	{
		conn.executeQuery("DELETE FROM EventRoute " + whereClause);
	}

	public int getIdEventRoute() {
		return idEventRoute;
	}

	public void setIdEventRoute(int idEventRoute) {
		this.idEventRoute = idEventRoute;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
