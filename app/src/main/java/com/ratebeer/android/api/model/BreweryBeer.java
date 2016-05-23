package com.ratebeer.android.api.model;

import android.support.annotation.NonNull;

import java.util.Locale;

public final class BreweryBeer implements Comparable<BreweryBeer> {

	public long beerId;
	public String beerName;
	public long brewerId;
	public String brewerName;
	public Long contractId;
	public String contractName;
	public long styleId;
	public String styleName;
	public Float overallPercentile;
	public Float stylePercentile;
	public Float weightedRating;
	public Float alcohol;
	public int rateCount;
	public boolean alias;
	public boolean retired;
	public Boolean ratedByUser;
	public Float ratingOfUser;

	public String getOverallPercentileString() {
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1$.0f", overallPercentile);
	}

	public boolean isContractBeer() {
		return contractId != null && contractName != null;
	}

	@Override
	public int compareTo(@NonNull BreweryBeer another) {
		// Sort contract beers after non-contract beers, with contract beers grouped by contract brewery name, then alphabetically by beer name
		if (isContractBeer() && !another.isContractBeer())
			return 1;
		else if (another.isContractBeer() && !isContractBeer())
			return 0;
		else if (isContractBeer() && !contractName.equalsIgnoreCase(another.contractName))
			return contractName.compareToIgnoreCase(contractName);
		return beerName == null ? -1 : beerName.compareTo(another.beerName);
	}

}
