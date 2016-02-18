package com.ratebeer.android.rx;

import rx.Observable;
import rx.Subscriber;

public final class AsRangeOperator implements Observable.Operator<Integer, Integer> {

	@Override
	public Subscriber<? super Integer> call(Subscriber<? super Integer> subscriber) {

		return new Subscriber<Integer>() {
			@Override
			public void onCompleted() {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onCompleted();
				}
			}

			@Override
			public void onError(Throwable e) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onError(e);
				}
			}

			@Override
			public void onNext(Integer count) {
				if (!subscriber.isUnsubscribed()) {
					for (int i = 0; i < count; i++) {
						subscriber.onNext(i);
					}
				}
			}
		};

	}

}
