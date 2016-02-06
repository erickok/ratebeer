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

	public boolean isUploaded() {
		return timeEntered != null;
	}

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

	public static Rating fromOfflineRating(OfflineRating offlineRating) {
		// Convert legacy offline rating into local rating database object
		Rating rating = new Rating();
		rating.beerId = offlineRating.beerId;
		rating.beerName = offlineRating.beerName;
		rating.brewerName = offlineRating.beerName; // Yeah, we don' have the brewer name, but it's only for legacy ratings anyway

		rating.aroma = offlineRating.aroma;
		rating.flavor = offlineRating.taste;
		rating.mouthfeel = offlineRating.palate;
		rating.appearance = offlineRating.appearance;
		rating.overall = offlineRating.overall;
		rating.total =
				calculateTotal(offlineRating.aroma, offlineRating.taste, offlineRating.palate, offlineRating.appearance, offlineRating.overall);
		rating.comments = offlineRating.comments;

		rating.timeCached = offlineRating.timeSaved;
		return rating;
	}

	public static Float calculateTotal(Integer aroma, Integer flavor, Integer mouthfeel, Integer appearance, Integer overall) {
		if (aroma == null || flavor == null || mouthfeel == null || appearance == null || overall == null)
			return null;
		return (float) (aroma + flavor + mouthfeel + appearance + overall) / 10F;
	}

}
