package com.ratebeer.android.gui;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.quinny898.library.persistentsearch.SearchBox;
import com.ratebeer.android.R;
import com.ratebeer.android.gui.widget.RxSearchBox;
import com.ratebeer.android.gui.widget.SearchBoxSearchEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.Observable;

public class MainActivity extends RxAppCompatActivity {

	private SearchBox searchView;
	private RecyclerView ratingsList;
	private RecyclerView friendsFeedList;
	private RecyclerView localFeedList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		searchView = (SearchBox) findViewById(R.id.search_view);
		ViewPager listsPager = (ViewPager) findViewById(R.id.lists_pager);

		// Set up navigation drawer
		RxSearchBox.menuEvents(searchView).compose(bindToLifecycle()).subscribe();

		// Set up tabs
		listsPager.setAdapter(new ActivityPagerAdapter());

		// Set up search box
		Observable<SearchBoxSearchEvent> searchEvents = RxSearchBox.searchEvents(searchView).share();
		searchEvents.ofType(SearchBoxSearchEvent.SearchBoxQueryChangeEvent.class).compose(bindToLifecycle()).subscribe(queryChangeEvent -> refreshSearchResults(queryChangeEvent.query()));
		searchEvents.ofType(SearchBoxSearchEvent.SearchBoxResultClickEvent.class).compose(bindToLifecycle()).subscribe(resultClickEvent -> performSearch(resultClickEvent.searchResult().title));

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
