package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class BarcodeSearchResultDeserializer implements JsonDeserializer<BarcodeSearchResult> {

	@Override
	public BarcodeSearchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		BarcodeSearchResult barcodeSearchResult = new BarcodeSearchResult();
		barcodeSearchResult.beerId = object.get("BeerID").getAsInt();
		barcodeSearchResult.beerName = Normalizer.get().cleanHtml(object.get("BeerName").getAsString());
		barcodeSearchResult.brewerId = object.get("BrewerID").getAsInt();
		barcodeSearchResult.brewerName = Normalizer.get().cleanHtml(object.get("BrewerName").getAsString());

		if (!(object.get("AverageRating") instanceof JsonNull))
			barcodeSearchResult.weightedRating = object.get("AverageRating").getAsFloat();
		if (object.has("alcohol") && !(object.get("alcohol") instanceof JsonNull))
			barcodeSearchResult.alcohol = object.get("alcohol").getAsFloat();
		return barcodeSearchResult;
	}

}
