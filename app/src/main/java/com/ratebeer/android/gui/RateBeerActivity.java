package com.ratebeer.android.gui;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RateBeerActivity extends RxAppCompatActivity {

	public final <T> Observable.Transformer<T, T> onUi() {
		return source -> source.subscribeOn(AndroidSchedulers.mainThread());
	}

	public final <T> Observable.Transformer<T, T> toUi() {
		return source -> source.observeOn(AndroidSchedulers.mainThread());
	}

	public final <T> Observable.Transformer<T, T> toIo() {
		return source -> source.observeOn(Schedulers.io());
	}

	public final <T> Observable.Transformer<T, T> onIoToUi() {
		return source -> source.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
	}

}
