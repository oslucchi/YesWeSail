package com.yeswesail.rest.DBUtility;

import java.util.Date;

import com.yeswesail.rest.Constants;

public class DynamicPages extends DBInterface {
	private static final long serialVersionUID = 4904338993478927413L;
	protected int idDynamicPages;
	protected String URLReference;
	protected Date createdOn;
	protected String status;
	protected String innerHTML;
	protected int languageId;

	private void setNames()
	{
		tableName = "DynamicPages";
		idColName = "idDynamicPages";
	}

	public DynamicPages() 
	{
		setNames();
	}
	public DynamicPages(DBConnection conn, String URLReference, int languageId) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE URLReference = '" + URLReference + "' AND " +
					 "      languageId = " + languageId + " AND " +
					 "      status = '" + Constants.STATUS_ACTIVE + "'";
		populateObject(conn, sql, this);
	}

	public int getIdDynamicPages() {
		return idDynamicPages;
	}

	public void setIdDynamicPages(int idDynamicPages) {
		this.idDynamicPages = idDynamicPages;
	}

	public String getURLReference() {
		return URLReference;
	}

	public void setURLReference(String uRLReference) {
		URLReference = uRLReference;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInnerHTML() {
		return innerHTML;
	}

	public void setInnerHTML(String innerHTML) {
		this.innerHTML = innerHTML;
	}

	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}	
}
