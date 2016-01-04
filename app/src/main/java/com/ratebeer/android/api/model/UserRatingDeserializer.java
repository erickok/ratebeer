package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class UserRatingDeserializer implements JsonDeserializer<UserRating> {

	@Override
	public UserRating deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		UserRating userRating = new UserRating();
		userRating.beerId = object.get("BeerID").getAsInt();
		userRating.beerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		userRating.beerStyleId = object.get("BeerStyleID").getAsInt();
		userRating.beerStyleName = Normalizer.get().cleanHtml(object.get("BeerStyleName").getAsString());
		userRating.brewerId = object.get("BrewerID").getAsInt();
		userRating.brewerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());

		userRating.averageRating = object.get("AverageRating").getAsFloat();
		userRating.overallPercentile = object.get("OverallPctl").getAsFloat();
		userRating.stylePercentile = object.get("StylePctl").getAsFloat();
		userRating.rateCount = object.get("RateCount").getAsInt();

		userRating.ratingId = object.get("RatingID").getAsInt();
		userRating.aroma = object.get("Aroma").getAsInt();
		userRating.flavor = object.get("Flavor").getAsInt();
		userRating.mouthfeel = object.get("Mouthfeel").getAsInt();
		userRating.appearance = object.get("Appearance").getAsInt();
		userRating.overall = object.get("Overall").getAsInt();
		userRating.total = object.get("TotalScore").getAsFloat();
		userRating.comments = Normalizer.get().cleanHtml(object.get("Comments").getAsString());
		userRating.timeEntered = Normalizer.get().parseTime(object.get("TimeEntered").getAsString());
		userRating.timeUpdated = Normalizer.get().parseTime(object.get("TimeUpdated").getAsString());
		return userRating;
	}

}
