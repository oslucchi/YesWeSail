package com.yeswesail.rest.DBUtility;

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
					 "WHERE eventTicketId = " + id;
		this.populateObject(conn, sql, this);
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

}
