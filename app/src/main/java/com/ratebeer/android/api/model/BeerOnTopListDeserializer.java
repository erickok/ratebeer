package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BeerOnTopListDeserializer implements JsonDeserializer<BeerOnTopList> {

	@Override
	public BeerOnTopList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BeerOnTopList beerOnTopList = new BeerOnTopList();
		beerOnTopList.beerId = object.get("BeerID").getAsInt();
		beerOnTopList.beerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		if (object.has("BeerStyleID") && !(object.get("BeerStyleID") instanceof JsonNull))
			beerOnTopList.styleId = object.get("BeerStyleID").getAsInt();

		if (!(object.get("OverallPctl") instanceof JsonNull))
			beerOnTopList.overallPercentile = object.get("OverallPctl").getAsFloat();
		if (!(object.get("StylePctl") instanceof JsonNull))
			beerOnTopList.stylePercentile = object.get("StylePctl").getAsFloat();
		if (!(object.get("AverageRating") instanceof JsonNull))
			beerOnTopList.weightedRating = object.get("AverageRating").getAsFloat();
		beerOnTopList.rateCount = object.get("RateCount").getAsInt();
		if (object.has("HadIt") && !(object.get("HadIt") instanceof JsonNull))
			beerOnTopList.ratedByUser = object.get("HadIt").getAsInt() == 1;
		return beerOnTopList;
	}

}
