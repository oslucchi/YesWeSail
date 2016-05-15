package com.yeswesail.rest;

import java.util.HashMap;

import com.owlike.genson.Genson;

public class ResponseEntityCreator {
	private static ApplicationProperties prop = ApplicationProperties.getInstance();
	
	public static String formatEntity(String property, int languageId)
	{
		Genson g = new Genson();
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("message", LanguageResources.getResource(languageId, property));
		return g.serialize(jsonResponse);
	}
	public static String formatEntity(String language, String property)
	{
		if (language == null)
		{
			language = prop.getDefaultLang();
		}
		
		Genson g = new Genson();
		HashMap<String, Object> jsonResponse = new HashMap<>();
		jsonResponse.put("message", 
				LanguageResources.getResource(Constants.getLanguageCode(language), property));
		return g.serialize(jsonResponse);
	}
}
