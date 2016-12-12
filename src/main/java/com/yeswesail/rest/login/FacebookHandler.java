package com.yeswesail.rest.login;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//import javax.faces.bean.ManagedBean;
//import javax.faces.bean.SessionScoped;
//
//import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.owlike.genson.Genson;
import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;
import com.yeswesail.rest.LanguageResources;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
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
	private Response response = null;
	private UsersAuth ua = null;
	Users u = null;

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

	
	private JSONObject placeHttpRequest(String newUrl, String attributeName)
	{
		String responseBody = null;
		JSONObject json = null;
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) 
		{
			HttpGet httpget = new HttpGet(newUrl);
			log.debug("Placing request: '" + httpget.getURI() + "'");
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpClient.execute(httpget, responseHandler);
			log.debug("Received answer: '" + responseBody + "'");
			json = new JSONObject(responseBody);
			if ((attributeName != null) && (json != null))
			{
				log.debug("Attribute " + attributeName + " value: '" + 
						  getAttributeAsString(json, attributeName) + "'");
			}
		}
		catch (IOException e)
		{
			log.warn("Exception " + e.getMessage() + " retrieving frinds list");
			return null;
		}
		return json;
	}

	private AuthJson populateAuthJsonFromFBReponse(JSONObject json)
	{
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
		return jsonIn;
	}
	
	private Response getUserByFacebookId(DBConnection conn, String fbId, String token)
	{
		try
		{
			u.findByFacebookID(conn, fbId);
			// User was found in our archives.
			log.debug("User " + u.getIdUsers() + " already registered (fbId " + fbId + ")");
		}
		catch (Exception e)
		{
			if (e.getMessage().compareTo("No record found") != 0)
			{
				// All exceptions. No record found is treated as a particular case
				errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
						   e.getMessage() + ")";
				return Utils.jsonizeResponse(Status.UNAUTHORIZED, e, sd.getLanguage(token), "generic.execError");
			}
			u.setIdUsers(-1);
		}
		return(null);
	}
	
	private Response isUserAlreadyRegistered(DBConnection conn, JSONObject json, String token)
	{
		Response response = null;
		if ((response = getUserByFacebookId(conn, getAttributeAsString(json, "id"), token)) != null)
		{
			return response;
		}
		if (u.getIdUsers() == -1)
		{
			// User not found, create a new one and set the UsersAuth accordingly
			setUsersAuth(conn, token);
		}
		
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
			try
			{
				u.update(conn, "idUsers");
			}
			catch (Exception e)
			{
				return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, e, sd.getLanguage(token), "generic.execError");
			}
		}
		return(null);
	}
	
	private UsersAuth evalUsersAuth(int idUsers, String token)
	{
		try 
		{
			if ((ua = UsersAuth.findUserId(idUsers)) == null)
			{
				log.warn("Strangely we have the user but not the authtoken. Generate a new one and create it");
				response = new Auth().populateUsersAuthTable(token, idUsers, prop.getDefaultLang());
				if (response != null)
				{
					log.error("Error populating the UsersAuth object");
					return null;
				}
				ua = UsersAuth.findUserId(idUsers); // Now it should be populated... no need to test
				log.debug("Populated UsersAuth");
			}
		} 
		catch (Exception e) 
		{
			// All exceptions. No record found is treated as a particular case
			errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
					   e.getMessage() + ")";
			response = Utils.jsonizeResponse(Status.UNAUTHORIZED, e, 
							 					 sd.getLanguage(ua.getToken()), "generic.execError");
			return null;
		}
		return ua;
	}
	
	private Response setUsersAuth(DBConnection conn, String token)
	{
		Auth a = new Auth();
		if (u.getIdUsers() == -1)
		{
			AuthJson jsonIn = populateAuthJsonFromFBReponse(json);
			response = a.populateUsersTable(jsonIn, true, prop.getDefaultLang());
			if (response != null)
			{
				log.debug("Got an error while populating user '" + errorMsg + "'");
				return null;
			}
			u = a.getUser();
			u.setConnectedVia("F");
			try
			{
				u.update(conn, "idUsers");
			}
			catch(Exception e)
			{
				log.warn("Exception " + e.getMessage() + " updating the user");
			}
			if ((ua = evalUsersAuth(u.getIdUsers(), token)) == null)
			{
				return response;
			}	
		}
		else
		{
			try 
			{
				ua = UsersAuth.findToken(token);
			}
			catch (Exception e)
			{
				if (!e.getMessage().equals("No record found"))
				{
					log.warn("Exception " + e.getMessage() + " retrieving UsersAuth by token '" + token + "'");
					return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, e, Constants.LNG_EN, "generic.execError");
				}
				response = a.populateUsersAuthTable(token, u.getIdUsers(), prop.getDefaultLang());
				if (response != null)
				{
					log.debug("Got an error while populating user '" + errorMsg + "'");
					return response;
				}
				try
				{
					ua = UsersAuth.findToken(token);
				}
				catch(Exception e1)
				{
					log.warn("Very strange, we can't retrieve what we just added.." + e.getMessage());
					return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, e, Constants.LNG_EN, "generic.execError");
				}
			}
		}
		return null;
	}
	
	private String saveAvatar(String imageUrl)
	{
        String destinationFile = null;
        String fileName = "u_" + u.getIdUsers() + "" + imageUrl.substring(imageUrl.lastIndexOf("."), imageUrl.lastIndexOf("?"));
		try 
		{
	        destinationFile = prop.getContext().getResource("/images/avatars").getPath();
			destinationFile += fileName;
			
			URL url = new URL(imageUrl);
	        InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(new File(destinationFile));

	        byte[] b = new byte[2048];
	        int length;

	        while ((length = is.read(b)) != -1) {
	            os.write(b, 0, length);
	        }

	        is.close();
	        os.close();
		}
		catch (MalformedURLException e) 
		{
			log.warn("Exception " + e.getMessage() + " retrieving images/avatars path");	
		} 
		catch (FileNotFoundException e) {
			log.warn("Exception " + e.getMessage() + " creating new file '" + destinationFile + "'");	
		} 
		catch (IOException e) {
			log.warn("Exception " + e.getMessage() + " handling the output file");	
		}
		return("images/avatars/" + fileName);
	}
	
	private Response getJsonResponseFromFb(String accessToken) 
	{
		String token = UUID.randomUUID().toString();
		
		if (accessToken == null && ! "".equals(accessToken)) 
		{
			log.error("Token for facebook is null");
			return Utils.jsonizeResponse(Status.UNAUTHORIZED, null, Constants.LNG_EN, "auth.invalidTokenExternalRegistration");
		}

		String newUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name,birthday&access_token=" + accessToken;
		json = placeHttpRequest(newUrl, null);
		if (json == null)
		{
			log.warn("Error getting facebook profile");
			return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, null, Constants.LNG_EN, "generic.execError");
		}
		
		DBConnection conn = null;
		try
		{
			conn = DBInterface.connect();
			u = new Users();
			// Check if the user is already registered with us
			if ((response = isUserAlreadyRegistered(conn, json, token)) != null)
			{
				log.warn("Error " + response.getEntity().toString() + " getting facebook profile");
				return response;
			}

			log.trace("Getting user's avatar");
			newUrl = "https://graph.facebook.com/" + getAttributeAsString(json, "id") + "/picture?redirect=0&access_token=" + accessToken;
			if ((json = placeHttpRequest(newUrl, null)) == null)
			{
				log.warn("Unable to get user's avatar");
			}
			else
			{
				JSONObject fbData = getAttributeAsObject(json, "data");
				String imageURL = getAttributeAsString(fbData, "url");
				u.setImageURL(saveAvatar(imageURL));
				try {
					u.update(conn, "idUsers");
				}
				catch(Exception e)
				{
					log.warn("Not able to retrieve the avatar's URL");
				}
			}
			
			if ((response = setUsersAuth(conn, token)) != null)
			{
				log.warn("Error " + response.getEntity().toString() + " setting UsersAuth on token '" + token + "'");
				return response;
			}
			
			log.trace("Getting user's friends list");
			newUrl = "https://graph.facebook.com/" + getAttributeAsString(json, "id") + "/friendlists?access_token=" + accessToken;
			json = placeHttpRequest(newUrl, "data");
			
			String uri = null;
			try 
			{
				// No exception hence the user is already registered. Set the token and redirect to the login page
				uri = prop.getRedirectWebHost() + "/" + prop.getRedirectOnLogin() + 
					  "?token=" + ua.getToken() + "&invalidEmail=" + (u.isAFakeEmail() ? "true" : "false");
				log.debug("User '" + u.getEmail() + "' already registered. Returning a valid token");
				log.debug("Redirect to '" + uri + "'");
				location = new URI(uri);
			}
			catch (URISyntaxException e) 
			{
				log.error("Invalid URL generated '" + uri + "'. Error " + e.getMessage());
				return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, e, 
											 sd.getLanguage(ua.getToken()), "generic.execError");
			} 
		}
		catch(Exception e)
		{
			log.warn("Unable to get a connection. Exception " + e.getMessage());
			return Utils.jsonizeResponse(Status.INTERNAL_SERVER_ERROR, null, Constants.LNG_EN, "generic.execError");
		}
		finally 
		{
			DBInterface.disconnect(conn);
		}
	    return null;
	}    
	
	public Response getFacebookAccessToken(String faceCode)
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
			try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) 
			{
				HttpGet httpget = new HttpGet(newUrl);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = httpClient.execute(httpget, responseHandler);
				log.debug("FB callback received. Response body '" + responseBody + "'");
				token = responseBody.substring(13);
				token = token.substring(0, token.length() - 16);
				log.debug("authentication token '" + token + "'");

			}
			catch (IOException e)
			{
				return Utils.jsonizeResponse(Status.UNAUTHORIZED, e, Constants.LNG_EN, "generic.execError");
			}
		}
		return getJsonResponseFromFb(token);
	}

	public URI getLocation() {
		return location;
	}
	
}