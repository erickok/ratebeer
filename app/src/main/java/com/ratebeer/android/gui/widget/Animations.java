package com.ratebeer.android.gui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public final class Animations {

	public static void fadeFlip(final View in, final View out) {
		ObjectAnimator animOut = ObjectAnimator.ofFloat(out, "alpha", 1F, 0F);
		ObjectAnimator animIn = ObjectAnimator.ofFloat(in, "alpha", 0F, 1F);
		in.setAlpha(0F);
		in.setVisibility(View.VISIBLE);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animOut, animIn);
		animSetXY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				out.setVisibility(View.GONE);
			}
		});
		animSetXY.start();
	}

}
