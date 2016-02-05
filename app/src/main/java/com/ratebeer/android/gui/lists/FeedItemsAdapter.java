package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.FeedItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public final class FeedItemsAdapter extends RecyclerView.Adapter<FeedItemsAdapter.ViewHolder> {

	private final List<FeedItem> feedItems;

	public FeedItemsAdapter(List<FeedItem> feedItems) {
		this.feedItems = feedItems;
	}

	@Override
	public FeedItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_feed_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		FeedItem feedItem = feedItems.get(position);
		Picasso.with(holder.avatarImage.getContext()).load(ImageUrls.getUserPhotoUrl(feedItem.userName))
				.placeholder(ImageUrls.getColor(position, true)).fit().centerCrop().into(holder.avatarImage);
		if (feedItem.getBeerId() != null) {
			holder.beerImage.setVisibility(View.VISIBLE);
			Picasso.with(holder.beerImage.getContext()).load(ImageUrls.getBeerPhotoUrl(feedItem.getBeerId()))
					.placeholder(ImageUrls.getColor(position, false)).fit().centerInside().into(holder.beerImage);
		} else {
			holder.beerImage.setVisibility(View.GONE);
		}
		holder.activityText.setText(buildActivityText(feedItem.userName, feedItem.linkText));
	}

	private CharSequence buildActivityText(String userName, String linkText) {
		// Build a span that contains the user name and the action he/she performed
		SpannableStringBuilder builder = new SpannableStringBuilder();
		StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
		// Start with username, in bold
		builder.append(userName);
		builder.setSpan(bold, 0, userName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.append(" ");
		// Parse HTML as spannable from raw link text
		Spanned html = Html.fromHtml(linkText);
		builder.append(html);
		URLSpan[] links = builder.getSpans(0, html.length(), URLSpan.class);
		for (URLSpan link : links) {
			// Make link tags bold (they are not clickable though)
			builder.setSpan(bold, builder.getSpanStart(link), builder.getSpanEnd(link), builder.getSpanFlags(link));
		}
		return builder;
	}

	@Override
	public int getItemCount() {
		return feedItems.size();
	}

	public FeedItem get(int position) {
		return feedItems.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final ImageView avatarImage;
		final ImageView beerImage;
		final TextView activityText;

		public ViewHolder(View v) {
			super(v);
			avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
			beerImage = (ImageView) v.findViewById(R.id.beer_image);
			activityText = (TextView) v.findViewById(R.id.activity_text);
		}

	}

}
