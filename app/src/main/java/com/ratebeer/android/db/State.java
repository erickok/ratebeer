package com.ratebeer.android.db;

import com.ratebeer.android.api.model.StateInfo;

import java.util.Date;

public final class State implements Comparable<State> {

	public Long _id;
	public long countryId;
	public String name;
	public String abbreviation;

	public Date timeCached;

	public static State fromInfo(StateInfo info) {
		State style = new State();
		style._id = info.stateId;
		style.countryId = info.countryId;
		style.name = info.stateName;
		style.abbreviation = info.abbreviation;

		style.timeCached = new Date();
		return style;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(State other) {
		return name.compareTo(other.name);
	}

}
