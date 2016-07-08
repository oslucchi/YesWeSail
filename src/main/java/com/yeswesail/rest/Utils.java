package com.yeswesail.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.owlike.genson.Genson;

public class Utils {
	static ApplicationProperties prop = ApplicationProperties.getInstance();
	static HashMap<String, Object>jsonResponse = new HashMap<>();
	final static Logger log = Logger.getLogger(Utils.class);

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

	private static Field[] getAllFields(Class<?> cType)
	{
		List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = cType; c != null; c = c.getSuperclass()) 
        {
        	fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        Field[] fieldArr = new Field[fields.size()];
        return fields.toArray(fieldArr);
	}

	public static Object populateObjectFromJSON(String jsonIn, Object objInst)
	{
		JSONObject jsonObj = new JSONObject(jsonIn);
		Field[] clFields = getAllFields(objInst.getClass());
		for(Field field : clFields)
		{
			try
			{
				switch(field.getType().getName())
				{
				case "int":
				case "long":
					field.set(objInst, Integer.parseInt(jsonObj.getString(field.getName())));
					break;
	
				case "java.lang.String":
					field.set(objInst, jsonObj.getString(field.getName()));
					break;
				}
			}			
			catch(JSONException | IllegalArgumentException | IllegalAccessException e)
			{
				log.warn("Exception " + e.getMessage() + " parsing json object");
			} 
		}
		return objInst;
	}
}
