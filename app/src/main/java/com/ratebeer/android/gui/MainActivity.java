package com.ratebeer.android.gui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.CupboardDbHelper;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.gui.widget.RxSearchBox;
import com.ratebeer.android.gui.widget.SearchBoxSearchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.nl2312.rxcupboard.RxCupboard;
import nl.nl2312.rxcupboard.RxDatabase;
import rx.Observable;

public class MainActivity extends RateBeerActivity {

	private SearchView searchText;
	private SimpleCursorAdapter searchSuggestionsAdapter;
	private SearchBox searchView;
	private RecyclerView ratingsList;
	private RecyclerView friendsFeedList;
	private RecyclerView localFeedList;
	private RecyclerView globalFeedList;
	private List<View> tabs;
	private List<String> tabsTitles;
	private RxDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		searchText = (SearchView) findViewById(R.id.search_text);
		searchView = (SearchBox) findViewById(R.id.search_view);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		ViewPager listsPager = (ViewPager) findViewById(R.id.lists_pager);

		database = RxCupboard.withDefault(CupboardDbHelper.connection(this));

		// Set up navigation drawer
		RxSearchBox.menuEvents(searchView).compose(bindToLifecycle()).subscribe();

		// Set up tabs
		tabs = new ArrayList<>(4);
		tabsTitles = new ArrayList<>(4);
		if (Session.get().isLoggedIn()) {
			addTab(ratingsList = new RecyclerView(this), R.string.main_myratings);
			addTab(friendsFeedList = new RecyclerView(this), R.string.main_friends);
			addTab(localFeedList = new RecyclerView(this), R.string.main_friends);
		} else {
			addTab(globalFeedList = new RecyclerView(this), R.string.main_global);
		}
		listsPager.setAdapter(new ActivityPagerAdapter());
		tabLayout.setupWithViewPager(listsPager);

		// Set up search box
		RxSearchView.queryTextChanges(searchText).debounce(400, TimeUnit.MILLISECONDS).filter(query -> query.length() >= 1).compose(toUi())
				.compose(bindToLifecycle()).subscribe(query -> refreshSearchResults2(query.toString()));
		Observable<SearchBoxSearchEvent> searchEvents = RxSearchBox.searchEvents(searchView).share();
		searchEvents.ofType(SearchBoxSearchEvent.SearchBoxQueryChangeEvent.class).debounce(400, TimeUnit.MILLISECONDS).compose(bindToLifecycle())
				.subscribe(queryChangeEvent -> refreshSearchResults(((SearchBoxSearchEvent.SearchBoxQueryChangeEvent) queryChangeEvent).query()));
		searchEvents.ofType(SearchBoxSearchEvent.SearchBoxResultClickEvent.class).compose(bindToLifecycle()).subscribe(
				resultClickEvent -> performSearch(((SearchBoxSearchEvent.SearchBoxResultClickEvent) resultClickEvent).searchResult().title));

	}

	private void refreshSearchResults2(String query) {
		Cursor suggestions = CupboardDbHelper.database(this).query(Beer.class).withSelection("name like ?", "%" + query + "%").limit(10).getCursor();
		if (searchSuggestionsAdapter == null) {
			searchSuggestionsAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, suggestions, new String[]{"name"},
					new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			searchText.setSuggestionsAdapter(searchSuggestionsAdapter);
		} else {
			searchSuggestionsAdapter.changeCursor(suggestions);
		}
		Observable.just(query)
				// Ratebeer search route only allows queries with > 4 characters
				.filter(q -> query.length() >= 4)
				// Perform live search
				.flatMap(q -> Api.get().searchBeers(q))
				.map(result -> {
					Beer beer = CupboardDbHelper.database(this).get(Beer.class, result.beerId);
					if (beer == null) {
						beer = Beer.fromSearchResult(result);
					}
					return beer;
				})
				.doOnNext(database::put).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(ignore -> {
			Cursor updatedSuggestions =
					CupboardDbHelper.database(this).query(Beer.class).withSelection("name like ?", "%" + query + "%").limit(10).getCursor();
			searchSuggestionsAdapter.changeCursor(updatedSuggestions);
		});
	}

	private void refreshSearchResults(String query) {
		Api.get().searchBeers(query)
				// Create a searchable item for the search box from each beer found
				.map(beerSearchResult -> new SearchResult(beerSearchResult.beerName))
				// Sort by beer name
				.toSortedList((left, right) -> left.title.compareTo(right.title)).flatMapIterable(results -> results).compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(searchable -> searchView.addSearchable((SearchResult) searchable), e -> RBLog.e("Search failed for '" + query + "'", e),
						() -> searchView.updateResults());
	}

	private void performSearch(String query) {
		startActivity(SearchActivity.start(this, query));
	}

	private void addTab(View view, int title) {
		tabs.add(view);
		tabsTitles.add(getString(title));
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
