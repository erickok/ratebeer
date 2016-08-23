package com.ratebeer.android.db;

import com.ratebeer.android.api.model.BeerDetails;

import java.util.Date;
import java.util.Locale;

public final class Beer {

	private static final float CALORIES_PER_FLOZ = 30f;

	public Long _id;
	public String name;
	public Long styleId;
	public String styleName;
	public Long brewerId;
	public String brewerName;
	public Long brewerCountryId;

	public Float alcohol;
	public Float ibu;
	public String description;
	public Boolean alias;

	public Float realRating;
	public Float weightedRating;
	public Float overallPercentile;
	public Float stylePercentile;
	public int rateCount;

	public Date timeCached;

	public static Beer fromDetails(BeerDetails details) {
		Beer beer = new Beer();
		beer._id = details.beerId;
		beer.name = details.beerName;
		beer.styleId = details.styleId;
		beer.styleName = details.styleName;
		beer.brewerId = details.brewerId;
		beer.brewerName = details.brewerName;
		beer.brewerCountryId = details.brewerCountryId;

		beer.alcohol = details.alcohol;
		beer.ibu = details.ibu;
		beer.description = details.description;
		beer.alias = details.alias;

		beer.realRating = details.realRating;
		beer.stylePercentile = details.stylePercentile;
		beer.weightedRating = details.weightedRating;
		beer.overallPercentile = details.overallPercentile;
		beer.stylePercentile = details.stylePercentile;
		beer.rateCount = details.rateCount;

		beer.timeCached = new Date();
		return beer;
	}

	public String getRealRatingString() {
		if (realRating == null)
			return "-";
		return String.format(Locale.getDefault(), "%1.1f", realRating);
	}

	public String getWeightedRatingString() {
		if (weightedRating == null)
			return "-";
		return String.format(Locale.getDefault(), "%1.1f", weightedRating);
	}

	public String getOverallPercentileString() {
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1.0f", overallPercentile);
	}

	public String getStylePercentileString() {
		if (stylePercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1.0f", stylePercentile);
	}

	public String getRateCountString() {
		return String.format(Locale.getDefault(), "%1$d", rateCount);
	}

	public String getAlcoholString() {
		if (alcohol == null || alcohol == 0)
			return "-";
		return String.format(Locale.getDefault(), "%s", alcohol);
	}

	public String getIbuString() {
		if (ibu == null || ibu == 0)
			return "-";
		return String.format(Locale.getDefault(), "%1.0f", ibu);
	}

	public String getCaloriesString() {
		if (alcohol == null || alcohol == 0)
			return "-";
		return String.format(Locale.getDefault(), "%1.0f", alcohol * CALORIES_PER_FLOZ);
	}

	public boolean isAlias() {
		return alias != null && alias;
	}

}
