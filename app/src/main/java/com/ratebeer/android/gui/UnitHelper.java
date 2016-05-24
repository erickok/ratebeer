package com.ratebeer.android.gui;

public final class UnitHelper {

	public static double asKmOrMiles(double meters, boolean useMetric) {
		if (useMetric)
			return meters / 1000; // Convert to kilometers
		else
			return meters * 0.000621371; // Convert to miles
	}

}
