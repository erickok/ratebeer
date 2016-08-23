package com.ratebeer.android.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class ResponseInterceptor implements Interceptor {

	@Override
	public Response intercept(Interceptor.Chain chain) throws IOException {
		Request request = chain.request();
		Response response = chain.proceed(request);
		return response;
	}

}
