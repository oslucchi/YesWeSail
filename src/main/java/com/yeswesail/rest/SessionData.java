package com.yeswesail.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

public class SessionData {
	final public static int BASIC_PROFILE = 0; // a users Object only
	final public static int WHOLE_PROFILE = 1; // Users and AddressInfo array
	final public static int LANGUAGE = 2; // Language 
	final public static int SESSION_ELEMENTS = 3;
	
	private static Map<String, Object[]> sessionData;
	private static SessionData singletonInstance = null;

	private SessionData()
	{
		sessionData = new HashMap<>();
	}
	
	public static SessionData getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new SessionData();
		}
		return(singletonInstance);
	}
	
	public Users getBasicProfile(String token)
	{
		Object[] profile = sessionData.get(token);
		if (profile == null)
		{
			return(null);
		}
		return((Users) profile[BASIC_PROFILE]);
	}

	public Object[] getWholeProfile(String token)
	{
		Object[] profile = sessionData.get(token);
		if (profile == null)
		{
			return(null);
		}
		Object[] newArray = new Object[WHOLE_PROFILE];
		java.lang.System.arraycopy(profile, 0, newArray, 0, WHOLE_PROFILE);
		return(newArray);
	}
	
	public int getLanguage(String token)
	{
		if (sessionData.get(token) == null)
			return 1;
		return(((Integer) sessionData.get(token)[LANGUAGE]).intValue());
	}
	
	public Object[] getSessionData(String token)
	{
		Object[] profile = sessionData.get(token);
		if (profile == null)
		{
			return(null);
		}
		return profile;
	}

	public int getLanguage(int userId)
	{
		for(String token: sessionData.keySet())
		{
			if (((Users)sessionData.get(token)[BASIC_PROFILE]).getIdUsers() == userId)
			{
				return(((Integer) sessionData.get(token)[LANGUAGE]).intValue());
			}
		}
		return(Constants.LNG_EN);
	}
	

	public void setLanguage(String token, int languageId)
	{
		Object[] profile = sessionData.get(token);
		if (profile == null)
		{
			return;
		}
		profile[LANGUAGE] = new Integer(languageId);
	}
	
	public void addUser(String token, int languageId) throws Exception
	{
		if (sessionData.get(token) != null)
			return;
		Object[] userData = new Object[SESSION_ELEMENTS];
		UsersAuth ua = UsersAuth.findToken(token);
		DBConnection conn = new DBConnection();
		userData[BASIC_PROFILE] = new Users(conn, ua.getUserId());
		userData[WHOLE_PROFILE] = AddressInfo.findUserId(ua.getUserId());
		userData[LANGUAGE] = new Integer(languageId);
		sessionData.put(token, userData);
		conn.getSt().close();
		conn.getRs().close();
		return;
	}

	public void removeUser(String token)
	{
		sessionData.remove(token);
	}

	public Users getBasicProfile(int userId)
	{
		for(String token: sessionData.keySet())
		{
			if (((Users)sessionData.get(token)[BASIC_PROFILE]).getIdUsers() == userId)
			{
				return((Users)sessionData.get(token)[BASIC_PROFILE]);
			}
		}
		return(null);
	}

	public Object[] getWholeProfile(int userId)
	{
		for(String token: sessionData.keySet())
		{
			if (((Users)sessionData.get(token)[BASIC_PROFILE]).getIdUsers() == userId)
			{
				return(sessionData.get(token));
			}
		}
		return(null);
	}

	public void addUser(int userId, int languageId) throws Exception
	{
		UsersAuth ua = UsersAuth.findUserId(userId);
		if (sessionData.get(ua.getToken()) != null)
		{
			removeUser(ua.getUserId());
		}

		Object[] userData = new Object[SESSION_ELEMENTS];
		DBConnection conn = new DBConnection();
		userData[BASIC_PROFILE] = new Users(conn, ua.getUserId());
		userData[WHOLE_PROFILE] = AddressInfo.findUserId(ua.getUserId());
		userData[LANGUAGE] = new Integer(languageId);
		sessionData.put(ua.getToken(), userData);
		conn.getSt().close();
		conn.getRs().close();
	}

	public void removeUser(int userId)
	{
		Iterator<Map.Entry<String, Object[]>> iter = sessionData.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry<String, Object[]> entry = iter.next();
		    if(((Users) entry.getValue()[BASIC_PROFILE]).getIdUsers() == userId)
			{
		        iter.remove();
		        break;
		    }
		}
		return;
	}

	public void updateSession(int userId, Object[] data, String newToken)
	{
		Iterator<Map.Entry<String, Object[]>> iter = sessionData.entrySet().iterator();
		Object[] sessionItem = null;
		while (iter.hasNext()) 
		{
		    Map.Entry<String, Object[]> entry = iter.next();
		    if(((Users) entry.getValue()[BASIC_PROFILE]).getIdUsers() == userId)
			{
		    	sessionItem = entry.getValue();
		        iter.remove();
		        break;
		    }
		}
		if ((sessionItem == null) || (sessionItem.length != SESSION_ELEMENTS))
		{
			data[LANGUAGE] = Utils.setLanguageId("EN");
		}
		else
		{
			data[LANGUAGE] = sessionItem[LANGUAGE];
		}
		sessionData.put(newToken, data);
	}

	public void updateSession(String token, Object[] data)
	{
		sessionData.put(token, data);
	}

}
