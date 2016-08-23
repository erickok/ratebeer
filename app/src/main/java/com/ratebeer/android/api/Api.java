package com.ratebeer.android.api;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.BuildConfig;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.model.BarcodeSearchResult;
import com.ratebeer.android.api.model.BarcodeSearchResultDeserializer;
import com.ratebeer.android.api.model.BeerDetails;
import com.ratebeer.android.api.model.BeerDetailsDeserializer;
import com.ratebeer.android.api.model.BeerRating;
import com.ratebeer.android.api.model.BeerRatingDeserializer;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.api.model.BeerSearchResultDeserializer;
import com.ratebeer.android.api.model.BreweryBeer;
import com.ratebeer.android.api.model.BreweryBeerDeserializer;
import com.ratebeer.android.api.model.BreweryDetails;
import com.ratebeer.android.api.model.BreweryDetailsDeserializer;
import com.ratebeer.android.api.model.BrewerySearchResult;
import com.ratebeer.android.api.model.BrewerySearchResultDeserializer;
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.api.model.FeedItemDeserializer;
import com.ratebeer.android.api.model.PlaceCheckinResult;
import com.ratebeer.android.api.model.PlaceCheckinResultDeserializer;
import com.ratebeer.android.api.model.PlaceDetails;
import com.ratebeer.android.api.model.PlaceDetailsDeserializer;
import com.ratebeer.android.api.model.PlaceNearby;
import com.ratebeer.android.api.model.PlaceNearbyDeserializer;
import com.ratebeer.android.api.model.PlaceSearchResult;
import com.ratebeer.android.api.model.PlaceSearchResultDeserializer;
import com.ratebeer.android.api.model.UserInfo;
import com.ratebeer.android.api.model.UserInfoDeserializer;
import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.api.model.UserRateCountDeserializer;
import com.ratebeer.android.api.model.UserRating;
import com.ratebeer.android.api.model.UserRatingDeserializer;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.rx.AsRangeOperator;

import org.javatuples.Pair;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public final class Api {

	private static final String ENDPOINT = "http://www.ratebeer.com/json/";
	private static final String KEY = "tTmwRTWT-W7tpBhtL";
	private static final String COOKIE_USERID = "UserID";
	private static final String COOKIE_SESSIONID = "SessionID";
	private static final long SESSION_TIMEOUT_FORCED = 30 * 60 * 1000; // 30 minute session max until forced sign in
	private static final int RATINGS_PER_PAGE = 100;

	private final Routes routes;
	private final CookieManager cookieManager;
	private long lastSignIn = 0;

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

		// @formatter:off
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor(RBLog::v);
		if (BuildConfig.DEBUG)
			logging.setLevel(HttpLoggingInterceptor.Level.BODY);
		cookieManager = new CookieManager(new PersistentCookieStore(), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
		OkHttpClient httpclient = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.writeTimeout(5, TimeUnit.SECONDS)
				.readTimeout(10, TimeUnit.SECONDS)
				.cookieJar(new JavaNetCookieJar(cookieManager))
				.addInterceptor(logging)
				.addNetworkInterceptor(new ResponseInterceptor())
				.build();
		Gson gson = new GsonBuilder()
				.disableHtmlEscaping()
				.registerTypeAdapter(FeedItem.class, new FeedItemDeserializer())
				.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer())
				.registerTypeAdapter(UserRateCount.class, new UserRateCountDeserializer())
				.registerTypeAdapter(UserRating.class, new UserRatingDeserializer())
				.registerTypeAdapter(BeerSearchResult.class, new BeerSearchResultDeserializer())
				.registerTypeAdapter(BarcodeSearchResult.class, new BarcodeSearchResultDeserializer())
				.registerTypeAdapter(BeerDetails.class, new BeerDetailsDeserializer())
				.registerTypeAdapter(BeerRating.class, new BeerRatingDeserializer())
				.registerTypeAdapter(BrewerySearchResult.class, new BrewerySearchResultDeserializer())
				.registerTypeAdapter(BreweryDetails.class, new BreweryDetailsDeserializer())
				.registerTypeAdapter(BreweryBeer.class, new BreweryBeerDeserializer())
				.registerTypeAdapter(PlaceSearchResult.class, new PlaceSearchResultDeserializer())
				.registerTypeAdapter(PlaceNearby.class, new PlaceNearbyDeserializer())
				.registerTypeAdapter(PlaceDetails.class, new PlaceDetailsDeserializer())
				.registerTypeAdapter(PlaceCheckinResult.class, new PlaceCheckinResultDeserializer())
				.create();
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(ENDPOINT)
				.client(httpclient)
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(new HtmlConverterFactory())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.build();
		// @formatter:on
		routes = retrofit.create(Routes.class);

	}

	private boolean haveLoginCookie() {
		if (cookieManager.getCookieStore().getCookies().isEmpty())
			return false;
		boolean hasUserCookie = false, hasSessionCookie = false;
		for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
			if (cookie.getName().equals(COOKIE_USERID) && !TextUtils.isEmpty(cookie.getValue()) && !cookie.hasExpired())
				hasUserCookie = true;
			if (cookie.getName().equals(COOKIE_SESSIONID) && !TextUtils.isEmpty(cookie.getValue()) && !cookie.hasExpired())
				hasSessionCookie = true;
		}
		return hasUserCookie && hasSessionCookie;
	}

	private boolean isSignedIn() {
		return lastSignIn >= System.currentTimeMillis() - SESSION_TIMEOUT_FORCED && haveLoginCookie();
	}

	private Observable<Boolean> getLoginRoute(String username, String password) {
		return routes.login(username, password, "on").flatMap(result -> {
			if (haveLoginCookie()) {
				return Observable.just(true);
			} else {
				return Observable.error(new IOException("Invalid login response; no session cookies received"));
			}
		});
	}

	/**
	 * A wrapper observable that returns an empty sequence on success such that we can use someLoginDependendCall.startWith(getLoginCookie())
	 */
	private <T> Observable<T> getLoginCookie() {
		return getLoginRoute(Session.get().getUserName(), Session.get().getPassword()).subscribeOn(Schedulers.io())
				.flatMap(result -> Observable.empty());
	}

	/**
	 * Performs a login on the server, ensures that the rate counts are updated in our local session and emits true on success
	 */
	public Observable<Boolean> login(String username, String password) {
		// @formatter:off
		return Observable.zip(
					// Combine the latest user counts
					routes.getUserInfo(KEY, username).subscribeOn(Schedulers.newThread()).flatMapIterable(infos -> infos).first(),
					// And sign in the user (get login cookies)
					getLoginRoute(username, password).subscribeOn(Schedulers.newThread()),
					(userInfo, loginSuccess) -> userInfo)
				// Then add the user id the user's rate counts
				.flatMap(user -> Observable.zip(
						Observable.just(user),
						routes.getUserRateCount(KEY, user.userId).flatMapIterable(userRateCounts -> userRateCounts),
						RxTuples.toPair()))
				// Store in our own instance the new user data
				.doOnNext(user -> Session.get().startSession(user.getValue0().userId, username, password, user.getValue1()))
				// Store the time of this last successful login to time out the session forcefully after some time
				.doOnNext(ignore -> lastSignIn = System.currentTimeMillis())
				// Return login success
				.map(ignore -> true);
		// @formatter:on
	}

	/**
	 * Calls the server to log out, clear cookies and clear our local session
	 */
	public Observable<Boolean> logout() {
		return routes.logout().map(result -> true).map(success -> cookieManager.getCookieStore().removeAll())
				.doOnNext(success -> Session.get().endSession());
	}

	/**
	 * Retrieves updated rate counts from the server and persists them in our user session before emitting the updated values
	 */
	public Observable<UserRateCount> updateUserRateCounts() {
		return routes.getUserRateCount(KEY, Session.get().getUserId()).flatMapIterable(userRateCounts -> userRateCounts)
				.doOnNext(counts -> Session.get().updateCounts(counts));
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
		Observable<FeedItem> feed = routes.getFeed(KEY, 2).flatMapIterable(items -> items);
		if (!isSignedIn())
			feed = feed.startWith(getLoginCookie());
		return feed;
	}

	/**
	 * Returns an observable sequence (list) of items that appear on the personalized friends feed; requires a user to be logged in
	 */
	public Observable<FeedItem> getFriendsFeed() {
		Observable<FeedItem> feed = routes.getFeed(KEY, 0).flatMapIterable(items -> items);
		if (!isSignedIn())
			feed = feed.startWith(getLoginCookie());
		return feed;
	}

	/**
	 * Returns an observable sequence (list) of beers (search results) for a text query
	 */
	public Observable<BeerSearchResult> searchBeers(String query) {
		return routes.searchBeers(KEY, Session.get().getUserId(), Normalizer.get().normalizeSearchQuery(query)).flatMapIterable(results -> results);
	}

	/**
	 * Returns an observable sequence (list) of beers (search results) for a scanned UPC barcode
	 */
	public Observable<BarcodeSearchResult> searchByBarcode(String barcode) {
		return routes.searchByBarcode(KEY, barcode.trim()).flatMapIterable(results -> results);
	}

	/**
	 * Returns the full details for a beer, or throws an exception if it could not be retrieved
	 */
	public Observable<BeerDetails> getBeerDetails(long beerId) {
		return routes.getBeerDetails(KEY, (int) beerId).flatMapIterable(beers -> beers).first();
	}

	/**
	 * Returns a single id of the beer that is aliased to from a certain beer id, or throws an exception if it could not be retrieved
	 */
	public Observable<Long> getBeerAlias(long beerId) {
		return routes.getBeerAlias((int) beerId).filter(alias -> alias != null).first().map(alias -> alias.id);
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

	/**
	 * Returns a (possibly empty) observable sequence (list) of all ratings by the logged in user, from most recent to oldest
	 * @param onPageProgress An action, run on the main (UI) thread, which can report sync progress
	 */
	public Observable<UserRating> getUserRatings(Action1<Float> onPageProgress) {
		if (Session.get().getUserId() == null)
			return Observable.empty();
		// Based on the up-to-date rate count, get all pages of ratings necessary and emit them in reverse order
		Observable<Integer> pageCount = routes.getUserRateCount(KEY, Session.get().getUserId()).subscribeOn(Schedulers.io()).flatMapIterable(counts
				-> counts).doOnNext(counts -> Session.get().updateCounts(counts)).map(counts -> (int) Math.ceil((float) counts.rateCount /
				RATINGS_PER_PAGE));
		Observable<UserRating> ratings = Observable.combineLatest(pageCount, pageCount.lift(new AsRangeOperator()).onBackpressureBuffer(), RxTuples
				.toPair()).onBackpressureBuffer().flatMap(page -> Observable.combineLatest(Observable.just(page), routes.getUserRatings(KEY, page
				.getValue1() + 1), RxTuples.toPair())).observeOn(AndroidSchedulers.mainThread()).doOnNext(objects -> onPageProgress.call((((float)
				objects.getValue0().getValue1() + 1) / objects.getValue0().getValue0()) * 100)).observeOn(Schedulers.io()).flatMapIterable
				(Pair::getValue1);
		if (!isSignedIn())
			ratings = ratings.startWith(getLoginCookie());
		return ratings;
	}

	/**
	 * Posts or updates a rating and emits the stored rating, as validated on the server side, if the post was successful
	 */
	public Observable<BeerRating> postRating(Rating rating, long userId) {
		Observable<Response<Void>> post;
		// NOTE Manually encode the comments, as RB only accepts ISO-8859-1 encoding here...
		String comments = Normalizer.urlEncode(rating.comments);
		if (rating.ratingId == null)
			post = routes.postRating(rating.beerId.intValue(), rating.aroma, rating.appearance, rating.flavor, rating.mouthfeel, rating.overall,
					comments);
		else
			post = routes.updateRating(rating.beerId.intValue(), rating.ratingId.intValue(), rating.aroma, rating.appearance, rating.flavor,
					rating.mouthfeel, rating.overall, comments);
		return post.flatMap(posted -> routes.getBeerRatings(KEY, rating.beerId.intValue(), (int) userId, 1, 1).flatMapIterable(ratings -> ratings))
				.filter(storedRating -> storedRating.timeEntered != null).first();
	}

	/**
	 * Returns an observable sequence (list) of breweries (search results) for a text query
	 */
	public Observable<BrewerySearchResult> searchBreweries(String query) {
		return routes.searchBreweries(KEY, Normalizer.get().normalizeSearchQuery(query)).flatMapIterable(results -> results);
	}

	/**
	 * Returns the full details for a brewery, or throws an exception if it could not be retrieved
	 */
	public Observable<BreweryDetails> getBreweryDetails(long breweryId) {
		return routes.getBreweryDetails(KEY, (int) breweryId).flatMapIterable(breweries -> breweries).first();
	}

	/**
	 * Returns a (possibly empty) observable sequence (list) of beers made by some brewery
	 */
	public Observable<BreweryBeer> getBreweryBeers(long breweryId) {
		return routes.getBreweryBeers(KEY, (int) breweryId, Session.get().getUserId()).flatMapIterable(beers -> beers);
	}

	/**
	 * Returns an observable sequence (list) of places (search results) for a text query
	 */
	public Observable<PlaceSearchResult> searchPlaces(String query) {
		return routes.searchPlaces(KEY, Normalizer.get().normalizeSearchQuery(query)).flatMapIterable(results -> results);
	}

	/**
	 * Returns a (possibly empty) observable sequence (list) of nearby places
	 */
	public Observable<PlaceNearby> getPlacesNearby(int radius, double latitude, double longitude) {
		return routes.getPlacesNearby(KEY, radius, latitude, longitude).flatMapIterable(places -> places);
	}

	/**
	 * Returns the full details for a place, or throws an exception if it could not be retrieved
	 */
	public Observable<PlaceDetails> getPlaceDetails(long placeId) {
		return routes.getPlaceDetails(KEY, (int) placeId).flatMapIterable(places -> places).first();
	}

	/**
	 * Performs a place check-in on the server and returns true or false to indicate success, or throws an exception if the check-in request failed
	 */
	public Observable<Boolean> performPlaceCheckin(long placeId) {
		Observable<Boolean> checkin = routes.performCheckin(KEY, (int) placeId).map(result -> !TextUtils.isEmpty(result.okResult));
		if (!isSignedIn())
			checkin = checkin.startWith(getLoginCookie());
		return checkin;
	}

}
