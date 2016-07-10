package com.yeswesail.rest.DBUtility;

public class DocumentTypes extends DBInterface 
{
	final public static int SAILING_LICENSE = 1;
	final public static int NAVIGATION_CERTIFICATE = 2;
	final public static int RTF_LICENSE = 3;
	final public static int INSURANCE = 4;
	
	private static final long serialVersionUID = 1878310932212266623L;

	protected int idDocumentTypes;
	protected String name;

	private void setNames()
	{
		tableName = "DocumentTypes";
		idColName = "idDocumentTypes";
	}

	public DocumentTypes() throws Exception
	{
		setNames();
	}

	public DocumentTypes(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public int getIdDocumentTypes() {
		return idDocumentTypes;
	}

	public void setIdDocumentsType(int idDocumentsType) {
		this.idDocumentTypes = idDocumentsType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
