package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.db.Rating;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public final class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.ViewHolder> {

	private final List<Rating> ratings;
	private final DateFormat timeEnteredFormat;

	public RatingsAdapter(Context context, List<Rating> ratings) {
		this.ratings = ratings;
		this.timeEnteredFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	public RatingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rating, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Rating rating = ratings.get(position);
		holder.ratingMarkText.setText(String.format(Locale.getDefault(), "%1$.1f", rating.total));
		holder.beerNameText.setText(rating.beerName);
		Picasso.with(holder.photoImage.getContext()).load(ImageUrls.getBeerPhotoUrl(rating.beerId)).placeholder(android.R.color.white).fit()
				.centerInside().into(holder.photoImage);
		holder.timeEnteredText.setText(timeEnteredFormat.format(rating.timeEntered));
		holder.brewerNameText.setText(rating.brewerName);
	}

	@Override
	public int getItemCount() {
		return ratings.size();
	}

	public Rating get(int position) {
		return ratings.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final TextView ratingMarkText;
		final TextView beerNameText;
		final ImageView photoImage;
		final TextView timeEnteredText;
		final TextView brewerNameText;

		public ViewHolder(View v) {
			super(v);
			ratingMarkText = (TextView) v.findViewById(R.id.rating_mark_text);
			beerNameText = (TextView) v.findViewById(R.id.beer_name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			timeEnteredText = (TextView) v.findViewById(R.id.date_text);
			brewerNameText = (TextView) v.findViewById(R.id.brewer_name_text);
		}

	}

}
