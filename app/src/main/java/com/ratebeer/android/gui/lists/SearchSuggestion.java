package com.ratebeer.android.gui.lists;

import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.Brewery;
import com.ratebeer.android.db.HistoricSearch;
import com.ratebeer.android.db.Place;
import com.ratebeer.android.db.Rating;

public final class SearchSuggestion {

	public static final int TYPE_HISTORY = 0;
	public static final int TYPE_BEER = 1;
	public static final int TYPE_RATING = 2;
	public static final int TYPE_BREWERY = 3;
	public static final int TYPE_PLACE = 4;

	public int type;
	public Long itemId;
	public String suggestion;

	private SearchSuggestion() {}

	public static SearchSuggestion fromHistoricSearch(HistoricSearch historicSearch) {
		SearchSuggestion suggestion = new SearchSuggestion();
		// NOTE No item id attached
		suggestion.type = TYPE_HISTORY;
		suggestion.suggestion = historicSearch.name;
		return suggestion;
	}

	public static SearchSuggestion fromBeer(Beer beer) {
		SearchSuggestion suggestion = new SearchSuggestion();
		suggestion.type = TYPE_BEER;
		suggestion.itemId = beer._id;
		suggestion.suggestion = beer.name;
		return suggestion;
	}

	public static SearchSuggestion fromRating(Rating rating) {
		SearchSuggestion suggestion = new SearchSuggestion();
		suggestion.type = TYPE_RATING;
		suggestion.itemId = rating.beerId;
		suggestion.suggestion = rating.beerName;
		return suggestion;
	}

	public static SearchSuggestion fromBrewery(Brewery brewery) {
		SearchSuggestion suggestion = new SearchSuggestion();
		suggestion.type = TYPE_BREWERY;
		suggestion.itemId = brewery._id;
		suggestion.suggestion = brewery.name;
		return suggestion;
	}

	public static SearchSuggestion fromPlace(Place place) {
		SearchSuggestion suggestion = new SearchSuggestion();
		suggestion.type = TYPE_PLACE;
		suggestion.itemId = place._id;
		suggestion.suggestion = place.name;
		return suggestion;
	}

	public String uniqueCode() {
		// A search suggestion is unique per type (but beers and ratings are not) and per id (or suggestion text if no id is available)
		return (type == TYPE_BEER? TYPE_RATING: type) + (itemId != null? Long.toString(itemId): suggestion);
	}

}
