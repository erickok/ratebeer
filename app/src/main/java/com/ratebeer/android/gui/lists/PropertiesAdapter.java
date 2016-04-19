package com.ratebeer.android.gui.lists;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ratebeer.android.gui.widget.PropertyView;

import java.util.List;

public final class PropertiesAdapter extends BaseAdapter {

	private final List<PropertyView> properties;

	public PropertiesAdapter(List<PropertyView> properties) {
		this.properties = properties;
	}

	@Override
	public int getCount() {
		return properties.size();
	}

	@Override
	public PropertyView getItem(int position) {
		return properties.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return properties.get(position);
	}

}
