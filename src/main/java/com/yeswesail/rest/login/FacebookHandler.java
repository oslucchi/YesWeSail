package com.yeswesail.rest.login;

import java.io.IOException;
import java.io.Serializable;

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
import com.yeswesail.rest.LanguageResources;

//@ManagedBean(name = "loginPageCode")
//@SessionScoped
public class FacebookHandler implements Serializable 
{
	private static final long serialVersionUID = -9030829206142122149L;
	ApplicationProperties prop = new ApplicationProperties();
	final Logger log = Logger.getLogger(this.getClass());
	Genson genson = new Genson();
	private JSONObject json = null;

	private JSONObject getJsonResponseFromFb(String accessToken) 
	{
		
		if (accessToken == null && ! "".equals(accessToken)) 
		{
			log.error("Token for facebook is null");
			return null;
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		try 
		{
			String newUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name&access_token=" + accessToken;
			httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(newUrl);
			log.debug("Get info from face --> executing request: " + httpget.getURI());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);
			json = new JSONObject(responseBody);
			httpclient.getConnectionManager().shutdown();

			newUrl = "https://graph.facebook.com/" + getAttribute("id") + "/picture?redirect=0&access_token=" + accessToken;
			httpclient = new DefaultHttpClient();
			httpget = new HttpGet(newUrl);
			log.debug("Gettting picutre --> executing request: " + httpget.getURI());
			responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
			json = new JSONObject(responseBody);
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
	
	public String getAttribute(String attribute) 
	{
		if (json == null)
		{
			return null;
		}
		return json.getString(attribute);
	}
	
	public String getFacebookAccessToken(String faceCode)
	{
		String token = null;
		String errorMsg = null;
		
		if (faceCode != null && ! "".equals(faceCode)) {
			String appId = "484756285065008";
			String redirectUrl = prop.getWebHost() + "/rest/auth/fbLogin";
			String faceAppSecret = "78497990b2d78011cacfe77af7d76f0b";
			String newUrl = "https://graph.facebook.com/oauth/access_token?client_id="
					+ appId + "&redirect_uri=" + redirectUrl + "&client_secret="
					+ faceAppSecret + "&code=" + faceCode;
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
}
