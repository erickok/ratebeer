package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

public final class PropertiesAdapter extends RecyclerView.Adapter<Property.PropertyHolder> {

	private final List<Property> properties;

	public PropertiesAdapter(List<Property> properties) {
		this.properties = properties;
	}

	@Override
	public Property.PropertyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return Property.PropertyHolder.build(parent);
	}

	@Override
	public void onBindViewHolder(Property.PropertyHolder holder, int position) {
		holder.bind(properties.get(position));
	}

	@Override
	public int getItemCount() {
		return properties.size();
	}

}
