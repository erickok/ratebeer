package com.ratebeer.android.gui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.jakewharton.rxbinding.support.v7.widget.SearchViewQueryTextEvent;
import com.jakewharton.rxbinding.view.RxView;
import com.pacoworks.rxtuples.RxTuples;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.FeedItem;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.HistoricSearch;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.db.views.CustomListWithCount;
import com.ratebeer.android.gui.lists.BarcodeSearchResultsAdapter;
import com.ratebeer.android.gui.lists.CustomListsAdapter;
import com.ratebeer.android.gui.lists.FeedItemsAdapter;
import com.ratebeer.android.gui.lists.LocalPlace;
import com.ratebeer.android.gui.lists.LocalPlacesAdapter;
import com.ratebeer.android.gui.lists.PropertiesAdapter;
import com.ratebeer.android.gui.lists.Property;
import com.ratebeer.android.gui.lists.RatingsAdapter;
import com.ratebeer.android.gui.lists.SearchSuggestion;
import com.ratebeer.android.gui.lists.SearchSuggestionsAdapter;
import com.ratebeer.android.gui.services.SyncService;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.ItemClickSupport;
import com.ratebeer.android.rx.RxLocation;
import com.ratebeer.android.rx.RxViewPager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.ratebeer.android.db.CupboardDbHelper.database;

public class MainActivity extends RateBeerActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

	private static final int TAB_RATINGS = 0;
	private static final int TAB_FEED_FRIENDS = 1;
	private static final int TAB_FEED_LOCAL = 2;
	private static final int TAB_FEED_GLOBAL = 3;
	private static final int TAB_NEARBY = 4;
	private static final int TAB_MY_LISTS = 5;
	private static final int TAB_TOP50 = 6;

	private static final int REQUEST_LOCATION_PERMISSION = 1;

	private SearchView searchEdit;
	private ViewPager listsPager;
	private View loadingProgress;
	private TextView statusText;
	private TextView emptyText;
	private FloatingActionButton rateButton;
	private FloatingActionButton listAddButton;
	private SearchSuggestionsAdapter searchSuggestionsAdaper;
	private List<Integer> tabTypes;
	private List<View> tabs;
	private List<String> tabsTitles;
	private int tabSelected = 0;
	private Subscription syncSubscription;
	private boolean freshStart = true;
	private boolean preventLocationPermissionRequest = false;

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

		View scanButton = findViewById(R.id.scan_button);
		View helpButton = findViewById(R.id.help_button);
		searchEdit = (SearchView) findViewById(R.id.search_edit);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		listsPager = (ViewPager) findViewById(R.id.lists_pager);
		loadingProgress = findViewById(R.id.loading_progress);
		statusText = (TextView) findViewById(R.id.status_text);
		emptyText = (TextView) findViewById(R.id.empty_text);
		RecyclerView searchList = (RecyclerView) findViewById(R.id.search_list);
		rateButton = (FloatingActionButton) findViewById(R.id.rate_button);
		listAddButton = (FloatingActionButton) findViewById(R.id.list_add_button);

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
		addTab(TAB_NEARBY, R.string.main_nearby);
		addTab(TAB_MY_LISTS, R.string.main_mylists);
		addTab(TAB_TOP50, R.string.main_top50);
		RxViewPager.pageSelected(listsPager).subscribe(this::refreshTab);
		listsPager.setAdapter(new ActivityPagerAdapter());
		tabLayout.setupWithViewPager(listsPager);
		if (tabs.size() == 1) {
			tabLayout.setVisibility(View.GONE);
		} else if (tabs.size() > 4) {
			tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
		}

		// Set up access buttons
		RxView.clicks(scanButton)
				.compose(bindToLifecycle())
				.subscribe(v -> new BarcodeIntentIntegrator(this).initiateScan());
		RxView.clicks(helpButton)
				.compose(bindToLifecycle())
				.subscribe(v -> startActivity(HelpActivity.start(this)));
		RxView.clicks(statusText)
				.compose(bindToLifecycle())
				.subscribe(v -> startService(SyncService.start(this)));

		// Set up search box: show results with search view focus, start search on query submit and show suggestions on query text changes
		searchList.setLayoutManager(new LinearLayoutManager(this));
		searchList.setAdapter(searchSuggestionsAdaper = new SearchSuggestionsAdapter());
		ItemClickSupport.addTo(searchList)
				.setOnItemClickListener((parent, pos, v) -> searchFromSuggestion(searchSuggestionsAdaper.get(pos)));
		Observable<SearchViewQueryTextEvent> queryTextChangeEvents = RxSearchView.queryTextChangeEvents(searchEdit)
				.compose(onUi())
				.replay(1)
				.refCount();
		queryTextChangeEvents.map(event -> !TextUtils.isEmpty(event.queryText())).subscribe(hasQuery -> {
			searchList.setVisibility(hasQuery ? View.VISIBLE : View.GONE);
			tabLayout.setVisibility(hasQuery ? View.GONE : (tabs.size() == 1 ? View.GONE : View.VISIBLE));
			scanButton.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
		});
		queryTextChangeEvents
				.filter(SearchViewQueryTextEvent::isSubmitted)
				.subscribe(event -> performSearch(event.queryText().toString()));
		queryTextChangeEvents
				.map(event -> event.queryText().toString())
				.map(String::trim)
				.switchMap(query -> {
					if (query.length() == 0) {
						return Db.getAllHistoricSearches(this).toList();
					} else {
						return Db.getSuggestions(this, query).toList();
					}
				})
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(suggestions -> searchSuggestionsAdaper.update(suggestions));
		searchEdit.setInputType(InputType.TYPE_CLASS_TEXT);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (freshStart || tabTypes.get(tabSelected) == TAB_RATINGS || tabTypes.get(tabSelected) == TAB_MY_LISTS)
			refreshTab(tabSelected);
		freshStart = false;
		preventLocationPermissionRequest = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		BarcodeIntentResult scanResult = BarcodeIntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null && scanResult.getContents() != null) {
			statusText.setVisibility(View.GONE);
			Animations.fadeFlip(loadingProgress, listsPager);
			Api.get().searchByBarcode(scanResult.getContents())
					.toList()
					.compose(onIoToUi())
					.compose(bindToLifecycle())
					.subscribe(results -> {
								Animations.fadeFlip(listsPager, loadingProgress);
								if (results.size() == 0) {
									Snackbar.show(this, R.string.search_noresults);
								} else if (results.size() == 1) {
									// Exact match; directly open the beer
									startActivity(BeerActivity.start(this, results.get(0).beerId));
								} else {
									// Multiple matches; show selection dialog
									new AlertDialog.Builder(this)
											.setAdapter(new BarcodeSearchResultsAdapter(this, results), (dialogInterface, i) ->
													startActivity(BeerActivity.start(this, results.get(i).beerId))).show();
								}
							},
							e -> Snackbar.show(this, R.string.error_connectionfailure));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_LOCATION_PERMISSION) {
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				// Request for location permission granted; find the places tab
				int placesTab = -1;
				for (int i = 0; i < tabTypes.size(); i++) {
					if (tabTypes.get(i) == TAB_NEARBY) {
						placesTab = i;
						break;
					}
				}
				if (placesTab == tabSelected) {
					// Showing the places tab now: refresh
					refreshTab(placesTab);
				} else if (placesTab >= 0) {
					// Not showing the places tab: scroll to it (which enforces a data refresh)
					listsPager.setCurrentItem(placesTab);
				}

			} else {

				// Permission request denied: show user that we need this for the nearby places feature
				Snackbar.show(this, R.string.error_locationpermissionrequired);
				preventLocationPermissionRequest = true;

			}
		}
	}

	private void addTab(int tabType, int title) {
		RecyclerView recyclerView = (RecyclerView) getLayoutInflater().inflate(R.layout.view_feedlist, listsPager, false);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		tabTypes.add(tabType);
		tabs.add(recyclerView);
		tabsTitles.add(getString(title));
	}

	private void refreshTab(int position) {
		tabSelected = position;
		int type = tabTypes.get(position);
		RecyclerView view = ((RecyclerView) tabs.get(position));
		if (type == TAB_RATINGS) {

			// Show ratings sync progress, or show the already stored ratings
			emptyText.setVisibility(View.GONE);
			boolean needsFirstSync = Session.get().isLoggedIn() && Session.get().getUserRateCount() > 0 && !Db.hasSyncedRatings(this);
			syncSubscription = SyncService.getSyncStatus()
					.distinctUntilChanged()
					.compose(toUi())
					.flatMap(inSync -> {
						if (inSync) {
							// Emit null to indicate we are in sync
							return Observable.just(null);
						} else {
							// Emit a list of all stored ratings
							return Db.getRatings(this)
									.compose(onIoToUi())
									.subscribeOn(Schedulers.io())
									.toList()
									.doOnError(e -> {
										Snackbar.show(this, R.string.error_connectionfailure);
										e.printStackTrace();
									})
									.onErrorResumeNext(Observable.just(new ArrayList<>()));
						}
					})
					.compose(toUi())
					.compose(bindToLifecycle())
					.subscribe(ratings -> {
						if (ratings == null) {
							// Syncing: show progress spinner
							if (loadingProgress.getVisibility() != View.VISIBLE)
								Animations.fadeFlipOut(loadingProgress, listsPager, statusText);
						} else if (needsFirstSync) {
							// Never synced yet (but user has ratings): show sync invitation text
							Animations.fadeFlipOut(statusText, listsPager, loadingProgress);
						} else {
							if (listsPager.getVisibility() != View.VISIBLE)
								Animations.fadeFlipOut(listsPager, loadingProgress, statusText);
							if (view.getAdapter() == null)
								view.setAdapter(new RatingsAdapter(this, getMenuInflater(), ratings));
							else
								((RatingsAdapter) view.getAdapter()).update(ratings);
							if (ratings.isEmpty()) {
								emptyText.setVisibility(View.VISIBLE);
								emptyText.setText(R.string.error_noplaces);
							}
						}
					});
			ItemClickSupport.addTo(view)
					.setOnItemClickListener((parent, pos, v) -> openRating(((RatingsAdapter) view.getAdapter()).get(pos)));
			rateButton.show();
			listAddButton.hide();

		} else if (type == TAB_NEARBY) {

			if (syncSubscription != null)
				syncSubscription.unsubscribe();
			statusText.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			rateButton.hide();
			listAddButton.hide();

			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// No permission yet to use the user location
				emptyText.setVisibility(View.VISIBLE);
				emptyText.setText(R.string.error_nolocationpermission);
				if (!preventLocationPermissionRequest)
					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
				return;
			}

			// Show (fresh and cached) nearby places as list
			Animations.fadeFlip(loadingProgress, listsPager);
			ItemClickSupport.addTo(view)
					.setOnItemClickListener((parent, pos, v) -> openLocalPlace(((LocalPlacesAdapter) view.getAdapter()).get(pos)));
			Observable<Location> lastOrQuickLocation = new RxLocation(this).getLastOrQuickLocation().compose(onUi());
			Observable.combineLatest(
					lastOrQuickLocation,
					lastOrQuickLocation
							.compose(toIo())
							.flatMap(showLocation -> Db.getPlacesNearby(this, showLocation)),
					RxTuples.toPair())
					.map(place -> LocalPlace.from(place.getValue1(), place.getValue0()))
					.toSortedList()
					.compose(toUi())
					.compose(bindToLifecycle())
					.subscribe(places -> {
						if (view.getAdapter() == null)
							view.setAdapter(new LocalPlacesAdapter(this, places));
						else
							((LocalPlacesAdapter) view.getAdapter()).update(places);
						if (places.isEmpty()) {
							emptyText.setVisibility(View.VISIBLE);
							emptyText.setText(R.string.error_noplaces);
						}
					}, e -> {
						Animations.fadeFlip(listsPager, loadingProgress);
						Snackbar.show(this, R.string.error_connectionfailure);
					}, () -> Animations.fadeFlip(listsPager, loadingProgress));

		} else if (type == TAB_MY_LISTS) {

			if (syncSubscription != null)
				syncSubscription.unsubscribe();
			statusText.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			rateButton.hide();
			listAddButton.show();

			// Show the custom, local user lists
			Animations.fadeFlip(loadingProgress, listsPager);
			ItemClickSupport.addTo(view)
					.setOnItemClickListener((parent, pos, v) -> openCustomList(((CustomListsAdapter) view.getAdapter()).get(pos)));
			Db.getCustomListsWithCount(this)
					.toSortedList()
					.compose(onIoToUi())
					.compose(bindToLifecycle())
					.subscribe(customLists -> {
						if (view.getAdapter() == null)
							view.setAdapter(new CustomListsAdapter(customLists));
						else
							((CustomListsAdapter) view.getAdapter()).update(customLists);
						if (customLists.isEmpty()) {
							emptyText.setVisibility(View.VISIBLE);
							emptyText.setText(R.string.error_nolists);
						}
					}, e -> {
						Animations.fadeFlip(listsPager, loadingProgress);
						Snackbar.show(this, R.string.error_connectionfailure);
					}, () -> Animations.fadeFlip(listsPager, loadingProgress));

		} else if (type == TAB_TOP50) {

			if (syncSubscription != null)
				syncSubscription.unsubscribe();
			statusText.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			loadingProgress.setVisibility(View.GONE);
			ItemClickSupport.removeFrom(view);
			rateButton.hide();
			listAddButton.hide();

			// Show the links to overall and style top 50s
			List<Property> listLinks = new ArrayList<>();
			listLinks.add(new Property(R.drawable.ic_prop_top, getString(R.string.top_overall),
					v -> startActivity(TopListActivity.start(this, TopListActivity.Mode.OVERALL))));
			listLinks.add(new Property(R.drawable.ic_prop_country, getString(R.string.top_bycountry),
					v -> startActivity(TopListActivity.start(this, TopListActivity.Mode.BY_COUNTRY))));
			listLinks.add(new Property(R.drawable.ic_prop_style, getString(R.string.top_bystyle),
					v -> startActivity(TopListActivity.start(this, TopListActivity.Mode.BY_STYLE))));
			view.setAdapter(new PropertiesAdapter(listLinks));
			listsPager.setVisibility(View.VISIBLE);

		} else {

			if (syncSubscription != null)
				syncSubscription.unsubscribe();
			statusText.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			rateButton.hide();
			listAddButton.hide();

			// Load a feed (global, local or friends)
			Animations.fadeFlip(loadingProgress, listsPager);
			ItemClickSupport.addTo(view)
					.setOnItemClickListener((parent, pos, v) -> openFeedItem(((FeedItemsAdapter) view.getAdapter()).get(pos)));
			getTabFeed(type)
					.toList()
					.compose(onIoToUi())
					.compose(bindToLifecycle())
					.subscribe(feed -> {
						if (view.getAdapter() == null)
							view.setAdapter(new FeedItemsAdapter(this, feed));
						else
							((FeedItemsAdapter) view.getAdapter()).update(feed);
						if (feed.isEmpty()) {
							emptyText.setVisibility(View.VISIBLE);
							emptyText.setText(R.string.error_noplaces);
						}
					}, e -> {
						Animations.fadeFlip(listsPager, loadingProgress);
						Snackbar.show(this, R.string.error_connectionfailure);
					}, () -> Animations.fadeFlip(listsPager, loadingProgress));

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

	private void openFeedItem(FeedItem feedItem) {
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

	private void openLocalPlace(LocalPlace place) {
		if (place != null)
			startActivity(PlaceActivity.start(this, place.place._id));
	}

	private void openCustomList(CustomListWithCount customList) {
		startActivity(CustomListActivity.start(this, customList._id));
	}

	@Override
	public void onBackPressed() {
		if (!TextUtils.isEmpty(searchEdit.getQuery())) {
			// Do not navigate back (closing the activity) but clear the search results
			searchEdit.setQuery(null, false);
			return;
		}
		super.onBackPressed();
	}

	private void searchFromSuggestion(SearchSuggestion searchSuggestion) {
		if ((searchSuggestion.type == SearchSuggestion.TYPE_BEER || searchSuggestion.type == SearchSuggestion.TYPE_RATING)
				&& searchSuggestion.itemId != null) {
			// Directly open the searched beer
			startActivity(BeerActivity.start(this, searchSuggestion.itemId));
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_BREWERY && searchSuggestion.itemId != null) {
			// Directly open the searched brewery
			startActivity(BreweryActivity.start(this, searchSuggestion.itemId));
		} else if (searchSuggestion.type == SearchSuggestion.TYPE_PLACE && searchSuggestion.itemId != null) {
			// Directly open the searched place
			startActivity(PlaceActivity.start(this, searchSuggestion.itemId));
		} else {
			// Start a new search
			performSearch(searchSuggestion.suggestion);
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

	public void startOfflineRating(View view) {
		startActivity(RateActivity.start(this));
	}

	public void startCustomList(View view) {
		startActivity(CustomListActivity.start(this));
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
