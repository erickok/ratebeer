package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding.support.v7.widget.SearchViewQueryTextEvent;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.HistoricSearch;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.gui.lists.FeedItemsAdapter;
import com.ratebeer.android.gui.lists.RatingsAdapter;
import com.ratebeer.android.gui.lists.SearchSuggestion;
import com.ratebeer.android.gui.lists.SearchSuggestionsAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.ItemClickSupport;
import com.ratebeer.android.rx.RxViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.database;

public class MainActivity extends RateBeerActivity {

	private static final int TAB_RATINGS = 0;
	private static final int TAB_FEED_FRIENDS = 1;
	private static final int TAB_FEED_LOCAL = 2;
	private static final int TAB_FEED_GLOBAL = 3;

	private ViewPager listsPager;
	private View loadingProgress;
	private TextView progressText;
	private SearchSuggestionsAdapter searchSuggestionsAdaper;
	private int currentTab;
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
		} else if (!Session.get().hasIgnoredAccount() && !Session.get().isLoggedIn()) {
			startActivity(WelcomeActivity.start(this));
			finish();
			return;
		}

		ActionMenuView optionsMenu = (ActionMenuView) findViewById(R.id.options_menu);
		SearchView searchEdit = (SearchView) findViewById(R.id.search_edit);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		listsPager = (ViewPager) findViewById(R.id.lists_pager);
		loadingProgress = findViewById(R.id.loading_progress);
		progressText = (TextView) findViewById(R.id.progress_text);
		RecyclerView searchList = (RecyclerView) findViewById(R.id.search_list);

		// Set up tabs
		tabTypes = new ArrayList<>(3);
		tabs = new ArrayList<>(3);
		tabsTitles = new ArrayList<>(3);
		if (Session.get().isLoggedIn()) {
			addTab(TAB_RATINGS, R.string.main_myratings);
			addTab(TAB_FEED_FRIENDS, R.string.main_friends);
			addTab(TAB_FEED_LOCAL, R.string.main_local);
		} else {
			addTab(TAB_FEED_GLOBAL, R.string.main_global);
		}
		RxViewPager.pageSelected(listsPager).subscribe(page -> refreshTab(page, false));
		listsPager.setAdapter(new ActivityPagerAdapter());
		tabLayout.setupWithViewPager(listsPager);
		refreshTab(0, false);
		if (tabs.size() == 1)
			tabLayout.setVisibility(View.GONE);

		// Set up toolbar actions
		getMenuInflater().inflate(R.menu.menu_refresh, optionsMenu.getMenu());
		optionsMenu.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.menu_refresh) {
				refreshTab(currentTab, true);
			}
			return true;
		});

		// Set up search box: show results with search view focus, start search on query submit and show suggestions on query text changes
		searchList.setLayoutManager(new LinearLayoutManager(this));
		searchList.setAdapter(searchSuggestionsAdaper = new SearchSuggestionsAdapter());
		searchEdit.setOnQueryTextFocusChangeListener((view, b) -> searchList.setVisibility(b ? View.VISIBLE : View.GONE));
		ItemClickSupport.addTo(searchList).setOnItemClickListener((parent, pos, v) -> searchFromSuggestion(searchSuggestionsAdaper.get(pos)));
		Observable<SearchViewQueryTextEvent> queryTextChangeEvents =
				RxSearchView.queryTextChangeEvents(searchEdit).compose(onUi()).replay(1).refCount();
		queryTextChangeEvents.filter(SearchViewQueryTextEvent::isSubmitted).subscribe(event -> performSearch(event.queryText().toString()));
		queryTextChangeEvents.map(event -> event.queryText().toString()).switchMap(query -> {
			if (query.length() == 0) {
				return Db.getAllHistoricSearches(this).toList();
			} else {
				return Db.getSuggestions(this, query).toList();
			}
		}).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(suggestions -> searchSuggestionsAdaper.update(suggestions));

	}

	private void addTab(int tabType, int title) {
		RecyclerView recyclerView = (RecyclerView) getLayoutInflater().inflate(R.layout.view_feedlist, listsPager, false);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		tabTypes.add(tabType);
		tabs.add(recyclerView);
		tabsTitles.add(getString(title));
	}

	private void refreshTab(int position, boolean fresh) {
		currentTab = position;
		int type = tabTypes.get(position);
		RecyclerView view = ((RecyclerView) tabs.get(position));
		if (type == TAB_RATINGS) {

			Observable<List<Rating>> source;
			boolean needsFirstSync = Session.get().isLoggedIn() && Session.get().getUserRateCount() > 0 && !Db.hasSyncedRatings(this);
			if (fresh || needsFirstSync) {
				progressText.setVisibility(View.VISIBLE);
				progressText.setText(getString(R.string.main_syncprogress, 0F));
				Animations.fadeFlip(loadingProgress, listsPager);
				// Perform sync, while reporting progress, and then query the db for all ratings in the usual fashion
				source = Db.syncUserRatings(this, progress -> progressText.setText(getString(R.string.main_syncprogress, progress))).takeLast(1)
						.flatMap(last -> Db.getUserRatings(this)).toList().compose(toUi());
			} else {
				source = Db.getUserRatings(this).toList().compose(onIoToUi()).compose(bindToLifecycle());
			}
			ItemClickSupport.addTo(view).setOnItemClickListener((parent, pos, v) -> openRating(((RatingsAdapter) view.getAdapter()).get(pos)));
			source.subscribe(ratings -> view.setAdapter(new RatingsAdapter(this, ratings)), e -> {
				Animations.fadeFlip(listsPager, loadingProgress);
				Snackbar.show(this, R.string.error_connectionfailure);
				e.printStackTrace();
			}, () -> Animations.fadeFlip(listsPager, loadingProgress));

		} else {

			progressText.setVisibility(View.GONE);
			Animations.fadeFlip(loadingProgress, listsPager);
			ItemClickSupport.addTo(view).setOnItemClickListener((parent, pos, v) -> openItem(((FeedItemsAdapter) view.getAdapter()).get(pos)));
			getTabFeed(type).toList().compose(onIoToUi()).compose(bindToLifecycle())
					.subscribe(feed -> view.setAdapter(new FeedItemsAdapter(this, feed)), e -> Snackbar.show(this, R.string.error_connectionfailure),
							() -> Animations.fadeFlip(listsPager, loadingProgress));

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

	private void openRating(Rating rating) {
		if (rating != null && rating.beerId != null && rating.beerId > 0) {
			startActivity(BeerActivity.start(this, rating.beerId));
		} else if (rating != null && !rating.isUploaded()) {
			startActivity(RateActivity.start(this, rating));
		}
	}

	private void searchFromSuggestion(SearchSuggestion searchSuggestion) {
		if (searchSuggestion.beerId == null) {
			// Start a new search
			performSearch(searchSuggestion.suggestion);
		} else {
			// Directly open the searched beer
			startActivity(BeerActivity.start(this, searchSuggestion.beerId));
		}
	}

	private void performSearch(String query) {
		// Store as historic search query (or update it)
		HistoricSearch historicSearch = database(this).query(HistoricSearch.class).withSelection("name = ?", query).get();
		if (historicSearch == null) {
			historicSearch = new HistoricSearch();
			historicSearch.name = query;
		}
		historicSearch.time = new Date();
		database(this).put(historicSearch);

		// Perform live search on server
		startActivity(SearchActivity.start(this, query));
	}

	public void openHelp(View view) {
		startActivity(AboutActivity.start(this));
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
