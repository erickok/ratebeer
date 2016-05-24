package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BrewerySearchResultDeserializer implements JsonDeserializer<BrewerySearchResult> {

	@Override
	public BrewerySearchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BrewerySearchResult brewerySearchResult = new BrewerySearchResult();

		brewerySearchResult.brewerId = object.get("BrewerID").getAsInt();
		brewerySearchResult.brewerName = Normalizer.get().cleanHtml(object.get("BrewerName").getAsString());
		brewerySearchResult.city = Normalizer.get().cleanHtml(object.get("BrewerCity").getAsString());
		brewerySearchResult.countryId = object.get("CountryID").getAsInt();
		if (object.has("StateID") && !(object.get("StateID") instanceof JsonNull))
			brewerySearchResult.stateId = object.get("StateID").getAsInt();

		return brewerySearchResult;
	}

}
