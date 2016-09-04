package com.yeswesail.rest.DBUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;

public class Cart extends DBInterface
{	
	private static final long serialVersionUID = 7529874266570588072L;
	private static final Logger log = Logger.getLogger(DBInterface.class);
	
	protected int idEvents;
	protected Date dateStart;
	protected Date dateEnd;
	protected String title;
	protected String location;
	protected int idEventTickets;
	protected int ticketType;
	protected String ticketDescription;
	protected int price;
	protected int quantity;
	protected int idTicketLocks;
	protected Date lockTime;
	private boolean toBuy;
	
	private void setNames()
	{
		tableName = "Events";
		idColName = "idEvents";
	}

	public Cart() throws Exception
	{
		setNames();
	}

	@SuppressWarnings("unchecked")
	public static TicketsInCart[] getCartItemsAlt(DBConnection conn, int userId, int languageId) throws Exception
	{
		String sql = 
				"SELECT DISTINCT idEvents, idTicketLocks, dateStart, dateEnd, " + 
				"      price, location, eventTicketId, d.description AS title, " +
				"      e.ticketType, e.description AS ticketDescription " +
				"FROM ((EventTickets a INNER JOIN TicketLocks b ON " +
				"        a.idEventTickets = b.eventTicketId) INNER JOIN " +
				"      (Events c INNER JOIN EventDescription d ON " +
				"        d.eventId = c.idEvents AND " +
				"        d.languageId = " + languageId + " AND " +
				"        d.anchorZone = 0) ON " +
				"      a.eventId = c.idEvents) " +
				"	INNER JOIN EventTicketsDescription e ON " +
				"	  e.ticketType = a.ticketType AND " +
				"	  e.languageId = " + languageId + " " +
				"WHERE b.userId = " + userId + " " +
				"ORDER BY idEvents, a.ticketType";
		
		log.debug("Querying cart. SQL:\n" + sql);
		ArrayList<Cart> carts = (ArrayList<Cart>) populateCollection(sql, Cart.class);

		log.trace("Transforming in Array of tickets in the format expected by clients");
		ArrayList<TicketsInCart> retList = new ArrayList<TicketsInCart>();
		int idEvents = -1;
		int i = -1;
		TicketsInCart cartItem = null;
		TicketsInCart.Tickets t = null;
		for(Cart cart : carts)
		{
			if (idEvents != cart.idEvents)
			{
				idEvents = cart.idEvents;
				cartItem = new TicketsInCart();
				cartItem.setIdEvents(cart.idEvents);
				cartItem.setDateStart(cart.dateStart);
				cartItem.setDateEnd(cart.dateEnd);
				cartItem.setLocation(cart.location);
				cartItem.setTitle(cart.title);
				cartItem.setTickets(new ArrayList<TicketsInCart.Tickets>());
				retList.add(cartItem);
				i++;
			}
			
			t = cartItem.new Tickets();
			t.setIdEventTickets(cart.idEventTickets);
			t.setPrice(cart.price);
			t.setQuantity(1);
			t.setTicketDescription(cart.ticketDescription);
			t.setTicketType(cart.ticketType);
			t.setToBuy(true);
			retList.get(i).getTickets().add(t);
		}
		return (retList.toArray(new TicketsInCart[retList.size()]));
	}

	@SuppressWarnings("unchecked")
	public static TicketsInCart[] getCartItems(DBConnection conn, int userId, int languageId) throws Exception
	{
		ApplicationProperties prop = ApplicationProperties.getInstance();
		String sql = 
				"SELECT DISTINCT idEvents, dateStart, dateEnd, location, description AS title " +
				"FROM ((EventTickets a INNER JOIN TicketLocks b ON " +
				"        a.idEventTickets = b.eventTicketId) INNER JOIN " +
				"      (Events c INNER JOIN EventDescription d ON " +
				"        d.eventId = c.idEvents AND " +
				"        d.languageId = " + languageId + " AND " +
				"        d.anchorZone = 0) ON " +
				"      a.eventId = c.idEvents) " +
				"WHERE b.userId = " + userId + " " +
				"ORDER BY idEvents, a.ticketType";
		ArrayList<Events> events = (ArrayList<Events>) populateCollection(sql, Events.class);
		ArrayList<TicketsInCart> retList = new ArrayList<TicketsInCart>();
		int i = -1;
		for(Events e : events)
		{
			sql = 	"SELECT idTicketLocks, idEventTickets, price, e.ticketType, " +
					"                e.description AS ticketDescription, lockTime " +
					"FROM (EventTickets a INNER JOIN TicketLocks b ON " +
					"        a.idEventTickets = b.eventTicketId AND " +
					"        b.userId = " + userId + ") " +
					"	INNER JOIN EventTicketsDescription e ON " +
					"	  e.ticketType = a.ticketType AND " +
					"	  e.languageId = " + languageId + " " +
					"WHERE a.eventId = " + e.getIdEvents() + " " +
					"ORDER BY a.eventId, a.ticketType, price";
			ArrayList<Cart> tickets = (ArrayList<Cart>) populateCollection(sql, Cart.class);
			TicketsInCart cartItem = null;
			cartItem= new TicketsInCart();
			cartItem.setIdEvents(e.idEvents);
			cartItem.setDateStart(e.dateStart);
			cartItem.setDateEnd(e.dateEnd);
			cartItem.setLocation(e.location);
			cartItem.setTitle(e.title);
			cartItem.setTickets(new ArrayList<TicketsInCart.Tickets>());
			retList.add(cartItem);
			i++;
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			for(Cart ticket : tickets)
			{
				TicketsInCart.Tickets t = cartItem.new Tickets();
				t.setIdEventTickets(ticket.idEventTickets);
				t.setPrice(ticket.price);
				t.setTicketDescription(ticket.ticketDescription);
				t.setTicketType(ticket.ticketType);
				t.setIdTicketLocks(ticket.idTicketLocks);
				t.setLockTimeDate(new Date(ticket.lockTime.getTime() + prop.getReleaseTicketLocksAfter() * 1000));
				t.setLockTime(format.format(t.getLockTimeDate()));
				// t.setLockTime(format.format(new Date(new Date().getTime() + prop.getReleaseTicketLocksAfter() * 1000)));
				t.setQuantity(1);
				t.setToBuy(true);
				retList.get(i).getTickets().add(t);
			}
		}		
		return (retList.toArray(new TicketsInCart[retList.size()]));
	}
	
	public int getIdEventTickets() {
		return idEventTickets;
	}

	public void setIdEventTickets(int idEventTickets) {
		this.idEventTickets = idEventTickets;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
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

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTicketType() {
		return ticketType;
	}

	public void setTicketType(int ticketType) {
		this.ticketType = ticketType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public boolean getToBuy()
	{
		return toBuy;
	}
	public void setToBuy(boolean toBuy)
	{
		this.toBuy = toBuy;
	}

	public Date getLockTime() {
		return lockTime;
	}

	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}
	
}
