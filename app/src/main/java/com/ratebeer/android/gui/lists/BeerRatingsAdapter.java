package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BeerRating;
import com.ratebeer.android.gui.widget.Images;

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
		Images.with(holder.avatarImage.getContext()).loadUser(rating.userName).placeholder(android.R.color.white).fit().centerCrop()
				.into(holder.avatarImage);
		if (rating.timeEntered == null) {
			// Directly show comments
			holder.ratingCommentsText.setText(asHtml(rating.comments));
		} else {
			// Show comments and (in grey) the rating date
			Context context = holder.ratingCommentsText.getContext();
			String timeEnteredText = DateUtils.formatDateTime(context, rating.timeEntered.getTime(),
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
			SpannableStringBuilder commentsMarkup = new SpannableStringBuilder(asHtml(rating.comments));
			commentsMarkup.append(" ");
			commentsMarkup.append(timeEnteredText);
			commentsMarkup.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.grey_light)),
					commentsMarkup.length() - timeEnteredText.length(), commentsMarkup.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			commentsMarkup.setSpan(new RelativeSizeSpan(0.8F), commentsMarkup.length() - timeEnteredText.length(), commentsMarkup.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.ratingCommentsText.setText(commentsMarkup);
		}
		holder.ratingMarkText.setBackgroundResource(ImageUrls.getColor(position, true));
		holder.userNameText.setText(rating.userName);
		holder.userCountText.setText(String.format(Locale.getDefault(), "%1$d", rating.userRateCount));
		if (rating.timeEntered == null) {
			holder.offlineBadge.setVisibility(View.VISIBLE);
			holder.userCountryText.setVisibility(View.GONE);
			holder.ratingMarkText.setText("-");
		} else {
			holder.offlineBadge.setVisibility(View.GONE);
			holder.userCountryText.setVisibility(View.VISIBLE);
			holder.userCountryText.setText(rating.userCountryName);
			holder.ratingMarkText.setText(String.format(Locale.getDefault(), "%1$.1f", rating.total));
		}
	}

	private Spanned asHtml(String raw) {
		if (raw == null)
			return null;
		try {
			return Html.fromHtml(raw);
		} catch (Exception e) {
			// Happens such as when running out of memory on large strings
			return new SpannableString(raw);
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
		final View offlineBadge;

		public ViewHolder(View v) {
			super(v);
			avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
			ratingMarkText = (TextView) v.findViewById(R.id.rating_mark_text);
			ratingCommentsText = (TextView) v.findViewById(R.id.rating_comments_text);
			userNameText = (TextView) v.findViewById(R.id.user_name_text);
			userCountText = (TextView) v.findViewById(R.id.user_count_text);
			userCountryText = (TextView) v.findViewById(R.id.user_country_text);
			offlineBadge = v.findViewById(R.id.offline_badge);
		}

	}

}
