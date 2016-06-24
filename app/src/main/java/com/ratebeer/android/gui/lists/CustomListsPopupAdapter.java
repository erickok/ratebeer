package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.ratebeer.android.R;
import com.ratebeer.android.db.views.CustomListWithPresence;

import java.util.List;

public final class CustomListsPopupAdapter extends RecyclerView.Adapter<CustomListsPopupAdapter.ViewHolder> {

	private List<CustomListWithPresence> customLists;

	public CustomListsPopupAdapter(List<CustomListWithPresence> customLists) {
		this.customLists = customLists;
	}

	@Override
	public CustomListsPopupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_custom_list_popup, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		CustomListWithPresence customList = customLists.get(position);
		holder.nameText.setText(customList.name);
		holder.nameText.setChecked(customList.hasBeer);
	}

	@Override
	public int getItemCount() {
		return customLists.size();
	}

	public CustomListWithPresence get(int position) {
		return customLists.get(position);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		final CheckedTextView nameText;

		public ViewHolder(View v) {
			super(v);
			nameText = (CheckedTextView) v.findViewById(R.id.name_text);
		}

	}

}
