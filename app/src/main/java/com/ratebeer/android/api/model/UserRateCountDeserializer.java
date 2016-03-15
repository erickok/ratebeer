package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.model.UserRateCount;

import java.lang.reflect.Type;

public final class UserRateCountDeserializer implements JsonDeserializer<UserRateCount> {

	@Override
	public UserRateCount deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		UserRateCount userRateCount = new UserRateCount();
		if (!(object.get("RateCount") instanceof JsonNull))
			userRateCount.rateCount = object.get("RateCount").getAsInt();
		if (!(object.get("PlaceRatings") instanceof JsonNull))
			userRateCount.placeCount = object.get("PlaceRatings").getAsInt();
		if (!(object.get("TickCount") instanceof JsonNull))
			userRateCount.tickCount = object.get("TickCount").getAsInt();
		return userRateCount;
	}

}
