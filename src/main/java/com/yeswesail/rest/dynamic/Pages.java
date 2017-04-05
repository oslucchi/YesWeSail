package com.yeswesail.rest.dynamic;

import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.DynamicPages;
import com.yeswesail.rest.jsonInt.PagesJson;

@Path("/pages")
public class Pages {
	final Logger log = Logger.getLogger(this.getClass());
	Utils utils = new Utils();

	
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
		
		try
		{
			conn = DBInterface.connect();
			page = new DynamicPages(conn, URLReference, languageId);
			utils.addToJsonContainer("dynamicPage", page, true);
			return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
		}
		catch(Exception e)
		{
       		log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.NOT_FOUND, e, languageId, "generic.pageNotFound");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}
	
	@GET
	@Path("/dynamic/edit/{idDynamicPage}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDynamicPageById(@HeaderParam("Authorization") String token,
									   @PathParam("idDynamicPage") int idDynamicPage)

	{
		SessionData sd = SessionData.getInstance();
		int languageId = sd.getLanguage(token);
			
		DynamicPages page = null;
		DBConnection conn = null;
		
		try
		{
			conn = DBInterface.connect();
			page = new DynamicPages(conn, idDynamicPage);
			utils.addToJsonContainer("dynamicPage", page, true);
			return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
		}
		catch(Exception e)
		{
       		log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}

	@GET
	@Path("/dynamic")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllDynamicPage(@HeaderParam("Authorization") String token,
									  @HeaderParam("Language") String language)

	{
		int languageId = Utils.setLanguageId(language);
		ArrayList<DynamicPages> pageList = null;
		DBConnection conn = null;
		
		try
		{
			conn = DBInterface.connect();
			pageList = DynamicPages.getAllPages(conn, languageId);
			utils.addToJsonContainer("dynamicPages", pageList, true);
			return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
		}
		catch(Exception e)
		{
       		log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}

	@PUT
	@Path("/dynamic/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putDynamicPage(@PathParam("id") int id,
								   @HeaderParam("Authorization") String token,
								   PagesJson jsonIn)
	{
		int languageId = Utils.setLanguageId(jsonIn.language);
		DynamicPages page = null;
		DBConnection conn = null;		
		try
		{
			conn = DBInterface.connect();
			page = new DynamicPages(conn, id);
			page.setInnerHTML(jsonIn.innerHTML);
			page.setStatus(jsonIn.status);
			page.update(conn, "idDynamicPages");
		}
		catch(Exception e)
		{
       		log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@POST
	@Path("/dynamic")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertDynamicPage(@HeaderParam("Authorization") String token,
									  PagesJson jsonIn)
	{
		int languageId = Utils.setLanguageId(jsonIn.language);
		DynamicPages page = null;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.connect();
			page = new DynamicPages();
			page = new DynamicPages();
			page.setCreatedOn(new Date());
			page.setURLReference(jsonIn.URLReference);
			page.setLanguageId(languageId);
			page.setInnerHTML(jsonIn.innerHTML);
			page.setStatus(jsonIn.status);
			page.setIdDynamicPages(page.insertAndReturnId(conn, "idDynamicPages", page));
		}
		catch(Exception e)
		{
       		log.warn("Exception " + e.getMessage(), e);
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		utils.addToJsonContainer("dynamicPage", page, true);
		return Response.status(Response.Status.OK).entity(utils.jsonize()).build();
	}
}
