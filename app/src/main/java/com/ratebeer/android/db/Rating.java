package com.ratebeer.android.db;

import com.ratebeer.android.api.model.BeerRating;

import java.util.Date;

public final class Rating {

	public Long _id;
	public long beerId;
	public String beerName;
	public String brewerName;

	public Integer aroma;
	public Integer flavor;
	public Integer mouthfeel;
	public Integer appearance;
	public Integer overall;
	public Float total;
	public String comments;

	public Date timeCached;
	public Date timeEntered;
	public Date timeUpdated;

	public static Rating fromBeerRating(Beer beer, BeerRating beerRating) {
		Rating rating = new Rating();
		rating._id = (long) beerRating.ratingId;
		rating.beerId = beer._id;
		rating.beerName = beer.name;
		rating.brewerName = beer.brewerName;

		rating.aroma = beerRating.aroma;
		rating.flavor = beerRating.flavor;
		rating.mouthfeel = beerRating.mouthfeel;
		rating.appearance = beerRating.appearance;
		rating.overall = beerRating.overall;
		rating.total = beerRating.total;
		rating.comments = beerRating.comments;

		rating.timeCached = new Date();
		rating.timeEntered = beerRating.timeEntered;
		rating.timeUpdated = beerRating.timeUpdated;
		return rating;
	}

}
