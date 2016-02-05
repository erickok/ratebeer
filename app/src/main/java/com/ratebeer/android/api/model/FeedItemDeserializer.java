package com.ratebeer.android.api.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ratebeer.android.api.Normalizer;

import java.lang.reflect.Type;

public final class FeedItemDeserializer implements JsonDeserializer<FeedItem> {

	@Override
	public FeedItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		FeedItem feedItem = new FeedItem();
		feedItem.activityId = object.get("ActivityID").getAsInt();
		feedItem.userId = object.get("UserID").getAsInt();
		feedItem.userName = Normalizer.get().cleanHtml(object.get("Username").getAsString());
		feedItem.type = object.get("Type").getAsInt();
		if (!(object.get("NumComments") instanceof JsonNull))
			feedItem.linkId = object.get("NumComments").getAsInt();
		feedItem.linkText = object.get("LinkText").getAsString(); // Keep raw HTML
		if (!(object.get("ActivityNumber") instanceof JsonNull))
			feedItem.activityNumber = object.get("ActivityNumber").getAsInt();
		feedItem.timeEntered = Normalizer.get().parseTime(object.get("TimeEntered").getAsString());
		if (!(object.get("NumComments") instanceof JsonNull))
			feedItem.numComments = object.get("NumComments").getAsInt();
		return feedItem;
	}

}
