package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.gui.widget.Images;

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
		holder.nameText.setText(searchSuggestion.suggestion);
		if (searchSuggestion.beerId != null) {
			holder.photoImage.setVisibility(View.VISIBLE);
			Images.with(holder.photoImage.getContext()).loadBeer(searchSuggestion.beerId).placeholder(android.R.color.white).fit().centerInside().into(holder.photoImage);
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

		final TextView nameText;
		final ImageView photoImage;

		public ViewHolder(View v) {
			super(v);
			nameText = (TextView) v.findViewById(R.id.name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
		}

	}

}
