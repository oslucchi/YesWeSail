package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.SessionData;

public class Events extends DBInterface implements Comparable<Events>
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
	protected int languageId;
	protected String locale;
	protected int backgroundOffsetX;
	protected int backgroundOffsetY;

	
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
		getEvents(conn, id);
	}
	
	public Events(DBConnection conn, int id, int languageId) throws Exception
	{
		getEvents(conn, id, languageId);
	}

	public Events(DBConnection conn, int id, int languageId, boolean activeOnly) throws Exception
	{
		getEvents(conn, id, languageId, activeOnly);
	}
	
	public void getEvents(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}
	
	public void getEvents(DBConnection conn, int id, int languageId) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description AS `title`, b.languageId " +
					 "FROM Events AS a LEFT OUTER JOIN EventDescription AS b " +
					 "     ON a.idEvents = b.eventId AND " +
					 "		  b.anchorZone = 0 ";
				 
		String whereClause = 
					 "WHERE idEvents = " + id + " AND " +
			 	 	 "      b.languageId = " + languageId + " AND " +
				 	 "      status = '" + Constants.STATUS_ACTIVE + "'";

		String fallbackWhereClause = 
					 "WHERE idEvents = " + id + " AND " +
				 	 "      b.languageId = " + Constants.getAlternativeLanguage(languageId) + " AND " +
				 	 "      status = '" + Constants.STATUS_ACTIVE + "'";
		getSingleEventWithLanguageFallbackPolicy(conn, sql, whereClause, fallbackWhereClause);
		getTicketMaxAndMin(events);
	}

	public void getEvents(DBConnection conn, int id, int languageId, boolean activeOnly) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, b.description AS `title`, b.languageId " +
					 "FROM Events AS a LEFT OUTER JOIN EventDescription AS b " +
					 "     ON a.idEvents = b.eventId AND " +
					 "		  b.anchorZone = 0 ";
				 
		String whereClause = 
					 "WHERE idEvents = " + id + " AND " +
			 	 	 "      b.languageId = " + languageId;

		String fallbackWhereClause = 
					 "WHERE idEvents = " + id + " AND " +
				 	 "      b.languageId = " + Constants.getAlternativeLanguage(languageId);
		if (activeOnly)
		{			
			whereClause += " AND status = '" + Constants.STATUS_ACTIVE + "'";
			fallbackWhereClause += " AND status = '" + Constants.STATUS_ACTIVE + "'";
		}
		getSingleEventWithLanguageFallbackPolicy(conn, sql, whereClause, fallbackWhereClause);
		getTicketMaxAndMin(events);
	}

	private void getSingleEventWithLanguageFallbackPolicy(
		DBConnection conn, String sql, String whereClause, String fallbackWhereClause
				) throws Exception
	{
		try
		{
			this.populateObject(conn, sql + whereClause, this);
		}
		catch(Exception e)
		{
			if (e.getMessage().compareTo("No record found") != 0)
			{
				throw e;
			}
			this.populateObject(conn, sql + fallbackWhereClause, this);
		}
		
		setLocale(Constants.getLocale(this.getLanguageId()));
		events = new ArrayList<Events>();
		events.add(this);
	}
	
	public static ArrayList<Events> sort(ArrayList<Events> unsorted)
	{
		Collections.sort(unsorted, new Comparator<Events>() {
	        @Override 
	        public int compare(Events e1, Events e2) {
	            return (int) (e1.dateStart.getTime() - e2.dateStart.getTime()); // Ascending
	        }
	    });
		return unsorted;
	}

	public static Events[] sort(Events[] unsorted)
	{
	    Arrays.sort(unsorted, new Comparator<Events>() {
	        @Override 
	        public int compare(Events e1, Events e2) {
	            return (int) (e1.dateStart.getTime() - e2.dateStart.getTime()); // Ascending
	        }
	    });
		return unsorted;
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<Events> 
		getEventsListWithLanguageFallbackPolicy(String sql, String whereClause, int languageId) throws Exception
	{
		whereClause = whereClause.trim().toUpperCase().startsWith("WHERE") ?
							whereClause.trim().substring(6) : whereClause.trim();
		String where = "WHERE " + 
					   "      b.languageId = " + languageId + 
					   (whereClause.isEmpty() || whereClause.toUpperCase().startsWith("ORDER") ? " " : " AND ") + whereClause;
		log.trace("Getting events in the primary language requested");
		ArrayList<Events> events = (ArrayList<Events>) Events.populateCollection(sql + where, Events.class);
		String sep = "";
		String eventIds = "";
		if (!events.isEmpty())
		{
			for(Events e : events)
			{
				eventIds += sep + e.getIdEvents();
				sep = ",";
			}
			eventIds = "a.idEvents NOT IN (" + eventIds + ") AND ";
		}
		where = "WHERE " + eventIds +
				"      b.languageId = " + Constants.getAlternativeLanguage(languageId) +
				   (whereClause.isEmpty() || whereClause.toUpperCase().startsWith("ORDER") ? " " : " AND ") + whereClause;
		log.trace("Getting other events in alternative language");
		ArrayList<Events> fallbackEvents = (ArrayList<Events>) Events.populateCollection(sql + where, Events.class);
		events.addAll(fallbackEvents);
		for(Events event : events)
		{
			event.setLocale(Constants.getLocale(event.getLanguageId()));
		}
		return(sort(events));
	}
	
	public static Events[] findHot(int languageId) throws Exception
	{
		String sql = "SELECT a.*, b.description AS `title`, b.languageId " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
					 "		  b.anchorZone = 0 ";
		String whereClause = "WHERE   a.status = '" + Constants.STATUS_ACTIVE + "' AND " +
							 "        a.dateStart > NOW() AND " +
							 "        a.hotEvent = 1 " +
						 	 "ORDER BY dateStart ";
		
		ArrayList<Events> events = getEventsListWithLanguageFallbackPolicy(sql, whereClause, languageId);
		getTicketMaxAndMin(events);
		
		log.trace("Event and tickets retireved. Set locale and prepare return list accordingly with limits");
		ArrayList<Events> retList = new ArrayList<Events>();
		for(Events e : events)
		{
			if (e.minPrice != 0)
			{
				e.setLocale(Constants.getLocale(e.getLanguageId()));
				retList.add(e);
			}
			if ((prop.getMaxNumHotOffers() != 0) && (retList.size() == prop.getMaxNumHotOffers()))
			{
				break;
			}
		}
		log.trace("Returning finalized list to caller. List size = " + retList.size());
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
		String sql = "SELECT a.*, b.description AS `title`, b.languageId " +
				 	 "FROM Events AS a INNER JOIN EventDescription AS b " +
				 	 "     ON a.idEvents = b.eventId AND " +
					 "		  b.anchorZone = 0 ";
		
		ArrayList<Events> events = getEventsListWithLanguageFallbackPolicy(sql, whereClause, languageId);
		if (events.size() == 0)
			return null;
		getTicketMaxAndMin(events);
		
		for(Events event : events)
		{
			event.setLocale(Constants.getLocale(event.getLanguageId()));
		}
		Collections.sort(events, new Comparator<Events>() {
	        @Override 
	        public int compare(Events e1, Events e2) {
	            if (e1.dateStart.getTime() >  e2.dateStart.getTime())
	            {
	            	return 1;
	            }
	            else if (e1.dateStart.getTime() ==  e2.dateStart.getTime())
	            {
	            	return 0;
	            }
	            else
	            {
	            	return -1;
	            }
	        }
	    });
		// return(sort(events.toArray(new Events[events.size()])));
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
		if ((events == null) || (events.size() == 0))
			return;
		
		String sql = null;
		String eventsList = "";
		String sep = "";
		for(Events e : events)
		{
			eventsList += sep + e.getIdEvents();
			sep = ",";
		}
		sql = "SELECT DISTINCT eventId as idEvents, MAX(price) as maxPrice, MIN(PRICE) as minPrice " +
			  "FROM EventTickets c " + 
			  "WHERE c.booked < c.available AND " +
			  "      c.eventId IN (" + eventsList + ") AND " +
			  "      c.ticketType != " + EventTickets.WHOLE_BOAT + " " +
			  "GROUP BY eventId " +
			  "ORDER BY eventId";
		ArrayList<Events> tickets = null;
		try {
			tickets = (ArrayList<Events>) Events.populateCollection(sql, Events.class);
		} 
		catch (Exception e1) {
			log.warn("Exception '" + e1.getMessage() + "' ");
		}
		for(Events e : events)
		{
			e.minPrice = 0;
			e.maxPrice = 0;
			for(Events ePrice : tickets)
			{
				if (e.idEvents == ePrice.idEvents)
				{
					e.minPrice = ePrice.minPrice;
					e.maxPrice = ePrice.maxPrice;
					tickets.remove(ePrice);
					break;
				}
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
		if (imageURL == null)
		{
			return prop.getWebHost() + "/" + "defaultEvent.png";
		}
		else if (imageURL.startsWith("http"))
		{
			return imageURL;
		}
		else
		{
			return prop.getWebHost() + "/" + imageURL;
		}
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL.replaceAll(prop.getWebHost() + "/",  "");
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

	@Override
	public int compareTo(Events o) {
		return (int) (this.dateStart.getTime() - o.dateStart.getTime());
	}

	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public int getBackgroundOffsetX() {
		return backgroundOffsetX;
	}

	public void setBackgroundOffsetX(int backgroundOffsetX) {
		this.backgroundOffsetX = backgroundOffsetX;
	}

	public int getBackgroundOffsetY() {
		return backgroundOffsetY;
	}

	public void setBackgroundOffsetY(int backgroundOffsetY) {
		this.backgroundOffsetY = backgroundOffsetY;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
}
