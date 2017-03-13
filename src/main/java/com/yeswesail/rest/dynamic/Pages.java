package com.yeswesail.rest.dynamic;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.DynamicPages;
import com.yeswesail.rest.jsonInt.PagesJson;

@Path("/pages")
public class Pages {

	@GET
	@Path("/dynamic/{URLReference}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDynamicPage(@PathParam("URLReference") String URLReference,
							 @HeaderParam("Authorization") String token,
							 @HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);
		DynamicPages page = null;
		DBConnection conn = null;
		Utils utils = new Utils();
		
		try
		{
			conn = DBInterface.connect();
			page = new DynamicPages(conn, URLReference, languageId);
			utils.addToJsonContainer("dynamicPage", page, true);
			return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}
	
	@PUT
	@Path("/dynamic/{URLReference}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putDynamicPage(PagesJson jsonIn,
							 @HeaderParam("Authorization") String token,
							 @HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(jsonIn.language);
		DynamicPages page = null;
		DBConnection conn = null;		
		try
		{
			conn = DBInterface.connect();
			page = new DynamicPages(conn, jsonIn.uRLReference, languageId);
			page.setCreatedOn(jsonIn.createdOn);
			page.setInnerHTML(jsonIn.innerHTML);
			page.setLanguageId(languageId);
			page.setURLReference(jsonIn.uRLReference);
			page.setStatus(jsonIn.status);
			page.update(conn, "idDynamicPages");
			return Response.status(Response.Status.OK).entity("{}").build();
		}
		catch(Exception e)
		{
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}
}
