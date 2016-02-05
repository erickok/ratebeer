package com.ratebeer.android.api.model;

import java.util.Locale;

public final class BeerSearchResult {

	public long beerId;
	public String beerName;
	public long brewerId;
	public String brewerName;
	public Float overallPercentile;
	public int rateCount;
	public boolean unrateable;
	public boolean alias;
	public boolean retired;
	public boolean ratedByUser;

	public String getOverallPercentileString() {
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1$.0f", overallPercentile);
	}

}
