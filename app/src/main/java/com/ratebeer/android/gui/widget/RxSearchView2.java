package com.ratebeer.android.gui.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;

import rx.Observable;

public final class RxSearchView2 {

	public static Observable<Integer> suggestionClicks(@NonNull SearchView view, boolean overrideSubmitOnClick) {
		return Observable.create(new SearchViewSuggestionClicksOnSubscribe(view, overrideSubmitOnClick));
	}

}
