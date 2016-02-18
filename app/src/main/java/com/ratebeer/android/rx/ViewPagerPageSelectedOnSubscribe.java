package com.ratebeer.android.rx;

import android.support.v4.view.ViewPager;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

final class ViewPagerPageSelectedOnSubscribe implements Observable.OnSubscribe<Integer> {

	final ViewPager view;

	public ViewPagerPageSelectedOnSubscribe(ViewPager view) {
		this.view = view;
	}

	@Override
	public void call(Subscriber<? super Integer> subscriber) {

		ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// Ignore
			}

			@Override
			public void onPageSelected(int position) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(position);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// Ignore
			}
		};

		view.addOnPageChangeListener(listener);
	}

}
