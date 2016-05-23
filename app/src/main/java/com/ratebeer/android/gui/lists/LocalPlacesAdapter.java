package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.gui.PlaceActivity;
import com.ratebeer.android.gui.UnitHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LocalPlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;

	private final boolean useMetricUnits;
	private final String unitString;
	private List<LocalPlace> places;
	private Map<Marker, Long> markerPlaceIds;

	public LocalPlacesAdapter(Context context, List<LocalPlace> places) {
		this.useMetricUnits = Session.get().useMetricUnits();
		this.unitString = context.getString(useMetricUnits ? R.string.place_km : R.string.place_miles);
		this.places = places;
		MapsInitializer.initialize(context);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_HEADER)
			return new MapHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header_map, parent, false));
		else
			return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof MapHeaderHolder) {

			MapHeaderHolder mapHeaderHolder = (MapHeaderHolder) holder;
			mapHeaderHolder.mapView.getMapAsync(googleMap -> {

				// For every place, add a marker and record its position for the camera view bounds
				markerPlaceIds = new HashMap<>(places.size());
				LatLngBounds.Builder bounds = new LatLngBounds.Builder();
				for (LocalPlace place : places) {
					LatLng latLng = new LatLng(place.place.latitude, place.place.longitude);
					bounds.include(latLng);
					Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(place.place.name).icon(BitmapDescriptorFactory
							.defaultMarker(place.place.getTypeMarkerHue())));
					markerPlaceIds.put(marker, place.place._id);
				}

				// Zoom camera to include all markers and allow marker clicks
				int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mapHeaderHolder.mapView.getResources()
						.getDisplayMetrics());
				//noinspection MissingPermission Already checked permission before loading the places
				googleMap.setMyLocationEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(false);
				if (!markerPlaceIds.isEmpty())
					googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), padding));
				googleMap.setOnInfoWindowClickListener(marker -> {
					if (markerPlaceIds != null && markerPlaceIds.get(marker) != null)
						mapHeaderHolder.mapView.getContext().startActivity(PlaceActivity.start(mapHeaderHolder.mapView.getContext(), markerPlaceIds
								.get(marker)));
				});

			});

		} else {

			LocalPlace place = places.get(position - 1);
			ItemHolder itemHolder = (ItemHolder) holder;
			itemHolder.distanceLayout.setBackgroundResource(ImageUrls.getColor(position));
			itemHolder.distanceUnitText.setText(unitString);
			itemHolder.distanceAmountText.setText(String.format(Locale.getDefault(), "%.1f", UnitHelper.asKmOrMiles(place.distance,
					useMetricUnits)));
			itemHolder.placeNameText.setText(place.place.name);
			itemHolder.placeCityText.setText(place.place.city);

		}
	}

	@Override
	public int getItemCount() {
		return places.size() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? TYPE_HEADER : TYPE_ITEM;
	}

	public LocalPlace get(int position) {
		if (position == 0)
			return null; // Header, not a rating
		return places.get(position - 1);
	}

	public void update(List<LocalPlace> places) {
		this.places = places;
		notifyDataSetChanged();
	}

	static class MapHeaderHolder extends RecyclerView.ViewHolder {

		final MapView mapView;

		public MapHeaderHolder(View v) {
			super(v);
			mapView = (MapView) v;
			// Call through onCreate and onResume because we had these already but the MapView did not yet exist then
			mapView.onCreate(null);
			mapView.onResume();
			mapView.getMapAsync(googleMap -> googleMap.getUiSettings().setMapToolbarEnabled(false));
		}

	}

	static class ItemHolder extends RecyclerView.ViewHolder {

		final View rowLayout;
		final View distanceLayout;
		final TextView distanceAmountText;
		final TextView distanceUnitText;
		final TextView placeNameText;
		final TextView placeCityText;

		public ItemHolder(View v) {
			super(v);
			rowLayout = v;
			distanceLayout = v.findViewById(R.id.distance_layout);
			distanceAmountText = (TextView) v.findViewById(R.id.distance_amount_text);
			distanceUnitText = (TextView) v.findViewById(R.id.distance_unit_text);
			placeNameText = (TextView) v.findViewById(R.id.place_name_text);
			placeCityText = (TextView) v.findViewById(R.id.place_city_text);
		}

	}

}
