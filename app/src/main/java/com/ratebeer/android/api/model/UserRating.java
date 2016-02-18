package com.ratebeer.android.api.model;

import java.util.Date;

public final class UserRating {

	public int beerId;
	public String beerName;
	public int beerStyleId;
	public String beerStyleName;
	public int brewerId;
	public String brewerName;

	public float averageRating;
	public Float overallPercentile;
	public Float stylePercentile;
	public int rateCount;

	public int ratingId;
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
