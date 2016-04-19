package com.ratebeer.android.db;

import android.location.Location;

import com.ratebeer.android.api.model.PlaceDetails;
import com.ratebeer.android.api.model.PlaceNearby;

import java.util.Date;

import nl.qbusict.cupboard.annotation.Ignore;

public final class Place {

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

}
