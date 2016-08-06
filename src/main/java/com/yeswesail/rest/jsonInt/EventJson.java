package com.yeswesail.rest.jsonInt;


public class EventJson {
	public int categoryId;
	public int eventId;
	public int idEvents;
	public int shipOwnerId;
	public int boatId;
	public int eventType;
	public String status;
	public String dateStart;
	public String dateEnd;
	public String location;
	public String title;
	public EventDescriptionJson description;
	public EventDescriptionJson logistics;
	public EventDescriptionJson includes;
	public EventDescriptionJson excludes;
	public String notes;
	public String imageURL;
	public String[] labels;
	public String aggregateKey;
	public boolean earlyBooking;
	public boolean lastMinute;
	public boolean hotEvent;
	public UsersJson[] participants;
	public TicketJson[][] tickets;
	public boolean activeOnly;
	public EventRouteJson[] route;
}
