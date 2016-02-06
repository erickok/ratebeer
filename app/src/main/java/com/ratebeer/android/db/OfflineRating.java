package com.ratebeer.android.db;

import java.util.Date;

public final class OfflineRating {

	public Long _id;
	public Integer offlineId;
	public Integer beerId;
	public String beerName;
	public Integer originalRatingId;
	public String originalRatingDate;
	public Integer appearance;
	public Integer aroma;
	public Integer taste;
	public Integer palate;
	public Integer overall;
	public String comments;
	public Date timeSaved;

}
