package com.ratebeer.android.gui.widget;

import android.content.Context;

import com.ratebeer.android.ConnectivityHelper;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.ImageUrls;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public final class Images {

	private final Context context;

	public Images(Context context) {
		this.context = context;
	}

	public static Images with(Context context) {
		return new Images(context);
	}


	public RequestCreator loadBeer(long beerId) {
		return loadBeer(beerId, false);
	}

	public RequestCreator loadBeer(long beerId, boolean highResolution) {
		RequestCreator request =
				Picasso.with(context).load(highResolution ? ImageUrls.getBeerPhotoHighResUrl(beerId) : ImageUrls.getBeerPhotoUrl(beerId));
		if (Session.get().inDataSaverMode() && ConnectivityHelper.current(context) != ConnectivityHelper.ConnectivityType.Wifi)
			request.networkPolicy(NetworkPolicy.OFFLINE);
		return request;
	}

	public RequestCreator loadUser(String username) {
		return loadUser(username, false);
	}

	public RequestCreator loadUser(String username, boolean highResolution) {
		RequestCreator request =
				Picasso.with(context).load(highResolution ? ImageUrls.getUserPhotoHighResUrl(username) : ImageUrls.getUserPhotoUrl(username));
		if (Session.get().inDataSaverMode() && ConnectivityHelper.current(context) != ConnectivityHelper.ConnectivityType.Wifi)
			request.networkPolicy(NetworkPolicy.OFFLINE);
		return request;
	}

}
