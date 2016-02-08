package com.ratebeer.android.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.BuildConfig;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.model.BeerDetails;
import com.ratebeer.android.api.model.BeerDetailsDeserializer;
import com.ratebeer.android.api.model.BeerRating;
import com.ratebeer.android.api.model.BeerRatingDeserializer;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.api.model.BeerSearchResultDeserializer;
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.api.model.FeedItemDeserializer;
import com.ratebeer.android.api.model.UserInfo;
import com.ratebeer.android.api.model.UserInfoDeserializer;
import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.api.model.UserRateCountDeserializer;
import com.ratebeer.android.api.model.UserRating;
import com.ratebeer.android.api.model.UserRatingDeserializer;
import com.ratebeer.android.db.RBLog;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;

public final class Api {

	private static final String ENDPOINT = "http://ratebeer.com/json/";
	private static final String KEY = "tTmwRTWT-W7tpBhtL";

	private final Routes routes;

	private static class Holder {
		// Holder with static instance which implements a thread safe lazy loading singleton
		static final Api INSTANCE = new Api();
	}

	public static Api get() {
		return Holder.INSTANCE;
	}

	public static Api api() {
		return get();
	}

	private Api() {

		// OkHttp client with logging
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor(RBLog::v);
		if (BuildConfig.DEBUG)
			logging.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient httpclient = new OkHttpClient.Builder().addInterceptor(logging).addNetworkInterceptor(new LoginHeaderInterceptor()).build();
		//httpclient.setFollowRedirects(false); // Handle redirects ourselves, so we can grab the response headers/body

		// @formatter:off
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(FeedItem.class, new FeedItemDeserializer())
				.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer())
				.registerTypeAdapter(UserRateCount.class, new UserRateCountDeserializer())
				.registerTypeAdapter(UserRating.class, new UserRatingDeserializer())
				.registerTypeAdapter(BeerSearchResult.class, new BeerSearchResultDeserializer())
				.registerTypeAdapter(BeerDetails.class, new BeerDetailsDeserializer())
				.registerTypeAdapter(BeerRating.class, new BeerRatingDeserializer())
				.create();
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(ENDPOINT)
				.client(httpclient)
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.build();
		// @formatter:on
		routes = retrofit.create(Routes.class);

	}

	public Observable<Boolean> login(String username, String password) {
		// @formatter:off
		return Observable.zip(
					routes.login(username, password, "on"),
					routes.getUserInfo(KEY, username).flatMapIterable(infos -> infos).first(),
					RxTuples.toPair())
				// Add to the user id the user's rate counts
				.flatMap(user -> Observable.zip(
						Observable.just(user.getValue1()),
						routes.getUserRateCount(KEY, user.getValue1().userId).flatMapIterable(userRateCounts -> userRateCounts),
						RxTuples.toPair()))
				// Store in our own instance the new user data
				.doOnNext(user -> Session.get().startSession(user.getValue0().userId, username, password, user.getValue1()))
				// Return login success
				.map(ignore -> true);
		// @formatter:on
	}

	/**
	 * Returns an observable sequence (list) of items that appear on the global news feed; does not require user login
	 */
	public Observable<FeedItem> getGlobalFeed() {
		return routes.getFeed(KEY, 1).flatMapIterable(items -> items);
	}

	/**
	 * Returns an observable sequence (list) of items that appear on the local news feed; requires a user to be logged in for its locale
	 */
	public Observable<FeedItem> getLocalFeed() {
		return routes.getFeed(KEY, 2).flatMapIterable(items -> items);
	}

	/**
	 * Returns an observable sequence (list) of items that appear on the personalized friends feed; requires a user to be logged in
	 */
	public Observable<FeedItem> getFriendsFeed() {
		return routes.getFeed(KEY, 0).flatMapIterable(items -> items);
	}

	/**
	 * Returns an observable sequence (list) of beers (search results) for a text query
	 */
	public Observable<BeerSearchResult> searchBeers(String query) {
		return routes.searchBeers(KEY, Session.get().getUserId(), Normalizer.get().normalizeSearchQuery(query)).flatMapIterable(results -> results);
	}

	/**
	 * Returns the full details for a beer, or throws an exception if it could not be retrieved
	 */
	public Observable<BeerDetails> getBeerDetails(long beerId) {
		return routes.getBeerDetails(KEY, (int) beerId).flatMapIterable(beers -> beers).first();
	}

	/**
	 * Returns a (possibly empty) observable sequence (list) of the most recent ratings for a beer
	 */
	public Observable<BeerRating> getBeerRatings(long beerId) {
		return routes.getBeerRatings(KEY, (int) beerId, null, 1, 1).flatMapIterable(ratings -> ratings);
	}

	/**
	 * Returns the beer rating of a specific user, or null if the user did not rate it yet
	 */
	public Observable<BeerRating> getBeerUserRating(long beerId, long userId) {
		return routes.getBeerRatings(KEY, (int) beerId, (int) userId, 1, 1).flatMapIterable(ratings -> ratings).firstOrDefault(null);
	}

}
