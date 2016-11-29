package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class CountryInfoDeserializer implements JsonDeserializer<CountryInfo> {

	@Override
	public CountryInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		CountryInfo countryInfo = new CountryInfo();

		countryInfo.countryId = object.get("CountryID").getAsInt();
		countryInfo.countryName = Normalizer.get().cleanHtml(object.get("Country").getAsString(), true);

		return countryInfo;
	}

}
