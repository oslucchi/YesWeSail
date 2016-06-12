package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

public class CategoriesLanguages extends DBInterface
{
	private static final long serialVersionUID = -2207431265593111520L;

	protected int categoryId;
	protected int languageId;
	protected String description;
	protected String url;
	
	private void setNames()
	{
		tableName = "CategoriesLanguages";
		idColName = "";
	}

	public CategoriesLanguages() throws Exception
	{
		setNames();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Object> getAll(int languageId) throws Exception
	{
		String sql = "SELECT a.*, b.url " +
					 "FROM CategoriesLanguages a INNER JOIN Categories b ON " +
					 "     a.categoryId = b.idCategories AND " +
					 "     a.languageId = " + languageId;
		return (ArrayList<Object>) populateCollection(sql, CategoriesLanguages.class);
	}
	
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
