package edu.njit.cs643.group7.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

	private String user_id;
	private String name;
	private Long review_count;
	private String yelping_since;
	private List<String> friends;
	private Long useful;
	private Long funny;
	private Long cool;
	private Long fans;
	private List<Long> elite;
	private Double average_stars;
	private Long compliment_hot;
	private Long compliment_more;
	private Long compliment_profile;
	private Long compliment_cute;
	private Long compliment_list;
	private Long compliment_note;
	private Long compliment_plain;
	private Long compliment_cool;
	private Long compliment_funny;
	private Long compliment_writer;
	private Long compliment_photos;
	private String type;

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getReview_count() {
		return review_count;
	}

	public void setReview_count(Long review_count) {
		this.review_count = review_count;
	}

	public String getYelping_since() {
		return yelping_since;
	}

	public void setYelping_since(String yelping_since) {
		this.yelping_since = yelping_since;
	}

	public List<String> getFriends() {
		return friends;
	}

	public void setFriends(List<String> friends) {
		this.friends = friends;
	}

	public Long getUseful() {
		return useful;
	}

	public void setUseful(Long useful) {
		this.useful = useful;
	}

	public Long getFunny() {
		return funny;
	}

	public void setFunny(Long funny) {
		this.funny = funny;
	}

	public Long getCool() {
		return cool;
	}

	public void setCool(Long cool) {
		this.cool = cool;
	}

	public Long getFans() {
		return fans;
	}

	public void setFans(Long fans) {
		this.fans = fans;
	}

	public List<Long> getElite() {
		return elite;
	}

	public void setElite(List<Long> elite) {
		this.elite = elite;
	}

	public Double getAverage_stars() {
		return average_stars;
	}

	public void setAverage_stars(Double average_stars) {
		this.average_stars = average_stars;
	}

	public Long getCompliment_hot() {
		return compliment_hot;
	}

	public void setCompliment_hot(Long compliment_hot) {
		this.compliment_hot = compliment_hot;
	}

	public Long getCompliment_more() {
		return compliment_more;
	}

	public void setCompliment_more(Long compliment_more) {
		this.compliment_more = compliment_more;
	}

	public Long getCompliment_profile() {
		return compliment_profile;
	}

	public void setCompliment_profile(Long compliment_profile) {
		this.compliment_profile = compliment_profile;
	}

	public Long getCompliment_cute() {
		return compliment_cute;
	}

	public void setCompliment_cute(Long compliment_cute) {
		this.compliment_cute = compliment_cute;
	}

	public Long getCompliment_list() {
		return compliment_list;
	}

	public void setCompliment_list(Long compliment_list) {
		this.compliment_list = compliment_list;
	}

	public Long getCompliment_note() {
		return compliment_note;
	}

	public void setCompliment_note(Long compliment_note) {
		this.compliment_note = compliment_note;
	}

	public Long getCompliment_plain() {
		return compliment_plain;
	}

	public void setCompliment_plain(Long compliment_plain) {
		this.compliment_plain = compliment_plain;
	}

	public Long getCompliment_cool() {
		return compliment_cool;
	}

	public void setCompliment_cool(Long compliment_cool) {
		this.compliment_cool = compliment_cool;
	}

	public Long getCompliment_funny() {
		return compliment_funny;
	}

	public void setCompliment_funny(Long compliment_funny) {
		this.compliment_funny = compliment_funny;
	}

	public Long getCompliment_writer() {
		return compliment_writer;
	}

	public void setCompliment_writer(Long compliment_writer) {
		this.compliment_writer = compliment_writer;
	}

	public Long getCompliment_photos() {
		return compliment_photos;
	}

	public void setCompliment_photos(Long compliment_photos) {
		this.compliment_photos = compliment_photos;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
