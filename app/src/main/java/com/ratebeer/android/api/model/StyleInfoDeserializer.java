package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class StyleInfoDeserializer implements JsonDeserializer<StyleInfo> {

	@Override
	public StyleInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		StyleInfo styleInfo = new StyleInfo();

		styleInfo.styleId = object.get("BeerStyleID").getAsInt();
		styleInfo.styleName = Normalizer.get().cleanHtml(object.get("BeerStyleName").getAsString());
		styleInfo.description = Normalizer.get().cleanHtml(object.get("BeerStyleDescription").getAsString());
		styleInfo.srmRange = Normalizer.get().cleanHtml(object.get("SRMRange").getAsString());

		return styleInfo;
	}

}
