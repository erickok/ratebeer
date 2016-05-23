package com.ratebeer.android.gui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ratebeer.android.R;

public class PropertyView extends LinearLayout {

	private ImageView propertyImage;
	private TextView propertyText;
	private Drawable selectableBackgroundDrawable;

	public PropertyView(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public PropertyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public PropertyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PropertyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
		final View view = LayoutInflater.from(context).inflate(R.layout.list_item_property, this, true);
		propertyImage = (ImageView) view.findViewById(R.id.property_image);
		propertyText = (TextView) view.findViewById(R.id.property_text);
	}

	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);

		// Set a selectable ripple drawable background
		if (selectableBackgroundDrawable == null) {
			int[] attrs = new int[]{android.R.attr.selectableItemBackground};
			TypedArray ta = getContext().obtainStyledAttributes(attrs);
			selectableBackgroundDrawable = ta.getDrawable(0);
			ta.recycle();
		}
		setBackgroundDrawable(selectableBackgroundDrawable.getConstantState().newDrawable().mutate());
	}

	public void setPropertyImage(Drawable drawable) {
		propertyImage.setImageDrawable(drawable);
	}

	public void setPropertyImage(int resId) {
		propertyImage.setImageResource(resId);
	}

	public void setPropertyText(CharSequence text) {
		propertyText.setText(text);
	}

}
