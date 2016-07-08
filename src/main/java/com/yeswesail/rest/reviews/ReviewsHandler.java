package com.yeswesail.rest.reviews;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.JsonHandler;
import com.yeswesail.rest.SessionData;
import com.yeswesail.rest.Utils;
import com.yeswesail.rest.DBUtility.DBConnection;
import com.yeswesail.rest.DBUtility.DBInterface;
import com.yeswesail.rest.DBUtility.PendingActions;
import com.yeswesail.rest.DBUtility.Reviews;
import com.yeswesail.rest.DBUtility.Roles;
import com.yeswesail.rest.DBUtility.Users;
import com.yeswesail.rest.jsonInt.ReviewsJson;

@Path("/reviews")
public class ReviewsHandler {
	@Context
	private ServletContext context;

	ApplicationProperties prop = ApplicationProperties.getInstance();
	final Logger log = Logger.getLogger(this.getClass());
	Users u = null;
	JsonHandler jh = new JsonHandler();
	String contextPath = null;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private String buildWhereCondition(ReviewsJson jsonIn)
	{
		String where = " WHERE ";
		String and = "";
				
		if (jsonIn.idReviews != 0)
		{
			where += and + " idReviews = " + jsonIn.idReviews;
			and = " AND ";
		}
		if (jsonIn.reviewerId != 0)
		{
			where += and + " reviewerId = " + jsonIn.reviewerId;
			and = " AND ";
		}
		if (jsonIn.reviewForId != 0)
		{
			where += and + " reviewForId = '" + jsonIn.reviewForId + "'";
			and = " AND ";
		}
		if (jsonIn.review != null)
		{
			where += and + " review like '%" + jsonIn.review + "'";
			and = " AND ";
		}
		if (jsonIn.status != null)
		{
			where += and + " a.status = '" + jsonIn.status + "'";
			and = " AND ";
		}
		if (jsonIn.created != null)
		{
			where += and + " a.created < '" + jsonIn.created + "'";
			and = " AND ";
		}
		if (jsonIn.updated != null)
		{
			where += and + " a.updated < '" + jsonIn.updated + "'";
			and = " AND ";
		}
		if (where.trim().compareTo("WHERE") == 0)
		{
			where = "";
		}
		return(where);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveAll(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token,
								@QueryParam("idReviews") int idReviews,
								@QueryParam("reviewerId") int reviewerId,
								@QueryParam("reviewForId") int reviewForId)
	{
		int languageId = Utils.setLanguageId(language);
		ReviewsJson jsonIn = new ReviewsJson();
		jsonIn.idReviews = idReviews;
		jsonIn.reviewerId = reviewerId;
		jsonIn.reviewForId = reviewForId;

		SessionData sd = SessionData.getInstance();
		if ((sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR) ||
			(idReviews != 0) || (reviewerId != 0) || (reviewForId != 0))
		{
			jsonIn.status = "A";
		}
		
		Reviews[] reviews = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving reviews");
			conn = DBInterface.connect();
			reviews = Reviews.search(conn, buildWhereCondition(jsonIn));
			log.trace("Retrieval completed");
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Reviews.search");
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		if (jh.jasonize(reviews, language) != Response.Status.OK)
		{
			log.error("Error '" + jh.json + "' jsonizing the hot event object");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json)
					.build();
		}
	
		log.trace("Returning an array of ");
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@GET
	@Path("/{idReviews}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveAll(@HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token,
								@PathParam("idReviews") int idReviews)
	{
		int languageId = Utils.setLanguageId(language);

		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}

		Reviews review = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving review");
			conn = DBInterface.connect();
			review = new Reviews(conn, idReviews, false);
			log.trace("Retrieval completed");
		}
		catch (Exception e) 
		{
			log.error("Exception '" + e.getMessage() + "' on Reviews.search");
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
		
		if (jh.jasonize(review, language) != Response.Status.OK)
		{
			log.error("Error '" + jh.json + "' jsonizing the hot event object");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(jh.json).build();
		}
	
		return Response.status(Response.Status.OK).entity(jh.json).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(ReviewsJson jsonIn, @HeaderParam("Language") String language,
								@HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		SessionData sd = SessionData.getInstance();

		Reviews review = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Review added");
			conn = DBInterface.TransactionStart();
			review = new Reviews();
			review.setReview(jsonIn.review);
			review.setReviewerId(sd.getBasicProfile(token).getIdUsers());
			review.setReviewForId(jsonIn.reviewForId);
			review.setRating(jsonIn.rating);
			review.setStatus("P");
			review.setCreated(new Date());
			review.setUpdated(review.getCreated());
			int id = review.insertAndReturnId(conn, "idReviews", review);
			
			PendingActions pa = new PendingActions();
			pa.setActionType("review");
			pa.setUserId(sd.getBasicProfile(token).getIdUsers());
			pa.setLink("rest/requests/reviews/" + id);
			pa.setCreated(review.getCreated());
			pa.setUpdated(pa.getCreated());
			pa.setStatus("P");
			pa.insert(conn, "idPendingActions", pa);

			DBInterface.TransactionCommit(conn);
			log.trace("Review added");
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on insert");
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.TransactionRollback(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}

	@PUT
	@Path("/{idReviews}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authorize(@PathParam("idReviews") int idReviews, @HeaderParam("Language") String language,
							  @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		
		Reviews review = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving review to update. Id " + idReviews);
			conn = DBInterface.TransactionStart();
			review = new Reviews(conn, idReviews, false);
			review.setStatus("A");
			review.update(conn, "idReviews");
			DBInterface.TransactionCommit(conn);
			log.trace("Status changed");
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on Reviews.search");
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.TransactionRollback(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}
	
	@DELETE
	@Path("/{idReviews}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("idReviews") int idReviews, @HeaderParam("Language") String language,
						   @HeaderParam("Authorization") String token)
	{
		int languageId = Utils.setLanguageId(language);

		SessionData sd = SessionData.getInstance();
		if (sd.getBasicProfile(token).getRoleId() != Roles.ADMINISTRATOR)
		{
			return Utils.jsonizeResponse(Response.Status.UNAUTHORIZED, null, languageId, "generic.unauthorized");
		}
		
		Reviews review = null;
		DBConnection conn = null;
		try 
		{
			log.trace("Retrieving review to update. Id " + idReviews);
			conn = DBInterface.TransactionStart();
			review = new Reviews(conn, idReviews, false);
			review.delete(conn, idReviews);
			DBInterface.TransactionCommit(conn);
			log.trace("Review deleted");
		}
		catch (Exception e) 
		{
			DBInterface.TransactionRollback(conn);
			log.error("Exception '" + e.getMessage() + "' on Reviews.search");
			return Utils.jsonizeResponse(Response.Status.INTERNAL_SERVER_ERROR, e, languageId, "generic.execError");
		}
		finally
		{
			DBInterface.TransactionRollback(conn);
		}
		return Response.status(Response.Status.OK).entity("{}").build();
	}
}
