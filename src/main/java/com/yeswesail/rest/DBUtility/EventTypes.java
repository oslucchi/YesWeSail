package com.yeswesail.rest.DBUtility;

public class EventTypes extends DBInterface 
{
	private static final long serialVersionUID = 7805943614787085014L;

	protected int idEventTypes;
	protected int languageId;
	protected String description;
	
	private void setNames()
	{
		tableName = "EventTypes";
		idColName = "idEventTypes";
	}

	public EventTypes() throws Exception
	{
		setNames();
	}

	public EventTypes(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public int getIdEventTypes() {
		return idEventTypes;
	}

	public void setIdEventTypes(int idEventTypes) {
		this.idEventTypes = idEventTypes;
	}

	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
