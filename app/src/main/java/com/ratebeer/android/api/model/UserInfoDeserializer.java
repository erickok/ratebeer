package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public final class UserInfoDeserializer implements JsonDeserializer<UserInfo> {

	@Override
	public UserInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		UserInfo userInfo = new UserInfo();
		userInfo.userId = object.get("UserID").getAsInt();
		userInfo.userName = object.get("UserName").getAsString();
		if (!(object.get("PrimaryEmail") instanceof JsonNull))
			userInfo.primaryEmail = object.get("PrimaryEmail").getAsString();
		return userInfo;
	}

}
