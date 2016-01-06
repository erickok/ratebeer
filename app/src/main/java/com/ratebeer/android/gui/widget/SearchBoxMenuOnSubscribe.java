package com.ratebeer.android.gui.widget;

import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import com.quinny898.library.persistentsearch.SearchBox;

import rx.Observable;
import rx.Subscriber;

final class SearchBoxMenuOnSubscribe implements Observable.OnSubscribe<Void> {

	private final SearchBox view;

	public SearchBoxMenuOnSubscribe(SearchBox view) {
		this.view = view;
	}

	@Override
	public void call(Subscriber<? super Void> subscriber) {
		view.setMenuListener(() -> subscriber.onNext(null));

		subscriber.add(new MainThreadSubscription() {
			@Override
			protected void onUnsubscribe() {
				view.setMenuListener(null);
			}
		});
	}

}
