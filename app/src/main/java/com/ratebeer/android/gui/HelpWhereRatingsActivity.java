package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ratebeer.android.R;

public final class HelpWhereRatingsActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, HelpWhereRatingsActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_whereratings);

		setupDefaultUpButton();
	}

}
