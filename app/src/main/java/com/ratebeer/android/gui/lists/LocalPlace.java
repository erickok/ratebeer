package com.ratebeer.android.gui.lists;

import android.location.Location;

import com.ratebeer.android.db.Place;

public final class LocalPlace implements Comparable<LocalPlace> {

	public Place place;
	public Location placeLocation;
	public Location userLocation;
	public Float distance;

	public static LocalPlace from(Place place, Location userLocation) {
		LocalPlace localPlace = new LocalPlace();
		localPlace.place = place;
		if (place.latitude != null && place.longitude != null) {
			localPlace.placeLocation = new Location("LocalPlace");
			localPlace.placeLocation.setLatitude(place.latitude);
			localPlace.placeLocation.setLongitude(place.longitude);
			localPlace.userLocation = userLocation;
			localPlace.distance = userLocation.distanceTo(localPlace.placeLocation);
		}
		return localPlace;
	}

	@Override
	public int compareTo(LocalPlace another) {
		return Float.compare(distance == null ? Float.MAX_VALUE : distance, another == null || another.distance == null ? Float.MAX_VALUE :
				another.distance);
	}

}
