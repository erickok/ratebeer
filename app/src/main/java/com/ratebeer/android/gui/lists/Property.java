package com.ratebeer.android.gui.lists;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ratebeer.android.R;

public final class Property {

	public int image;
	public String text;
	public View.OnClickListener clickListener;

	public Property() {}

	public Property(int image, String text, View.OnClickListener clickListener) {
		this.image = image;
		this.text = text;
		this.clickListener = clickListener;
	}

	static class PropertyHolder extends RecyclerView.ViewHolder {

		final View view;
		final ImageView propertyImage;
		final TextView propertyText;
		final int staticBackground;
		final int selectableBackground;
		Drawable selectableBackgroundDrawable;

		private PropertyHolder(View v, int staticBackgroundResId, int selectableBackgroundResId) {
			super(v);
			view = v;
			propertyImage = (ImageView) v.findViewById(R.id.property_image);
			propertyText = (TextView) v.findViewById(R.id.property_text);
			staticBackground = staticBackgroundResId;
			selectableBackground = selectableBackgroundResId;
		}

		public static PropertyHolder build(ViewGroup parent) {
			return build(parent, 0, 0);
		}

		public static PropertyHolder build(ViewGroup parent, int staticBackgroundResId) {
			return build(parent, staticBackgroundResId, 0);
		}

		public static PropertyHolder build(ViewGroup parent, int staticBackgroundResId, int selectableBackgroundResId) {
			return new PropertyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_property, parent, false),
					staticBackgroundResId, selectableBackgroundResId);
		}

		public void bind(Property property) {
			propertyImage.setImageResource(property.image);
			propertyText.setText(property.text);
			if (property.clickListener == null) {
				// Remove touch feedback
				view.setOnClickListener(null);
				view.setClickable(false);
				view.setFocusable(false);
				view.setBackgroundResource(staticBackground);
			} else {
				view.setOnClickListener(property.clickListener);
				view.setClickable(true);
				view.setFocusable(true);
				if (selectableBackground > 0) {
					view.setBackgroundResource(selectableBackground);
				} else {
					// Set a selectable ripple drawable background
					if (selectableBackgroundDrawable == null) {
						int[] attrs = new int[]{android.R.attr.selectableItemBackground};
						TypedArray ta = view.getContext().obtainStyledAttributes(attrs);
						selectableBackgroundDrawable = ta.getDrawable(0);
						ta.recycle();
					}
					view.setBackgroundDrawable(selectableBackgroundDrawable.getConstantState().newDrawable().mutate());
				}
			}
		}
	}

}
