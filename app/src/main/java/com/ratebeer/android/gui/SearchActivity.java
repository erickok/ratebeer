package com.ratebeer.android.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.widget.ProgressBar;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.gui.lists.BeerSearchResultAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.ItemClickSupport;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class SearchActivity extends RateBeerActivity {

	public static final String EXTRA_BEERID = "beerId";
	public static final String EXTRA_BEERNAME = "beerName";

	private boolean modePicker;
	private RecyclerView resultsList;
	private ProgressBar loadingProgress;

	public static Intent start(Context context, String query) {
		return start(context, query, false);
	}

	public static Intent start(Context context, String query, boolean modePicker) {
		return new Intent(context, SearchActivity.class).putExtra("query", query).putExtra("modePicker", modePicker);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		SearchView searchEdit = (SearchView) findViewById(R.id.search_text);
		resultsList = (RecyclerView) findViewById(R.id.results_list);
		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);

		setupDefaultUpButton();

		// When in picker mode, set the activity result as cancelled until we actually picked a beer
		modePicker = getIntent().getBooleanExtra("modePicker", false);
		if (modePicker)
			setResult(RESULT_CANCELED);

		// Set up search box
		searchEdit.setQuery(getIntent().getStringExtra("query"), false);
		// @formatter:off
		RxSearchView.queryTextChanges(searchEdit).compose(onUi())
				.debounce(400, TimeUnit.MILLISECONDS).filter(query -> query.length() > 3)
				.compose(toUi())
				.doOnNext(query -> Animations.fadeFlip(loadingProgress, resultsList))
				.compose(toIo())
				.switchMap(query ->
						Api.get().searchBeers(query.toString()).toList()
								.doOnError(e -> Snackbar.show(SearchActivity.this, R.string.error_connectionfailure))
								.onErrorResumeNext(Observable.empty()))
				.compose(toUi())
				.compose(bindToLifecycle())
				.subscribe(results -> {
					Animations.fadeFlip(resultsList, loadingProgress);
					resultsList.setAdapter(new BeerSearchResultAdapter(results));
				});
		// @formatter:on

		// Set up search results list
		resultsList.setLayoutManager(new LinearLayoutManager(this));
		ItemClickSupport.addTo(resultsList)
				.setOnItemClickListener((parent, position, view) -> handleResult(((BeerSearchResultAdapter) resultsList.getAdapter()).get(position)));
	}

	private void handleResult(BeerSearchResult beerSearchResult) {
		if (!modePicker) {
			startActivity(BeerActivity.start(this, beerSearchResult.beerId));
		} else {
			// Picker mode: return beer id and name as activity result extras and close this activity
			setResult(Activity.RESULT_OK, new Intent().putExtra(EXTRA_BEERID, beerSearchResult.beerId).putExtra(EXTRA_BEERNAME, beerSearchResult.beerName));
			finish();
		}
	}

}
