package com.ratebeer.android.gui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.ratebeer.android.R;

public final class Animations {

	private static final long EXPAND_COLLAPSE_DURATION = 600; // ms

	public static void fadeFlip(final View in, final View out) {
		ObjectAnimator animOut = ObjectAnimator.ofFloat(out, "alpha", 1F, 0F);
		ObjectAnimator animIn = ObjectAnimator.ofFloat(in, "alpha", 0F, 1F);
		in.setAlpha(0F);
		in.setVisibility(View.VISIBLE);
		in.setTag(View.VISIBLE);
		out.setTag(View.INVISIBLE);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animOut, animIn);
		animSetXY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				//noinspection WrongConstant
				out.setVisibility((Integer) out.getTag());
			}
		});
		animSetXY.start();
	}

	public static void fadeFlipOut(final View in, final View out1, final View out2) {
		ObjectAnimator animOut1 = ObjectAnimator.ofFloat(out1, "alpha", 1F, 0F);
		ObjectAnimator animOut2 = ObjectAnimator.ofFloat(out2, "alpha", 1F, 0F);
		ObjectAnimator animIn = ObjectAnimator.ofFloat(in, "alpha", 0F, 1F);
		in.setAlpha(0F);
		in.setVisibility(View.VISIBLE);
		in.setTag(View.VISIBLE);
		out1.setTag(View.INVISIBLE);
		out2.setTag(View.INVISIBLE);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animOut1, animOut2, animIn);
		animSetXY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				//noinspection WrongConstant
				out1.setVisibility((Integer) out1.getTag());
				//noinspection WrongConstant
				out2.setVisibility((Integer) out2.getTag());
			}
		});
		animSetXY.start();
	}

	public static void fadeFlipIn(final View in1, final View in2, final View out) {
		ObjectAnimator animOut = ObjectAnimator.ofFloat(out, "alpha", 1F, 0F);
		ObjectAnimator animIn1 = ObjectAnimator.ofFloat(in1, "alpha", 0F, 1F);
		ObjectAnimator animIn2 = ObjectAnimator.ofFloat(in2, "alpha", 0F, 1F);
		in1.setAlpha(0F);
		in2.setAlpha(0F);
		in1.setVisibility(View.VISIBLE);
		in2.setVisibility(View.VISIBLE);
		in1.setTag(View.VISIBLE);
		in2.setTag(View.VISIBLE);
		out.setTag(View.INVISIBLE);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animOut, animIn1);
		animSetXY.playTogether(animOut, animIn2);
		animSetXY.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				//noinspection WrongConstant
				out.setVisibility((Integer) out.getTag());
			}
		});
		animSetXY.start();
	}

	public static void collapseToTop(View view) {
		final int startHeight = (int) view.getTag(R.id.expand_collapse_height_tag);
		Animation animation = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				super.applyTransformation(interpolatedTime, t);
				view.getLayoutParams().height = (int) (startHeight * (1 - interpolatedTime));
				view.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		animation.setDuration(EXPAND_COLLAPSE_DURATION);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.getLayoutParams().height = startHeight;
				view.setVisibility(View.GONE);
				view.requestLayout();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		view.startAnimation(animation);
	}

	public static void expandFromTop(View view) {
		view.setVisibility(View.VISIBLE);
		Animation animation = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				super.applyTransformation(interpolatedTime, t);
				if (view.getTag(R.id.expand_collapse_height_tag) == null)
					view.setTag(R.id.expand_collapse_height_tag, view.getHeight());
				view.getLayoutParams().height = (int) (((int) view.getTag(R.id.expand_collapse_height_tag)) * interpolatedTime);
				view.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		animation.setDuration(EXPAND_COLLAPSE_DURATION);
		view.startAnimation(animation);
	}

}
