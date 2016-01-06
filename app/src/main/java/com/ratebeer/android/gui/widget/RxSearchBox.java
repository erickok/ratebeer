package com.ratebeer.android.gui.widget;

import com.quinny898.library.persistentsearch.SearchBox;

import rx.Observable;

public final class RxSearchBox {

	public static Observable<SearchBoxSearchEvent> searchEvents(SearchBox view) {
		return Observable.create(new SearchBoxSearchOnSubscribe(view));
	}

	public static Observable<Void> menuEvents(SearchBox view) {
		return Observable.create(new SearchBoxMenuOnSubscribe(view));
	}

	private RxSearchBox() {
		throw new AssertionError("No instances.");
	}

}
