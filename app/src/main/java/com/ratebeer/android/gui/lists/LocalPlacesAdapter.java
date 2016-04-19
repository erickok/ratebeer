package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.gui.UnitHelper;

import java.util.List;
import java.util.Locale;

public final class LocalPlacesAdapter extends RecyclerView.Adapter<LocalPlacesAdapter.ViewHolder> {

	private final boolean useMetricUnits;
	private final String unitString;
	private List<LocalPlace> places;

	public LocalPlacesAdapter(Context context, List<LocalPlace> places) {
		this.useMetricUnits = Session.get().useMetricUnits();
		this.unitString = context.getString(useMetricUnits ? R.string.place_km : R.string.place_miles);
		this.places = places;
	}

	@Override
	public LocalPlacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		LocalPlace place = places.get(position);
		holder.distanceUnitText.setText(unitString);
		holder.distanceAmountText.setText(String.format(Locale.getDefault(), "%.1f", UnitHelper.asKmOrMiles(place.distance, useMetricUnits)));
		holder.placeNameText.setText(place.place.name);
		holder.placeCityText.setText(place.place.city);
	}

	@Override
	public int getItemCount() {
		return places.size();
	}

	public LocalPlace get(int position) {
		return places.get(position);
	}

	public void update(List<LocalPlace> places) {
		this.places = places;
		notifyDataSetChanged();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final View rowLayout;
		final TextView distanceAmountText;
		final TextView distanceUnitText;
		final TextView placeNameText;
		final TextView placeCityText;

		public ViewHolder(View v) {
			super(v);
			rowLayout = v;
			distanceAmountText = (TextView) v.findViewById(R.id.distance_amount_text);
			distanceUnitText = (TextView) v.findViewById(R.id.distance_unit_text);
			placeNameText = (TextView) v.findViewById(R.id.place_name_text);
			placeCityText = (TextView) v.findViewById(R.id.place_city_text);
		}

	}

}
