package com.ratebeer.android.gui.widget;

import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import rx.Observable;
import rx.Subscriber;

final class SearchBoxSearchOnSubscribe implements Observable.OnSubscribe<SearchBoxSearchEvent> {

	private final SearchBox view;

	public SearchBoxSearchOnSubscribe(SearchBox view) {
		this.view = view;
	}

	@Override
	public void call(Subscriber<? super SearchBoxSearchEvent> subscriber) {
		view.setSearchListener(new SearchBox.SearchListener() {
			@Override
			public void onSearchOpened() {
				subscriber.onNext(new SearchBoxSearchEvent.SearchBoxOpenEvent());
			}

			@Override
			public void onSearchClosed() {
				subscriber.onNext(new SearchBoxSearchEvent.SearchBoxCloseEvent());
			}

			@Override
			public void onSearchCleared() {
				subscriber.onNext(new SearchBoxSearchEvent.SearchBoxClearEvent());
			}

			@Override
			public void onSearchTermChanged(String query) {
				subscriber.onNext(new SearchBoxSearchEvent.SearchBoxQueryChangeEvent(query));
			}

			@Override
			public void onSearch(String query) {
				subscriber.onNext(new SearchBoxSearchEvent.SearchBoxQuerySubmitEvent(query));
			}

			@Override
			public void onResultClick(SearchResult searchResult) {
				subscriber.onNext(new SearchBoxSearchEvent.SearchBoxResultClickEvent(searchResult));
			}
		});

		subscriber.add(new MainThreadSubscription() {
			@Override
			protected void onUnsubscribe() {
				view.setSearchListener(null);
			}
		});
	}

}
