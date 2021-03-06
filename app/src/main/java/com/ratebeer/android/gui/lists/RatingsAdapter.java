package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.gui.services.SyncService;
import com.ratebeer.android.gui.widget.Images;

import java.util.List;
import java.util.Locale;

import rx.android.schedulers.AndroidSchedulers;

public final class RatingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;

	private final Context context;
	private final MenuInflater menuInflater;
	private List<Rating> ratings;

	public RatingsAdapter(Context context, MenuInflater menuInflater, List<Rating> ratings) {
		this.context = context;
		this.ratings = ratings;
		this.menuInflater = menuInflater;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_HEADER && Session.get().isLoggedIn())
			return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header_account, parent, false), menuInflater);
		else
			return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rating, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof HeaderHolder) {
			HeaderHolder headerHolder = (HeaderHolder) holder;
			Images.with(headerHolder.avatarImage.getContext()).loadUser(Session.get().getUserName()).placeholder(R.color.grey_dark).fit()
					.centerCrop()
					.into(headerHolder.avatarImage);
			headerHolder.userNameText.setText(Session.get().getUserName());
			headerHolder.refreshMenu.setOnMenuItemClickListener(item -> {
				if (item.getItemId() == R.id.menu_refresh) {
					context.startService(SyncService.start(context));
				}
				return true;
			});
			// Monitor changes in the user counts too
			Session.get().getUpdates(context, true).observeOn(AndroidSchedulers.mainThread()).subscribe(
					counts -> headerHolder.userCountText.setText(String.format(Locale.getDefault(), "%1$d", Session.get().getUserRateCount())));
		} else {
			Rating rating = ratings.get(position - 1);
			ItemHolder itemHolder = (ItemHolder) holder;
			if (rating.beerId != null && rating.beerId > 0) {
				Images.with(itemHolder.photoImage.getContext()).loadBeer(rating.beerId).placeholder(android.R.color.white).fit().centerInside()
						.into(itemHolder.photoImage);
			} else {
				itemHolder.rowLayout.setBackgroundResource(0);
				itemHolder.photoImage.setImageResource(0);
			}
			itemHolder.ratingMarkText.setBackgroundResource(ImageUrls.getColor(position, true));
			itemHolder.beerNameText.setText(rating.beerName);
			if (rating.isUploaded()) {
				// Already uploaded to RB: show rating date
				itemHolder.ratingMarkText.setText(String.format(Locale.getDefault(), "%1$.1f", rating.total));
				itemHolder.offlineBadge.setVisibility(View.GONE);
				itemHolder.timeEnteredText.setVisibility(View.VISIBLE);
				itemHolder.timeEnteredText.setText(DateUtils.formatDateTime(context, rating.timeEntered.getTime(),
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_NUMERIC_DATE));
			} else {
				// Offline rating
				itemHolder.ratingMarkText.setText("-");
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
		if (position == 0)
			return null; // Header, not a rating
		return ratings.get(position - 1);
	}

	public void update(List<Rating> ratings) {
		this.ratings = ratings;
		notifyDataSetChanged();
	}

	static class HeaderHolder extends RecyclerView.ViewHolder {

		final ImageView avatarImage;
		final TextView userNameText;
		final ActionMenuView refreshMenu;
		final TextView userCountText;

		public HeaderHolder(View v, MenuInflater menuInflater) {
			super(v);
			avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
			userNameText = (TextView) v.findViewById(R.id.user_name_text);
			userCountText = (TextView) v.findViewById(R.id.user_count_text);
			refreshMenu = (ActionMenuView) v.findViewById(R.id.refresh_menu);
			menuInflater.inflate(R.menu.menu_refresh, refreshMenu.getMenu());
		}

	}

	static class ItemHolder extends RecyclerView.ViewHolder {

		final View rowLayout;
		final TextView ratingMarkText;
		final TextView beerNameText;
		final ImageView photoImage;
		final TextView timeEnteredText;
		final TextView brewerNameText;
		final View offlineBadge;

		public ItemHolder(View v) {
			super(v);
			rowLayout = v;
			ratingMarkText = (TextView) v.findViewById(R.id.rating_mark_text);
			beerNameText = (TextView) v.findViewById(R.id.beer_name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			timeEnteredText = (TextView) v.findViewById(R.id.date_text);
			brewerNameText = (TextView) v.findViewById(R.id.brewer_name_text);
			offlineBadge = v.findViewById(R.id.offline_badge);
		}

	}

}
