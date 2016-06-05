package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;
import java.util.Date;

public class Reviews extends DBInterface 
{
	private static final long serialVersionUID = 84894262980646849L;

	protected int idReviews;
	protected String review;
	protected int reviewerId;
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

	public Reviews(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	public static Reviews[] search(DBConnection conn, String sqlWhere) throws Exception
	{
		sqlWhere = "SELECT * " +
				   "FROM Reviews " + sqlWhere;
		@SuppressWarnings("unchecked")
		ArrayList<Reviews> reviews = (ArrayList<Reviews>) Reviews.populateCollection(sqlWhere, Reviews.class);
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
}
