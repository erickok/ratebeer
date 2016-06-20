package com.ratebeer.android.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ratebeer.android.db.views.CustomListWithCount;

import nl.nl2312.rxcupboard.RxCupboard;
import nl.nl2312.rxcupboard.RxDatabase;
import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.DatabaseCompartment;

public final class CupboardDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ratebeer.db";
	private static final int DATABASE_VERSION = 13;
	private static final Cupboard cupboardTables;
	private static final Cupboard cupboardAll;

	private static SQLiteDatabase database;
	private static DatabaseCompartment dbc;
	private static RxDatabase rxDatabase;

	static {
		// Register our database table models
		cupboardTables = new CupboardBuilder().build();
		cupboardTables.register(HistoricSearch.class);
		cupboardTables.register(StoredSession.class);
		cupboardTables.register(Style.class);
		cupboardTables.register(Brewery.class);
		cupboardTables.register(Beer.class);
		cupboardTables.register(Rating.class);
		cupboardTables.register(Place.class);
		cupboardTables.register(CustomList.class);
		cupboardTables.register(CustomListBeer.class);
		// Legacy OfflineRating table to migrate
		cupboardTables.register(OfflineRating.class);

		// Register views in a separate Cupboard instance to not create tables
		cupboardAll = new CupboardBuilder().build();
		cupboardAll.register(HistoricSearch.class);
		cupboardAll.register(StoredSession.class);
		cupboardAll.register(Style.class);
		cupboardAll.register(Brewery.class);
		cupboardAll.register(Beer.class);
		cupboardAll.register(Rating.class);
		cupboardAll.register(Place.class);
		cupboardAll.register(CustomList.class);
		cupboardAll.register(CustomListBeer.class);
		// Views
		cupboardAll.register(CustomListWithCount.class);
	}

	public CupboardDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Returns a raw handle to the SQLite database connection. Do not close!
	 * @param context A context, which is used to (when needed) set up a connection to the database
	 * @return The single, unique connection to the database, as is (also) used by our Cupboard instance
	 */
	public synchronized static SQLiteDatabase connection(Context context) {
		if (database == null) {
			// Construct the single helper and open the unique(!) db connection for the app
			database = new CupboardDbHelper(context.getApplicationContext()).getWritableDatabase();
		}
		return database;
	}

	public synchronized static DatabaseCompartment database(Context context) {
		if (dbc == null) {
			dbc = cupboardAll.withDatabase(connection(context));
		}
		return dbc;
	}

	public synchronized static RxDatabase rxdb(Context context) {
		if (rxDatabase == null) {
			rxDatabase = RxCupboard.with(cupboardAll, connection(context));
		}
		return rxDatabase;
	}

	public synchronized static Cupboard cupboard() {
		return cupboardAll;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		cupboardTables.withDatabase(db).createTables();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion <= 7) {
			try {
				// Remove old RateBeer app data
				db.execSQL("DROP TABLE IF EXISTS BeerMail");
				db.execSQL("DROP TABLE IF EXISTS ErrorLogEntry");
				db.execSQL("DROP TABLE IF EXISTS CustomList");
				db.execSQL("DROP TABLE IF EXISTS CustomListBeer");
				// Keep OfflineRating for migration to new app
			} catch (SQLException e) {
				RBLog.e("Could not clear up old tables from old RateBeer app", e);
			}
		}
		cupboardTables.withDatabase(db).upgradeTables();
	}

}
