package com.ratebeer.android.api.model;

import java.util.Date;

public final class BeerRating {

	public int ratingId;
	public int userId;
	public String userName;
	public int userCountryId;
	public String userCountryName;
	public int userRateCount;

	public Integer aroma;
	public Integer flavor;
	public Integer mouthfeel;
	public Integer appearance;
	public Integer overall;
	public Float total;
	public String comments;
	public Date timeEntered;
	public Date timeUpdated;

}
