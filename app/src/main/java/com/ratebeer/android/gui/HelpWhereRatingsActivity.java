package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.ratebeer.android.R;

public final class HelpWhereRatingsActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, HelpWhereRatingsActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_whereratings);

		// Set up toolbar
		Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		mainToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		RxToolbar.navigationClicks(mainToolbar).subscribe(ignore -> onBackPressed());
	}

}
