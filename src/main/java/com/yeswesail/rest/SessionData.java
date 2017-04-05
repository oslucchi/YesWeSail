package com.yeswesail.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

public class SessionData {
	final static Logger log = Logger.getLogger(SessionData.class);
	
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
		return sessionData.get(token);
	}

	public Object[] getSessionData(int userId)
	{
		Iterator<Map.Entry<String, Object[]>> iter = sessionData.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry<String, Object[]> entry = iter.next();
		    Object[] profile = entry.getValue();
			if (profile[BASIC_PROFILE] == null)
			{
		        iter.remove();
			}
			else if (((Users)profile[BASIC_PROFILE]).getIdUsers() == userId)
			{
				return(profile);
			}
		}
		return null;
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
		UsersAuth ua = null;
		DBConnection conn = null;

		try
		{
			conn = DBInterface.connect();
			ua = UsersAuth.findToken(conn, token);
			if (ua == null)
			{
				throw new Exception("Token '" + token + "' not found");
			}
			userData[BASIC_PROFILE] = new Users(conn, ua.getUserId());
			userData[WHOLE_PROFILE] = AddressInfo.findUserId(conn, ua.getUserId());
			userData[LANGUAGE] = new Integer(languageId);
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		sessionData.put(token, userData);
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

	public void addUser(DBConnection conn, int userId, int languageId) throws Exception
	{
		UsersAuth ua = UsersAuth.findUserId(conn, userId);
		if (ua == null)
		{
			throw new Exception("User " + userId + " not found");
		}
		addUser(ua, languageId);
		return;
	}

	public void addUser(UsersAuth ua, String language) throws Exception
	{
		int languageId = Constants.getLanguageCode(language);
		addUser(ua, languageId);
	}
	
	public void addUser(UsersAuth ua, int languageId) throws Exception
	{
		log.trace("ua " + ua);
		if (ua == null)
		{
			throw new Exception("UsersAuth is null...");
		}
		
		log.trace("Adding user id " + ua.getUserId() + " token '" + ua.getToken() + 
				  "' using language " + languageId);
		if (sessionData.get(ua.getToken()) != null)
		{
			removeUser(ua.getUserId());
		}

		Object[] userData = new Object[SESSION_ELEMENTS];
		DBConnection conn = null;
		try
		{
			conn = DBInterface.connect();
			userData[BASIC_PROFILE] = new Users(conn, ua.getUserId());
			userData[WHOLE_PROFILE] = AddressInfo.findUserId(conn, ua.getUserId());
			userData[LANGUAGE] = new Integer(languageId);
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		sessionData.put(ua.getToken(), userData);
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

	public Map<String, Object[]> getAllItems()
	{
		return sessionData;
	}

}
