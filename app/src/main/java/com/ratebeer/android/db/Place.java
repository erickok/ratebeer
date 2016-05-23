package com.ratebeer.android.db;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.ratebeer.android.R;
import com.ratebeer.android.api.model.PlaceDetails;
import com.ratebeer.android.api.model.PlaceNearby;

import java.util.Date;
import java.util.Locale;

public final class Place {

	private static final int TYPE_UNKNOWN = 0;
	private static final int TYPE_BREWPUB = 1;
	private static final int TYPE_BAR = 2;
	private static final int TYPE_BEERSTORE = 3;
	private static final int TYPE_RESTAURANT = 4;
	private static final int TYPE_BREWERY = 5;
	private static final int TYPE_HOMEBREWSHOP = 6;
	private static final int TYPE_GROCERYSTORE = 7;
	private static final int TYPE_INTERNETBASED = 8;

	public Long _id;
	public String name;
	public Integer type;
	public Long brewerId;
	public Long userId;
	public Boolean isRetired;

	public String address;
	public String city;
	public String postalCode;
	public Integer countryId;
	public Integer stateId;
	public String hours;
	public String taps;
	public String bottles;

	public String website;
	public String facebook;
	public String twitter;
	public String phoneNumber;
	public Float latitude;
	public Float longitude;

	public Float realRating;
	public Float weightedRating;
	public Float overallPercentile;
	public Integer rateCount;

	public Date timeCached;

	public static Place fromNearby(PlaceNearby nearby) {
		Place place = new Place();
		place._id = nearby.placeId;
		place.name = nearby.placeName;
		place.type = nearby.placeType;

		place.address = nearby.address;
		place.city = nearby.city;
		place.postalCode = nearby.postalCode;
		place.countryId = nearby.countryId;
		place.stateId = nearby.stateId;

		place.phoneNumber = nearby.phoneNumber;
		place.latitude = nearby.latitude;
		place.longitude = nearby.longitude;

		place.realRating = nearby.averageRating;
		place.rateCount = nearby.rateCount;

		// place.timeCached is left empty to indicate that we do not have an offline version of the full place details
		return place;
	}

	public static Place fromDetails(PlaceDetails details) {
		Place place = new Place();
		place._id = details.placeId;
		place.name = details.placeName;
		place.type = details.placeType;
		place.brewerId = details.brewerId;
		place.userId = details.userId;
		place.isRetired = details.retired;

		place.address = details.address;
		place.city = details.city;
		place.postalCode = details.postalCode;
		place.countryId = details.countryId;
		place.stateId = details.stateId;
		place.hours = details.hours;
		place.taps = details.taps;
		place.bottles = details.bottles;

		place.website = details.websiteUrl;
		place.facebook = details.facebook;
		place.twitter = details.twitter;
		place.phoneNumber = details.phoneNumber;
		place.latitude = details.latitude;
		place.longitude = details.longitude;

		place.realRating = details.averageRating;
		place.weightedRating = details.baysianMean;
		place.overallPercentile = details.percentile;
		place.rateCount = details.rateCount;

		place.timeCached = new Date();
		return place;
	}

	public String getTypeName(Context context) {
		if (type == null)
			return context.getString(R.string.place_type_unkkown);
		switch (type) {
			case TYPE_BREWPUB:
				return context.getString(R.string.place_type_brewpub);
			case TYPE_BAR:
				return context.getString(R.string.place_type_bar);
			case TYPE_BEERSTORE:
				return context.getString(R.string.place_type_beerstore);
			case TYPE_RESTAURANT:
				return context.getString(R.string.place_type_restaurant);
			case TYPE_BREWERY:
				return context.getString(R.string.place_type_brewery);
			case TYPE_HOMEBREWSHOP:
				return context.getString(R.string.place_type_homebrewshop);
			case TYPE_GROCERYSTORE:
				return context.getString(R.string.place_type_grocerystore);
			case TYPE_INTERNETBASED:
				return context.getString(R.string.place_type_internetbased);
			default:
				return context.getString(R.string.place_type_unkkown);
		}
	}

	public String getPercentileScoreString() {
		// NOTE It seems that the weightedRating ('BayMean' field) doesn't reflect the actual score as shown in RB; use percentile instead
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1.2f", (overallPercentile / 100) * 5);
	}

	public String getOverallPercentileString() {
		if (overallPercentile == null)
			return "-";
		return String.format(Locale.getDefault(), "%1.0f", overallPercentile);
	}

	public String getRateCountString() {
		return String.format(Locale.getDefault(), "%1$d", rateCount);
	}

	public String getWebsiteUrl() {
		String url = website;
		if (!url.startsWith("http") && !url.startsWith("https"))
			url = "http://" + url;
		if (url.endsWith("/"))
			url = url.substring(0, url.length() - 1);
		return url;
	}

	public Uri getWebsiteUri() {
		return Uri.parse(getWebsiteUrl());
	}

	public float getTypeMarkerHue() {
		return getTypeMarkerHue(type);
	}

	private static float getTypeMarkerHue(Integer type) {
		if (type == null)
			return BitmapDescriptorFactory.HUE_ROSE;
		switch (type) {
			case TYPE_BREWPUB:
				return BitmapDescriptorFactory.HUE_RED;
			case TYPE_BAR:
				return BitmapDescriptorFactory.HUE_BLUE;
			case TYPE_BEERSTORE:
				return BitmapDescriptorFactory.HUE_GREEN;
			case TYPE_RESTAURANT:
				return BitmapDescriptorFactory.HUE_YELLOW;
			case TYPE_BREWERY:
				return BitmapDescriptorFactory.HUE_ORANGE;
			case TYPE_HOMEBREWSHOP:
				return BitmapDescriptorFactory.HUE_AZURE;
			case TYPE_GROCERYSTORE:
				return BitmapDescriptorFactory.HUE_CYAN;
			case TYPE_INTERNETBASED:
				return BitmapDescriptorFactory.HUE_MAGENTA;
			default:
				return BitmapDescriptorFactory.HUE_ROSE;
		}
	}

}
