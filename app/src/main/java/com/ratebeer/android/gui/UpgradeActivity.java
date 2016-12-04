package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.db.OfflineRating;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.gui.widget.Animations;

import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public class UpgradeActivity extends RateBeerActivity {

	private boolean doSkip = false;

	public static Intent start(Context context) {
		return new Intent(context, UpgradeActivity.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgrade);
	}

	public void accept(View view) {

		View upgradeProgress = findViewById(R.id.upgrade_progress);
		View decisionLayout = findViewById(R.id.decision_layout);

		Animations.fadeFlip(upgradeProgress, decisionLayout);

		// Copy over old offline ratings to the new ratings database table
		Observable<Rating> upgradeRatings = rxdb(this).query(OfflineRating.class)
				.map(Rating::fromOfflineRating)
				.doOnNext(rxdb(this).put());

		// If user was logged in, copy over the account info
		Observable<Boolean> legacyLogin = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// Old app stored the user details in a "user_settings" setting with |-separated fields
		String legacyUser = prefs.getString("user_settings", null);
		if (legacyUser != null) {
			String[] parts = legacyUser.split("\\|");
			if (parts.length > 2) {
				String legacyUserName = parts[1];
				String legacyUserPassword = parts[2];
				legacyLogin = Api.get().login(legacyUserName, legacyUserPassword);
			}
		}

		// Execute upgrade (emit error when unsuccessful)
		Observable<Boolean> upgrade = upgradeRatings.count()
				.doOnNext(count -> RBLog.d("Copied " + count + " offline ratings"))
				.map(count -> true);
		if (legacyLogin != null)
			upgrade = Observable.combineLatest(
					upgrade,
					legacyLogin,
					(upgraded, signedIn) -> upgraded && signedIn)
					.filter(done -> done);
		upgrade.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(success -> {
					// Remove the old "is_first_start" key to indicate that we have upgraded
					prefs.edit().remove("is_first_start").apply();
					// Successfully upgraded the account
					startActivity(MainActivity.start(this));
					finish();
				}, e -> {
					Snackbar.show(this, R.string.error_upgradefailed);
					Animations.fadeFlip(decisionLayout, upgradeProgress);
					// Allow skipping of the upgrade/login step
					doSkip = true;
					((Button) findViewById(R.id.decline_button)).setText(R.string.signin_skip);
				});
	}

	public void declineSkip(View view) {
		if (doSkip) {
			// Remove the old "is_first_start" key to indicate that we have upgraded
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.edit().remove("is_first_start").apply();
			startActivity(MainActivity.start(this));
			finish();
		} else {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Api.DOMAIN + "/feedback")));
		}
	}

}
