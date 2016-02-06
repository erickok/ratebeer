package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ratebeer.android.R;
import com.ratebeer.android.Session;

public final class SignInActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, SignInActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
	}

	public void advance(View view) {
		Session.get().registerIgnoreAccount();
		startActivity(MainActivity.start(this));
		finish();
	}

}
