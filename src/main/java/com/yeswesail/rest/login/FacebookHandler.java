package com.yeswesail.rest.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.DBUtility.UsersAuth;

//@ManagedBean(name = "loginPageCode")
//@SessionScoped
public class FacebookHandler implements Serializable 
{
	private static final long serialVersionUID = -9030829206142122149L;
	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Genson genson = new Genson();
	private JSONObject json = null;
	private boolean newRegistration = false;
	private String redirectLocation = "";
	private URI location = null;
	private String errorMsg = null;
	private SessionData sd = SessionData.getInstance();
	
	public String getAttribute(String attribute) 
	{
		if (json == null)
		{
			return null;
		}
		return json.getString(attribute);
	}
	
	private JSONObject getJsonResponseFromFb(String accessToken) 
	{
		Users u = null;
		UsersAuth ua = null;
		
		if (accessToken == null && ! "".equals(accessToken)) 
		{
			log.error("Token for facebook is null");
			return null;
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		try 
		{
			// Request profile data 
			String newUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name&access_token=" + accessToken;
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
				u = new Users(getAttribute("email"));
				ua = new UsersAuth(u.getIdUsers());
				// No exception hence the user is already register. Set the token and redirect to the login page
				String uri = prop.getWebHost() + "/" + prop.getRedirectOnLogin() + "?token=" + ua.getToken();
				try 
				{
					location = new URI(uri);
					newRegistration = true;
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
					if (u != null)
					{
						log.warn("Strangely we have the user but not the authtoken. Generate a new one and create it");
						String token = UUID.randomUUID().toString();
						if ((errorMsg = new Auth().populateUsersAuthTable(token, u.getIdUsers())) != null)
						{
							errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
									   e.getMessage() + ")";
							return null;
						}
					}
					else
					{
						// All exceptions. No record found is treated as a particular case
						errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
								   e.getMessage() + ")";
						return null;
					}
				}
			}
			
			newRegistration = false;
			
			String uri = prop.getWebHost() + "/" + prop.getRedirectRegistrationCompleted() + "?token=" + ua.getToken();
			try 
			{
				location = new URI(uri);
			} 
			catch (URISyntaxException e) 
			{
				errorMsg = LanguageResources.getResource(Constants.LNG_EN, "generic.execError") + "(" +
						   e.getMessage() + ")";
			}

			newUrl = "https://graph.facebook.com/" + getAttribute("id") + "/picture?redirect=0&access_token=" + accessToken;
			httpclient = new DefaultHttpClient();
			httpget = new HttpGet(newUrl);
			log.debug("Getting profile picture --> executing request: " + httpget.getURI());
			responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
			json = new JSONObject(responseBody);
			InputStream in;
			try {
				URL url = new URL(getAttribute("data.url"));
				in = url.openStream();
				String savePath = "/images/avatars/" + u.getIdUsers() +  
						 		  url.getPath().substring(url.getPath().lastIndexOf("."));
				savePath = getClass().getResource(savePath).getPath();
				Files.copy(in, Paths.get(savePath));
				u.setImageURL(savePath);
				u.update("idUsers");
			} 
			catch (Exception e) {
				;
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
		}
	    return json;
	}    
	
	public String getFacebookAccessToken(String faceCode)
	{
		String token = null;
		
		if (faceCode != null && ! "".equals(faceCode)) {
			String appId = prop.getFbApplicationId();
			String redirectUrl = prop.getWebHost() + "/rest/auth/fbLogin";
			String faceAppSecret = prop.getFbApplicationSecret();
			String newUrl = "https://graph.facebook.com/oauth/access_token?client_id=" + appId +
							"&redirect_uri=" + redirectUrl + 
							"&client_secret=" + faceAppSecret + 
							"&code=" + faceCode;
			System.out.println(newUrl);
			HttpClient httpclient = new DefaultHttpClient();
			try
			{
				HttpGet httpget = new HttpGet(newUrl);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = httpclient.execute(httpget, responseHandler);
				System.out.println(responseBody);
				token = responseBody.substring(13);
				token = token.substring(0, token.length() - 16);
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

	public boolean isNewRegistration() {
		return newRegistration;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public URI getLocation() {
		return location;
	}
	
}