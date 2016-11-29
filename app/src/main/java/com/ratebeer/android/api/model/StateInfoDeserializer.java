package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class StateInfoDeserializer implements JsonDeserializer<StateInfo> {

	@Override
	public StateInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		StateInfo stateInfo = new StateInfo();

		stateInfo.countryId = object.get("CountryID").getAsInt();
		stateInfo.stateId = object.get("CountryID").getAsInt();
		stateInfo.stateName = Normalizer.get().cleanHtml(object.get("Country").getAsString(), true);
		if (object.has("Abbrev") && !(object.get("Abbrev") instanceof JsonNull))
			stateInfo.abbreviation = Normalizer.get().cleanHtml(object.get("Abbrev").getAsString(), true);

		return stateInfo;
	}

}
