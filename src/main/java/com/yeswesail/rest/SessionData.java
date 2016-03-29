package com.yeswesail.rest;

import java.util.HashMap;
import java.util.Map;

import com.yeswesail.rest.DBUtility.AddressInfo;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

public class SessionData {
	private static Map<String, Object[]> sessionData;
	private static SessionData singletonInstance = null;

	private SessionData()
	{
		sessionData = new HashMap<>();
	}
	
	public static SessionData getInstance(String errCode)
	{
		if (singletonInstance == null)
		{
			singletonInstance = new SessionData();
		}
		return(singletonInstance);
	}
	
	public static Users getBasicProfile(String token)
	{
		Object[] profile = sessionData.get(token);
		if (profile == null)
		{
			return(null);
		}
		return((Users) profile[0]);
	}

	public static Object[] getWholeProfile(String token)
	{
		Object[] profile = sessionData.get(token);
		if (profile == null)
		{
			return(null);
		}
		return(profile);
	}

	public static void addUser(String token) throws Exception
	{
		Object[] userData = new Object[2];
		UsersAuth ua = UsersAuth.findToken(token);
		userData[0] = new Users(ua.getUserId());
		userData[1] = AddressInfo.findUserId(ua.getUserId());
		sessionData.put(token, userData);
		return;
	}

	public static void removeUser(String token)
	{
		sessionData.remove(token);
	}

	public static Users getBasicProfile(int userId)
	{
		for(String token: sessionData.keySet())
		{
			if (((Users)sessionData.get(token)[0]).getIdUsers() == userId)
			{
				return((Users)sessionData.get(token)[0]);
			}
		}
		return(null);
	}

	public static Object[] getWholeProfile(int userId)
	{
		for(String token: sessionData.keySet())
		{
			if (((Users)sessionData.get(token)[0]).getIdUsers() == userId)
			{
				return(sessionData.get(token));
			}
		}
		return(null);
	}

	public static void addUser(int userId) throws Exception
	{
		Object[] userData = new Object[2];
		UsersAuth ua = UsersAuth.findUserId(userId);
		userData[0] = new Users(userId);
		userData[1] = AddressInfo.findUserId(userId);
		sessionData.put(ua.getToken(), userData);
	}

	public static void removeUser(int userId)
	{
		for(String token: sessionData.keySet())
		{
			if (((Users)sessionData.get(token)[0]).getIdUsers() == userId)
			{
				sessionData.remove(token);
			}
		}
		return;
	}
}
