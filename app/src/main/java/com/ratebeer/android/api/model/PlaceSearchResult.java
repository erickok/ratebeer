package com.ratebeer.android.api.model;

import java.util.Locale;

public final class PlaceSearchResult {

	public long placeId;
	public String placeName;
	public int placeType;
	public String city;
	public Integer countryId;
	public Integer stateId;
	public Float overallPercentile;
	public Float averageRating;
	public int rateCount;

	public String getOverallPercentileString() {
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1$.0f", overallPercentile);
	}

}
