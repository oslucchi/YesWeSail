package com.yeswesail.rest.DBUtility;

public class Roles extends DBInterface {
	public static final int TRAVELLER = 1;
	public static final int SHIP_OWNER = 2;
	public static final int ADMINISTRATOR = 3;
	
	private static final long serialVersionUID = 188496684307289805L;

	protected int roleId;
	protected int languageId;
	protected String description;

	private void setNames()
	{
		tableName = "RolesLanguages";
		idColName = "";
	}

	public Roles() throws Exception
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
