package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BeerSearchResult;
import com.ratebeer.android.gui.lists.BeerSearchResultAdapter;
import com.ratebeer.android.gui.widget.ItemClickSupport;

import java.util.concurrent.TimeUnit;

import rx.Observable;

public class SearchActivity extends RateBeerActivity {

	private SearchView searchEdit;
	private RecyclerView resultsList;

	public static Intent start(Context context, String query) {
		return new Intent(context, SearchActivity.class).putExtra("query", query);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		searchEdit = (SearchView) findViewById(R.id.search_text);
		resultsList = (RecyclerView) findViewById(R.id.results_list);

		// Set up search box
		searchEdit.setQuery(getIntent().getStringExtra("query"), false);
		// @formatter:off
		RxSearchView.queryTextChanges(searchEdit)
				.compose(onUi())
				.debounce(400, TimeUnit.MILLISECONDS)
				.filter(query -> query.length() > 3)
				.flatMap(query ->
						Api.get().searchBeers(query.toString()).toList()
								.doOnError(e -> Snackbar.show(SearchActivity.this, R.string.error_connectionfailure))
								.onErrorResumeNext(Observable.empty()))
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(results -> resultsList.setAdapter(new BeerSearchResultAdapter(results)));
		// @formatter:on

		// Set up search results list
		resultsList.setLayoutManager(new LinearLayoutManager(this));
		ItemClickSupport.addTo(resultsList)
				.setOnItemClickListener((parent, position, view) -> openResult(((BeerSearchResultAdapter) resultsList.getAdapter()).get(position)));
	}

	private void openResult(BeerSearchResult beerSearchResult) {
		startActivity(BeerActivity.start(this, beerSearchResult.beerId));
	}

}
