package com.ratebeer.android;

import android.app.Application;

import com.ratebeer.android.api.Api;

import rx.schedulers.Schedulers;

public class RateBeerApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Session.get().init(this);

		// Start with a refresh of the user counts (if logged in; ignore any errors)
		if (Session.get().isLoggedIn()) {
			Api.get().updateUserRateCounts().subscribeOn(Schedulers.io()).subscribe(result -> {}, Throwable::printStackTrace);
		}

		//Picasso.setSingletonInstance(new Picasso.Builder(this).indicatorsEnabled(BuildConfig.DEBUG).loggingEnabled(BuildConfig.DEBUG).build());
	}

}
