package com.ratebeer.android.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.widget.Toolbar;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.ratebeer.android.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class RateBeerActivity extends RxAppCompatActivity {

	protected final <T> Observable.Transformer<T, T> onUi() {
		return source -> source.subscribeOn(AndroidSchedulers.mainThread());
	}

	protected final <T> Observable.Transformer<T, T> toUi() {
		return source -> source.observeOn(AndroidSchedulers.mainThread());
	}

	protected final <T> Observable.Transformer<T, T> toIo() {
		return source -> source.observeOn(Schedulers.io());
	}

	protected final <T> Observable.Transformer<T, T> onIoToUi() {
		return source -> source.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
	}

	@SuppressLint("PrivateResource")
	protected final Toolbar setupDefaultUpButton() {
		Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		mainToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		RxToolbar.navigationClicks(mainToolbar).subscribe(ignore -> navigateUp());
		return mainToolbar;
	}

	protected final void navigateUp() {
		Intent clearStack = MainActivity.start(this);
		clearStack.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(clearStack);
	}

}
