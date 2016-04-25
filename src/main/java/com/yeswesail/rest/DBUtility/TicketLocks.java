package com.yeswesail.rest.DBUtility;

import java.util.Date;

public class TicketLocks extends DBInterface 
{
	private static final long serialVersionUID = 7805943614787085014L;

	protected int idTicketLocks;
	protected int eventTicketId;
	protected Date lockTime;
	protected String bookedTo;
	
	private void setNames()
	{
		tableName = this.getClass().getName();
		idColName = "id" + tableName;
	}

	public TicketLocks() throws Exception
	{
		setNames();
	}

	public TicketLocks(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
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

}
