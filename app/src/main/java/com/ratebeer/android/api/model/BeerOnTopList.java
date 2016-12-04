package com.ratebeer.android.api.model;

import java.util.Locale;

public final class BeerOnTopList {

	public long beerId;
	public String beerName;
	public long styleId;
	public Float weightedRating;
	public Float overallPercentile;
	public Float stylePercentile;
	public int rateCount;
	public boolean ratedByUser;

	public String getOverallPercentileString() {
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1$.0f", overallPercentile);
	}

	public String getStylePercentileString() {
		if (stylePercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1$.0f", stylePercentile);
	}

}
