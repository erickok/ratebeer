package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BeerRating;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public final class BeerRatingsAdapter extends RecyclerView.Adapter<BeerRatingsAdapter.ViewHolder> {

	private final List<BeerRating> ratings;

	public BeerRatingsAdapter(List<BeerRating> ratings) {
		this.ratings = ratings;
	}

	@Override
	public BeerRatingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_beer_rating, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		BeerRating rating = ratings.get(position);
		Picasso.with(holder.avatarImage.getContext()).load(ImageUrls.getUserPhotoUrl(rating.userName)).placeholder(android.R.color.white).fit()
				.centerCrop().into(holder.avatarImage);
		holder.ratingMarkText.setText(String.format(Locale.getDefault(), "%1$.1f", rating.total));
		holder.ratingMarkText.setBackgroundResource(ImageUrls.getColor(position, true));
		holder.ratingCommentsText.setText(asHtml(rating.comments));
		holder.userNameText.setText(rating.userName);
		holder.userCountText.setText(String.format(Locale.getDefault(), "%1$d", rating.userRateCount));
		holder.userCountryText.setText(rating.userCountryName);
	}

	private CharSequence asHtml(String raw) {
		try {
			return Html.fromHtml(raw);
		} catch (Exception e) {
			// Happens such as when running out of memory on large strings
			return raw;
		}
	}

	@Override
	public int getItemCount() {
		return ratings.size();
	}

	public BeerRating get(int position) {
		return ratings.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final ImageView avatarImage;
		final TextView ratingMarkText;
		final TextView ratingCommentsText;
		final TextView userNameText;
		final TextView userCountText;
		final TextView userCountryText;

		public ViewHolder(View v) {
			super(v);
			avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
			ratingMarkText = (TextView) v.findViewById(R.id.rating_mark_text);
			ratingCommentsText = (TextView) v.findViewById(R.id.rating_comments_text);
			userNameText = (TextView) v.findViewById(R.id.user_name_text);
			userCountText = (TextView) v.findViewById(R.id.user_count_text);
			userCountryText = (TextView) v.findViewById(R.id.user_country_text);
		}

	}

}
