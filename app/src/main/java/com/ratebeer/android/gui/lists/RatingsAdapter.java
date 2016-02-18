package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.db.Rating;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public final class RatingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;

	private final List<Rating> ratings;
	private final Context context;

	public RatingsAdapter(Context context, List<Rating> ratings) {
		this.ratings = ratings;
		this.context = context;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_HEADER && Session.get().isLoggedIn())
			return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header_account, parent, false));
		else
			return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rating, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof HeaderHolder) {
			HeaderHolder headerHolder = (HeaderHolder) holder;
			Picasso.with(headerHolder.avatarImage.getContext()).load(ImageUrls.getUserPhotoUrl(Session.get().getUserName()))
					.placeholder(android.R.color.white).fit().centerCrop().into(headerHolder.avatarImage);
			headerHolder.userNameText.setText(Session.get().getUserName());
			headerHolder.userCountText.setText(String.format(Locale.getDefault(), "%1$d", Session.get().getUserRateCount()));
		} else {
			Rating rating = ratings.get(position - 1);
			ItemHolder itemHolder = (ItemHolder) holder;
			itemHolder.ratingMarkText.setText(String.format(Locale.getDefault(), "%1$.1f", rating.total));
			itemHolder.ratingMarkText.setBackgroundResource(ImageUrls.getColor(position, true));
			itemHolder.beerNameText.setText(rating.beerName);
			Picasso.with(itemHolder.photoImage.getContext()).load(ImageUrls.getBeerPhotoUrl(rating.beerId)).placeholder(android.R.color.white).fit()
					.centerInside().into(itemHolder.photoImage);
			if (rating.isUploaded()) {
				// Already uploaded to RB: show rating date
				itemHolder.offlineBadge.setVisibility(View.GONE);
				itemHolder.timeEnteredText.setVisibility(View.VISIBLE);
				itemHolder.timeEnteredText.setText(DateUtils.formatDateTime(context, rating.timeEntered.getTime(),
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_NUMERIC_DATE));
			} else {
				// Offline rating
				itemHolder.offlineBadge.setVisibility(View.VISIBLE);
				itemHolder.timeEnteredText.setVisibility(View.GONE);
			}
			itemHolder.brewerNameText.setText(rating.brewerName);
		}
	}

	@Override
	public int getItemCount() {
		return ratings.size() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? TYPE_HEADER : TYPE_ITEM;
	}

	public Rating get(int position) {
		return ratings.get(position - 1);
	}

	static class HeaderHolder extends RecyclerView.ViewHolder {

		final ImageView avatarImage;
		final TextView userNameText;
		final TextView userCountText;

		public HeaderHolder(View v) {
			super(v);
			avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
			userNameText = (TextView) v.findViewById(R.id.user_name_text);
			userCountText = (TextView) v.findViewById(R.id.user_count_text);
		}

	}

	static class ItemHolder extends RecyclerView.ViewHolder {

		final TextView ratingMarkText;
		final TextView beerNameText;
		final ImageView photoImage;
		final TextView timeEnteredText;
		final TextView brewerNameText;
		final View offlineBadge;

		public ItemHolder(View v) {
			super(v);
			ratingMarkText = (TextView) v.findViewById(R.id.rating_mark_text);
			beerNameText = (TextView) v.findViewById(R.id.beer_name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			timeEnteredText = (TextView) v.findViewById(R.id.date_text);
			brewerNameText = (TextView) v.findViewById(R.id.brewer_name_text);
			offlineBadge = v.findViewById(R.id.offline_badge);
		}

	}

}
