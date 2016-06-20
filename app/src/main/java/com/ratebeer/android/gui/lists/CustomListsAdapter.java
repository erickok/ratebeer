package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.db.views.CustomListWithCount;

import java.util.List;
import java.util.Locale;

public final class CustomListsAdapter extends RecyclerView.Adapter<CustomListsAdapter.ViewHolder> {

	private List<CustomListWithCount> customLists;

	public CustomListsAdapter(List<CustomListWithCount> customLists) {
		this.customLists = customLists;
	}

	@Override
	public CustomListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_custom_list, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		CustomListWithCount customList = customLists.get(position);
		holder.countText.setBackgroundResource(ImageUrls.getColor(position));
		holder.countText.setText(String.format(Locale.getDefault(), "%1$d", customList.beerCount));
		holder.nameText.setText(customList.name);
	}

	@Override
	public int getItemCount() {
		return customLists.size();
	}

	public CustomListWithCount get(int position) {
		return customLists.get(position);
	}

	public void update(List<CustomListWithCount> customLists) {
		this.customLists = customLists;
		notifyDataSetChanged();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final View rowLayout;
		final TextView countText;
		final TextView nameText;

		public ViewHolder(View v) {
			super(v);
			rowLayout = v;
			countText = (TextView) v.findViewById(R.id.count_text);
			nameText = (TextView) v.findViewById(R.id.name_text);
		}

	}

}
