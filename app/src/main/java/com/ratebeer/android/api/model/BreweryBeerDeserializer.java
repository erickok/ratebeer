package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BreweryBeerDeserializer implements JsonDeserializer<BreweryBeer> {

	@Override
	public BreweryBeer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BreweryBeer breweryBeer = new BreweryBeer();

		breweryBeer.beerId = object.get("BeerID").getAsInt();
		breweryBeer.beerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		breweryBeer.brewerId = object.get("BrewerID").getAsInt();
		breweryBeer.brewerName = Normalizer.get().cleanHtml(object.get("BrewerName").getAsString());
		if (!(object.get("ContractBrewerID") instanceof JsonNull))
			breweryBeer.contractId = object.get("ContractBrewerID").getAsLong();
		if (!(object.get("ContractBrewer") instanceof JsonNull))
			breweryBeer.contractName = Normalizer.get().cleanHtml(object.get("ContractBrewer").getAsString());
		breweryBeer.styleId = object.get("BeerStyleID").getAsInt();
		breweryBeer.styleName = Normalizer.get().cleanHtml(object.get("BeerStyleName").getAsString());

		if (!(object.get("OverallPctl") instanceof JsonNull))
			breweryBeer.overallPercentile = object.get("OverallPctl").getAsFloat();
		if (!(object.get("StylePctl") instanceof JsonNull))
			breweryBeer.stylePercentile = object.get("StylePctl").getAsFloat();
		if (!(object.get("AverageRating") instanceof JsonNull))
			breweryBeer.weightedRating = object.get("AverageRating").getAsFloat();
		breweryBeer.rateCount = object.get("RateCount").getAsInt();

		breweryBeer.alcohol = object.get("Alcohol").getAsFloat();
		breweryBeer.alias = object.get("IsAlias").getAsBoolean();
		breweryBeer.retired = object.get("Retired").getAsBoolean();
		if (!(object.get("UserHadIt") instanceof JsonNull))
			breweryBeer.ratedByUser = object.get("UserHadIt").getAsInt() == 1;
		if (!(object.get("UserRating") instanceof JsonNull))
			breweryBeer.ratingOfUser = object.get("UserRating").getAsFloat();

		return breweryBeer;
	}

}
