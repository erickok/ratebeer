package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.PlaceSearchResult;

import java.util.List;

public final class PlaceSearchResultAdapter extends RecyclerView.Adapter<PlaceSearchResultAdapter.ViewHolder> {

	private final List<PlaceSearchResult> placeSearchResults;

	public PlaceSearchResultAdapter(List<PlaceSearchResult> placeSearchResults) {
		this.placeSearchResults = placeSearchResults;
	}

	@Override
	public PlaceSearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place_searched, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		PlaceSearchResult placeSearchResult = placeSearchResults.get(position);
		holder.ratingText.setBackgroundResource(ImageUrls.getColor(position));
		holder.ratingText.setText(placeSearchResult.getOverallPercentileString());
		holder.placeNameText.setText(placeSearchResult.placeName);
		holder.placeCityText.setText(placeSearchResult.city);
	}

	@Override
	public int getItemCount() {
		return placeSearchResults.size();
	}

	public PlaceSearchResult get(int position) {
		return placeSearchResults.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final TextView ratingText;
		final TextView placeNameText;
		final TextView placeCityText;

		public ViewHolder(View v) {
			super(v);
			ratingText = (TextView) v.findViewById(R.id.rating_text);
			placeNameText = (TextView) v.findViewById(R.id.place_name_text);
			placeCityText = (TextView) v.findViewById(R.id.place_city_text);
		}

	}

}
