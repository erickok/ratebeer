package com.ratebeer.android.gui;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ratebeer.android.R;
import com.ratebeer.android.gui.widget.RxMaterialSearchView;
import com.search.material.library.MaterialSearchView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

public class MainActivity extends RxAppCompatActivity {

	private MaterialSearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		searchView = (MaterialSearchView) findViewById(R.id.search_view);
		ViewPager listsPager = (ViewPager) findViewById(R.id.lists_pager);

		// Set up main toolbar with search
		mainToolbar.setTitle("RateBeer");
		mainToolbar.inflateMenu(R.menu.main);
		searchView.setMenuItem(mainToolbar.getMenu().findItem(R.id.action_search));
		RxMaterialSearchView.itemClicks(searchView).compose(bindToLifecycle()).subscribe();

		// Set up tabs
		listsPager.setAdapter(new ActivityPagerAdapter());

	}

	@Override
	public void onBackPressed() {
		if (searchView.isSearchOpen()) {
			searchView.closeSearch();
		} else {
			super.onBackPressed();
		}
	}

	private class ActivityPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 0;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

}
