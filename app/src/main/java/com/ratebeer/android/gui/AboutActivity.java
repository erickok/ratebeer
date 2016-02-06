package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.ratebeer.android.BuildConfig;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;

import java.util.Locale;

public final class AboutActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, AboutActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		// Set up toolbar
		Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		mainToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		RxToolbar.navigationClicks(mainToolbar).subscribe(ignore -> onBackPressed());
	}

	public void openHelpWhereRatings(View view) {
		startActivity(HelpWhereRatingsActivity.start(this));
	}

	public void openReport(View view) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, "rb@2312.nl");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help with RateBeer Android app");
		emailIntent.putExtra(Intent.EXTRA_TEXT,
				String.format(Locale.US, "Please describe your problem:\n\n\n\nRateBeer username: %1$s\nRateBeer version: %2$s (%3$d)\n\n",
						Session.get().isLoggedIn() ? Session.get().getUserName() : "<not connected>", BuildConfig.VERSION_NAME,
						BuildConfig.VERSION_CODE));
		try {
			startActivity(emailIntent);
		} catch (Exception e) {
			// No email app available
		}
	}

	public void openRatebeer(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ratebeer.com")));
	}

	public void openUserAgreement(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ratebeer.com/UserAgreement.asp")));
	}

}
