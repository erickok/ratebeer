package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BreweryBeer;
import com.ratebeer.android.gui.widget.Images;

import java.util.List;

public final class BreweryPropertiesBeersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_PROPERTY = 0;
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_SECTION_DIVIDER = 2;

	private List<Property> properties;
	private List<BreweryBeer> beers;

	public void setProperties(List<Property> properties) {
		this.properties = properties;
		notifyDataSetChanged();
	}

	public void setBeers(List<BreweryBeer> beers) {
		boolean hadBeers = this.beers != null;
		this.beers = beers;
		// Do not notify yet of the new data until we have properties loaded
		if (this.properties != null) {
			if (hadBeers)
				notifyDataSetChanged(); // Brute force refresh the adapter view
			else
				notifyItemRangeInserted(this.properties.size() + 1, this.properties.size() + 1 + this.beers.size());
		}
	}

	public BreweryBeer getBeer(int position) {
		if (properties != null && beers != null && position >= properties.size() + 1) {
			return beers.get(position - (properties.size() + 1));
		}
		return null;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_PROPERTY)
			return Property.PropertyHolder.build(parent, R.color.yellow_main, R.drawable.back_selectable_yellow);
		else if (viewType == TYPE_SECTION_DIVIDER)
			return SectionDividerHolder.build(parent);
		else
			return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_beer_by_brewery, parent, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof Property.PropertyHolder) {
			((Property.PropertyHolder) holder).bind(properties.get(position));
		} else if (holder instanceof ItemHolder) {
			BreweryBeer beer = beers.get(position - (properties.size() + 1));
			ItemHolder itemHolder = (ItemHolder) holder;
			itemHolder.ratingText.setBackgroundResource(ImageUrls.getColor(position));
			itemHolder.ratingText.setText(beer.getOverallPercentileString());
			itemHolder.titleText.setText(beer.beerName);
			Images.with(itemHolder.photoImage.getContext()).loadBeer(beer.beerId).placeholder(android.R.color.white).fit().centerInside().into
					(itemHolder.photoImage);
			itemHolder.retiredBadge.setVisibility(beer.retired ? View.VISIBLE : View.GONE);
			itemHolder.aliasBadge.setVisibility(beer.alias ? View.VISIBLE : View.GONE);
			itemHolder.ratedBadge.setVisibility(beer.ratedByUser ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		// We will show data iff the (possibly empty) brewer properties are loaded, regardless of whether the beers are loaded already
		if (properties == null)
			return 0;
		return properties.size() + 1 + (beers == null ? 0 : beers.size());
	}

	@Override
	public int getItemViewType(int position) {
		if (properties != null && position < properties.size())
			return TYPE_PROPERTY;
		else if (properties != null && position == properties.size())
			return TYPE_SECTION_DIVIDER;
		else
			return TYPE_ITEM;
	}

	static class ItemHolder extends RecyclerView.ViewHolder {

		final TextView ratingText;
		final TextView titleText;
		final ImageView photoImage;
		final View retiredBadge;
		final View aliasBadge;
		final View ratedBadge;

		public ItemHolder(View v) {
			super(v);
			ratingText = (TextView) v.findViewById(R.id.rating_text);
			titleText = (TextView) v.findViewById(R.id.name_text);
			photoImage = (ImageView) v.findViewById(R.id.photo_image);
			retiredBadge = v.findViewById(R.id.retired_badge);
			aliasBadge = v.findViewById(R.id.alias_badge);
			ratedBadge = v.findViewById(R.id.rated_badge);
		}

	}

}
