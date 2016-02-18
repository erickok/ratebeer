package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BeerRatingDeserializer implements JsonDeserializer<BeerRating> {

	@Override
	public BeerRating deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BeerRating userRating = new BeerRating();
		userRating.ratingId = object.get("RatingID").getAsInt();
		userRating.userId = object.get("UserID").getAsInt();
		userRating.userName = Normalizer.get().cleanHtml(object.get("UserName").getAsString());
		userRating.userCountryId = object.get("CountryID").getAsInt();
		if (object.get("Country") != null && !(object.get("Country") instanceof JsonNull))
			userRating.userCountryName = Normalizer.get().cleanHtml(object.get("Country").getAsString());
		userRating.userRateCount = object.get("RateCount").getAsInt();

		userRating.aroma = object.get("Aroma").getAsInt();
		userRating.flavor = object.get("Flavor").getAsInt();
		userRating.mouthfeel = object.get("Mouthfeel").getAsInt();
		userRating.appearance = object.get("Appearance").getAsInt();
		userRating.overall = object.get("Overall").getAsInt();
		userRating.total = object.get("TotalScore").getAsFloat();
		userRating.comments = Normalizer.get().cleanHtml(object.get("Comments").getAsString());
		userRating.timeEntered = Normalizer.get().parseTime(object.get("TimeEntered").getAsString());
		if (!(object.get("TimeUpdated") instanceof JsonNull))
			userRating.timeUpdated = Normalizer.get().parseTime(object.get("TimeUpdated").getAsString());
		return userRating;
	}

}
