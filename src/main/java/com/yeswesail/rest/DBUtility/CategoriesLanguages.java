package com.yeswesail.rest.DBUtility;

public class CategoriesLanguages extends DBInterface
{
	private static final long serialVersionUID = -2207431265593111520L;

	protected int categoryId;
	protected int languageId;
	protected String description;
	
	private void setNames()
	{
		tableName = "Categories";
		idColName = "";
	}

	public CategoriesLanguages() throws Exception
	{
		setNames();
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


}
