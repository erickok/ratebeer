package com.ratebeer.android.api;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

interface HtmlRoutes {

	@GET("/beer/alias/{beerId}")
	Observable<Response> getBeerAlias(@Path("beerId") int beerId);

}
