package com.yeswesail.rest.login;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.owlike.genson.Genson;
import com.yeswesail.rest.LanguageResources;

@ManagedBean(name = "loginPageCode")
@SessionScoped
public class FacebookHandler implements Serializable 
{
	private static final long serialVersionUID = -9030829206142122149L;
	final Logger log = Logger.getLogger(this.getClass());
	Genson genson = new Genson();
	private JSONObject json = null;

	private JSONObject getJsonResponseFromFb(String accessToken) 
	{
		
		if (accessToken != null && ! "".equals(accessToken)) 
		{
			log.error("Token for facebook is null");
			return null;
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		try 
		{
			String newUrl = "https://graph.facebook.com/me?access_token=" + accessToken;
			httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(newUrl);
			log.debug("Get info from face --> executing request: " + httpget.getURI());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);

			json = genson.deserializeInto(responseBody, json);
//			String facebookId = json.getString("id");
//			String firstName = json.getString("first_name");
//			String lastName = json.getString("last_name");
//			email= json.getString("email");
		}
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally 
		{
			httpclient.getConnectionManager().shutdown();
		}
		return json;
	}    
	
	public String getAttribute(String accessToken, String attribute) 
	{
		if (json == null)
		{
			return null;
		}
//			String facebookId = json.getString("id");
//			String firstName = json.getString("first_name");
//			String lastName = json.getString("last_name");
//			email= json.getString("email");
		//put user data in session
		return json.getString(attribute);
	}
	
	public String getFacebookAccessToken(String faceCode)
	{
		String token = null;
		String errorMsg = null;
		
		if (faceCode != null && ! "".equals(faceCode)) {
			String appId = "484756285065008";
			String redirectUrl = "http://localhost:8080/index.sec";
			String faceAppSecret = "78497990b2d78011cacfe77af7d76f0b";
			String newUrl = "https://graph.facebook.com/oauth/access_token?client_id="
					+ appId + "&redirect_uri=" + redirectUrl + "&client_secret="
					+ faceAppSecret + "&code=" + faceCode;
			HttpClient httpclient = new DefaultHttpClient();
			try
			{
				HttpGet httpget = new HttpGet(newUrl);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				String responseBody = httpclient.execute(httpget, responseHandler);
				token = StringUtils.removeEnd
						(StringUtils.removeStart(responseBody, "access_token="),
								"&expires=5180795");
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
}
