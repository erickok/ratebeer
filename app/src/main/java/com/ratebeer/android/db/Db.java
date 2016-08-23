package com.ratebeer.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Location;

import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.ConnectivityHelper;
import com.ratebeer.android.db.views.CustomListWithCount;
import com.ratebeer.android.db.views.CustomListWithPresence;
import com.ratebeer.android.gui.lists.SearchSuggestion;

import java.util.Date;

import nl.nl2312.rxcupboard.DatabaseChange;
import nl.nl2312.rxcupboard.RxCupboard;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.ratebeer.android.api.Api.api;
import static com.ratebeer.android.db.CupboardDbHelper.connection;
import static com.ratebeer.android.db.CupboardDbHelper.cupboard;
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

		Observable<HistoricSearch> lastHistoric = rxdb(context).query(database(context).query(HistoricSearch.class).withSelection(whereName,
				whereArgs).orderBy("time desc").limit(2));
		Observable<Brewery> localBrewers = rxdb(context).query(database(context).query(Brewery.class).withSelection(whereName, whereArgs).orderBy
				("name").limit(3));
		Observable<Rating> localRatings = rxdb(context).query(database(context).query(Rating.class).withSelection("beerId is not null and (" +
				whereBeerName + ")", whereArgs).orderBy("timeEntered IS NULL, timeEntered desc").limit(10));
		Observable<Place> localPlaces = rxdb(context).query(database(context).query(Place.class).withSelection(whereName, whereArgs).orderBy
				("rateCount desc").limit(5));
		Observable<Beer> localBeers = rxdb(context).query(database(context).query(Beer.class).withSelection(whereName, whereArgs).orderBy
				("rateCount" + " desc").limit(10));

		return Observable.merge(lastHistoric.map(SearchSuggestion::fromHistoricSearch), localBrewers.map(SearchSuggestion::fromBrewery),
				localRatings.map(SearchSuggestion::fromRating), localPlaces.map(SearchSuggestion::fromPlace), localBeers.map
						(SearchSuggestion::fromBeer)).distinct(searchSuggestion -> searchSuggestion.uniqueCode());
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
		return api().postRating(rating, userId).flatMap(postedRating -> Observable.combineLatest(getBeer(context, rating.beerId), Observable.just
				(postedRating), Observable.just(rating), Rating::fromBeerRating)).flatMap(combinedRating -> rxdb(context).putRx(combinedRating))
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

	public static Observable<Brewery> getBrewery(Context context, long breweryId) {
		return getBrewery(context, breweryId, false);
	}

	public static Observable<Brewery> getBrewery(Context context, long breweryId, boolean refresh) {
		Observable<Brewery> fresh = api().getBreweryDetails(breweryId).map(Brewery::fromDetails).flatMap(brewery -> rxdb(context).putRx(brewery));
		if (refresh)
			return fresh;
		else
			return getFresh(rxdb(context).get(Brewery.class, breweryId), fresh, brewery -> isFresh(context, brewery.timeCached));
	}

	public static Observable<Place> getPlacesNearby(Context context, Location location) {
		int radius = 40000; // Meters
		// Fresh (from the sever) places are received in a 40 kilometer (±25 mile) radius
		Observable<Place> fresh = api().getPlacesNearby((int) (radius * 0.000621371192D), location.getLatitude(), location.getLongitude()).map
				(Place::fromNearby).flatMap(place -> rxdb(context).putRx(place));
		// Database places are received in a rough rectangular area of 40 kilometers (±0.4 degrees) in each direction
		final double accuracy = radius / 111111;
		String minLat = Double.toString(location.getLatitude() - accuracy);
		String maxLat = Double.toString(location.getLatitude() + accuracy);
		String minLong = Double.toString(location.getLongitude() - accuracy);
		String maxLong = Double.toString(location.getLongitude() + accuracy);
		Observable<Place> db = rxdb(context).query(Place.class, "(latitude BETWEEN ? AND ?) AND (longitude BETWEEN ? AND ?)", minLat, maxLat,
				minLong, maxLong);
		return Observable.merge(fresh, db);
	}

	public static Observable<Place> getPlace(Context context, long placeId) {
		return getPlace(context, placeId, false);
	}

	public static Observable<Place> getPlace(Context context, long placeId, boolean refresh) {
		Observable<Place> fresh = api().getPlaceDetails(placeId).map(Place::fromDetails).flatMap(place -> rxdb(context).putRx(place));
		if (refresh)
			return fresh;
		else
			return getFresh(rxdb(context).get(Place.class, placeId), fresh, place -> isFresh(context, place.timeCached));
	}

	public static Observable<CustomListWithCount> getCustomListsWithCount(Context context) {
		Cursor cursor = connection(context).rawQuery("select l._id, l.name, count(b._id) as beerCount from CustomList as l left outer join " +
				"CustomListBeer as b on b.listId = l._id group by l._id having beerCount > 0 or (l.name is not null and l.name != '')", null);
		return RxCupboard.with(cupboard(), cursor).iterate(CustomListWithCount.class);
	}

	public static Observable<CustomListWithPresence> getCustomLists(Context context, long withBeerId) {
		Cursor cursor = connection(context).rawQuery("select l._id, l.name, (select count(*) from CustomListBeer as b where b.listId = l._id and " +
				"b.beerId = ?) > 0 as hasBeer from CustomList as l", new String[]{Long.toString(withBeerId)});
		return RxCupboard.with(cupboard(), cursor).iterate(CustomListWithPresence.class);
	}

	public static Observable<CustomListBeer> getCustomListBeer(Context context, long listId, long beerId) {
		return rxdb(context).query(CustomListBeer.class, "listId = ? and beerId = ?", Long.toString(listId), Long.toString(beerId));
	}

	public static Observable<CustomListBeer> getCustomListBeers(Context context, long listId) {
		return rxdb(context).query(CustomListBeer.class, "listId = ?", Long.toString(listId));
	}

	public static Observable<DatabaseChange<CustomListBeer>> getCustomListBeerChanges(Context context, long listId) {
		return rxdb(context).changes(CustomListBeer.class).filter(change -> change.entity().listId == listId);
	}

	public static Observable<Integer> deleteCustomList(Context context, CustomList list) {
		return Observable.defer(() -> Observable.just(database(context).delete(CustomListBeer.class, "listId = ?", Long.toString(list._id))))
				.first().doOnNext(ignore -> rxdb(context).delete(list));
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
