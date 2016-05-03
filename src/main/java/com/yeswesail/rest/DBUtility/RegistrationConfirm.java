package com.yeswesail.rest.DBUtility;

import java.util.Date;

public class RegistrationConfirm extends DBInterface
{	
	private static final long serialVersionUID = -7175860686398253282L;

	protected int idRegistrationConfirm;
	protected Date created;
	protected String token;
	protected int userId;
	protected String status;

	private void setNames()
	{
		tableName = "RegistrationConfirm";
		idColName = "idRegistrationConfirm";
	}

	public RegistrationConfirm() throws Exception
	{
		setNames();
	}

	public RegistrationConfirm(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public RegistrationConfirm(String email) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE email = '" + email + "'";
		this.populateObject(sql, this);
	}

	public void findActiveRecordByMail(String email) throws Exception
	{
		String sql = "SELECT * " +
				 "FROM " + tableName + " " +
				 "WHERE email = '" + email + "' AND " +
				 "      status = 'A'";
		this.populateObject(sql, this);
	}
	
	public void findActiveRecordByToken(String token) throws Exception
	{
		String sql = "SELECT * " +
				 "FROM " + tableName + " " +
				 "WHERE token = '" + token + "' AND " +
				 "      status = 'A'";
		this.populateObject(sql, this);
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
}
