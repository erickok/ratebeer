package com.ratebeer.android.db;

import com.ratebeer.android.api.model.CountryInfo;

import java.util.Date;

public final class Country implements Comparable<Country> {

	public Long _id;
	public String name;

	public Date timeCached;

	public static Country fromInfo(CountryInfo info) {
		Country style = new Country();
		style._id = info.countryId;
		style.name = info.countryName;

		style.timeCached = new Date();
		return style;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Country other) {
		return name.compareTo(other.name);
	}

}
