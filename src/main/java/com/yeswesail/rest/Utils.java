package com.yeswesail.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.owlike.genson.Genson;

public class Utils {
	static ApplicationProperties prop = ApplicationProperties.getInstance();
	static HashMap<String, Object>jsonResponse = new HashMap<>();
	
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
		
	public static void addToJsonContainer(String key, Object object, boolean clear)
	{
		if (clear)
		{
			jsonResponse.clear();
		}
		jsonResponse.put(key, object);
	}

	public static String jsonize()
	{
		Genson genson = new Genson();
		return genson.serialize(jsonResponse);
	}

}
