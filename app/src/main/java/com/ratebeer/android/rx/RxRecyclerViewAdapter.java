package com.ratebeer.android.rx;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import rx.Observable;

public final class RxRecyclerViewAdapter {

	public static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> Observable<RecyclerAdapterDataEvent<T>> dataEvents(@NonNull
																																		   T view) {
		return Observable.create(new RecyclerAdapterDataEventOnSubscribe<>(view));
	}

}
