package com.ratebeer.android.db;

import android.content.Context;
import android.database.DatabaseUtils;

import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.gui.lists.SearchSuggestion;

import java.util.Date;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.ratebeer.android.api.Api.api;
import static com.ratebeer.android.db.CupboardDbHelper.connection;
import static com.ratebeer.android.db.CupboardDbHelper.database;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class Db {

	private static final long MAX_AGE = 5 * 60 * 1000; // 5 minutes cache

	public static Observable<SearchSuggestion> getAllHistoricSearches(Context context) {
		return rxdb(context).query(database(context).query(HistoricSearch.class).orderBy("time desc")).map(SearchSuggestion::fromHistoricSearch);
	}

	public static Observable<SearchSuggestion> getSuggestions(Context context, String query) {
		String whereName = "";
		String whereBeerName = "";
		String[] parts = query.split(" ");
		String[] whereArgs = new String[parts.length];
		for (int i = 0; i < parts.length; i++) {
			whereName += (whereName.length() == 0 ? "" : " and ") + "name like ?";
			whereBeerName += (whereBeerName.length() == 0 ? "" : " and ") + "beerName like ?";
			whereArgs[i] = "%" + parts[i] + "%";
		}
		Observable<HistoricSearch> lastHistoric = rxdb(context)
				.query(database(context).query(HistoricSearch.class).withSelection("name like ?", "%" + query + "%").orderBy("time desc").limit(2));
		Observable<Beer> localBeers =
				rxdb(context).query(database(context).query(Beer.class).withSelection(whereName, whereArgs).orderBy("rateCount desc").limit(25));
		Observable<Rating> localRatings = rxdb(context)
				.query(database(context).query(Rating.class).withSelection("beerId is not null and (" + whereBeerName + ")", whereArgs)
						.orderBy("timeEntered IS NULL, timeEntered desc").limit(25));
		return Observable.merge(lastHistoric.map(SearchSuggestion::fromHistoricSearch), localBeers.map(SearchSuggestion::fromBeer),
				localRatings.map(SearchSuggestion::fromRating)).distinct(searchSuggestion -> searchSuggestion.suggestion);
	}

	public static Observable<Beer> getBeer(Context context, long beerId) {
		return getBeer(context, beerId, false);
	}

	public static Observable<Beer> getBeer(Context context, long beerId, boolean refresh) {
		Observable<Beer> fresh = api().getBeerDetails(beerId).map(Beer::fromDetails).flatMap(beer -> rxdb(context).putRx(beer));
		if (refresh)
			return fresh;
		else
			return getFresh(rxdb(context).get(Beer.class, beerId), fresh, beer -> isFresh(beer.timeCached));
	}

	public static Observable<Rating> getUserRating(Context context, long ratingId) {
		return rxdb(context).get(Rating.class, ratingId);
	}

	public static Observable<Rating> getUserRating(Context context, long beerId, long userId) {
		return getFresh(rxdb(context).query(Rating.class, "beerId = ?", Long.toString(beerId)),
				Observable.combineLatest(getBeer(context, beerId), api().getBeerUserRating(beerId, userId), RxTuples.toPair())
						.filter(pair -> pair.getValue1() != null).map(pair -> Rating.fromBeerRating(pair.getValue0(), pair.getValue1()))
						.flatMap(rating -> rxdb(context).putRx(rating)), rating -> !rating.isUploaded() || isFresh(rating.timeCached));
	}

	public static boolean hasSyncedRatings(Context context) {
		return DatabaseUtils.queryNumEntries(connection(context), Rating.class.getSimpleName()) > 0;
	}

	public static Observable<Rating> getUserRatings(Context context) {
		return rxdb(context).query(database(context).query(Rating.class).orderBy("timeEntered IS NOT NULL, timeEntered desc"));
	}

	public static Observable<Rating> syncUserRatings(Context context, Action1<Float> onPageProgress) {
		return api().getUserRatings(onPageProgress).map(Rating::fromUserRating).flatMap(rating -> rxdb(context).putRx(rating));
	}

	private static <T> Observable<T> getFresh(Observable<T> db, Observable<T> server, Func1<T, Boolean> isFresh) {
		db = db.filter(beer -> beer != null);
		return Observable.concat(Observable.concat(db, server).takeFirst(isFresh::call), db).take(1);
	}

	private static boolean isFresh(Date timeCached) {
		return timeCached != null && timeCached.after(new Date(System.currentTimeMillis() - MAX_AGE));
	}

	public static boolean clearRatings(Context context) {
		return database(context).delete(Rating.class);
	}

}
