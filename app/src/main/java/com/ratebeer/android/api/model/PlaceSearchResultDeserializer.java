package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class PlaceSearchResultDeserializer implements JsonDeserializer<PlaceSearchResult> {

	@Override
	public PlaceSearchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		PlaceSearchResult placeSearchResult = new PlaceSearchResult();
		placeSearchResult.placeId = object.get("PlaceID").getAsInt();
		placeSearchResult.placeName = Normalizer.get().cleanHtml(object.get("PlaceName").getAsString());
		placeSearchResult.placeType = object.get("PlaceType").getAsInt();
		placeSearchResult.city = Normalizer.get().cleanHtml(object.get("City").getAsString());
		if (object.has("CountryID") && !(object.get("CountryID") instanceof JsonNull))
			placeSearchResult.countryId = object.get("CountryID").getAsInt();
		if (object.has("StateId") && !(object.get("StateID") instanceof JsonNull))
			placeSearchResult.stateId = object.get("StateID").getAsInt();
		if (object.has("Percentile") && !(object.get("Percentile") instanceof JsonNull))
			placeSearchResult.overallPercentile = object.get("Percentile").getAsFloat();
		if (object.has("AvgRating") && !(object.get("AvgRating") instanceof JsonNull))
			placeSearchResult.averageRating = object.get("AvgRating").getAsFloat();
		if (object.has("RateCount") && !(object.get("RateCount") instanceof JsonNull))
			placeSearchResult.rateCount = object.get("RateCount").getAsInt();
		return placeSearchResult;
	}

}
