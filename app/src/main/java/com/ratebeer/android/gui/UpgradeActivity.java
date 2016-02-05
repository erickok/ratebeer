package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.ratebeer.android.R;
import com.ratebeer.android.Session;

public class UpgradeActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, UpgradeActivity.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgrade);
	}

	public void accept(View view) {
		Session.get().completeUpgrade();
		startActivity(MainActivity.start(this));
		finish();
	}

	public void decline(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ratebeer.com/feedback")));
	}

}
