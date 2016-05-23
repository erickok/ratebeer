package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public final class PlaceCheckinResultDeserializer implements JsonDeserializer<PlaceCheckinResult> {

	@Override
	public PlaceCheckinResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		// NOTE Success looks like:
		//   {"OK":"User has checked into The World of Drinks Krabbendijke ."}
		// NOTE Error looks like:
		//   {"Error":"User has already checked into The World of Drinks Krabbendijke recently."}
		PlaceCheckinResult placeCheckinResult = new PlaceCheckinResult();
		if (object.has("OK") && !(object.get("OK") instanceof JsonNull))
			placeCheckinResult.okResult = object.get("OK").getAsString();
		if (object.has("Error") && !(object.get("Error") instanceof JsonNull))
			placeCheckinResult.errorResult = object.get("Error").getAsString();
		return placeCheckinResult;
	}

}
