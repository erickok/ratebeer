package com.ratebeer.android.db;

import com.ratebeer.android.api.model.StyleInfo;

import java.util.Date;

public final class Style implements Comparable<Style> {

	public Long _id;
	public String name;
	public String description;
	public String srmRange;

	public Date timeCached;

	public static Style fromInfo(StyleInfo info) {
		Style style = new Style();
		style._id = info.styleId;
		style.name = info.styleName;
		style.description = info.description;
		style.srmRange = info.srmRange;

		style.timeCached = new Date();
		return style;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Style other) {
		return name.compareTo(other.name);
	}

}
