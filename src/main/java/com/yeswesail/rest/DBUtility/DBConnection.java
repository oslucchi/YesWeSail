package com.yeswesail.rest.DBUtility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DBConnection 
{
    private DataSource ds = null;
    private ResultSet rs = null;
    private ResultSetMetaData rsm = null;
    private Statement st = null;
	private Connection conn = null;
	
	private static DBConnection singletonInstance = new DBConnection();
	
	public void getConnection() throws Exception  
	{
		if (conn != null)
		{
			try
			{
				st.execute("SELECT 1");
				return;
			} 
			catch (SQLException e) 
			{
				;
			}
			try 
			{
				finalize();
			}
			catch (Throwable e) 
			{
				;
			}
		}
		
    	String retVal = null;
 		Exception e1 = null;
		try
		{
			// ApplicationProperties prop = new ApplicationProperties();
			// String connectionURL = "jdbc:mysql://" + prop.getDbHost() + "/" + prop.getDbName();
			// conn = DriverManager.getConnection(connectionURL, prop.getDbUser(), prop.getDbPasswd());
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			ds = (DataSource) envCtx.lookup("jdbc/YesWeSail");
			conn = ds.getConnection();
			st = conn.createStatement();
		}
		catch (SQLException e) 
		{
			retVal = "Error on database connection (" + e.getMessage() + ")";
			e1 = e;
		}
		if (retVal != null)
		{
			try 
			{
				finalize();
			} 
			catch (Throwable e) 
			{
				// No action required
				;
			}
			throw new Exception(e1);
		}
	}
    
	protected void finalize() throws Throwable 
	{
		try 
		{
			ds = null;
			rsm = null;
			if (rs != null)
			{
				rs.close();
				rs = null;
			}
			if (st != null)
			{
				st.close();
				st = null;
			}
			if(conn != null)
			{
				conn.close();
				conn = null;
			}
		}
		catch(Exception e)
		{
			;
		}
	}
	
	public void executeQuery(String sql) throws Exception
	{
		StringTokenizer stok = new StringTokenizer(sql);
		String queryType = "";
		if (stok != null)
		{
			queryType = stok.nextToken().toUpperCase();
		}

		if (st == null)
		{
			try 
			{
				finalize();
				getConnection();
			} 
			catch(Exception e)
			{
				throw e;
			}
			catch (Throwable e) 
			{
				// No action required
				throw new Exception(e);
			}
		}
		try
		{
			if ((queryType.compareTo("INSERT") == 0) ||
				(queryType.compareTo("DELETE") == 0) ||
				(queryType.compareTo("UPDATE") == 0))
			{
				st.execute(sql);
			}
			else
			{
				rs = st.executeQuery(sql);
				rsm = rs.getMetaData();
			}
		}
		catch(Exception e)
		{
			throw new Exception(e);
		}
	}

	public ResultSetMetaData getRsm() {
		return rsm;
	}

	public ResultSet getRs() {
		return rs;
	}

	public Statement getSt()
	{
		return st;
	}
	
	private DBConnection()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} 
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	public static DBConnection getInstance() throws Exception
	{
		if ((singletonInstance.conn == null) || 
			(singletonInstance.ds == null) ||
			(singletonInstance.st == null))
		{
			try 
			{
				singletonInstance.finalize();
			}
			catch (Throwable e) 
			{
				// no actions
				;
			}
			singletonInstance = new DBConnection();
		}
		
		singletonInstance.getConnection();
		return singletonInstance;
	}
}
