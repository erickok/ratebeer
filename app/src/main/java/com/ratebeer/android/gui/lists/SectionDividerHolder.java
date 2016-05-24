package com.ratebeer.android.gui.lists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ratebeer.android.R;

public final class SectionDividerHolder extends RecyclerView.ViewHolder {

	private SectionDividerHolder(View v) {
		super(v);
	}

	public static SectionDividerHolder build(ViewGroup parent) {
		return new SectionDividerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_section_divider, parent, false));
	}

}
