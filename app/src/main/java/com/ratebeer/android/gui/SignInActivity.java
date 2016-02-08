package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.gui.widget.Animations;

public final class SignInActivity extends RateBeerActivity {

	public static Intent start(Context context) {
		return new Intent(context, SignInActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
	}

	public void signIn(View view) {

		View loginProgress = findViewById(R.id.login_progress);
		View decisionLayout = findViewById(R.id.decision_layout);
		String username = ((EditText) findViewById(R.id.username_edit)).getText().toString();
		String password = ((EditText) findViewById(R.id.password_edit)).getText().toString();

		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			Snackbar.show(this, R.string.error_nouserorpass);
			return;
		}

		Animations.fadeFlip(loginProgress, decisionLayout);

		Api.api().login(username, password).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(success -> {
			finish();
		}, e -> {
			Snackbar.show(this, R.string.error_authenticationfailed);
			Animations.fadeFlip(decisionLayout, loginProgress);
		});
	}

	public void createAccount(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ratebeer.com/newuser/")));
	}

}
