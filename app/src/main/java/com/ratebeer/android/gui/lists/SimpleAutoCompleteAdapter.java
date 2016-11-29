package com.ratebeer.android.gui.lists;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class SimpleAutoCompleteAdapter<T> extends ArrayAdapter<T> {

	public SimpleAutoCompleteAdapter(Context context, List<T> items) {
		super(context, android.R.layout.simple_list_item_1, items);
	}

}
