package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ratebeer.android.R;

public class SearchActivity extends RateBeerActivity {

	public static Intent start(Context context, String query) {
		return new Intent(context, SearchActivity.class).putExtra("query", query);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
	}

}
