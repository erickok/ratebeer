package com.ratebeer.android.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class LoginHeaderInterceptor implements Interceptor {
	@Override
	public Response intercept(Interceptor.Chain chain) throws IOException {

		Request request = chain.request();

		Response response;
		if (request.url().encodedPath().endsWith("Signin_r.asp")) {
			response = chain.proceed(request);
			//response = response.newBuilder().code(200).build();
		} else {
			response = chain.proceed(request);
		}

		return response;
	}
}
