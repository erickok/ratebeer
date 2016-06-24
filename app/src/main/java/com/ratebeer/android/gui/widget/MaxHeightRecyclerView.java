package com.ratebeer.android.gui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ratebeer.android.R;

public final class MaxHeightRecyclerView extends RecyclerView {

	private int maxHeight;

	public MaxHeightRecyclerView(Context context) {
		super(context);
	}

	public MaxHeightRecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public MaxHeightRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView);
		if (a.hasValue(R.styleable.MaxHeightRecyclerView_android_maxHeight)) {
			maxHeight = (int) a.getDimension(R.styleable.MaxHeightRecyclerView_android_maxHeight, 0);
		}
		a.recycle();
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		if (maxHeight > 0) {
			int hSize = MeasureSpec.getSize(heightSpec);
			int hMode = MeasureSpec.getMode(heightSpec);

			switch (hMode) {
				case MeasureSpec.AT_MOST:
					heightSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.AT_MOST);
					break;
				case MeasureSpec.UNSPECIFIED:
					heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
					break;
				case MeasureSpec.EXACTLY:
					heightSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.EXACTLY);
					break;
			}
		}
		super.onMeasure(widthSpec, heightSpec);
	}

}
