package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class PlaceDetailsDeserializer implements JsonDeserializer<PlaceDetails> {

	@Override
	public PlaceDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		PlaceDetails placeDetails = new PlaceDetails();
		placeDetails.placeId = object.get("PlaceID").getAsInt();
		placeDetails.placeName = Normalizer.get().cleanHtml(object.get("PlaceName").getAsString());
		placeDetails.placeType = object.get("PlaceType").getAsInt();

		placeDetails.address = Normalizer.get().cleanHtml(object.get("Address").getAsString());
		placeDetails.city = Normalizer.get().cleanHtml(object.get("City").getAsString());
		if (object.has("PostalCode") && !(object.get("PostalCode") instanceof JsonNull))
			placeDetails.postalCode = Normalizer.get().cleanHtml(object.get("PostalCode").getAsString());
		if (object.has("CountryID") && !(object.get("CountryID") instanceof JsonNull))
			placeDetails.countryId = object.get("CountryID").getAsInt();
		if (object.has("StateId") && !(object.get("StateID") instanceof JsonNull))
			placeDetails.stateId = object.get("StateID").getAsInt();
		placeDetails.address = Normalizer.get().cleanHtml(object.get("Address").getAsString());
		placeDetails.phoneNumber = Normalizer.get().cleanHtml(object.get("PhoneNumber").getAsString());
		if (object.has("WebSiteURL") && !(object.get("WebSiteURL") instanceof JsonNull))
			placeDetails.websiteUrl = Normalizer.get().cleanHtml(object.get("WebSiteURL").getAsString());
		if (object.has("Facebook") && !(object.get("Facebook") instanceof JsonNull))
			placeDetails.facebook = Normalizer.get().cleanHtml(object.get("Facebook").getAsString());
		if (object.has("Twitter") && !(object.get("Twitter") instanceof JsonNull))
			placeDetails.twitter = Normalizer.get().cleanHtml(object.get("Twitter").getAsString());

		if (object.has("Taps") && !(object.get("Taps") instanceof JsonNull))
			placeDetails.taps = Normalizer.get().cleanHtml(object.get("Taps").getAsString());
		if (object.has("Bottles") && !(object.get("Bottles") instanceof JsonNull))
			placeDetails.bottles = Normalizer.get().cleanHtml(object.get("Bottles").getAsString());
		if (object.has("Hours") && !(object.get("Hours") instanceof JsonNull))
			placeDetails.hours = Normalizer.get().cleanHtml(object.get("Hours").getAsString());
		if (object.has("UserID") && !(object.get("UserID") instanceof JsonNull))
			placeDetails.userId = object.get("UserID").getAsLong();
		if (object.has("BrewerID") && !(object.get("BrewerID") instanceof JsonNull))
			placeDetails.brewerId = object.get("BrewerID").getAsLong();
		if (object.has("Retired") && !(object.get("Retired") instanceof JsonNull))
			placeDetails.retired = object.get("Retired").getAsBoolean();

		if (object.has("AvgRating") && !(object.get("AvgRating") instanceof JsonNull))
			placeDetails.averageRating = object.get("AvgRating").getAsFloat();
		if (object.has("BayMean") && !(object.get("BayMean") instanceof JsonNull))
			placeDetails.baysianMean = object.get("BayMean").getAsFloat();
		if (object.has("Percentile") && !(object.get("Percentile") instanceof JsonNull))
			placeDetails.percentile = object.get("Percentile").getAsFloat();
		if (object.has("RateCount") && !(object.get("RateCount") instanceof JsonNull))
			placeDetails.rateCount = object.get("RateCount").getAsInt();

		placeDetails.latitude = object.get("Latitude").getAsFloat();
		placeDetails.longitude = object.get("Longitude").getAsFloat();

		return placeDetails;
	}

}
