package com.ratebeer.android.db;

import android.content.Context;
import android.database.DatabaseUtils;

import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.ConnectivityHelper;
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

	private static final long MAX_AGE = 30 * 60 * 1000; // 30 minutes cache

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
			return getFresh(rxdb(context).get(Beer.class, beerId), fresh, beer -> isFresh(context, beer.timeCached));
	}

	public static Observable<Rating> getOfflineRating(Context context, long ratingId) {
		return rxdb(context).get(Rating.class, ratingId);
	}

	public static Observable<Rating> getOfflineRatingForBeer(Context context, long beerId) {
		return rxdb(context).query(Rating.class, "beerId = ?", Long.toString(beerId)).first();
	}

	public static Observable<Rating> getRating(Context context, long beerId, long userId) {
		// @formatter:off
		return getFresh(
				// Local cached value (recent or an offline rating) or...
				rxdb(context).query(Rating.class, "beerId = ?", Long.toString(beerId)),
				// Retrieve fresh value
				Observable.combineLatest(
							getBeer(context, beerId),
							api().getBeerUserRating(beerId, userId),
							rxdb(context).query(Rating.class, "beerId = ?", Long.toString(beerId)).firstOrDefault(null),
							RxTuples.toTriplet())
						// When a value exists online
						.filter(pair -> pair.getValue1() != null)
						// Create new or override existing local rating
						.map(pair -> Rating.fromBeerRating(pair.getValue0(), pair.getValue1(), pair.getValue2()))
						// And store it in the database
						.flatMap(rating -> rxdb(context).putRx(rating)),
				rating -> !rating.isUploaded() || isFresh(context, rating.timeCached));
		// @formatter:on
	}

	public static boolean hasSyncedRatings(Context context) {
		return DatabaseUtils.queryNumEntries(connection(context), Rating.class.getSimpleName()) > 0;
	}

	public static Observable<Rating> getRatings(Context context) {
		return rxdb(context).query(database(context).query(Rating.class).orderBy("timeEntered IS NOT NULL, timeEntered desc, timeCached desc"));
	}

	public static Observable<Rating> syncRatings(Context context, Action1<Float> onPageProgress) {
		return api().getUserRatings(onPageProgress).map(Rating::fromUserRating).flatMap(rating -> {
			// If the rating already exists in our database, override it
			Rating existing = database(context).query(Rating.class).withSelection("ratingId = ?", rating.ratingId.toString()).get();
			if (existing != null)
				rating._id = existing._id;
			return rxdb(context).putRx(rating);
		});
	}

	public static Observable<Rating> postRating(Context context, Rating rating, long userId) {
		return api().postRating(rating, userId).flatMap(postedRating -> Observable
				.combineLatest(getBeer(context, rating.beerId), Observable.just(postedRating), Observable.just(rating), Rating::fromBeerRating))
				.flatMap(combinedRating -> rxdb(context).putRx(combinedRating))
				.doOnNext(combinedRating -> api().updateUserRateCounts().toBlocking().first());
	}

	public static Observable<Rating> deleteOfflineRating(Context context, Rating rating, long userId) {
		return rxdb(context).deleteRx(rating).flatMap(deletedRating -> {
			if (deletedRating.ratingId == null)
				// Was local only, so we are done
				return Observable.empty();
			else
				// Was stored online, so refresh from the RB server our local rating instance
				return getRating(context, deletedRating.beerId, userId);
		});
	}

	private static <T> Observable<T> getFresh(Observable<T> db, Observable<T> server, Func1<T, Boolean> isFresh) {
		db = db.filter(item -> item != null);
		return Observable.concat(Observable.concat(db, server).takeFirst(isFresh::call), db).take(1);
	}

	private static boolean isFresh(Context context, Date timeCached) {
		if (ConnectivityHelper.current(context) == ConnectivityHelper.ConnectivityType.NoConnection)
			return true;
		return timeCached != null && timeCached.after(new Date(System.currentTimeMillis() - MAX_AGE));
	}

	public static boolean clearRatings(Context context) {
		return database(context).delete(Rating.class, "timeEntered is not null") > 0;
	}

}
