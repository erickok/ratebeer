package com.ratebeer.android.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.ratebeer.android.ConnectivityHelper;
import com.ratebeer.android.R;

public final class Snackbar {

	private final View view;
	private final CharSequence text;

	private Snackbar(View view, CharSequence text) {
		this.view = view;
		this.text = text;
	}

	public static void show(View view, int resId) {
		// HACK Override error message when no connection is available at all
		if (resId == R.string.error_connectionfailure && ConnectivityHelper.current(view.getContext()) == ConnectivityHelper.ConnectivityType.NoConnection)
			resId = R.string.error_connectionunavailable;
		new Snackbar(view, view.getContext().getString(resId)).show();
	}

	public static void show(View view, String text) {
		new Snackbar(view, text).show();
	}

	public static void show(Activity activity, int resId) {
		// HACK Override error message when no connection is available at all
		if (resId == R.string.error_connectionfailure && ConnectivityHelper.current(activity) == ConnectivityHelper.ConnectivityType.NoConnection)
			resId = R.string.error_connectionunavailable;
		new Snackbar(activity.findViewById(android.R.id.content), activity.getString(resId)).show();
	}

	public static void show(Activity activity, String text) {
		new Snackbar(activity.findViewById(android.R.id.content), text).show();
	}

	public void show() {
		android.support.design.widget.Snackbar snackbar =
				android.support.design.widget.Snackbar.make(view, text, android.support.design.widget.Snackbar.LENGTH_LONG);
		TextView snackbarText = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarText.setTextColor(Color.WHITE);
		snackbar.show();
	}

}
