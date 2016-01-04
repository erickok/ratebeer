package com.ratebeer.android.api;

import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.api.model.UserRateCount;
import com.ratebeer.android.api.model.UserRating;

import java.util.List;

import retrofit.Response;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

interface Routes {

	@POST("/Signin_r.asp")
	Observable<Response<Void>> login(@Field("k") String key, @Field("username") String username, @Field("pwd") String password,
									 @Field("saveinfo") String assignCookie);

	@GET("rc.asp")
	Observable<List<UserRateCount>> getUserRateCount(@Field("k") String key, @Field("uid") int userId);

	@GET("revs.asp?m=BR&x=2&x2=1")
	Observable<List<UserRating>> getUserRatings(@Query("k") String key, @Query("p") int page);

	@GET("s.asp")
	Observable<BeerSearchResult> searchBeers(@Query("k") String key, @Query("u") Integer userId, @Query("b") String query);

}
