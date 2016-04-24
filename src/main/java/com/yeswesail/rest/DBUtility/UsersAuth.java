package com.yeswesail.rest.DBUtility;

import java.util.Date;

public class UsersAuth extends DBInterface 
{
	private static final long serialVersionUID = -1169864911732409989L;

	protected int idUsersAuth;
	protected int userId;
	protected Date created;
	protected Date lastRefreshed;
	protected String token;

	private void setNames()
	{
		tableName = "UsersAuth";
		idColName = "idUsersAuth";
	}

	public UsersAuth() throws Exception
	{
		setNames();
	}

	public UsersAuth(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public static UsersAuth findToken(String token) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM UsersAuth " +
				 	 "WHERE  token = '" + token + "'";
		UsersAuth ua = (UsersAuth) UsersAuth.populateByQuery(sql, UsersAuth.class);
		return(ua);
	}
	
	public static UsersAuth findUserId(int userId) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM UsersAuth " +
				 	 "WHERE userId = " + userId;
		UsersAuth ua = null;
		try
		{
			ua = (UsersAuth) UsersAuth.populateByQuery(sql, UsersAuth.class);
		}
		catch(Exception e)
		{
			if (e.getMessage().compareTo("No record found") != 0)
			{
				return null;
			}
		}
		return(ua);
	}

	public int getIdUsersAuth() {
		return idUsersAuth;
	}

	public void setIdUsersAuth(int idUsersAuth) {
		this.idUsersAuth = idUsersAuth;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastRefreshed() {
		return lastRefreshed;
	}

	public void setLastRefreshed(Date lastRefreshed) {
		this.lastRefreshed = lastRefreshed;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
