package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BeerOnTopList;
import com.ratebeer.android.gui.widget.Images;

import java.util.List;

public final class BeerOnTopListAdapter extends RecyclerView.Adapter<BeerOnTopListAdapter.ViewHolder> {

	private final List<BeerOnTopList> beersOnTopList;
	private final boolean showStyleRating;

	public BeerOnTopListAdapter(List<BeerOnTopList> beersOnTopList, boolean showStyleRating) {
		this.beersOnTopList = beersOnTopList;
		this.showStyleRating = showStyleRating;
	}

	@Override
	public BeerOnTopListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_beer_on_top_list, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		BeerOnTopList beerOnTopList = beersOnTopList.get(position);
		holder.ratingText.setBackgroundResource(ImageUrls.getColor(position));
		holder.ratingText.setText(showStyleRating? beerOnTopList.getStylePercentileString(): beerOnTopList.getOverallPercentileString());
		holder.titleText.setText(beerOnTopList.beerName);
		Images.with(holder.photoImage.getContext()).loadBeer(beerOnTopList.beerId).placeholder(android.R.color.white).fit().centerInside()
				.into(holder.photoImage);
		holder.ratedBadge.setVisibility(beerOnTopList.ratedByUser ? View.VISIBLE : View.GONE);
	}

	@Override
	public int getItemCount() {
		return beersOnTopList.size();
	}

	public BeerOnTopList get(int position) {
		return beersOnTopList.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final TextView ratingText;
		final TextView titleText;
		final ImageView photoImage;
		final View ratedBadge;

		public ViewHolder(View v) {
			super(v);
			ratingText = (TextView) v.findViewById(R.id.rating_text);
			titleText = (TextView) v.findViewById(R.id.name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			ratedBadge = v.findViewById(R.id.rated_badge);
		}

	}

}
