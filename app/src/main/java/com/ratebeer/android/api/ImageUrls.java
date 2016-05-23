package com.ratebeer.android.api;

import com.ratebeer.android.R;

public final class ImageUrls {

	private static final int[] PLACEHOLDER_COLORS =
			new int[]{R.color.blue_main, R.color.grey_light, R.color.orange_main, R.color.grey_dark, R.color.yellow_main, R.color.red_main};
	private static final int PLACEHOLDER_COLORS_COUNT = PLACEHOLDER_COLORS.length;

	public static int getColor(int position) {
		return getColor(position, false);
	}

	public static int getColor(int position, boolean reversed) {
		if (reversed)
			return PLACEHOLDER_COLORS[PLACEHOLDER_COLORS_COUNT - 1 - (position % PLACEHOLDER_COLORS_COUNT)];
		return PLACEHOLDER_COLORS[position % PLACEHOLDER_COLORS_COUNT];
	}

	public static String getBeerPhotoUrl(long beerId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100,d_beer_def.png/beer_" + beerId + ".jpg";
	}

	public static String getBeerPhotoHighResUrl(long beerId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_1024,c_limit,q_100,d_beer_def.png/beer_" + beerId + ".jpg";
	}

	public static String getBreweryPhotoUrl(long breweryId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100/brew_" + breweryId + ".jpg";
	}

	public static String getBreweryPhotoHighResUrl(long breweryId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_1024,c_limit,q_100/brew_" + breweryId + ".jpg";
	}

	public static String getUserPhotoUrl(String username) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100,d_user_def.png/user_" + username + ".jpg";
	}

	public static String getUserPhotoHighResUrl(String username) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_1024,c_limit,q_100,d_user_def.png/user_" + username + ".jpg";
	}

}
