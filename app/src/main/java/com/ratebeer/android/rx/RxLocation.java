package com.ratebeer.android.rx;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.TimeUnit;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;

public final class RxLocation {

	private static final long WAITTIME_SHORT = 5000;

	private final ReactiveLocationProvider provider;

	public RxLocation(Context context) {
		provider = new ReactiveLocationProvider(context);
	}

	/**
	 * Returns the last known location, or an empty sequence if none is available
	 */
	@SuppressWarnings("MissingPermission")
	public Observable<Location> getLastLocation() {
		// getLastKnownLocation() completes directly after emitting the last location (which might be null)
		return provider.getLastKnownLocation();
	}

	/**
	 * Returns a fresh and fairly accurate location, or an empty sequence if none is quickly available
	 */
	@SuppressWarnings("MissingPermission")
	public Observable<Location> getQuickLocation() {
		LocationRequest request = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
				.setExpirationDuration(WAITTIME_SHORT);
		return provider.getUpdatedLocation(request)
				.timeout(WAITTIME_SHORT, TimeUnit.MILLISECONDS)
				.take(1)
				.onErrorResumeNext(Observable.empty());
	}

	/**
	 * Returns at most one location, which is the last known location, or when this is not available, a fresh
	 * location, or an empty sequence if none is quickly available
	 */
	public Observable<Location> getLastOrQuickLocation() {
		return getLastLocation().switchIfEmpty(getQuickLocation());
	}

	/**
	 * Returns at most one location, which is fresh and fairly accurate, or the last known location if a new update is
	 * not available quickly, or an empty sequence if no last location is known at all
	 */
	public Observable<Location> getQuickOrLastLocation() {
		return getQuickLocation().switchIfEmpty(getLastLocation());
	}

}
