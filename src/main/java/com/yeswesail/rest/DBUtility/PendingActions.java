package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

public class PendingActions extends DBInterface 
{
	final public static String STATUS_UPGRADE = "statusUpgrade";
	final public static String REVIEW = "review";
	final public static String TICKETS_BUY = "ticketsBuy";
	
	private static final long serialVersionUID = 5253990869677923299L;

	protected int idPendingActions;
	protected int userId;
	protected String actionType;
	protected String link;
	protected Date created;
	protected String status;
	protected Date updated;

	private void setNames()
	{
		tableName = "PendingActions";
		idColName = "idPendingActions";
	}

	public PendingActions() throws Exception
	{
		setNames();
	}

	public PendingActions(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public static PendingActions[] getActives(DBConnection conn) throws Exception
	{
		String sql = "SELECT * " +
				 "FROM PendingActions " +
				 "WHERE status != 'C'";
		@SuppressWarnings("unchecked")
		ArrayList<PendingActions> actions = 
				(ArrayList<PendingActions>) populateCollection(sql, PendingActions.class);
		return actions.toArray(new PendingActions[actions.size()]);
	}
	
	public static PendingActions[] getPendingOnUser(DBConnection conn, int userId) throws Exception
	{
		String sql = "SELECT * " +
					 "FROM PendingActions " +
					 "WHERE status != 'C' AND " +
					 "      userId = " + userId;
		@SuppressWarnings("unchecked")
		ArrayList<PendingActions> actions = 
				(ArrayList<PendingActions>) populateCollection(sql, PendingActions.class);
		return actions.toArray(new PendingActions[actions.size()]);
	}

	public int getIdPendingActions() {
		return idPendingActions;
	}

	public void setIdPendingActions(int idPendingActions) {
		this.idPendingActions = idPendingActions;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

}
