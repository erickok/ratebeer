package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class PlaceNearbyDeserializer implements JsonDeserializer<PlaceNearby> {

	@Override
	public PlaceNearby deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		PlaceNearby placeNearby = new PlaceNearby();
		placeNearby.placeId = object.get("PlaceID").getAsInt();
		placeNearby.placeName = Normalizer.get().cleanHtml(object.get("PlaceName").getAsString());
		placeNearby.placeType = object.get("PlaceType").getAsInt();

		placeNearby.address = Normalizer.get().cleanHtml(object.get("Address").getAsString());
		placeNearby.city = Normalizer.get().cleanHtml(object.get("City").getAsString());
		if (object.has("PostalCode") && !(object.get("PostalCode") instanceof JsonNull))
			placeNearby.postalCode = Normalizer.get().cleanHtml(object.get("PostalCode").getAsString());
		if (object.has("CountryID") && !(object.get("CountryID") instanceof JsonNull))
			placeNearby.countryId = object.get("CountryID").getAsInt();
		if (object.has("StateId") && !(object.get("StateID") instanceof JsonNull))
			placeNearby.stateId = object.get("StateID").getAsInt();
		placeNearby.address = Normalizer.get().cleanHtml(object.get("Address").getAsString());
		placeNearby.phoneNumber = Normalizer.get().cleanHtml(object.get("PhoneNumber").getAsString());

		if (object.has("AvgRating") && !(object.get("AvgRating") instanceof JsonNull))
			placeNearby.averageRating = object.get("AvgRating").getAsFloat();
		if (object.has("RateCount") && !(object.get("RateCount") instanceof JsonNull))
			placeNearby.rateCount = object.get("RateCount").getAsInt();

		placeNearby.latitude = object.get("Latitude").getAsFloat();
		placeNearby.longitude = object.get("Longitude").getAsFloat();

		return placeNearby;
	}

}
