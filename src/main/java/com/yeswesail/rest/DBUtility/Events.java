package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.SessionData;

public class Events extends DBInterface
{	
	private static final long serialVersionUID = 2658938461796360575L;
	
	protected int idEvents;
	protected int eventType;
	protected Date dateStart;
	protected Date dateEnd;
	protected String location;
	protected int categoryId;
	protected String imageURL;
	protected int shipownerId;
	protected int shipId;
	protected String labels;
	protected String description;
	
	private void setNames()
	{
		tableName = "Events";
		idColName = "idEvents";
	}

	public Events() throws Exception
	{
		setNames();
	}

	public Events(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}
	
	public Events(int id, int languageId) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description " +
				 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 "     ON a.idEvents = b.eventId AND " +
			 	 "        b.languageId = " + languageId + " AND " +
				 "		  b.anchorZone = 0 " +
				 "WHERE idEvents = " + id;
		this.populateObject(sql, this);
	}
	
	public static Events[] findHot(int languageId) throws Exception
	{
		String sql = "SELECT *, b.description  " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
				 	 "        b.languageId = " + languageId + " AND " +
					 "		  b.anchorZone = 0 " +
				 	 "ORDER BY dateStart " +
				 	 "LIMIT " + ApplicationProperties.getInstance().getMaxNumHotOffers();
		@SuppressWarnings("unchecked")
		ArrayList<Events> hotEvents = (ArrayList<Events>) Events.populateCollection(sql, Events.class);
		if (hotEvents.size() == 0)
			return null;
		return(hotEvents.toArray(new Events[hotEvents.size()]));
	}

	public static Events[] findHot(String token) throws Exception
	{
		int languageId = SessionData.getInstance().getLanguage(token);
		return(findHot(languageId));
	}

	public static Events[] findByFilter(String whereClause, int languageId) throws Exception
	{
		String sql = "SELECT *, b.description  " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
				 	 "        b.languageId = " + languageId + " AND " +
					 "		  b.anchorZone = 0 " +
				 	 whereClause;
		@SuppressWarnings("unchecked")
		ArrayList<Events> events = (ArrayList<Events>) Events.populateCollection(sql, Events.class);
		if (events.size() == 0)
			return null;
		return(events.toArray(new Events[events.size()]));
	}

	public static Events[] findByFilter(String whereClause, String token) throws Exception
	{
		int languageId = SessionData.getInstance().getLanguage(token);
		return(findByFilter(whereClause, languageId));
	}

	public int getIdEvents() {
		return idEvents;
	}

	public void setIdEvents(int idEvents) {
		this.idEvents = idEvents;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
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

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public int getShipownerId() {
		return shipownerId;
	}

	public void setShipownerId(int shipownerId) {
		this.shipownerId = shipownerId;
	}

	public int getShipId() {
		return shipId;
	}

	public void setShipId(int shipId) {
		this.shipId = shipId;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
