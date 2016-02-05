package com.ratebeer.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.db.StoredSession;

import static com.ratebeer.android.db.CupboardDbHelper.database;

public class Session {

	private Context databaseContext;
	private SharedPreferences prefs;
	private StoredSession stored;

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

	public void startSession(int userId, String userName, String password, UserRateCount counts) {
		synchronized (this) {
			stored.userId = userId;
			stored.userName = userName;
			stored.password = password;
			stored.rateCount = counts.rateCount;
			stored.placeCount = counts.placeCount;
			database(databaseContext).put(stored);
		}
	}

	public void updateCounts(UserRateCount counts) {
		synchronized (this) {
			stored.rateCount = counts.rateCount;
			stored.placeCount = counts.placeCount;
			database(databaseContext).put(stored);
		}
	}

	public void endSession() {
		synchronized (this) {
			database(databaseContext).delete(stored);
			stored = null;
		}
	}

	public boolean isUpgrade() {
		// The "is_first_start" key was used on the old app to identify new installs and thus will always be present on upgrades
		return prefs.contains("is_first_start");
	}

	public void completeUpgrade() {
		prefs.edit().remove("is_first_start").apply();
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

	public Integer getUserRateCount() {
		return stored.rateCount;
	}

}
