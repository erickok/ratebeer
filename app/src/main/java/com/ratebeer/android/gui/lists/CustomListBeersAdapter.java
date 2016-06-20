package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxRatingBar;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.db.CustomListBeer;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.gui.BeerActivity;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.Images;
import com.ratebeer.android.gui.widget.ImeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nl.nl2312.rxcupboard.DatabaseChange;
import rx.subscriptions.CompositeSubscription;

import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class CustomListBeersAdapter extends RecyclerView.Adapter<CustomListBeersAdapter.ViewHolder> {

	private final List<CustomListBeer> beers;
	private Integer highlightedItem;

	public CustomListBeersAdapter() {
		this.beers = new ArrayList<>();
	}

	@Override
	public CustomListBeersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_beer_on_custom_list, parent, false));
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);
	}

	@Override
	public void onViewRecycled(ViewHolder holder) {
		super.onViewRecycled(holder);
		if (holder.subscriptions.hasSubscriptions())
			holder.subscriptions.clear();
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (holder.subscriptions.hasSubscriptions())
			holder.subscriptions.clear();
		holder.beer = beers.get(position);

		boolean isHighlighted = this.highlightedItem != null && this.highlightedItem == position;
		Images.with(holder.photoImage.getContext()).loadBeer(holder.beer.beerId).placeholder(android.R.color.white).fit().centerCrop().into(holder
				.photoImage);
		holder.rowLayout.setActivated(isHighlighted);

		holder.noteText.setVisibility(isHighlighted ? View.GONE : View.VISIBLE);
		holder.noteEntryEdit.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
		holder.nameText.setText(holder.beer.beerName);
		holder.noteText.setText(holder.beer.note);
		holder.noteEntryEdit.setText(holder.beer.note);
		holder.subscriptions.add(RxTextView.textChanges(holder.noteEntryEdit).skip(1).doOnNext(holder.noteText::setText).map(note -> {
			holder.beer.note = note.toString();
			return holder.beer;
		}).subscribe(rxdb(holder.rowLayout.getContext()).put()));

		holder.starsButton.setBackgroundResource(ImageUrls.getColor(position));
		holder.starsText.setVisibility(holder.beer.stars == null || holder.beer.stars == 0 ? View.GONE : View.VISIBLE);
		holder.starsEmpty.setVisibility(holder.beer.stars == null || holder.beer.stars == 0 ? View.VISIBLE : View.GONE);
		holder.starsText.setText(String.format(Locale.getDefault(), "%1$d", holder.beer.stars));
		holder.starsEntryRating.setRating(holder.beer.stars == null? 0: holder.beer.stars);
		holder.starsEntryRating.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
		holder.subscriptions.add(RxRatingBar.ratingChanges(holder.starsEntryRating).skip(1).doOnNext(rating -> {
			boolean showRating = rating != null && rating > 0F;
			RBLog.d("Rating: " + rating);
			holder.starsText.setText(String.format(Locale.getDefault(), "%1$d", rating == null ? null : rating.intValue()));
			if (showRating && holder.starsText.getVisibility() != View.VISIBLE)
				Animations.fadeFlip(holder.starsText, holder.starsEmpty);
			else if (!showRating && holder.starsEmpty.getVisibility() != View.VISIBLE)
				Animations.fadeFlip(holder.starsEmpty, holder.starsText);
		}).map(rating -> {
			holder.beer.stars = rating == null || rating == 0F ? null : rating.intValue();
			return holder.beer;
		}).subscribe(rxdb(holder.rowLayout.getContext()).put()));

		holder.openBeerButton.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
		holder.openBeerButton.setOnClickListener(v -> {
			Context context = holder.openBeerButton.getContext();
			context.startActivity(BeerActivity.start(context, holder.beer.beerId));
		});
		holder.removeBeerButton.setVisibility(isHighlighted ? View.VISIBLE : View.GONE);
		holder.subscriptions.add(RxView.clicks(holder.removeBeerButton).subscribe(v -> {
			rxdb(holder.removeBeerButton.getContext()).delete(holder.beer);
		}));

		holder.rowLayout.setOnClickListener(v -> {
			Integer existingHighlightedItem = this.highlightedItem;
			if (existingHighlightedItem != null && existingHighlightedItem == holder.getAdapterPosition())
				// We were highlighted: close item
				this.highlightedItem = null;
			else
				this.highlightedItem = holder.getAdapterPosition();
			if (holder.noteEntryEdit.hasFocus()) {
				holder.noteEntryEdit.clearFocus();
			}
			ImeUtils.hideIme(holder.noteEntryEdit);
			if (this.highlightedItem != null && existingHighlightedItem != null)
				// Close last highlighted item
				notifyItemChanged(existingHighlightedItem);
			notifyItemChanged(holder.getAdapterPosition());
		});
	}

	@Override
	public int getItemCount() {
		return beers.size();
	}

	public CustomListBeer get(int position) {
		return beers.get(position);
	}

	public void init(List<CustomListBeer> beers) {
		this.beers.addAll(beers);
		notifyDataSetChanged();
	}

	public void change(DatabaseChange<CustomListBeer> change) {
		if (change instanceof DatabaseChange.DatabaseInsert) {
			beers.add(change.entity());
			notifyItemInserted(beers.size() - 1);
		} else if (change instanceof DatabaseChange.DatabaseDelete) {
			int position = beers.indexOf(change.entity());
			beers.remove(change.entity());
			notifyItemRemoved(position);
		} else if (change instanceof DatabaseChange.DatabaseUpdate) {
			int position = beers.indexOf(change.entity());
			beers.set(position, change.entity());
			// NOTE Changes are handled locally in the ViewHolder and do not need to be visualized directly
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final View rowLayout;
		final ImageView photoImage;
		final View starsButton;
		final ImageView starsEmpty;
		final TextView starsText;
		final TextView nameText;
		final TextView noteText;
		final EditText noteEntryEdit;
		final RatingBar starsEntryRating;
		final ImageButton openBeerButton;
		final ImageButton removeBeerButton;

		CustomListBeer beer;
		CompositeSubscription subscriptions = new CompositeSubscription();

		public ViewHolder(View v) {
			super(v);
			rowLayout = v;
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			starsButton = v.findViewById(R.id.stars_button);
			starsEmpty = (ImageView) v.findViewById(R.id.stars_empty);
			starsText = (TextView) v.findViewById(R.id.stars_text);
			nameText = (TextView) v.findViewById(R.id.name_text);
			noteText = (TextView) v.findViewById(R.id.note_text);
			noteEntryEdit = (EditText) v.findViewById(R.id.note_entry_edit);
			starsEntryRating = (RatingBar) v.findViewById(R.id.stars_entry_rating);
			openBeerButton = (ImageButton) v.findViewById(R.id.open_beer_button);
			removeBeerButton = (ImageButton) v.findViewById(R.id.remove_beer_button);
		}

	}

}
