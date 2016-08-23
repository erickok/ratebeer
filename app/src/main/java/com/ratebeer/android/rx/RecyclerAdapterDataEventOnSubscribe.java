package com.ratebeer.android.rx;

import android.support.v7.widget.RecyclerView;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

import static rx.android.MainThreadSubscription.verifyMainThread;

final class RecyclerAdapterDataEventOnSubscribe<T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> implements Observable
		.OnSubscribe<RecyclerAdapterDataEvent<T>> {

	final T adapter;

	RecyclerAdapterDataEventOnSubscribe(T adapter) {
		this.adapter = adapter;
	}

	@Override
	public void call(final Subscriber<? super RecyclerAdapterDataEvent<T>> subscriber) {
		verifyMainThread();

		final RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {

			@Override
			public void onChanged() {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(RecyclerAdapterDataEvent.createChange(adapter));
				}
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(RecyclerAdapterDataEvent.createRangeChange(adapter, positionStart, itemCount));
				}
			}

			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(RecyclerAdapterDataEvent.createRangeInsert(adapter, positionStart, itemCount));
				}
			}

			@Override
			public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(RecyclerAdapterDataEvent.createRangeMove(adapter, fromPosition, toPosition, itemCount));
				}
			}

			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(RecyclerAdapterDataEvent.createRangeRemove(adapter, positionStart, itemCount));
				}
			}
		};

		adapter.registerAdapterDataObserver(observer);

		subscriber.add(new MainThreadSubscription() {
			@Override
			protected void onUnsubscribe() {
				adapter.unregisterAdapterDataObserver(observer);
			}
		});

		// Emit initial value.
		//subscriber.onNext(RecyclerAdapterDataEvent.createChange(adapter));
	}

}
