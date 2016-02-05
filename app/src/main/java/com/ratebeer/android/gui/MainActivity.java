package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding.support.v7.widget.SearchViewQueryTextEvent;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.CupboardDbHelper;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.HistoricSearch;
import com.ratebeer.android.gui.lists.FeedItemsAdapter;
import com.ratebeer.android.gui.lists.RatingsAdapter;
import com.ratebeer.android.gui.widget.ItemClickSupport;
import com.ratebeer.android.gui.widget.RxSearchView2;
import com.ratebeer.android.gui.widget.RxViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;

public class MainActivity extends RateBeerActivity {

	private static final int TAB_RATINGS = 0;
	private static final int TAB_FEED_FRIENDS = 1;
	private static final int TAB_FEED_LOCAL = 2;
	private static final int TAB_FEED_GLOBAL = 3;

	private SearchView searchEdit;
	private ViewPager listsPager;
	private List<Integer> tabTypes;
	private List<View> tabs;
	private List<String> tabsTitles;

	public static Intent start(Context context) {
		return new Intent(context, MainActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (Session.get().isUpgrade()) {
			startActivity(UpgradeActivity.start(this));
			finish();
			return;
		}

		searchEdit = (SearchView) findViewById(R.id.search_edit);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		listsPager = (ViewPager) findViewById(R.id.lists_pager);

		// Set up tabs
		tabTypes = new ArrayList<>(4);
		tabs = new ArrayList<>(4);
		tabsTitles = new ArrayList<>(4);
		if (Session.get().isLoggedIn()) {
			addTab(TAB_RATINGS, R.string.main_myratings);
			addTab(TAB_FEED_FRIENDS, R.string.main_friends);
			addTab(TAB_FEED_LOCAL, R.string.main_friends);
		} else {
			addTab(TAB_FEED_GLOBAL, R.string.main_global);
		}
		RxViewPager.pageSelected(listsPager).subscribe(this::refreshTab);
		listsPager.setAdapter(new ActivityPagerAdapter());
		tabLayout.setupWithViewPager(listsPager);
		refreshTab(0);

		// Set up search box
		Cursor historicSuggestions = CupboardDbHelper.database(this).query(HistoricSearch.class).orderBy("time desc").getCursor();
		Cursor beerSuggestions = CupboardDbHelper.database(this).query(Beer.class).orderBy("name asc").getCursor();
		SimpleCursorAdapter searchSuggestionsAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
				new MergeCursor(new Cursor[]{historicSuggestions, beerSuggestions}), new String[]{"name"}, new int[]{android.R.id.text1},
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		searchEdit.setSuggestionsAdapter(searchSuggestionsAdapter);
		RxSearchView.queryTextChangeEvents(searchEdit).filter(SearchViewQueryTextEvent::isSubmitted).compose(onUi())
				.subscribe(event -> performSearch(event.queryText().toString()));
		RxSearchView2.suggestionClicks(searchEdit, false).compose(onUi()).subscribe(position -> performSearch(searchEdit.getQuery().toString()));

	}

	private void addTab(int tabType, int title) {
		RecyclerView recyclerView = (RecyclerView) getLayoutInflater().inflate(R.layout.view_feedlist, listsPager, false);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		tabTypes.add(tabType);
		tabs.add(recyclerView);
		tabsTitles.add(getString(title));
	}

	private void refreshTab(int position) {
		int type = tabTypes.get(position);
		RecyclerView view = ((RecyclerView) tabs.get(position));
		if (type == TAB_RATINGS) {
			Db.getLatestRatings(this, Session.get().getUserId()).toList().compose(onIoToUi()).compose(bindToLifecycle())
					.subscribe(ratings -> view.setAdapter(new RatingsAdapter(this, ratings)),
							e -> Snackbar.show(this, R.string.error_connectionfailure));
		} else {
			ItemClickSupport.addTo(view).setOnItemClickListener((parent, pos, v) -> openItem(((FeedItemsAdapter) view.getAdapter()).get(pos)));
			getTabFeed(type).toList().compose(onIoToUi()).compose(bindToLifecycle())
					.subscribe(feed -> view.setAdapter(new FeedItemsAdapter(feed)), e -> Snackbar.show(this, R.string.error_connectionfailure));
		}
	}

	private Observable<FeedItem> getTabFeed(int type) {
		if (type == TAB_FEED_LOCAL)
			return Api.get().getLocalFeed();
		else if (type == TAB_FEED_FRIENDS)
			return Api.get().getFriendsFeed();
		else
			return Api.get().getGlobalFeed();
	}

	private void openItem(FeedItem feedItem) {
		if (feedItem.getBeerId() != null) {
			startActivity(BeerActivity.start(this, feedItem.getBeerId()));
		}
	}

	private void performSearch(String query) {
		// Store as historic search query (or update it)
		HistoricSearch historicSearch = CupboardDbHelper.database(this).query(HistoricSearch.class).withSelection("name = ?", query).get();
		if (historicSearch == null) {
			historicSearch = new HistoricSearch();
			historicSearch.name = query;
		}
		historicSearch.time = new Date();
		CupboardDbHelper.database(this).put(historicSearch);

		// Perform live search on server
		startActivity(SearchActivity.start(this, query));
	}

	private class ActivityPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return tabs.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return tabsTitles.get(position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(tabs.get(position));
			return tabs.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

}
