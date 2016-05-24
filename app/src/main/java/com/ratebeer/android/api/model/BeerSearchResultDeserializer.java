package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BeerSearchResultDeserializer implements JsonDeserializer<BeerSearchResult> {

	@Override
	public BeerSearchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BeerSearchResult beerSearchResult = new BeerSearchResult();
		beerSearchResult.beerId = object.get("BeerID").getAsInt();
		beerSearchResult.beerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		beerSearchResult.brewerId = object.get("BrewerID").getAsInt();

		if (!(object.get("OverallPctl") instanceof JsonNull))
			beerSearchResult.overallPercentile = object.get("OverallPctl").getAsFloat();
		beerSearchResult.rateCount = object.get("RateCount").getAsInt();
		beerSearchResult.unrateable = object.get("Unrateable").getAsBoolean();
		if (object.has("IsAlias") && !(object.get("IsAlias") instanceof JsonNull))
			beerSearchResult.alias = object.get("IsAlias").getAsBoolean();
		beerSearchResult.retired = object.get("Retired").getAsBoolean();
		beerSearchResult.ratedByUser = object.get("IsRated").getAsInt() == 1;
		return beerSearchResult;
	}

}
