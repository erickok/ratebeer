package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.ratebeer.android.BuildConfig;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.gui.widget.Animations;

import java.util.Locale;

public final class AboutActivity extends RateBeerActivity {

	private Button signInOutButton;
	private ProgressBar signoutProgress;

	public static Intent start(Context context) {
		return new Intent(context, AboutActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		signInOutButton = (Button) findViewById(R.id.signinout_button);
		signoutProgress = (ProgressBar) findViewById(R.id.signout_progress);

		setupDefaultUpButton();
	}

	@Override
	protected void onResume() {
		super.onResume();

		signInOutButton.setText(Session.get().isLoggedIn() ? R.string.help_signout : R.string.help_signin);
	}

	public void openSignInOut(View view) {
		if (!Session.get().isLoggedIn()) {
			startActivity(SignInActivity.start(this, true));
		} else {
			Animations.fadeFlip(signoutProgress, signInOutButton);
			Api.get().logout().doOnNext(ignore -> Db.clearRatings(this)).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(success -> {
				navigateUp(); // Restart main activity to refresh activities state
				finish();
			}, e -> {
				Snackbar.show(this, R.string.error_connectionfailure);
				Animations.fadeFlip(signInOutButton, signoutProgress);
			});
		}
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

	public void openGithub(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/erickok/ratebeer")));
	}

}
