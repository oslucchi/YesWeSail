package com.yeswesail.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LanguageResources {
	private static Properties[] resource = new Properties[2];
	private static LanguageResources singletonInstance = null;
	private static int languageCode = Constants.LNG_IT;

	private LanguageResources()
	{
    	try 
    	{
        	InputStream in = LanguageResources.class.getResourceAsStream("/it.properties");
        	resource[Constants.LNG_IT] = new Properties();
        	resource[Constants.LNG_IT].load(in);
	    	in.close();
        	in = LanguageResources.class.getResourceAsStream("/en.properties");
        	resource[Constants.LNG_EN] = new Properties();
        	resource[Constants.LNG_EN].load(in);
	    	in.close();
		}
    	catch (IOException e) 
    	{
			e.printStackTrace();
    		return;
		}
	}
	
	public static String getResource(String errCode)
	{
		if (singletonInstance == null)
		{
			singletonInstance = new LanguageResources();
		}
		return(resource[languageCode].getProperty(errCode, "Unknown error"));
	}
}
