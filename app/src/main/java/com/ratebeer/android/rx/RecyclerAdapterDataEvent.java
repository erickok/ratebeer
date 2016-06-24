package com.ratebeer.android.rx;

import android.support.v7.widget.RecyclerView;

public final class RecyclerAdapterDataEvent<T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> {

	final T adapter;
	final Kind kind;
	final Integer positionStart;
	final Integer positionTo;
	final Integer itemCount;

	private RecyclerAdapterDataEvent(T adapter, Kind kind, Integer positionStart, Integer positionTo, Integer itemCount) {
		this.adapter = adapter;
		this.kind = kind;
		this.positionStart = positionStart;
		this.positionTo = positionTo;
		this.itemCount = itemCount;
	}

	static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> RecyclerAdapterDataEvent<T> createChange(T adapter) {
		return new RecyclerAdapterDataEvent<>(adapter, Kind.CHANGE, null, null, null);
	}

	static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> RecyclerAdapterDataEvent<T> createRangeChange(T adapter, int
			positionStart, int itemCount) {
		return new RecyclerAdapterDataEvent<>(adapter, Kind.RANGE_CHANGE, positionStart, null, itemCount);
	}

	static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> RecyclerAdapterDataEvent<T> createRangeInsert(T adapter, int
			positionStart, int itemCount) {
		return new RecyclerAdapterDataEvent<>(adapter, Kind.RANGE_INSERT, positionStart, null, itemCount);
	}

	static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> RecyclerAdapterDataEvent<T> createRangeMove(T adapter, int
			fromPosition, int toPosition, int itemCount) {
		return new RecyclerAdapterDataEvent<>(adapter, Kind.RANGE_MOVE, fromPosition, toPosition, itemCount);
	}

	static <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> RecyclerAdapterDataEvent<T> createRangeRemove(T adapter, int
			positionStart, int itemCount) {
		return new RecyclerAdapterDataEvent<>(adapter, Kind.RANGE_REMOVE, positionStart, null, itemCount);
	}

	public T getAdapter() {
		return adapter;
	}

	public Kind getKind() {
		return kind;
	}

	public Integer getPositionStart() {
		return positionStart;
	}

	public Integer getPositionTo() {
		return positionTo;
	}

	public Integer getItemCount() {
		return itemCount;
	}

	public enum Kind {
		CHANGE,
		RANGE_CHANGE,
		RANGE_INSERT,
		RANGE_MOVE,
		RANGE_REMOVE
	}

}
