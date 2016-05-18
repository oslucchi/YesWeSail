package com.yeswesail.rest.login;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;


//import javax.faces.bean.ManagedBean;
//import javax.faces.bean.SessionScoped;
//
//import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;
import com.yeswesail.rest.jsonInt.AuthJson;

//@ManagedBean(name = "loginPageCode")
//@SessionScoped
public class FacebookHandler implements Serializable 
{
	private static final long serialVersionUID = -9030829206142122149L;
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Genson genson = new Genson();
	private JSONObject json = null;
	private URI location = null;
	private String errorMsg = null;
	private SessionData sd = SessionData.getInstance();
	
	public String getAttributeAsString(JSONObject obj, String attribute) 
	{
		if (obj == null)
		{
			return null;
		}
		try {
			return obj.getString(attribute);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public JSONObject getAttributeAsObject(JSONObject obj, String attribute) 
	{
		if (obj == null)
		{
			return null;
		}
		try {
			return obj.getJSONObject(attribute);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private void getFriendsList(String accessToken)
	{
		try 
		{
			String newUrl = "https://graph.facebook.com/" + getAttributeAsString(json, "id") + "/friendlists?access_token=" + accessToken;
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(newUrl);
			log.debug("Getting friendlists --> executing request: " + httpget.getURI());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = null;
			responseBody = httpclient.execute(httpget, responseHandler);
			JSONObject json = new JSONObject(responseBody);
			if (json != null)
				log.debug(getAttributeAsString(json, "data"));
		} 
		catch (IOException e)
		{
			;
		}
	}
	
	private JSONObject getJsonResponseFromFb(String accessToken) 
	{
		Users u = null;
		UsersAuth ua = null;
		String token = UUID.randomUUID().toString();
		
		if (accessToken == null && ! "".equals(accessToken)) 
		{
			log.error("Token for facebook is null");
			return null;
		}
		String uri = null;
		HttpClient httpclient = new DefaultHttpClient();
		DBConnection conn = null;
		try 
		{
			// Request profile data 
			String newUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name,birthday&access_token=" + accessToken;
			httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(newUrl);
			log.debug("Get info from face --> executing request: " + httpget.getURI());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			log.debug("Received valid http content: '" + responseBody + "'");
			json = new JSONObject(responseBody);
			httpclient.getConnectionManager().shutdown();
			
			// Check if the user is already registered with us
			try
			{
				conn = DBInterface.connect();
				u = new Users();
				String id = getAttributeAsString(json, "id");
				u.findByFacebookID(conn, id);
				boolean changed = false;
				if (u.isAFakeEmail() && getAttributeAsString(json, "email") != null)
				{
					u.setEmail(getAttributeAsString(json, "email"));
					changed = true;
				}
				if ((u.getBirthday() == null) && (getAttributeAsString(json, "birthday") != null))
				{
					changed = true;
					u.setBirthday(getAttributeAsString(json, "birthday") , "MM/dd/yyyy");
				}
				if (changed)
				{
					u.update(conn, "idUsers");
				}
				getFriendsList(accessToken);
				if ((ua = UsersAuth.findUserId(u.getIdUsers())) == null)
				{
					log.warn("Strangely we have the user but not the authtoken. Generate a new one and create it");
					if ((errorMsg = new Auth().populateUsersAuthTable(token, u.getIdUsers(), prop.getDefaultLang())) != null)
					{
						log.error("Error populating the UsersAuth object");
						return null;
					}
					ua = UsersAuth.findUserId(u.getIdUsers());
				}
				uri = null;
				try 
				{
					// No exception hence the user is already register. Set the token and redirect to the login page
					uri = prop.getRedirectWebHost() + "/" + prop.getRedirectOnLogin() + 
						  "?token=" + ua.getToken() + "&invalidEmail=" + (u.isAFakeEmail() ? "true" : "false");
					log.debug("User '" + u.getEmail() + "' already registered. Returning a valid token");
					log.debug("Redirect to '" + uri + "'");
					location = new URI(uri);
					return json;
				}
				catch (URISyntaxException e) 
				{
					log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage());
					errorMsg = LanguageResources.getResource(sd.getLanguage(ua.getToken()), "generic.execError") + "(" +
							   e.getMessage() + ")";
					return null;
				} 
			}
			catch (Exception e)
			{
				if (e.getMessage().compareTo("No record found") != 0)
				{
					// All exceptions. No record found is treated as a particular case
					errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
							   e.getMessage() + ")";
					return null;
				}
				else
				{
					Auth a = new Auth();
					AuthJson jsonIn = new AuthJson();
					jsonIn.firstName = getAttributeAsString(json, "first_name");
					jsonIn.lastName = getAttributeAsString(json, "last_name");
					jsonIn.facebookId = getAttributeAsString(json, "id");
					if (getAttributeAsString(json, "email") == null)
					{
						jsonIn.username = "fake.fb." + jsonIn.facebookId + "@yeswesail.com";
					}
					else
					{
						jsonIn.username = getAttributeAsString(json, "email");
					}
					log.debug("The user " + jsonIn.firstName + " " + jsonIn.lastName + " " + 
							  jsonIn.facebookId + " " + jsonIn.username + " is not registered yet"
							  		+ "populating users table");
					if ((errorMsg = a.populateUsersTable(jsonIn, true, prop.getDefaultLang())) != null)
					{
						log.debug("Got an error while populating user '" + errorMsg + "'");
						return null;
					}
					try 
					{
						u.findByFacebookID(conn, getAttributeAsString(json, "id"));
						u.setConnectedVia("F");
						if (getAttributeAsString(json, "user_birthday") != null)
							u.setAge(50);
						u.update(conn, "idUsers");
					}
					catch (Exception e1) 
					{
						// All exceptions. No record found is treated as a particular case
						errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + 
								   "(Unable to create user: " + e1.getMessage() + ")";
						return null;
					}
					if ((errorMsg = a.populateUsersAuthTable(token, u.getIdUsers(), prop.getDefaultLang())) != null)
					{
						return null;
					}
					try 
					{
						ua = UsersAuth.findToken(token);
					}
					catch (Exception e1)
					{
						errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + 
								   "(Unable to populate users auth token: " + e1.getMessage() + ")";
						return null;
					}
				}
			}
			
			uri = prop.getRedirectWebHost() + "/" + prop.getRedirectRegistrationCompleted() + 
				  "?token=" + ua.getToken() + "&invalidEmail=";
			if (getAttributeAsString(json, "email") == null)
			{
				uri += "true";
			}
			else
			{
				uri += "false";
			}
			
			log.debug("Preparing redirection to '" + uri + "'");
			try 
			{
				location = new URI(uri);
			} 
			catch (URISyntaxException e) 
			{
				errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
						   e.getMessage() + ")";
			}

			newUrl = "https://graph.facebook.com/" + getAttributeAsString(json, "id") + "/picture?redirect=0&access_token=" + accessToken;
			httpclient = new DefaultHttpClient();
			httpget = new HttpGet(newUrl);
			log.debug("Getting profile picture --> executing request: " + httpget.getURI());
			responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
			json = new JSONObject(responseBody);
			JSONObject fbData = getAttributeAsObject(json, "data");
			String imageURL = getAttributeAsString(fbData, "url");
			u.setImageURL(imageURL);
			try {
				u.update(conn, "idUsers");
			}
			catch(Exception e)
			{
				log.warn("Not able to retrieve the avatar's URL");
			}
		}
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			httpclient.getConnectionManager().shutdown();
			DBInterface.disconnect(conn);
		}
	    return json;
	}    
	
	public String getFacebookAccessToken(String faceCode)
	{
		String token = null;
		log.debug("Trying athetication vs FB servers");
		
		if (faceCode != null && ! "".equals(faceCode)) {
			String appId = prop.getFbApplicationId();
			String redirectUrl = prop.getWebHost() + "/rest/auth/fbLogin";
			String faceAppSecret = prop.getFbApplicationSecret();
			String newUrl = "https://graph.facebook.com/oauth/access_token?client_id=" + appId +
							"&redirect_uri=" + redirectUrl + 
							"&client_secret=" + faceAppSecret + 
							"&code=" + faceCode;
			log.debug("Request redirect on: '" + newUrl + "'");
			HttpClient httpclient = new DefaultHttpClient();
			try
			{
				HttpGet httpget = new HttpGet(newUrl);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = httpclient.execute(httpget, responseHandler);
				log.debug("FB callback received. Response body '" + responseBody + "'");
				token = responseBody.substring(13);
				token = token.substring(0, token.length() - 16);
				log.debug("authentication token '" + token + "'");
			}
			catch (ClientProtocolException e)
			{
				errorMsg = LanguageResources.getResource("generic.execError") +  " (" + e.getMessage() + ")";
			}
			catch (IOException e) 
			{
				errorMsg = LanguageResources.getResource("generic.execError") +  " (" + e.getMessage() + ")";
			} 
			finally 
			{
				httpclient.getConnectionManager().shutdown();
			}
		}
		getJsonResponseFromFb(token);
		return errorMsg;
	}

	public URI getLocation() {
		return location;
	}
	
}