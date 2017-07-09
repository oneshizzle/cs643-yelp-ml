package edu.njit.cs643.group7.model;

public class Review {

	private String review_id;
	private String user_id;
	private String business_id;
	private String stars;
	private String date;
	private String text;
	private String useful;
	private String funny;
	private String cool;
	private String type;
	
	public String getReview_id() {
		return review_id;
	}
	public void setReview_id(String review_id) {
		this.review_id = review_id;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getBusiness_id() {
		return business_id;
	}
	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}
	public String getStars() {
		return stars;
	}
	public void setStars(String stars) {
		this.stars = stars;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUseful() {
		return useful;
	}
	public void setUseful(String useful) {
		this.useful = useful;
	}
	public String getFunny() {
		return funny;
	}
	public void setFunny(String funny) {
		this.funny = funny;
	}
	public String getCool() {
		return cool;
	}
	public void setCool(String cool) {
		this.cool = cool;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
