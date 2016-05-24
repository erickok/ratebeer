package com.ratebeer.android.db;

import android.content.Context;
import android.net.Uri;

import com.ratebeer.android.R;
import com.ratebeer.android.api.model.BreweryDetails;

import java.util.Date;

public final class Brewery {

	private static final int TYPE_UNKNOWN = 0;
	private static final int TYPE_COMMERCIAL = 1;
	private static final int TYPE_MICRO = 2;
	private static final int TYPE_BREWPUB = 3;
	private static final int TYPE_BREWERY_PUB = 4;
	private static final int TYPE_CONTRACT = 5;
	private static final int TYPE_MEADERY = 6;
	private static final int TYPE_SAKEPRODUCER = 7;
	private static final int TYPE_CIDERY = 8;
	private static final int TYPE_CLIENT = 9;
	private static final int TYPE_COMMISIONER = 10;

	public Long _id;
	public String name;
	public Integer type;
	public Boolean isRetired;

	public String address;
	public String city;
	public String postalCode;
	public Integer countryId;
	public Integer stateId;

	public String phoneNumber;
	public String email;
	public String website;
	public String facebook;
	public String twitter;

	public Date timeCached;

	public static Brewery fromDetails(BreweryDetails details) {
		Brewery brewery = new Brewery();
		brewery._id = details.brewerId;
		brewery.name = details.brewerName;
		brewery.type = details.brewerType;
		brewery.isRetired = details.retired;

		brewery.address = details.address;
		brewery.city = details.city;
		brewery.postalCode = details.postalCode;
		brewery.countryId = details.countryId;
		brewery.stateId = details.stateId;

		brewery.phoneNumber = details.phoneNumber;
		brewery.email = details.email;
		brewery.website = details.websiteUrl;
		brewery.facebook = details.facebook;
		brewery.twitter = details.twitter;

		brewery.timeCached = new Date();
		return brewery;
	}

	public String getTypeName(Context context) {
		if (type == null)
			return context.getString(R.string.place_type_unkkown);
		switch (type) {
			case TYPE_MICRO:
				return context.getString(R.string.brewery_type_micro);
			case TYPE_BREWPUB:
				return context.getString(R.string.brewery_type_brewpub);
			case TYPE_BREWERY_PUB:
				return context.getString(R.string.brewery_type_brewery_pub);
			case TYPE_CONTRACT:
				return context.getString(R.string.brewery_type_contract);
			case TYPE_MEADERY:
				return context.getString(R.string.brewery_type_meadery);
			case TYPE_SAKEPRODUCER:
				return context.getString(R.string.brewery_type_sake);
			case TYPE_CIDERY:
				return context.getString(R.string.brewery_type_cidery);
			case TYPE_CLIENT:
				return context.getString(R.string.brewery_type_client);
			case TYPE_COMMISIONER:
				return context.getString(R.string.brewery_type_commissioner);
			default:
				return context.getString(R.string.brewery_type_brewery);
		}
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

}
