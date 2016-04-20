package com.ratebeer.android.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.Place;
import com.ratebeer.android.gui.lists.PropertiesAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.PropertyView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PlaceActivity extends RateBeerActivity {

	private MapView mapView;
	private ProgressBar loadingProgress;
	private View detailsLayout;

	private long placeId;

	public static Intent start(Context context, long placeId) {
		return new Intent(context, PlaceActivity.class).putExtra("placeId", placeId);
	}

	@SuppressLint("PrivateResource")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_place);

		setupDefaultUpButton();

		mapView = (MapView) findViewById(R.id.map_view);
		mapView.onCreate(savedInstanceState);
		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
		detailsLayout = findViewById(R.id.details_layout);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		refresh(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	private void refresh(boolean forceFresh) {

		// Load place from database or live
		placeId = getIntent().getLongExtra("placeId", 0);
		Db.getPlace(this, placeId, forceFresh).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(this::showPlace, e -> Snackbar.show(this, R
				.string.error_connectionfailure));

	}

	private void showPlace(Place place) {

		GoogleMap map = mapView.getMap();
		if (place.longitude != null && place.latitude != null && map != null) {
			LatLng placeLatLng = new LatLng(place.latitude, place.longitude);
			map.getUiSettings().setMapToolbarEnabled(false);
			map.setOnMapClickListener(latLng -> {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, "geo:%1$f,%2$f?q=%3$s&z=%4$d", place.latitude,
							place.longitude, Uri.encode(place.name), 17))));
				} catch (Exception e) {
					Snackbar.show(this, R.string.error_cannotopenurl);
				}
			});
			map.addMarker(new MarkerOptions().position(placeLatLng).icon(BitmapDescriptorFactory.defaultMarker(place.getTypeMarkerHue())));
			map.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
			Animations.fadeFlip(mapView, findViewById(R.id.map_placeholder));
		}

		if (!TextUtils.isEmpty(place.city)) {
			String typeText = place.getTypeName(this);
			String typeLocationText = getString(R.string.place_typelocation, typeText, place.city);
			SpannableStringBuilder typeLocationMarkup = new SpannableStringBuilder(typeLocationText);
			int typeStart = typeLocationText.indexOf(" ") + 1;
			int typeEnd = typeText.length();
			int locationStart = typeLocationText.indexOf(place.city);
			int locationEnd = locationStart + place.city.length();
			typeLocationMarkup.setSpan(new StyleSpan(Typeface.BOLD), typeStart, typeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			typeLocationMarkup.setSpan(new StyleSpan(Typeface.BOLD), locationStart, locationEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			((TextView) findViewById(R.id.place_location_text)).setText(typeLocationMarkup);
		} else {
			String typeText = place.getTypeName(this);
			SpannableStringBuilder typeMarkup = new SpannableStringBuilder(typeText);
			int typeStart = typeText.indexOf(" ") + 1;
			int typeEnd = typeText.length();
			typeMarkup.setSpan(new StyleSpan(Typeface.BOLD), typeStart, typeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			((TextView) findViewById(R.id.place_location_text)).setText(typeMarkup);
		}

		((TextView) findViewById(R.id.place_name_text)).setText(place.name);
		if (place.isRetired)
			findViewById(R.id.retired_badge).setVisibility(View.VISIBLE);

		((TextView) findViewById(R.id.mark_overall_text)).setText(place.getOverallPercentileString());
		((TextView) findViewById(R.id.mark_count_text)).setText(place.getRateCountString());
		((TextView) findViewById(R.id.mark_weighted_text)).setText(place.getPercentileScoreString());

		// Show additional data as (clickable) properties in a list
		List<PropertyView> properties = new ArrayList<>();

		PropertyView checkinProperty = new PropertyView(this);
		checkinProperty.setPropertyImage(R.drawable.ic_prop_checkin);
		checkinProperty.setPropertyText(getString(R.string.place_checkin));
		checkinProperty.setClickable(true);
		checkinProperty.setOnClickListener(v -> performCheckin());
		properties.add(checkinProperty);
		if (!TextUtils.isEmpty(place.hours)) {
			PropertyView hoursProperty = new PropertyView(this);
			hoursProperty.setPropertyImage(R.drawable.ic_prop_hours);
			hoursProperty.setPropertyText(getString(R.string.place_opentimes, place.hours));
			properties.add(hoursProperty);
		}
		if (!TextUtils.isEmpty(place.taps) || !TextUtils.isEmpty(place.bottles)) {
			PropertyView tapsBottlesProperty = new PropertyView(this);
			tapsBottlesProperty.setPropertyImage(R.drawable.ic_prop_taps);
			if (TextUtils.isEmpty(place.taps)) {
				tapsBottlesProperty.setPropertyText(getString(R.string.place_bottles, place.bottles));
			} else if (TextUtils.isEmpty(place.bottles)) {
				tapsBottlesProperty.setPropertyText(getString(R.string.place_taps, place.taps));
			} else {
				tapsBottlesProperty.setPropertyText(getString(R.string.place_taps, place.taps) + "\n" + getString(R.string.place_bottles, place
						.bottles));
			}
			properties.add(tapsBottlesProperty);
		}
		if (!TextUtils.isEmpty(place.phoneNumber)) {
			PropertyView phoneProperty = new PropertyView(this);
			phoneProperty.setPropertyImage(R.drawable.ic_prop_phone);
			phoneProperty.setPropertyText(place.phoneNumber);
			phoneProperty.setClickable(true);
			phoneProperty.setOnClickListener(v -> {
				try {
					startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + place.phoneNumber.replaceAll("[^0-9|\\+]", ""))));
				} catch (Exception e) {
					Snackbar.show(this, R.string.error_cannotopenurl);
				}
			});
			properties.add(phoneProperty);
		}
		if (!TextUtils.isEmpty(place.website)) {
			PropertyView websiteProperty = new PropertyView(this);
			websiteProperty.setPropertyImage(R.drawable.ic_prop_website);
			websiteProperty.setPropertyText(place.getWebsiteUrl());
			websiteProperty.setClickable(true);
			websiteProperty.setOnClickListener(v -> {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, place.getWebsiteUri()));
				} catch (Exception e) {
					Snackbar.show(this, R.string.error_cannotopenurl);
				}
			});
			properties.add(websiteProperty);
		}

		ListView propertiesList = (ListView) findViewById(R.id.properties_list);
		propertiesList.setAdapter(new PropertiesAdapter(properties));

		Animations.fadeFlip(detailsLayout, loadingProgress);

	}

	private void performCheckin() {
		Api.get().performPlaceCheckin(placeId).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(wasSuccessful -> Snackbar.show(this,
				wasSuccessful ? R.string.place_checkin_ok : R.string.place_checkin_error), e -> Snackbar.show(this, R.string
				.error_connectionfailure));
	}

}
