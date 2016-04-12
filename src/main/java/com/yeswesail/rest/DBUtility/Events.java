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
	protected int minPrice;
	protected int maxPrice;
	
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
		sql = "SELECT MIN(price) as minPrice, MAX(price) as maxPrice " +
			  "FROM EventTickets c " + 
			  "WHERE c.booked < c.available AND " +
//			  "      c.languageId = " + languageId + " AND " +
			  "      c.eventId = " + this.idEvents + " " +
			  "ORDER BY price ASC";
		@SuppressWarnings("unchecked")
		ArrayList<EventTickets> tickets = (ArrayList<EventTickets>) EventTickets.populateCollection(sql, EventTickets.class);
		if (tickets.size() != 0)
		{
			this.minPrice = tickets.get(0).price;
			this.maxPrice = tickets.get(tickets.size() - 1).price;
		}
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
		for(Events e : hotEvents)
		{
			sql = "SELECT price " +
					  "FROM EventTickets c " + 
					  "WHERE c.booked < c.available AND " +
//					  "      c.languageId = " + languageId + " AND " +
					  "      c.eventId = " + e.idEvents + " " +
					  "ORDER BY price ASC";
				@SuppressWarnings("unchecked")
				ArrayList<EventTickets> tickets = (ArrayList<EventTickets>) EventTickets.populateCollection(sql, EventTickets.class);
				if (tickets.size() != 0)
				{
					e.minPrice = tickets.get(0).price;
					e.maxPrice = tickets.get(tickets.size() - 1).price;
				}
		}
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
		for(Events e : events)
		{
			sql = "SELECT price " +
					  "FROM EventTickets c " + 
					  "WHERE c.booked < c.available AND " +
//					  "      c.languageId = " + languageId + " AND " +
					  "      c.eventId = " + e.idEvents + " " +
					  "ORDER BY price ASC";
				@SuppressWarnings("unchecked")
				ArrayList<EventTickets> tickets = (ArrayList<EventTickets>) EventTickets.populateCollection(sql, EventTickets.class);
				if (tickets.size() != 0)
				{
					e.minPrice = tickets.get(0).price;
					e.maxPrice = tickets.get(tickets.size() - 1).price;
				}
		}
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

	public int getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(int minPrice) {
		this.minPrice = minPrice;
	}

	public int getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(int maxPrice) {
		this.maxPrice = maxPrice;
	}
	
	
}
