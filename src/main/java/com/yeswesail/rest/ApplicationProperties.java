package com.yeswesail.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class ApplicationProperties {
	// site specific
	private String webHome = "";
	private String redirectRegistrationCompleted = "";
	private String redirectHome	= "";
	private String redirectOnLogin = "";
	private int sessionExpireTime = 0;
	private int maxNumHotOffers = 4;
	private String defaultLang = "";
	private String noAuthorizationRequired = "";
	private String noAuthorizationRequiredRoot = "";
	private boolean useCoars = false;
	private Double maxDistanceForEventsOnTheGround = 5.0;

	// package properties
	private String mailSmtpHost = "";
	private String mailFrom = "";
	private String mailUser = "";
	private String mailPassword = "";
	private String webHost = "";
	private boolean startReleaser = true;
	private int releaseTicketLocksAfter = 600;
	private String adminEmail;
	private String fbApplicationId = "";
	private String fbApplicationSecret = "";
	private String paypalClientId;
	private String paypalClientSecret;
	private String mailchimpURL = "";
	private String mailchimpListId = "";
	private String mailchimpAPIKEY = "";

	private ServletContext context;
	
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
    	
    	webHome = properties.getProperty("webHome");
    	redirectRegistrationCompleted = properties.getProperty("redirectRegistrationCompleted");
    	redirectHome = properties.getProperty("redirectHome");
    	redirectOnLogin = properties.getProperty("redirectOnLogin");
    	defaultLang = properties.getProperty("defaultLang");
    	noAuthorizationRequired = properties.getProperty("noAuthorizationRequired");
    	noAuthorizationRequiredRoot = properties.getProperty("noAuthorizationRequiredRoot");
		useCoars = Boolean.parseBoolean(properties.getProperty("useCoars"));
    	try
    	{
    		variable = "sessionExpireTime";
    		sessionExpireTime = Integer.parseInt(properties.getProperty("sessionExpireTime"));
    		variable = "maxNumHotOffers";
    		maxNumHotOffers = Integer.parseInt(properties.getProperty("maxNumHotOffers"));
    		variable = "maxDistanceForEventsOnTheGround";
    		maxDistanceForEventsOnTheGround = Double.parseDouble(properties.getProperty("maxDistanceForEventsOnTheGround"));
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")");
    	}

    	String envConf = System.getProperty("envConf");
    	try 
    	{
    		properties = new Properties();
        	InputStream in = ApplicationProperties.class.getResourceAsStream(
        							"/site." + (envConf == null ? "dev" : envConf) + ".properties");        	
			properties.load(in);
	    	in.close();
		}
    	catch (IOException e) 
    	{
			e.printStackTrace();
    		return;
		}
    	mailSmtpHost = properties.getProperty("mailSmtpHost");
    	mailFrom = properties.getProperty("mailFrom");
    	mailUser = properties.getProperty("mailUser");
    	mailPassword = properties.getProperty("mailPassword");
    	webHost = properties.getProperty("webHost");
		startReleaser = Boolean.parseBoolean(properties.getProperty("startReleaser"));
		adminEmail = properties.getProperty("adminEmail");
    	fbApplicationId = properties.getProperty("fbApplicationId");
    	fbApplicationSecret = properties.getProperty("fbApplicationSecret");
		paypalClientId = properties.getProperty("paypalClientId");
		paypalClientSecret = properties.getProperty("paypalClientSecret");
		mailchimpURL = properties.getProperty("mailchimpURL");
		mailchimpListId = properties.getProperty("mailchimpListId");
		mailchimpAPIKEY = properties.getProperty("mailchimpAPIKEY");
    	try
    	{
    		variable = "releaseTicketLocksAfter";
    		releaseTicketLocksAfter = Integer.parseInt(properties.getProperty("releaseTicketLocksAfter"));
    	}
    	catch(NumberFormatException e)
    	{
    		log.error("The format for the variable '" + variable + "' is incorrect (" +
    					 properties.getProperty("sessionExpireTime") + ")");
    	}		
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

	public String getDefaultLang() {
		return defaultLang;
	}

	public String getNoAuthorizationRequired() {
		return noAuthorizationRequired;
	}

	public boolean isUseCoars() {
		return useCoars;
	}

	public String getNoAuthorizationRequiredRoot() {
		return noAuthorizationRequiredRoot;
	}

	public ServletContext getContext() {
		return context;
	}		

	public void setContext(ServletContext context) {
		this.context = context;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public int getReleaseTicketLocksAfter() {
		return releaseTicketLocksAfter;
	}

	public String getPaypalClientId() {
		return paypalClientId;
	}

	public String getPaypalClientSecret() {
		return paypalClientSecret;
	}

	public Double getMaxDistanceForEventsOnTheGround() {
		return maxDistanceForEventsOnTheGround;
	}

	public String getMailchimpURL() {
		return mailchimpURL;
	}

	public String getMailchimpListId() {
		return mailchimpListId;
	}

	public String getMailchimpAPIKEY() {
		return mailchimpAPIKEY;
	}

	public boolean isStartReleaser() {
		return startReleaser;
	}	
	
}
