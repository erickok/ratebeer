package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BeerDetailsDeserializer implements JsonDeserializer<BeerDetails> {

	@Override
	public BeerDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BeerDetails beerDetails = new BeerDetails();
		beerDetails.beerId = object.get("BeerID").getAsInt();
		beerDetails.beerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		beerDetails.brewerId = object.get("BrewerID").getAsInt();
		beerDetails.brewerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		beerDetails.brewerCountryId = object.get("BrewerCountryID").getAsInt();
		beerDetails.styleId = object.get("BeerStyleID").getAsInt();
		beerDetails.styleName= Normalizer.get().cleanHtml(object.get("BeerStyleName").getAsString());

		if (!(object.get("OverallPctl") instanceof JsonNull))
			beerDetails.overallPercentile = object.get("OverallPctl").getAsFloat();
		if (!(object.get("StylePctl") instanceof JsonNull))
			beerDetails.stylePercentile = object.get("StylePctl").getAsFloat();
		if (!(object.get("RealAverage") instanceof JsonNull))
			beerDetails.realRating = object.get("RealAverage").getAsFloat();
		if (!(object.get("AverageRating") instanceof JsonNull))
			beerDetails.weightedRating = object.get("AverageRating").getAsFloat();
		beerDetails.rateCount = object.get("RateCount").getAsInt();

		beerDetails.alcohol = object.get("Alcohol").getAsFloat();
		if (!(object.get("IBU") instanceof JsonNull))
			beerDetails.ibu = object.get("IBU").getAsFloat();
		beerDetails.alias = object.get("IsAlias").getAsBoolean();
		beerDetails.description = Normalizer.get().cleanHtml(object.get("Description").getAsString());

		return beerDetails;
	}

}
