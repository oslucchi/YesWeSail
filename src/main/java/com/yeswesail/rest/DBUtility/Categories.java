package com.yeswesail.rest.DBUtility;

public class Categories extends DBInterface 
{
	private static final long serialVersionUID = 7805943614787085014L;

	protected int idCategories;
	protected String description;
	
	private void setNames()
	{
		tableName = "Categories";
		idColName = "idCategories";
	}

	public Categories() throws Exception
	{
		setNames();
	}

	public Categories(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public int getIdCategories() {
		return idCategories;
	}

	public void setIdCategories(int idCategories) {
		this.idCategories = idCategories;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
