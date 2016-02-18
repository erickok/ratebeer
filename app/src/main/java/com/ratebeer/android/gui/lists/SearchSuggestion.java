package com.ratebeer.android.gui.lists;

import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.HistoricSearch;
import com.ratebeer.android.db.Rating;

public final class SearchSuggestion {

	public Long beerId;
	public String suggestion;

	private SearchSuggestion() {}

	public static SearchSuggestion fromHistoricSearch(HistoricSearch historicSearch) {
		SearchSuggestion suggestion = new SearchSuggestion();
		// NOTE No beer id attached
		suggestion.suggestion = historicSearch.name;
		return suggestion;
	}

	public static SearchSuggestion fromBeer(Beer beer) {
		SearchSuggestion suggestion = new SearchSuggestion();
		suggestion.beerId = beer._id;
		suggestion.suggestion = beer.name;
		return suggestion;
	}

	public static SearchSuggestion fromRating(Rating rating) {
		SearchSuggestion suggestion = new SearchSuggestion();
		suggestion.beerId = rating.beerId;
		suggestion.suggestion = rating.beerName;
		return suggestion;
	}

}
