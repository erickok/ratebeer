package com.ratebeer.android.db;

import java.util.Date;

public final class Rating {

	public Long _id;
	public long beerId;

	public Integer ratingId;
	public Integer aroma;
	public Integer flavor;
	public Integer mouthfeel;
	public Integer appearance;
	public Integer overall;
	public Float total;
	public String comments;

	public Date timeLoaded;
	public Date timeEntered;
	public Date timeUpdated;

}
