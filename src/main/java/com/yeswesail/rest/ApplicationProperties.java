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
	private String redirectRegistrationCompleted = "";
	private String redirectHome	= "";
	private String redirectOnLogin = "";
	private String fbApplicationId = "";
	private String fbApplicationSecret = "";
	private int sessionExpireTime = 0;
	private int maxNumHotOffers = 4;
	
	private static ApplicationProperties instance = null;
	
	final Logger log = Logger.getLogger(this.getClass());
	
	public static ApplicationProperties getInstance()
	{
		if (instance == null)
		{
			instance = new ApplicationProperties();
		}
		return(instance);
	}
	
	private ApplicationProperties()
	{
		String variable = "";
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
    	redirectRegistrationCompleted = properties.getProperty("redirectRegistrationCompleted");
    	redirectHome = properties.getProperty("redirectHome");
    	redirectOnLogin = properties.getProperty("redirectOnLogin");
    	fbApplicationId = properties.getProperty("fbApplicationId");
    	fbApplicationSecret = properties.getProperty("fbApplicationSecret");
    	try
    	{
    		variable = "sessionExpireTime";
    		sessionExpireTime = Integer.parseInt(properties.getProperty("sessionExpireTime"));
    		variable = "maxNumHotOffers";
    		maxNumHotOffers = Integer.parseInt(properties.getProperty("maxNumHotOffers"));
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")");
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

	public String getRedirectRegistrationCompleted() {
		return redirectRegistrationCompleted;
	}

	public String getRedirectHome() {
		return redirectHome;
	}

	public String getRedirectOnLogin() {
		return redirectOnLogin;
	}

	public int getMaxNumHotOffers() {
		return maxNumHotOffers;
	}

	public String getFbApplicationId() {
		return fbApplicationId;
	}

	public String getFbApplicationSecret() {
		return fbApplicationSecret;
	}		

}
