package com.yeswesail.rest.DBUtility;

public class PasswordHandler extends DBInterface {
	private static final long serialVersionUID = -307149576390277583L;
	
	protected int idUsers;
	protected String password;

	private void setNames()
	{
		tableName = "Users";
		idColName = "idUsers";
	}

	public PasswordHandler() throws Exception
	{
		setNames();
	}

	public String userPassword(DBConnection conn, int idUsers) 
	{
		setNames();
		String sql = "SELECT password FROM Users WHERE idUsers = " + idUsers;
		try 
		{
			populateObject(conn, sql, this);
		}
		catch (Exception e) {
			return null;
		}
		return password;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void updatePassword(DBConnection conn, boolean inTransaction) throws Exception
	{
		String sql = "UPDATE Users SET  password = '" + password + "' WHERE idUsers = " + idUsers;
		executeStatement(conn, sql, inTransaction);
	}

	public int getIdUsers() {
		return idUsers;
	}

	public void setIdUsers(int idUsers) {
		this.idUsers = idUsers;
	}
	
	
}
