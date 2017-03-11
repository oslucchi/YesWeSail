package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

public class Documents extends DBInterface 
{
	private static final long serialVersionUID = 1878310932212266623L;

	protected int idDocuments;
	protected int documentTypesId;
	protected String number;
	protected int userId;
	protected String name;
	protected ArrayList<String> images;

	private static ArrayList<Documents> docs;
	
	private void setNames()
	{
		tableName = "Documents";
		idColName = "idDocuments";
	}

	public Documents() throws Exception
	{
		setNames();
	}

	public Documents(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static Documents[] findAllUsersDoc(int languageId, int userId) throws Exception
	{
		String sql = "SELECT a.*, b.name " +
				 	 "FROM Documents a INNER JOIN DocumentTypes b ON " +
				 	 "     b.idDocumentTypes = a.documentTypesId " + 
				 	 "WHERE userId = " + userId;
		
		docs = (ArrayList<Documents>) populateCollection(sql, Documents.class);
		return(docs.toArray(new Documents[docs.size()]));
	}
	public ArrayList<String> getImages()
	{
		return images;
	}

	public int getIdDocuments() {
		return idDocuments;
	}

	public void setIdDocuments(int idDocuments) {
		this.idDocuments = idDocuments;
	}

	public int getDocumentTypesId() {
		return documentTypesId;
	}

	public void setDocumentTypesId(int documentTypesId) {
		this.documentTypesId = documentTypesId;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public static ArrayList<Documents> getDocs() {
		return docs;
	}

	public static void setDocs(ArrayList<Documents> docs) {
		Documents.docs = docs;
	}

	public void setImages(ArrayList<String> images) {
		this.images = images;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
