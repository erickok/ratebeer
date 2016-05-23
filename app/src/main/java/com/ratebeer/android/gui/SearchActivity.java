package com.ratebeer.android.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.api.model.BrewerySearchResult;
import com.ratebeer.android.api.model.PlaceSearchResult;
import com.ratebeer.android.gui.lists.BeerSearchResultAdapter;
import com.ratebeer.android.gui.lists.BrewerySearchResultAdapter;
import com.ratebeer.android.gui.lists.PlaceSearchResultAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.ItemClickSupport;
import com.ratebeer.android.rx.RxViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class SearchActivity extends RateBeerActivity {

	public static final String EXTRA_BEERID = "beerId";
	public static final String EXTRA_BEERNAME = "beerName";

	private static final int TAB_BEERS = 0;
	private static final int TAB_BREWERIES = 1;
	private static final int TAB_PLACES = 2;

	private boolean modeBeerPicker;
	private SearchView searchEdit;
	private ViewPager resultsPager;
	private ProgressBar loadingProgress;
	private TextView emptyText;
	private List<Integer> tabTypes;
	private List<View> tabs;
	private List<String> tabsTitles;

	public static Intent start(Context context, String query) {
		return start(context, query, false);
	}

	public static Intent start(Context context, String query, boolean modePicker) {
		return new Intent(context, SearchActivity.class).putExtra("query", query).putExtra("modeBeerPicker", modePicker);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		searchEdit = (SearchView) findViewById(R.id.search_text);
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		resultsPager = (ViewPager) findViewById(R.id.results_pager);
		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
		emptyText = (TextView) findViewById(R.id.empty_text);

		setupDefaultUpButton();

		// When in picker mode, only search beers and set the activity result as cancelled until we actually picked a beer
		modeBeerPicker = getIntent().getBooleanExtra("modeBeerPicker", false);
		if (modeBeerPicker)
			setResult(RESULT_CANCELED);

		// Set up tabs
		searchEdit.setQuery(getIntent().getStringExtra("query"), false);
		tabTypes = new ArrayList<>(3);
		tabs = new ArrayList<>(3);
		tabsTitles = new ArrayList<>(3);
		addTab(TAB_BEERS, R.string.search_beers);
		if (!modeBeerPicker) {
			addTab(TAB_BREWERIES, R.string.search_breweries);
			addTab(TAB_PLACES, R.string.search_places);
		}
		RxViewPager.pageSelected(resultsPager).subscribe(this::refreshTab);
		resultsPager.setAdapter(new SearchPagerAdapter());
		tabLayout.setupWithViewPager(resultsPager);
		if (tabs.size() == 1)
			tabLayout.setVisibility(View.GONE);
		refreshTab(0);

	}

	private void addTab(int tabType, int title) {
		RecyclerView recyclerView = (RecyclerView) getLayoutInflater().inflate(R.layout.view_feedlist, resultsPager, false);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		tabTypes.add(tabType);
		tabs.add(recyclerView);
		tabsTitles.add(getString(title));
	}

	private void refreshTab(int position) {

		int type = tabTypes.get(position);
		RecyclerView view = ((RecyclerView) tabs.get(position));
		Animations.fadeFlipOut(loadingProgress, view, emptyText);

		Observable<CharSequence> debouncedQueries = RxSearchView.queryTextChanges(searchEdit).compose(onUi()).debounce(666, TimeUnit.MILLISECONDS)
				.filter(query -> query.length() > 3).compose(toIo());
		if (type == TAB_BEERS) {

			ItemClickSupport.addTo(view).setOnItemClickListener((parent, pos, v) -> handleBeerResult(((BeerSearchResultAdapter) view.getAdapter())
					.get(pos)));
			debouncedQueries.switchMap(query -> Api.get().searchBeers(query.toString()).toList().compose(toUi()).doOnError(e -> Snackbar.show
					(SearchActivity.this, R.string.error_connectionfailure)).onErrorResumeNext(Observable.empty())).compose(bindToLifecycle())
					.subscribe(results -> {

				view.setAdapter(new BeerSearchResultAdapter(results));
				if (results.isEmpty())
					Animations.fadeFlipOut(emptyText, view, loadingProgress);
				else
					Animations.fadeFlipOut(view, emptyText, loadingProgress);
			});

		} else if (type == TAB_BREWERIES) {

			ItemClickSupport.addTo(view).setOnItemClickListener((parent, pos, v) -> handleBreweryResult(((BrewerySearchResultAdapter) view
					.getAdapter()).get(pos)));
			debouncedQueries.switchMap(query -> Api.get().searchBreweries(query.toString()).toList().compose(toUi()).doOnError(e -> Snackbar.show
					(SearchActivity.this, R.string.error_connectionfailure)).onErrorResumeNext(Observable.empty())).compose(bindToLifecycle())
					.subscribe(results -> {

				view.setAdapter(new BrewerySearchResultAdapter(results));
				if (results.isEmpty())
					Animations.fadeFlipOut(emptyText, view, loadingProgress);
				else
					Animations.fadeFlipOut(view, emptyText, loadingProgress);
			});

		} else if (type == TAB_PLACES) {

			ItemClickSupport.addTo(view).setOnItemClickListener((parent, pos, v) -> handlePlaceResult(((PlaceSearchResultAdapter) view.getAdapter())
					.get(pos)));
			debouncedQueries.switchMap(query -> Api.get().searchPlaces(query.toString()).toList().compose(toUi()).doOnError(e -> Snackbar.show
					(SearchActivity.this, R.string.error_connectionfailure)).onErrorResumeNext(Observable.empty())).compose(bindToLifecycle())
					.subscribe(results -> {

				view.setAdapter(new PlaceSearchResultAdapter(results));
				if (results.isEmpty())
					Animations.fadeFlipOut(emptyText, view, loadingProgress);
				else
					Animations.fadeFlipOut(view, emptyText, loadingProgress);
			});

		}

	}

	private void handleBeerResult(BeerSearchResult beerSearchResult) {
		if (!modeBeerPicker) {
			startActivity(BeerActivity.start(this, beerSearchResult.beerId));
		} else {
			// Picker mode: return beer id and name as activity result extras and close this activity
			setResult(Activity.RESULT_OK, new Intent().putExtra(EXTRA_BEERID, beerSearchResult.beerId).putExtra(EXTRA_BEERNAME, beerSearchResult
					.beerName));
			finish();
		}
	}

	private void handleBreweryResult(BrewerySearchResult brewerySearchResult) {
		startActivity(BreweryActivity.start(this, brewerySearchResult.brewerId));
	}

	private void handlePlaceResult(PlaceSearchResult placeSearchResult) {
		startActivity(PlaceActivity.start(this, placeSearchResult.placeId));
	}

	private class SearchPagerAdapter extends PagerAdapter {

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
