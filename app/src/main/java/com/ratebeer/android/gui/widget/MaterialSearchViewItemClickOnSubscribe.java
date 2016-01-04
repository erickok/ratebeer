package com.ratebeer.android.gui.widget;

import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import com.search.material.library.MaterialSearchView;

import rx.Observable;
import rx.Subscriber;

public class MaterialSearchViewItemClickOnSubscribe implements Observable.OnSubscribe<Integer> {

	private final MaterialSearchView view;

	public MaterialSearchViewItemClickOnSubscribe(MaterialSearchView view) {
		this.view = view;
	}

	@Override
	public void call(Subscriber<? super Integer> subscriber) {
		view.setOnItemClickListener((adapterView, view1, i, l) -> subscriber.onNext(i));

		subscriber.add(new MainThreadSubscription() {
			@Override
			protected void onUnsubscribe() {
				view.setOnItemClickListener(null);
			}
		});
	}

}
