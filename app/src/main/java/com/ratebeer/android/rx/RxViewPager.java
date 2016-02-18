package com.ratebeer.android.rx;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;

import rx.Observable;

public final class RxViewPager {

	public static Observable<Integer> pageSelected(@NonNull ViewPager view) {
		return Observable.create(new ViewPagerPageSelectedOnSubscribe(view));
	}

}
