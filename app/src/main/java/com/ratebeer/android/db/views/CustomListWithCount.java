package com.ratebeer.android.db.views;

import android.support.annotation.NonNull;

public class CustomListWithCount implements Comparable<CustomListWithCount> {

	public Long _id;
	public String name;
	public int beerCount;

	@Override
	public int compareTo(@NonNull CustomListWithCount another) {
		return getNonNullName(name).compareTo(getNonNullName(another.name));
	}

	private String getNonNullName(String name) {
		if (name == null)
			return "";
		return name;
	}

}
