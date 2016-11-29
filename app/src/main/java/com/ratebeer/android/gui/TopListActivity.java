package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BeerOnTopList;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.gui.lists.BeerOnTopListAdapter;
import com.ratebeer.android.gui.lists.SimpleAutoCompleteAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.ImeUtils;
import com.ratebeer.android.gui.widget.ItemClickSupport;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public final class TopListActivity extends RateBeerActivity {

	private View filterEntry;
	private AutoCompleteTextView filterEdit;
	private ImageButton clearFilterButton;
	private RecyclerView beersList;
	private ProgressBar loadingProgress;

	@Mode
	private int mode;

	public static Intent start(Context context, @Mode int mode) {
		return new Intent(context, TopListActivity.class).putExtra("mode", mode);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toplist);

		//noinspection WrongConstant Enforced via the start() method
		mode = getIntent().getIntExtra("mode", Mode.OVERALL);

		// Set up toolbar
		Toolbar mainToolbar = setupDefaultUpButton();

		filterEntry = findViewById(R.id.filter_entry);
		filterEdit = (AutoCompleteTextView) findViewById(R.id.filter_edit);
		clearFilterButton = (ImageButton) findViewById(R.id.clear_filter_button);
		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
		beersList = (RecyclerView) findViewById(R.id.beers_list);
		beersList.setLayoutManager(new LinearLayoutManager(this));

		ItemClickSupport.addTo(beersList)
				.setOnItemClickListener((parent, pos, v) ->
						openBeer(((BeerOnTopListAdapter) beersList.getAdapter()).get(pos)));

		switch (mode) {
			case Mode.OVERALL:
				mainToolbar.setTitle(R.string.top_topoverall);
				filterEntry.setVisibility(View.GONE);
				refresh(Api.get().getTopOverall());
				break;
			case Mode.BY_COUNTRY:
				mainToolbar.setTitle(R.string.top_topbycountry);
				filterEdit.setHint(R.string.top_country);
				Db.getCountries(this)
						.compose(onIoToUi())
						.compose(bindToLifecycle())
						.subscribe((items) -> {
							setupAutoComplete(items,
									country -> refresh(Api.get().getTopByCountry(country._id)));
						}, e -> Snackbar.show(this, R.string.error_connectionfailure));
				break;
			case Mode.BY_STYLE:
				mainToolbar.setTitle(R.string.top_topbystyle);
				filterEdit.setHint(R.string.top_style);
				Db.getStyles(this)
						.compose(onIoToUi())
						.compose(bindToLifecycle())
						.subscribe((items) -> {
							setupAutoComplete(items,
									style -> refresh(Api.get().getTopByStyle(style._id)));
						}, e -> Snackbar.show(this, R.string.error_connectionfailure));
				break;
		}
		RxView.clicks(clearFilterButton)
				.subscribe(click -> filterEdit.setText(null));
	}

	private <T> void setupAutoComplete(List<T> items, Action1<T> onClick) {
		SimpleAutoCompleteAdapter<T> adapter = new SimpleAutoCompleteAdapter<>(new ContextThemeWrapper(this, R.style.AppTheme_Dark), items);
		filterEdit.setAdapter(adapter);
		RxView.focusChanges(filterEdit)
				.filter(hasFocus -> hasFocus)
				.subscribe(f -> filterEdit.showDropDown());
		RxAutoCompleteTextView.itemClickEvents(filterEdit)
				.doOnNext(click -> ImeUtils.hideIme(filterEdit))
				.map(click -> adapter.getItem(click.position()))
				.subscribe(onClick);
		RxTextView.textChanges(filterEdit)
				.map(text -> text.length() > 0)
				.subscribe(RxView.visibility(clearFilterButton));
	}

	private void refresh(Observable<BeerOnTopList> topBeers) {
		Animations.fadeFlip(loadingProgress, beersList);
		topBeers.toList()
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(
						beers -> {
							beersList.setAdapter(new BeerOnTopListAdapter(beers, mode == Mode.BY_STYLE));
							Animations.fadeFlip(beersList, loadingProgress);
						},
						e -> Snackbar.show(this, R.string.error_unexpectederror));
	}

	private void openBeer(BeerOnTopList beerOnTopList) {
		if (beerOnTopList != null)
			startActivity(BeerActivity.start(this, beerOnTopList.beerId));
	}

	@IntDef({Mode.OVERALL, Mode.BY_COUNTRY, Mode.BY_STYLE})
	@interface Mode {
		int OVERALL = 0;
		int BY_COUNTRY = 1;
		int BY_STYLE = 2;
	}

}
