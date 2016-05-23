package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BreweryDetailsDeserializer implements JsonDeserializer<BreweryDetails> {

	@Override
	public BreweryDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BreweryDetails breweryDetails = new BreweryDetails();
		breweryDetails.brewerId = object.get("BrewerID").getAsInt();
		breweryDetails.brewerName = Normalizer.get().cleanHtml(object.get("BrewerName").getAsString());
		breweryDetails.brewerType = object.get("BrewerTypeID").getAsInt();
		breweryDetails.retired = object.get("retired").getAsBoolean();

		breweryDetails.address = Normalizer.get().cleanHtml(object.get("BrewerAddress").getAsString());
		breweryDetails.city = Normalizer.get().cleanHtml(object.get("BrewerCity").getAsString());
		if (object.has("BrewerZipCode") && !(object.get("BrewerZipCode") instanceof JsonNull))
			breweryDetails.postalCode = Normalizer.get().cleanHtml(object.get("BrewerZipCode").getAsString());
		if (object.has("BrewerCountryID") && !(object.get("BrewerCountryID") instanceof JsonNull))
			breweryDetails.countryId = object.get("BrewerCountryID").getAsInt();
		if (object.has("BrewerStateID") && !(object.get("BrewerStateID") instanceof JsonNull))
			breweryDetails.stateId = object.get("BrewerStateID").getAsInt();
		if (object.has("BrewerPhone") && !(object.get("BrewerPhone") instanceof JsonNull))
			breweryDetails.phoneNumber = Normalizer.get().cleanHtml(object.get("BrewerPhone").getAsString());
		if (object.has("BrewerEmail") && !(object.get("BrewerEmail") instanceof JsonNull))
			breweryDetails.email = Normalizer.get().cleanHtml(object.get("BrewerEmail").getAsString());
		if (object.has("BrewerWebSite") && !(object.get("BrewerWebSite") instanceof JsonNull))
			breweryDetails.websiteUrl = Normalizer.get().cleanHtml(object.get("BrewerWebSite").getAsString());
		if (object.has("Facebook") && !(object.get("Facebook") instanceof JsonNull))
			breweryDetails.facebook = Normalizer.get().cleanHtml(object.get("Facebook").getAsString());
		if (object.has("Twitter") && !(object.get("Twitter") instanceof JsonNull))
			breweryDetails.twitter = Normalizer.get().cleanHtml(object.get("Twitter").getAsString());

		return breweryDetails;
	}

}
