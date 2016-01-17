package com.ratebeer.android;

import android.content.Context;

import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.db.StoredSession;

import static com.ratebeer.android.db.CupboardDbHelper.database;

public class Session {

	private Context databaseContext;
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

	public boolean isLoggedIn() {
		return stored.userId != null;
	}

	public Integer getUserId() {
		return stored.userId;
	}

	public String getUserName() {
		return stored.userName;
	}

}
