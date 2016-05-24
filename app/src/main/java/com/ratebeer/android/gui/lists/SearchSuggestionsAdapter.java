package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.gui.widget.Images;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.List;

public final class SearchSuggestionsAdapter extends RecyclerView.Adapter<SearchSuggestionsAdapter.ViewHolder> {

	private final List<SearchSuggestion> searchSuggestions;

	public SearchSuggestionsAdapter() {
		this.searchSuggestions = new ArrayList<>();
	}

	@Override
	public SearchSuggestionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_suggestion, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		SearchSuggestion searchSuggestion = searchSuggestions.get(position);
		int typeResId = 0;
		if (searchSuggestion.type == SearchSuggestion.TYPE_HISTORY) {
			typeResId = R.drawable.ic_type_historic;
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_BREWERY) {
			typeResId = R.drawable.ic_type_brewery;
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_PLACE) {
			typeResId = R.drawable.ic_type_place;
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_BEER) {
			typeResId = R.drawable.ic_type_beer;
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_RATING) {
			typeResId = R.drawable.ic_type_rating;
		}
		holder.typeImage.setImageResource(typeResId);
		holder.nameText.setText(searchSuggestion.suggestion);
		RequestCreator request = null;
		if (searchSuggestion.type == SearchSuggestion.TYPE_BEER && searchSuggestion.itemId != null) {
			request = Images.with(holder.photoImage.getContext()).loadBeer(searchSuggestion.itemId);
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_BREWERY && searchSuggestion.itemId != null) {
			request = Images.with(holder.photoImage.getContext()).loadBrewery(searchSuggestion.itemId);
		}
		if (request != null) {
			holder.photoImage.setVisibility(View.VISIBLE);
			request.placeholder(android.R.color.white).fit().centerInside().into(holder.photoImage);
		} else {
			holder.photoImage.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return searchSuggestions.size();
	}

	public SearchSuggestion get(int position) {
		return searchSuggestions.get(position);
	}

	public void update(List<SearchSuggestion> updated) {
		this.searchSuggestions.clear();
		this.searchSuggestions.addAll(updated);
		notifyDataSetChanged();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final ImageView typeImage;
		final TextView nameText;
		final ImageView photoImage;

		public ViewHolder(View v) {
			super(v);
			typeImage = (ImageView) v.findViewById(R.id.type_image);
			nameText = (TextView) v.findViewById(R.id.name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
		}

	}

}
