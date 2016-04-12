package com.ratebeer.android.api;

import com.ratebeer.android.api.model.BarcodeSearchResult;
import com.ratebeer.android.api.model.BeerAliasId;
import com.ratebeer.android.api.model.BeerDetails;
import com.ratebeer.android.api.model.BeerRating;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.api.model.UserInfo;
import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.api.model.UserRating;

import java.util.List;

import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

interface Routes {

	@FormUrlEncoded
	@POST("/Signin_r.asp")
	Observable<Response<Void>> login(@Field("username") String username, @Field("pwd") String password, @Field("SaveInfo") String assignCookie);

	@GET("/Signout.asp?v=1")
	Observable<Response<Void>> logout();

	@GET("users.asp")
	Observable<List<UserInfo>> getUserInfo(@Query("k") String key, @Query("u") String userName);

	@GET("feed.asp")
	Observable<List<FeedItem>> getFeed(@Query("k") String key, @Query("m") int mode);

	@GET("rc.asp")
	Observable<List<UserRateCount>> getUserRateCount(@Query("k") String key, @Query("uid") int userId);

	@GET("revs.asp?m=BR&x=2&x2=1")
	Observable<List<UserRating>> getUserRatings(@Query("k") String key, @Query("p") int page);

	@GET("s.asp")
	Observable<List<BeerSearchResult>> searchBeers(@Query("k") String key, @Query("u") Integer userId, @Query("b") String query);

	@GET("upc.asp")
	Observable<List<BarcodeSearchResult>> searchByBarcode(@Query("k") String key, @Query("upc") String barcode);

	@GET("bff.asp?vg=1&sid=1&cid=1&rc=1")
	Observable<List<BeerDetails>> getBeerDetails(@Query("k") String key, @Query("bd") int beerId);

	@GET("gr.asp")
	Observable<List<BeerRating>> getBeerRatings(@Query("k") String key, @Query("bid") int beerId, @Query("uid") Integer userId, @Query("p") int page,
												@Query("s") int sortOrder);

	// NOTE This is an html page, parsed manually via the HtmlConverterFactory
	@GET("/beer/alias/{beerId}/")
	Observable<BeerAliasId> getBeerAlias(@Path("beerId") int beerId);

	@FormUrlEncoded
	@POST("/saverating.asp")
	Observable<Response<Void>> postRating(@Field("BeerId") int beerId, @Field("aroma") int aroma, @Field("appearance") int appearance,
										  @Field("flavor") int flavor, @Field("palate") int palate, @Field("overall") int overall,
										  @Field(value = "Comments", encoded = true) String comments);

	@FormUrlEncoded
	@POST("/updaterating.asp")
	Observable<Response<Void>> updateRating(@Field("BeerId") int beerId, @Field("RatingId") int ratingId, @Field("aroma") int aroma,
											@Field("appearance") int appearance, @Field("flavor") int flavor, @Field("palate") int palate,
											@Field("overall") int overall, @Field(value = "Comments", encoded = true) String comments);

	// Brewer details
	// http://ratebeer.com/json/bi.asp?k=tTmwRTWT-W7tpBhtL&b=12
	// Brewer beers
	// http://ratebeer.com/json/bw.asp?k=tTmwRTWT-W7tpBhtL&b=12&u=101051

}
