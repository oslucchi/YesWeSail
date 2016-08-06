package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.SessionData;

public class Events extends DBInterface
{	
	private static final long serialVersionUID = 2658938461796360575L;
	private static final Logger log = Logger.getLogger(DBInterface.class);
	private static final ApplicationProperties prop = ApplicationProperties.getInstance();
	
	protected int idEvents;
	protected int eventType;
	protected Date dateStart;
	protected Date dateEnd;
	protected String location;
	protected int categoryId;
	protected String imageURL;
	protected int shipOwnerId;
	protected int boatId;
	protected String labels;
	protected String title;
	protected int minPrice;
	protected int maxPrice;
	protected String status;
	protected boolean earlyBooking;
	protected boolean lastMinute;
	protected boolean hotEvent;
	protected String eventRef;
	protected String aggregateKey;
	protected int createdBy;
	protected Date createdOn;
	
	private static ArrayList<Events> events;
	
	private void setNames()
	{
		tableName = "Events";
		idColName = "idEvents";
	}

	public Events() throws Exception
	{
		setNames();
	}

	public Events(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
		if (!getImageURL().startsWith("http"))
		{
			setImageURL(prop.getWebHost() + "/" + getImageURL());
		}
	}
	
	public Events(DBConnection conn, int id, int languageId) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description AS `title` " +
				 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 "     ON a.idEvents = b.eventId AND " +
			 	 "        b.languageId = " + languageId + " AND " +
				 "		  b.anchorZone = 0 " +
				 "WHERE idEvents = " + id + " AND " +
				 "      status = 'A'";
		this.populateObject(conn, sql, this);
		if (!getImageURL().startsWith("http"))
		{
			setImageURL(prop.getWebHost() + "/" + getImageURL());
		}
		events = new ArrayList<Events>();
		events.add(this);
		getTicketMaxAndMin(events);
	}
	
	public Events(DBConnection conn, int id, int languageId, boolean onlyActive) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description AS `title` " +
				 "FROM Events AS a LEFT OUTER JOIN EventDescription AS b " +
				 "     ON a.idEvents = b.eventId AND " +
				 "        b.languageId = " + languageId + " AND " +
				 "		  b.anchorZone = 0 " + 
				 "WHERE idEvents = " + id;
		if (onlyActive)
		{			
			sql += " AND status = 'A'";
		}
		
		this.populateObject(conn, sql, this);
		if (!getImageURL().startsWith("http"))
		{
			setImageURL(prop.getWebHost() + "/" + getImageURL());
		}
		events = new ArrayList<Events>();
		events.add(this);
		getTicketMaxAndMin(events);
	}

	@SuppressWarnings("unchecked")
	public static Events[] findHot(int languageId) throws Exception
	{
		String sql = "SELECT a.*, b.description AS `title` " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
				 	 "        b.languageId = " + languageId + " AND " +
					 "		  b.anchorZone = 0 " + " AND " +
					 "        a.status = 'A' AND " +
					 "        a.dateStart > NOW() AND " +
					 "        a.hotEvent = 1 " +
				 	 "ORDER BY dateStart ";
		
		log.trace("trying to populate collection with sql '" + sql + "'");
		events = (ArrayList<Events>) Events.populateCollection(sql, Events.class);
		log.trace("Done. There are " + events.size() + " elemets");
		log.trace("Get tickets for the event");
		getTicketMaxAndMin(events);
		
		ArrayList<Events> retList = new ArrayList<Events>();
		for(Events e : events)
		{
			if (e.minPrice != 0)
			{
				e.setImageURL(prop.getWebHost() + "/" + e.getImageURL());
				retList.add(e);
			}
			if ((prop.getMaxNumHotOffers() != 0) && (retList.size() == prop.getMaxNumHotOffers()))
			{
				break;
			}
		}
		log.trace("Returning to caller. List size = " + retList.size());
		if (retList.size() == 0)
			return null;
		
		return(retList.toArray(new Events[retList.size()]));
	}

	public static Events[] findHot(String token) throws Exception
	{
		int languageId = SessionData.getInstance().getLanguage(token);
		return(findHot(languageId));
	}

	public static Events[] findByFilter(String whereClause, int languageId) throws Exception
	{
		String sql = "SELECT a.*, b.description AS `title` " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
				 	 "        b.languageId = " + languageId + " AND " +
					 "		  b.anchorZone = 0 " + 
				 	 whereClause;
		@SuppressWarnings("unchecked")
		ArrayList<Events> events = (ArrayList<Events>) Events.populateCollection(sql, Events.class);
		if (languageId != Constants.LNG_IT)
		{
			sql = "SELECT a.*, b.description AS `title` " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
				 	 "        b.languageId = " + Constants.LNG_IT + " AND " +
					 "		  b.anchorZone = 0 " + 
				 	 whereClause;
			@SuppressWarnings("unchecked")
			ArrayList<Events> eventsIT = (ArrayList<Events>) Events.populateCollection(sql, Events.class);
			events.addAll(eventsIT);
		}
		if (events.size() == 0)
			return null;
		for(Events e : events)
		{
			if (e.minPrice != 0)
			{
				e.setImageURL(prop.getWebHost() + "/" + e.getImageURL());
			}
		}
		getTicketMaxAndMin(events);
		return(events.toArray(new Events[events.size()]));
	}

	public static Events[] findByFilter(String whereClause, String token) throws Exception
	{
		int languageId = SessionData.getInstance().getLanguage(token);
		return(findByFilter(whereClause, languageId));
	}

	@SuppressWarnings("unchecked")
	private static void getTicketMaxAndMin(ArrayList<Events> events)
	{
		String sql = null;
		for(Events e : events)
		{
			sql = "SELECT price " +
					  "FROM EventTickets c " + 
					  "WHERE c.booked < c.available AND " +
					  "      c.eventId = " + e.idEvents + " " +
					  "ORDER BY price ASC";
				ArrayList<EventTickets> tickets = null;
				try {
					tickets = (ArrayList<EventTickets>) EventTickets.populateCollection(sql, EventTickets.class);
				} 
				catch (Exception e1) {
					log.warn("Exception '" + e1.getMessage() + "' ");
				}
				if (tickets.size() != 0)
				{
					e.minPrice = tickets.get(0).price;
					e.maxPrice = tickets.get(tickets.size() - 1).price;
				}
				else
				{
					e.minPrice = 0;
					e.maxPrice = 0;
				}
		}
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

	public int getShipOwnerId() {
		return shipOwnerId;
	}

	public void setShipOwnerId(int shipOwnerId) {
		this.shipOwnerId = shipOwnerId;
	}

	public int getBoatId() {
		return boatId;
	}

	public void setBoatId(int boatId) {
		this.boatId = boatId;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean getEarlyBooking() {
		return earlyBooking;
	}

	public void setEarlyBooking(boolean earlyBooking) {
		this.earlyBooking = earlyBooking;
	}

	public boolean getLastMinute() {
		return lastMinute;
	}

	public void setLastMinute(boolean lastMinute) {
		this.lastMinute = lastMinute;
	}

	public boolean isHotEvent() {
		return hotEvent;
	}

	public void setHotEvent(boolean hotEvent) {
		this.hotEvent = hotEvent;
	}

	public String getEventRef() {
		return eventRef;
	}

	public void setEventRef(String eventRef) {
		this.eventRef = eventRef;
	}

	public String getAggregateKey() {
		return aggregateKey;
	}

	public void setAggregateKey(String aggregateKey) {
		this.aggregateKey = aggregateKey;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
}
