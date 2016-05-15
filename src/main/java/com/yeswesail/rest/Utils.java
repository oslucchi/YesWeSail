package com.yeswesail.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
	static ApplicationProperties prop = ApplicationProperties.getInstance();
	
	public static String printStackTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return (sw.toString()); 
	}
	
	public static int setLanguageId(String language)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		return Constants.getLanguageCode(language);
	}
}
