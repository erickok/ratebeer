package com.ratebeer.android.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.api.model.BeerSearchResultDeserializer;
import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.api.model.UserRateCountDeserializer;
import com.ratebeer.android.api.model.UserRating;
import com.ratebeer.android.api.model.UserRatingDeserializer;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;

public final class Api {

	private static final String ENDPOINT = "http://ratebeer.com/json/";
	private static final String KEY = "tTmwRTWT-W7tpBhtL";

	private final Routes routes;
	private Session session;

	private static class Holder {
		// Holder with static instance which implements a thread safe lazy loading singleton
		static final Api INSTANCE = new Api();
	}

	static Api get() {
		return Holder.INSTANCE;
	}

	private Api() {

		OkHttpClient httpclient = new OkHttpClient();
		httpclient.setFollowRedirects(false); // Handle redirects ourselves, so we can grab the response headers/body
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(UserRateCount.class, new UserRateCountDeserializer())
				.registerTypeAdapter(UserRating.class, new UserRatingDeserializer())
				.registerTypeAdapter(BeerSearchResult.class, new BeerSearchResultDeserializer())
				.create();

		// @formatter:off
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(ENDPOINT)
				.client(httpclient)
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.build();
		// @formatter:on
		routes = retrofit.create(Routes.class);

	}

	private void updateSession(String username, int userId, UserRateCount counts) {
		synchronized (this) {
			session = new Session();
			session.userId = userId;
			session.username = username;
			session.rateCount = counts.rateCount;
			session.placeCount = counts.placeCount;
		}
	}

	public Observable<Boolean> login(String username, String password) {
		// @formatter:off
		return routes.login(KEY, username, password, "1")
				// Get redirect URL, which is of form HTTP://WWW.RATEBEER.COM/?UID=101051
				.map(result -> result.headers().get("Location"))
				// Parse the user id from the response 301 redirect url
				.map(redirect -> Integer.parseInt(HttpUrl.parse(redirect).queryParameter("UID")))
				// Add to the user id the user's rate counts
				.flatMap(userId -> Observable.zip(
						Observable.just(userId),
						routes.getUserRateCount(KEY, userId).flatMap(Observable::from),
						RxTuples.toPair()))
				// Store in our own instance the new user data
				.doOnNext(user -> updateSession(username, user.getValue0(), user.getValue1()))
				// Return login success
				.map(ignore -> true);
		// @formatter:on
	}

	public Observable<BeerSearchResult> searchBeers(String query) {
		return routes.searchBeers(KEY, (session == null) ? null : session.userId, Normalizer.get().normalizeSearchQuery(query));
	}

}
