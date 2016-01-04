package com.ratebeer.android.db;

import java.util.Date;

public class Rating {

	public Long _id;
	public int beerId;

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
