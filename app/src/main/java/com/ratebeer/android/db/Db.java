package com.ratebeer.android.db;

import android.content.Context;

import com.pacoworks.rxtuples.RxTuples;

import java.util.Date;

import rx.Observable;
import rx.functions.Func1;

import static com.ratebeer.android.api.Api.api;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class Db {

	private static final long MAX_AGE = 5 * 60 * 1000; // 5 minutes cache

	public static Observable<Beer> getBeer(Context context, long beerId) {
		return getFresh(rxdb(context).get(Beer.class, beerId),
				api().getBeerDetails(beerId).map(Beer::fromDetails).flatMap(beer -> rxdb(context).putRx(beer)), beer -> isFresh(beer.timeCached));
	}

	public static Observable<Rating> getRating(Context context, long beerId, long userId) {
		return getFresh(rxdb(context).get(Rating.class, beerId),
				Observable.zip(getBeer(context, beerId), api().getBeerUserRating(beerId, userId), RxTuples.toPair())
						.map(pair -> Rating.fromBeerRating(pair.getValue0(), pair.getValue1())).flatMap(rating -> rxdb(context).putRx(rating)),
				rating -> isFresh(rating.timeCached));
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
