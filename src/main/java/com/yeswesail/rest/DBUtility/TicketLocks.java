package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

public class TicketLocks extends DBInterface 
{
	private static final long serialVersionUID = 7805943614787085014L;

	protected int idTicketLocks;
	protected int eventTicketId;
	protected Date lockTime;
	protected int userId;
	protected String bookedTo;
	protected String status;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private void setNames()
	{
		tableName = "TicketLocks";
		idColName = "idTicketLocks";
	}

	public TicketLocks() throws Exception
	{
		setNames();
	}

	public TicketLocks(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE idTicketLocks = " + id;
		this.populateObject(conn, sql, this);
	}
	
	@SuppressWarnings("unchecked")
	public static TicketLocks[] findByUserId(DBConnection conn, int userId) throws Exception
	{
		ArrayList<TicketLocks> tickets = new ArrayList<>();
		String sql = "SELECT * " +
					 "FROM TicketLocks " +
					 "WHERE userId = " + userId;
		tickets = (ArrayList<TicketLocks>) populateCollection(sql, TicketLocks.class);
		return(tickets.toArray(new TicketLocks[tickets.size()]));
	}
	
	@SuppressWarnings("unchecked")
	public static TicketLocks[] findByEventId(int eventId) throws Exception
	{
		ArrayList<TicketLocks> tickets = new ArrayList<>();
		String sql = "SELECT a.* " +
					 "FROM TicketLocks a INNER JOIN EventTickets b ON " +
					 "     a.eventTicketId = b.idEventTickets " +
					 "WHERE b.eventId = " + eventId;
		tickets = (ArrayList<TicketLocks>) populateCollection(sql, TicketLocks.class);
		return(tickets.toArray(new TicketLocks[tickets.size()]));
	}
	
	public static TicketLocks findByEventTicketId(DBConnection conn, int eventTicketId) throws Exception
	{
		TicketLocks ticket = new TicketLocks();
		String sql = "SELECT * " +
					 "FROM TicketLocks " +
					 "WHERE eventTicketId = " + eventTicketId;
		ticket.populateObject(conn, sql, ticket);
		return ticket;
	}
	
	@SuppressWarnings("unchecked")
	public static TicketLocks[] findAll(boolean logStatement) throws Exception
	{
		ArrayList<TicketLocks> tickets = new ArrayList<>();
		String sql = "SELECT a.* " +
					 "FROM TicketLocks a INNER JOIN EventTickets b ON " +
					 "     a.eventTicketId = b.idEventTickets ";
		tickets = (ArrayList<TicketLocks>) populateCollection(sql, TicketLocks.class);
		return(tickets.toArray(new TicketLocks[tickets.size()]));
	}
	
	public int getIdTicketLocks() {
		return idTicketLocks;
	}

	public void setIdTicketLocks(int idTicketLocks) {
		this.idTicketLocks = idTicketLocks;
	}

	public int getEventTicketId() {
		return eventTicketId;
	}

	public void setEventTicketId(int eventTicketId) {
		this.eventTicketId = eventTicketId;
	}

	public Date getLockTime() {
		return lockTime;
	}

	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}

	public String getBookedTo() {
		return bookedTo;
	}

	public void setBookedTo(String bookedTo) {
		this.bookedTo = bookedTo;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public static void deleteTicketsForUser(DBConnection conn, int idUsers) throws Exception 
	{
		String sql = "DELETE FROM TicketLocks WHERE userId = " + idUsers;
		executeStatement(conn, sql, false);	
	}
}
