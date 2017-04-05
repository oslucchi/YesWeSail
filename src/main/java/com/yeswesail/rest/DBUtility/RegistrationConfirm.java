package com.yeswesail.rest.DBUtility;

import java.util.Date;

import com.yeswesail.rest.Constants;

public class RegistrationConfirm extends DBInterface
{	
	private static final long serialVersionUID = -7175860686398253282L;

	protected int idRegistrationConfirm;
	protected Date created;
	protected String token;
	protected int userId;
	protected String status;
	protected String passwordChange;

	private void setNames()
	{
		tableName = "RegistrationConfirm";
		idColName = "idRegistrationConfirm";
	}

	public RegistrationConfirm() throws Exception
	{
		setNames();
	}

	public RegistrationConfirm(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public RegistrationConfirm(DBConnection conn, String email) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE email = '" + email + "'";
		this.populateObject(conn, sql, this);
	}

	public void findActiveRecordByMail(DBConnection conn, String email) throws Exception
	{
		String sql = "SELECT * " +
					 "FROM RegistrationConfirm AS a JOIN Users AS b ON " +
					 "       a.userId = b.idUsers " +
					 "WHERE b.email = '" + email + "' AND " +
					 "      a.status = '" + Constants.STATUS_ACTIVE + "'";
		this.populateObject(conn, sql, this);
	}
	
	public void findActiveRecordByToken(DBConnection conn, String token) throws Exception
	{
		String sql = "SELECT * " +
				 "FROM " + tableName + " " +
				 "WHERE token = '" + token + "' AND " +
				 "      status = '" + Constants.STATUS_ACTIVE + "'";
		this.populateObject(conn, sql, this);
	}
	
	public RegistrationConfirm findActiveRecordByUserId(DBConnection conn, int id) throws Exception
	{
		String sql = "SELECT * " +
				 "FROM " + tableName + " " +
				 "WHERE userId = " + id + " AND " +
				 "      status = '" + Constants.STATUS_ACTIVE + "'";
		try
		{
			this.populateObject(conn, sql, this);
		}
		catch(Exception e)
		{
			if (e.getMessage().compareTo("No record found") == 0)
			{
				return null;
			}
			throw e;
		}
		return this;
	}
	
	public int getIdRegistrationConfirm() {
		return idRegistrationConfirm;
	}

	public Date getCreated() {
		return created;
	}

	public String getToken() {
		return token;
	}

	public int getUserId() {
		return userId;
	}

	public void setIdRegistrationConfirm(int idRegistrationConfirm) {
		this.idRegistrationConfirm = idRegistrationConfirm;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setToken(String token) {
		this.token = token;
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

	public String getPasswordChange() {
		return passwordChange;
	}

	public void setPasswordChange(String passwordChange) {
		this.passwordChange = passwordChange;
	}

}
