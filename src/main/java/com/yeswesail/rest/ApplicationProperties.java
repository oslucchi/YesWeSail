package com.yeswesail.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ApplicationProperties {
	private String dbUser = "";
	private String dbPasswd = "";
	private String dbHost = "";
	private String dbName = "";
	private String mailSmtpHost = "";
	private String mailFrom = "";
	private String mailUser = "";
	private String mailPassword = "";
	private String webHost = "";
	private String webHome = "";
	private String redirectUserRegistered = "";
	private String redirectHome	= "";
	private int sessionExpireTime = 0;
	
	final Logger log = Logger.getLogger(this.getClass());
	
	public ApplicationProperties()
	{
		log.trace("ApplicationProperties start");
		Properties properties = new Properties();
    	try 
    	{
        	InputStream in = ApplicationProperties.class.getResourceAsStream("/package.properties");
        	
			properties.load(in);
	    	in.close();
		}
    	catch (IOException e) 
    	{
			e.printStackTrace();
    		return;
		}
       	dbUser = properties.getProperty("dbUser");
    	dbPasswd = properties.getProperty("dbPasswd");
    	dbHost = properties.getProperty("dbHost");
    	dbName = properties.getProperty("dbName");
    	mailSmtpHost = properties.getProperty("mailSmtpHost");
    	mailFrom = properties.getProperty("mailFrom");
    	mailUser = properties.getProperty("mailUser");
    	mailPassword = properties.getProperty("mailPassword");
    	webHost = properties.getProperty("webHost");
    	webHome = properties.getProperty("webHome");
    	redirectUserRegistered = properties.getProperty("redirectUserRegistered");
    	redirectHome = properties.getProperty("redirectHome");
    	try
    	{
    		sessionExpireTime = Integer.parseInt(properties.getProperty("sessionExpireTime"));
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable 'sessionExpireTime' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")");
    		sessionExpireTime = 0;
    	}
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPasswd() {
		return dbPasswd;
	}

	public String getDbHost() {
		return dbHost;
	}

	public String getDbName() {
		return dbName;
	}

	public String getMailSmtpHost() {
		return mailSmtpHost;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public String getMailUser() {
		return mailUser;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public String getWebHost() {
		return webHost;
	}

	public int getSessionExpireTime() {
		return sessionExpireTime;
	}

	public String getWebHome() {
		return webHome;
	}

	public String getRedirectUserRegistered() {
		return redirectUserRegistered;
	}

	public String getRedirectHome() {
		return redirectHome;
	}		
}
