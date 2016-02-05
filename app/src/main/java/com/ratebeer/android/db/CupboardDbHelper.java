package com.ratebeer.android.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nl.nl2312.rxcupboard.RxCupboard;
import nl.nl2312.rxcupboard.RxDatabase;
import nl.qbusict.cupboard.DatabaseCompartment;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public final class CupboardDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "ratebeer.db";
	private static final int DATABASE_VERSION = 9;

	private static SQLiteDatabase database;
	private static DatabaseCompartment dbc;
	private static RxDatabase rxDatabase;

	static {
		// Register our models with Cupboard as usual
		//CupboardFactory.setCupboard(new CupboardBuilder().useAnnotations().build());
		cupboard().register(HistoricSearch.class);
		cupboard().register(StoredSession.class);
		cupboard().register(Style.class);
		cupboard().register(Brewery.class);
		cupboard().register(Beer.class);
		cupboard().register(Rating.class);
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
			dbc = cupboard().withDatabase(connection(context));
		}
		return dbc;
	}

	public synchronized static RxDatabase rxdb(Context context) {
		if (rxDatabase == null) {
			rxDatabase = RxCupboard.with(cupboard(), connection(context));
		}
		return rxDatabase;
	}

	public CupboardDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		cupboard().withDatabase(db).createTables();
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
			} catch (SQLException e) {
				RBLog.e("Could not clear up old tables from old RateBeer app", e);
			}
		}
		cupboard().withDatabase(db).upgradeTables();
	}

}
