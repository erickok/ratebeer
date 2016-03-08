package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.gui.widget.Images;

import java.util.List;

public final class FeedItemsAdapter extends RecyclerView.Adapter<FeedItemsAdapter.ViewHolder> {

	private final Drawable selectableBackgroundDrawable;
	private List<FeedItem> feedItems;

	public FeedItemsAdapter(Context context, List<FeedItem> feedItems) {
		this.feedItems = feedItems;
		int[] attrs = new int[]{android.R.attr.selectableItemBackground};
		TypedArray ta = context.obtainStyledAttributes(attrs);
		selectableBackgroundDrawable = ta.getDrawable(0);
		ta.recycle();
	}

	@Override
	public FeedItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_feed_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		FeedItem feedItem = feedItems.get(position);
		Images.with(holder.avatarImage.getContext()).loadUser(feedItem.userName).fit().centerCrop().into(holder.avatarImage);
		if (feedItem.getBeerId() != null) {
			// A clickable row, bound to a specific beer (and show its photo)
			holder.rowLayout.setBackgroundDrawable(selectableBackgroundDrawable.getConstantState().newDrawable().mutate());
			holder.beerImage.setVisibility(View.VISIBLE);
			Images.with(holder.beerImage.getContext()).loadBeer(feedItem.getBeerId()).placeholder(android.R.color.white).fit().centerInside()
					.into(holder.beerImage);
		} else {
			// Not bound to a beer, so not clickable
			holder.rowLayout.setBackgroundResource(0);
			holder.beerImage.setVisibility(View.INVISIBLE);
		}
		holder.activityText.setText(buildActivityText(holder.activityText.getContext(), feedItem));
	}

	private CharSequence buildActivityText(Context context, FeedItem feedItem) {

		// Build a span that contains the user name and the action he/she performed
		SpannableStringBuilder builder = new SpannableStringBuilder();

		// Start with username, in bold
		builder.append(feedItem.userName);
		builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, feedItem.userName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		builder.append(" ");

		// For some activity types, insert the connecting string
		if (feedItem.type == FeedItem.ITEMTYPE_ISDRINKING) {
			builder.append(context.getString(R.string.feed_isdrinking));
			builder.append(" ");
			builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), builder.length() - 1, builder.length(),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		} else if (feedItem.type == FeedItem.ITEMTYPE_AWARD) {
			builder.append(context.getString(R.string.feed_wasawarded));
			builder.append(" ");
			builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), builder.length() - 1, builder.length(),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		} else if (feedItem.type == FeedItem.ITEMTYPE_PLACECHECKIN) {
			builder.append(context.getString(R.string.feed_checkedinat));
			builder.append(" ");
			builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), builder.length() - 1, builder.length(),
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		}

		// Parse HTML as spannable from raw link text and replace links with a bold text
		Spanned html = Html.fromHtml(feedItem.linkText);
		builder.append(html);
		URLSpan[] links = builder.getSpans(0, builder.length(), URLSpan.class);
		for (URLSpan link : links) {
			// Make link tags bold (they are not clickable though)
			builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), builder.getSpanStart(link), builder.getSpanEnd(link),
					builder.getSpanFlags(link));
			builder.removeSpan(link);
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

	public void update(List<FeedItem> feedItems) {
		this.feedItems = feedItems;
		notifyDataSetChanged();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final View rowLayout;
		final ImageView avatarImage;
		final ImageView beerImage;
		final TextView activityText;

		public ViewHolder(View v) {
			super(v);
			rowLayout = v;
			avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
			beerImage = (ImageView) v.findViewById(R.id.beer_image);
			activityText = (TextView) v.findViewById(R.id.activity_text);
		}

	}

}
