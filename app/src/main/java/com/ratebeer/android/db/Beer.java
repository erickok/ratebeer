package com.ratebeer.android.db;

import com.ratebeer.android.api.model.BeerSearchResult;

import java.util.Date;

public final class Beer {

	public Long _id;
	public String name;
	public Long beerStyleId;
	public Long brewerId;
	public Long ratingId;

	public Boolean unrateable;
	public Boolean alias;
	public Boolean retired;

	public Float averageRating;
	public Float overallPercentile;
	public Float stylePercentile;
	public Integer rateCount;

	public Date timeLoaded;

	public static Beer fromSearchResult(BeerSearchResult result) {
		Beer beer = new Beer();
		beer._id = result.beerId;
		beer.name = result.beerName;
		beer.brewerId = result.brewerId;
		beer.overallPercentile = result.overallPercentile;
		beer.rateCount = result.rateCount;
		beer.unrateable = result.unrateable;
		beer.alias = result.alias;
		beer.retired = result.retired;
		return beer;
	}
}
