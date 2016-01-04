package com.ratebeer.android.gui.widget;

import com.search.material.library.MaterialSearchView;

import rx.Observable;

public final class RxMaterialSearchView {

	public static Observable<Integer> itemClicks(MaterialSearchView view) {
		return Observable.create(new MaterialSearchViewItemClickOnSubscribe(view));
	}

	private RxMaterialSearchView() {
		throw new AssertionError("No instances.");
	}

}
