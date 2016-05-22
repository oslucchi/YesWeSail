package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

public class TicketsInCart 
{
	public class Tickets {
		private int idEventTickets;
		private int idTicketLocks;
		private int ticketType;
		private String ticketDescription;
		private int price;
		private int quantity;
	
		public int getIdEventTickets() {
			return idEventTickets;
		}
		public void setIdEventTickets(int idEventTickets) {
			this.idEventTickets = idEventTickets;
		}
		public int getTicketType() {
			return ticketType;
		}
		public void setTicketType(int ticketType) {
			this.ticketType = ticketType;
		}
		public String getTicketDescription() {
			return ticketDescription;
		}
		public void setTicketDescription(String ticketDescription) {
			this.ticketDescription = ticketDescription;
		}
		public int getPrice() {
			return price;
		}
		public void setPrice(int price) {
			this.price = price;
		}
		public int getQuantity() {
			return quantity;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
		public void increaseQuantity() {
			this.quantity++;
		}
		public int getIdTicketLocks() {
			return idTicketLocks;
		}
		public void setIdTicketLocks(int idTicketLocks) {
			this.idTicketLocks = idTicketLocks;
		}
		
	}
	private int idEvents;
	private Date dateStart;
	private Date dateEnd;
	private String title;
	private String location;
	private ArrayList<Tickets> tickets;


	public TicketsInCart() 
	{
		tickets = new ArrayList<Tickets>();
	}
	
	public int getIdEvents() {
		return idEvents;
	}

	public void setIdEvents(int idEvents) {
		this.idEvents = idEvents;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public ArrayList<Tickets> getTickets() {
		return tickets;
	}

	public void setTickets(ArrayList<Tickets> tickets) {
		this.tickets = tickets;
	}
}
