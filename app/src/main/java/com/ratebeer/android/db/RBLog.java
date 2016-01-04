package com.ratebeer.android.db;

import android.util.Log;

import com.ratebeer.android.BuildConfig;

public class RBLog {

	private static final String LOG_NAME = "RateBeer";

	public static void e(String message, Throwable innerException) {
		if (BuildConfig.DEBUG)
			Log.e(LOG_NAME, message);
	}

}
