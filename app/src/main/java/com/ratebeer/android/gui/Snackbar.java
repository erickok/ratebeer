package com.ratebeer.android.gui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public final class Snackbar {

	private final View view;
	private final CharSequence text;

	private Snackbar(View view, CharSequence text) {
		this.view = view;
		this.text = text;
	}

	public static void show(View view, int resId) {
		new Snackbar(view, view.getContext().getString(resId)).show();
	}

	public static void show(View view, String text) {
		new Snackbar(view, text).show();
	}

	public static void show(Activity activity, int resId) {
		new Snackbar(activity.findViewById(android.R.id.content), activity.getString(resId)).show();
	}

	public static void show(Activity activity, String text) {
		new Snackbar(activity.findViewById(android.R.id.content), text).show();
	}

	public void show() {
		android.support.design.widget.Snackbar snackbar =
				android.support.design.widget.Snackbar.make(view, text, android.support.design.widget.Snackbar.LENGTH_SHORT);
		TextView snackbarText = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarText.setTextColor(Color.WHITE);
		snackbar.show();
	}

}
