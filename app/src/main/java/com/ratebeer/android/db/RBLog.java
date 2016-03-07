package com.ratebeer.android.db;

import android.util.Log;

import com.ratebeer.android.BuildConfig;

import rx.Notification;

public final class RBLog {

	private static final String LOG_NAME = "RateBeer";

	public static void v(String message) {
		if (BuildConfig.DEBUG)
			Log.v(LOG_NAME, message);
	}

	public static void d(String message) {
		if (BuildConfig.DEBUG)
			Log.d(LOG_NAME, message);
	}

	public static void e(String message) {
		e(message, null);
	}

	public static void e(String message, Throwable innerException) {
		if (BuildConfig.DEBUG)
			Log.e(LOG_NAME, message + (innerException != null ? "\n\t" + innerException.toString() : ""));
	}

	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	public static <T> void rx(Notification<T> n) {
		if (n.isOnError()) {
			n.getThrowable().printStackTrace();
			RBLog.e("E: " + n.getThrowable());
		} else if (n.isOnCompleted()) {
			RBLog.d("C");
		} else {
			RBLog.d("N: " + n.getValue());
		}
	}

}
