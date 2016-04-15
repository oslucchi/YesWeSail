package com.yeswesail.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.yeswesail.rest.DBUtility.UsersAuth;

@Provider
@PreMatching
public class Authorizer implements ContainerRequestFilter 
{
	ApplicationProperties prop = ApplicationProperties.getInstance();
	private static String[] authorizedList = null;
	
	@Override
	public void filter(ContainerRequestContext request) throws IOException 
	{
		if (authorizedList == null)
		{
			authorizedList = prop.getNoAythorizationRequired().split(",");
		}
		String path = request.getUriInfo().getPath();
		if (path.startsWith("/auth/confirmUser"))
			return;
		
//		path = path.substring(path.lastIndexOf("/") + 1);
		for(String authorized : authorizedList)
		{
			if (path.compareTo(authorized) == 0)
			{
				return;
			}
		}
		String token = request.getHeaderString("Authorization");
		String language = request.getHeaderString("Language");
		if (language == null)
		{
			language = prop.getDefaultLang();
		}

		SessionData sd = SessionData.getInstance();
		Object[] sessionProfile = sd.getSessionData(token);
		if (sessionProfile != null)
		{
			if (SessionData.getInstance().getLanguage(token) != Constants.getLanguageCode(language))
			{
				sd.setLanguage(token, Constants.getLanguageCode(language));
			}
			return;
		}
		UsersAuth ua = null;
		try {
			ua = UsersAuth.findToken(token);
			sd.addUser(ua.getUserId(), Constants.getLanguageCode(language));
		} 
		catch (Exception e) {
			request.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity("Token not recognized")
					.build());;
		}
		return;
	}

}
