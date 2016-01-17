package com.ratebeer.android.db;

import android.content.Context;
import android.database.Cursor;

import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.database;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class Db {

	public static Observable<Beer> searchBeers(Context context, String query, int limit) {
		return rxdb(context).query(database(context).query(Beer.class).withSelection("name = ?", "'" + query + "'").limit(limit));
	}

	public static Cursor searchBeersCursor(Context context, String query, int limit) {
		return database(context).query(Beer.class).withSelection("name = ?", "'" + query + "'").limit(limit).getCursor();
	}

}
