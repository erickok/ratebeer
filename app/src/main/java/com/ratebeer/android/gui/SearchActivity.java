package com.ratebeer.android.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ratebeer.android.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

public class SearchActivity extends RxAppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

}
