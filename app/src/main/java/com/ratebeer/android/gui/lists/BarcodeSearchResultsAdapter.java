package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ratebeer.android.api.model.BarcodeSearchResult;

import java.util.List;

public class BarcodeSearchResultsAdapter extends ArrayAdapter<BarcodeSearchResult> {

	public BarcodeSearchResultsAdapter(Context context, List<BarcodeSearchResult> results) {
		super(context, android.R.layout.simple_list_item_1, results);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView) super.getView(position, convertView, parent);
		view.setText(getItem(position).beerName);
		return view;
	}

}
