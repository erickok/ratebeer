package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BrewerySearchResult;

import java.util.List;

public final class BrewerySearchResultAdapter extends RecyclerView.Adapter<BrewerySearchResultAdapter.ViewHolder> {

	private final List<BrewerySearchResult> brewerySearchResults;

	public BrewerySearchResultAdapter(List<BrewerySearchResult> brewerySearchResults) {
		this.brewerySearchResults = brewerySearchResults;
	}

	@Override
	public BrewerySearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_brewery_searched, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		BrewerySearchResult brewerySearchResult = brewerySearchResults.get(position);
		holder.dummyView.setBackgroundResource(ImageUrls.getColor(position));
		holder.breweryNameText.setText(brewerySearchResult.brewerName);
		holder.breweryCityText.setText(brewerySearchResult.city);
	}

	@Override
	public int getItemCount() {
		return brewerySearchResults.size();
	}

	public BrewerySearchResult get(int position) {
		return brewerySearchResults.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final View dummyView;
		final TextView breweryNameText;
		final TextView breweryCityText;

		public ViewHolder(View v) {
			super(v);
			dummyView = v.findViewById(R.id.dummy_text);
			breweryNameText = (TextView) v.findViewById(R.id.brewery_name_text);
			breweryCityText = (TextView) v.findViewById(R.id.brewery_city_text);
		}

	}

}
