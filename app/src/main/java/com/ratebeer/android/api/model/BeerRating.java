package com.ratebeer.android.api.model;

import java.util.Date;

public final class BeerRating {

	public int ratingId;
	public int userId;
	public String userName;
	public int userCountryId;
	public String userCountryName;
	public int userRateCount;

	public int aroma;
	public int flavor;
	public int mouthfeel;
	public int appearance;
	public int overall;
	public float total;
	public String comments;
	public Date timeEntered;
	public Date timeUpdated;

}
