package com.ratebeer.android.api;

import com.ratebeer.android.api.model.BeerAliasId;
import com.ratebeer.android.api.model.BeerAliasIdConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class HtmlConverterFactory extends Converter.Factory {

	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		if (type.equals(BeerAliasId.class)) {
			return new BeerAliasIdConverter();
		}
		return null;
	}

}
