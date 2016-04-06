package com.ratebeer.android.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class HtmlConverterFactory extends Converter.Factory {

	@Override
	public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		if (type.equals(String.class)) {
			return ResponseBody::string;
		}
		return null;
	}

}
