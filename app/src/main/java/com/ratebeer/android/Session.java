package com.ratebeer.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.db.StoredSession;

import nl.nl2312.rxcupboard.DatabaseChange;
import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.database;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public class Session {

	private Context databaseContext;
	private SharedPreferences prefs;
	private StoredSession stored;

	public Observable<StoredSession> getUpdates(Context context, boolean emitInitial) {
		Observable<StoredSession> updates = rxdb(context).changes(StoredSession.class).map(DatabaseChange::entity);
		if (emitInitial)
			updates = updates.startWith(Observable.just(stored));
		return updates;
	}

	private static class Holder {
		static final Session INSTANCE = new Session();
	}

	public static Session get() {
		return Holder.INSTANCE;
	}

	private Session() {
	}

	public void init(Context context) {
		synchronized (this) {
			databaseContext = context.getApplicationContext();
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
			// Resume session from the database
			stored = database(databaseContext).query(StoredSession.class).get();
			if (stored == null) {
				stored = new StoredSession();
			}
		}
	}

	public Context getApplicationContext() {
		return databaseContext;
	}

	public void startSession(int userId, String userName, String password, UserRateCount counts) {
		synchronized (this) {
			stored.userId = userId;
			stored.userName = userName;
			stored.password = password;
			stored.rateCount = counts.rateCount;
			stored.placeCount = counts.placeCount;
			rxdb(databaseContext).put(stored);
		}
	}

	public void updateCounts(UserRateCount counts) {
		synchronized (this) {
			stored.rateCount = counts.rateCount;
			stored.placeCount = counts.placeCount;
			rxdb(databaseContext).put(stored);
		}
	}

	public void endSession() {
		synchronized (this) {
			rxdb(databaseContext).delete(stored);
			stored = new StoredSession();
		}
	}

	public boolean isLoggedIn() {
		return stored.userId != null;
	}

	public Integer getUserId() {
		return stored.userId;
	}

	public String getUserName() {
		return stored.userName;
	}

	public String getPassword() {
		return stored.password;
	}

	public Integer getUserRateCount() {
		return stored.rateCount;
	}

	public boolean isUpgrade() {
		// The "is_first_start" key was used on the old app to identify new installs and thus will always be present on upgrades
		return prefs.contains("is_first_start");
	}

	public boolean hasIgnoredAccount() {
		return prefs.getBoolean("has_ignored_account", false);
	}

	public void registerIgnoreAccount() {
		prefs.edit().putBoolean("has_ignored_account", true).apply();
	}

	public boolean inDataSaverMode() {
		return prefs.getBoolean("data_saver_mode", false);
	}

	public void setDataSaverMode(boolean useDataSaverMode) {
		prefs.edit().putBoolean("data_saver_mode", useDataSaverMode).apply();
	}

}
