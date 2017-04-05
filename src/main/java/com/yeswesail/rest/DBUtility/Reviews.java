package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

import com.yeswesail.rest.ApplicationProperties;
import com.yeswesail.rest.Constants;

public class Reviews extends DBInterface 
{
	private static final long serialVersionUID = 84894262980646849L;
	private ApplicationProperties prop = ApplicationProperties.getInstance();
	
	protected int idReviews;
	protected String review;
	protected int reviewerId;
	protected String reviewerName;
	protected String reviewerSurname;
	protected String reviewerURL;
	protected String targetName;
	protected String targetSurname;
	protected String targetURL;
	protected int reviewForId;
	protected Date created;
	protected Date updated;
	protected int rating;
	protected String status;
	
	private void setNames()
	{
		tableName = "Reviews";
		idColName = "idReviews";
	}

	public Reviews() throws Exception
	{
		setNames();
	}

	public Reviews(DBConnection conn, int id, boolean activeOnly) throws Exception
	{
		setNames();
		String sql = "SELECT a.*, " +
					 "b.name AS reviewerName, b.surname AS reviewerSurname, b.imageURL AS reviewerURL, " +
					 "c.name AS targetName, c.surname AS targetSurname, c.imageURL AS targetURL " +
					 "FROM ( Reviews AS a INNER JOIN Users AS b ON " +
					 "         b.idUsers = a.reviewerId " +
					 		   (activeOnly ? " AND a.status = '" + Constants.STATUS_ACTIVE + "' " : "") +
					 "     ) INNER JOIN Users AS c ON " +
					 "     c.idUsers = a.reviewForId " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public static Reviews[] search(DBConnection conn, String sqlWhere) throws Exception
	{
		sqlWhere =	"SELECT a.*, b.name AS reviewerName, b.surname AS reviewerSurname, " +
				 	"b.imageURL AS reviewerURL " +
					"FROM Reviews AS a INNER JOIN Users AS b ON " +
					"     b.idUsers = a.reviewerId " +
					sqlWhere;
		@SuppressWarnings("unchecked")
		ArrayList<Reviews> reviews = (ArrayList<Reviews>) Reviews.populateCollection(conn, sqlWhere, Reviews.class);
		return(reviews.toArray(new Reviews[reviews.size()]));
	}

	public int getIdReviews() {
		return idReviews;
	}

	public void setIdReviews(int idReviews) {
		this.idReviews = idReviews;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public int getReviewerId() {
		return reviewerId;
	}

	public void setReviewerId(int reviewerId) {
		this.reviewerId = reviewerId;
	}

	public int getReviewForId() {
		return reviewForId;
	}

	public void setReviewForId(int reviewForId) {
		this.reviewForId = reviewForId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getReviewerName() {
		return reviewerName;
	}

	public String getReviewerSurname() {
		return reviewerSurname;
	}

	public String getReviewerURL() {
		if ((reviewerURL == null) || reviewerURL.startsWith("http"))
		{
			return reviewerURL;
		}
		else
		{
			return prop.getWebHost() + "/" + reviewerURL;
		}
	}

	public String getTargetName() {
		return targetName;
	}

	public String getTargetSurname() {
		return targetSurname;
	}

	public String getTargetURL() {
		if ((targetURL == null) || targetURL.startsWith("http"))
		{
			return targetURL;
		}
		else
		{
			return prop.getWebHost() + "/" + targetURL;
		}
	}
	
}
