package com.ratebeer.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class ConnectivityHelper {

	public static ConnectivityType current(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null || !ni.isConnectedOrConnecting())
			return ConnectivityType.NoConnection;
		if (ni.getType() == ConnectivityManager.TYPE_WIFI || ni.getType() == ConnectivityManager.TYPE_ETHERNET)
			return ConnectivityType.Wifi;
		// Return Cellular for any other type, including mobile, vpn and wimax
		return ConnectivityType.Cellular;
	}

	public enum ConnectivityType {
		Wifi,
		Cellular,
		NoConnection
	}

}
