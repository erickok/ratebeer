package com.ratebeer.android.gui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

public final class CheckableImageButton extends ImageButton implements Checkable {

	private static final int[] CheckedStateSet = {android.R.attr.state_checked};

	private boolean isChecked = false;

	public CheckableImageButton(Context context) {
		super(context);
	}

	public CheckableImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@SuppressWarnings("unused")
	public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public boolean isChecked() {
		return this.isChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if (this.isChecked != checked) {
			this.isChecked = checked;
			refreshDrawableState();
		}
	}

	@Override
	public void toggle() {
		setChecked(!this.isChecked);
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CheckedStateSet);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

}
