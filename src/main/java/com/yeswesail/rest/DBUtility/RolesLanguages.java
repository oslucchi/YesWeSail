package com.yeswesail.rest.DBUtility;

public class RolesLanguages extends DBInterface
{
	private static final long serialVersionUID = -2207431265593111520L;

	protected int roleId;
	protected int languageId;
	protected String description;
	
	private void setNames()
	{
		tableName = "RolesLanguages";
		idColName = "";
	}

	public RolesLanguages() throws Exception
	{
		setNames();
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
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
