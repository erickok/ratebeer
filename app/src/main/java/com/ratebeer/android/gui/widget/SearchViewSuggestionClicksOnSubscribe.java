package com.ratebeer.android.gui.widget;

import android.support.v7.widget.SearchView;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

final class SearchViewSuggestionClicksOnSubscribe implements Observable.OnSubscribe<Integer> {

	final SearchView view;
	final boolean overrideSubmitOnClick;

	SearchViewSuggestionClicksOnSubscribe(SearchView view, boolean overrideSubmitOnClick) {
		this.view = view;
		this.overrideSubmitOnClick = overrideSubmitOnClick;
	}

	@Override
	public void call(Subscriber<? super Integer> subscriber) {

		SearchView.OnSuggestionListener listener = new SearchView.OnSuggestionListener() {
			@Override
			public boolean onSuggestionSelect(int position) {
				return false;
			}

			@Override
			public boolean onSuggestionClick(int position) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(position);
				}
				return overrideSubmitOnClick;
			}
		};

		view.setOnSuggestionListener(listener);

		subscriber.add(new MainThreadSubscription() {
			@Override protected void onUnsubscribe() {
				view.setOnSuggestionListener(null);
			}
		});
	}

}
