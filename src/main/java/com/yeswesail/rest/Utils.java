package com.yeswesail.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.owlike.genson.Genson;

public class Utils {
	final static Logger log = Logger.getLogger(Utils.class);
	
	static ApplicationProperties prop = ApplicationProperties.getInstance();
	HashMap<String, Object>jsonResponse = new HashMap<>();
	
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
		
	public void addToJsonContainer(String key, Object object, boolean clear)
	{
		if (clear)
		{
			jsonResponse.clear();
		}
		jsonResponse.put(key, object);
	}

	public String jsonize()
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
			catch(JSONException e)
			{
				e.printStackTrace();;
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return objInst;
	}
	
	public static Response jsonizeResponse(Status status, Exception e, int languageId, String errResource )
	{
		HashMap<String, Object>jsonResponse = new HashMap<>();
		Genson genson = new Genson();
		jsonResponse.clear();
		jsonResponse.put("error", 
						 LanguageResources.getResource(languageId, errResource) + 
						 	(e == null ? "" : " (" + e.getMessage() + ")"));
		return Response.status(status).entity(genson.serialize(jsonResponse)).build();
	}

	public static Response jsonizeResponse(Status status, Exception e, String language, String errResource )
	{
		return(jsonizeResponse(status, e, setLanguageId(language), errResource));
	}

	public static Response jsonizeSingleObject(Object o, int languageId)
	{
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		
		try 
		{
			json = mapper.writeValueAsString(o);
		} 
		catch (IOException e) {
			log.error("Error jsonizing basic profile (" + e.getMessage() + ")");
			return jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		return Response.status(Status.OK).entity(json).build();
	}

	public static Response jsonizeSingleObject(Object o, String language)
	{
		return jsonizeSingleObject(o, setLanguageId(language));
	}
}