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

import org.apache.log4j.Logger;

public class DBConnection 
{
	final Logger log = Logger.getLogger(this.getClass());
	private DataSource ds = null;
    private ResultSet rs = null;
    private ResultSetMetaData rsm = null;
    private Statement st = null;
	private Connection conn = null;
	
	public DBConnection() throws Exception  
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
			// log.debug("Creted connection (" + conn + ") and statement (" + st + ")");
			st.executeQuery("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
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
    
	protected void finalize() 
	{
		// log.debug("Closing resources");
		try 
		{
			ds = null;
			if (rs != null)
			{
				rs.close();
			}
			if (st != null)
			{
				st.close();
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
	
	public void executeQuery(String sql, boolean logStatement) throws Exception
	{
		if (logStatement) 
		{
			log.debug("executing query '"  + sql + "'");
		}

		StringTokenizer stok = new StringTokenizer(sql);
		String queryType = "";
		if (stok != null)
		{
			queryType = stok.nextToken().toUpperCase();
		}
		try
		{
			if ((queryType.compareTo("INSERT") == 0) ||
				(queryType.compareTo("DELETE") == 0) ||
				(queryType.compareTo("UPDATE") == 0) ||
				(queryType.compareTo("START") == 0) ||
				(queryType.compareTo("COMMIT") == 0) ||
				(queryType.compareTo("ROLLBACK") == 0))
			{
				st.execute(sql);
			}
			else
			{
				rs = st.executeQuery(sql);
				rsm = rs.getMetaData();
				if (logStatement) 
				{
					if ((rs.getType() == ResultSet.TYPE_SCROLL_INSENSITIVE) &&
						(rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY))
					{
						rs.last();
						log.debug("Done. Retrived " + rs.getRow() + " rows");
						rs.beforeFirst();
					}
				}
			}
		}
		catch(Exception e)
		{
			log.warn("Exception " + e.getMessage() + " catched on statemet '" + sql + "'");		
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
}
