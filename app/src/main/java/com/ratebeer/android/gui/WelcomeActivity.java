package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ratebeer.android.R;
import com.ratebeer.android.Session;

public final class WelcomeActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, WelcomeActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
	}

	public void skip(View view) {
		// Don't show this screen again
		Session.get().registerIgnoreAccount();
		startActivity(MainActivity.start(this));
		finish();
	}

	public void signIn(View view) {
		startActivity(SignInActivity.start(this, false));
		finish();
	}

}
