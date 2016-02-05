package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.squareup.picasso.Picasso;

import java.util.List;

public final class BeerSearchResultAdapter extends RecyclerView.Adapter<BeerSearchResultAdapter.ViewHolder> {

	private final List<BeerSearchResult> applications;

	public BeerSearchResultAdapter(List<BeerSearchResult> applications) {
		this.applications = applications;
	}

	@Override
	public BeerSearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_beer_searched, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		BeerSearchResult beerSearchResult = applications.get(position);
		holder.ratingText.setBackgroundResource(ImageUrls.getColor(position));
		holder.ratingText.setText(beerSearchResult.getOverallPercentileString());
		holder.titleText.setText(beerSearchResult.beerName);
		Picasso.with(holder.photoImage.getContext()).load(ImageUrls.getBeerPhotoUrl(beerSearchResult.beerId))
				.placeholder(ImageUrls.getColor(position, true)).fit().centerInside().into(holder.photoImage);
		holder.unrateableBadge.setVisibility(beerSearchResult.unrateable ? View.VISIBLE : View.GONE);
		holder.retiredBadge.setVisibility(beerSearchResult.retired ? View.VISIBLE : View.GONE);
		holder.aliasBadge.setVisibility(beerSearchResult.alias ? View.VISIBLE : View.GONE);
		holder.ratedBadge.setVisibility(beerSearchResult.ratedByUser ? View.VISIBLE : View.GONE);
	}

	@Override
	public int getItemCount() {
		return applications.size();
	}

	public BeerSearchResult get(int position) {
		return applications.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final TextView ratingText;
		final TextView titleText;
		final ImageView photoImage;
		final View unrateableBadge;
		final View retiredBadge;
		final View aliasBadge;
		final View ratedBadge;

		public ViewHolder(View v) {
			super(v);
			ratingText = (TextView) v.findViewById(R.id.rating_text);
			titleText = (TextView) v.findViewById(R.id.name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			unrateableBadge = v.findViewById(R.id.unrateable_badge);
			retiredBadge = v.findViewById(R.id.retired_badge);
			aliasBadge = v.findViewById(R.id.alias_badge);
			ratedBadge = v.findViewById(R.id.rated_badge);
		}

	}

}
