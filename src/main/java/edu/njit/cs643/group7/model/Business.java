package edu.njit.cs643.group7.model;

import java.io.Serializable;
import java.util.List;

public class Business implements Serializable {

	private String business_id;
	private String name;
	private String neighborhood;
	private String address;
	private String city;
	private String state;
	private String postal_code;
	private Double latitude;
	private Double longitude;
	private Double stars;
	private Long review_count;
	private Long is_open;
	private List<String> attributes;
	private List<String> categories;
	private List<String> hours;
	private String type;

	public Business() {
	}

	public String getBusiness_id() {
		return business_id;
	}

	public void setBusiness_id(String business_id) {
		this.business_id = business_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostal_code() {
		return postal_code;
	}

	public void setPostal_code(String postalCode) {
		this.postal_code = postalCode;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getStars() {
		return stars;
	}

	public void setStars(Double stars) {
		this.stars = stars;
	}

	public Long getReview_count() {
		return review_count;
	}

	public void setReview_count(Long review_count) {
		this.review_count = review_count;
	}

	public Long getIs_open() {
		return is_open;
	}

	public void setIs_open(Long isOpen) {
		this.is_open = isOpen;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getHours() {
		return hours;
	}

	public void setHours(List<String> hours) {
		this.hours = hours;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Business {" + "name='" + name + '\'' + ", stars='" + stars + '\'' + '}';
	}

}
