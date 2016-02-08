package com.ratebeer.android.db;

import android.content.Context;

import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.gui.lists.SearchSuggestion;

import java.util.Date;

import rx.Observable;
import rx.functions.Func1;

import static com.ratebeer.android.api.Api.api;
import static com.ratebeer.android.db.CupboardDbHelper.database;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class Db {

	private static final long MAX_AGE = 5 * 60 * 1000; // 5 minutes cache

	public static Observable<SearchSuggestion> getAllHistoricSearches(Context context) {
		return rxdb(context).query(database(context).query(HistoricSearch.class).orderBy("time desc")).map(SearchSuggestion::fromHistoricSearch);
	}

	public static Observable<SearchSuggestion> getSuggestions(Context context, String query) {
		Observable<HistoricSearch> lastHistoric = rxdb(context)
				.query(database(context).query(HistoricSearch.class).withSelection("name like ?", "%" + query + "%").orderBy("time desc").limit(2));
		Observable<Beer> localBeers = rxdb(context)
				.query(database(context).query(Beer.class).withSelection("name like ?", "%" + query + "%").orderBy("rateCount desc").limit(25));
		return Observable.merge(lastHistoric.map(SearchSuggestion::fromHistoricSearch), localBeers.map(SearchSuggestion::fromBeer));
	}

	public static Observable<Beer> getBeer(Context context, long beerId) {
		return getFresh(rxdb(context).get(Beer.class, beerId),
				api().getBeerDetails(beerId).map(Beer::fromDetails).flatMap(beer -> rxdb(context).putRx(beer)), beer -> isFresh(beer.timeCached));
	}

	public static Observable<Rating> getRating(Context context, long beerId, long userId) {
		return getFresh(rxdb(context).get(Rating.class, beerId),
				Observable.zip(getBeer(context, beerId), api().getBeerUserRating(beerId, userId), RxTuples.toPair())
						.map(pair -> Rating.fromBeerRating(pair.getValue0(), pair.getValue1())).flatMap(rating -> rxdb(context).putRx(rating)),
				rating -> !rating.isUploaded() || isFresh(rating.timeCached));
	}

	public static Observable<Rating> getLatestRatings(Context context, long userId) {
		// TODO
		return Observable.empty();
	}

	private static <T> Observable<T> getFresh(Observable<T> db, Observable<T> server, Func1<T, Boolean> isFresh) {
		db = db.filter(beer -> beer != null);
		return Observable.concat(Observable.concat(db, server).takeFirst(isFresh::call), db).first();
	}

	private static boolean isFresh(Date timeCached) {
		return timeCached != null && timeCached.after(new Date(System.currentTimeMillis() - MAX_AGE));
	}

}
