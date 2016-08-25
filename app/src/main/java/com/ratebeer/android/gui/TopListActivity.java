package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.ratebeer.android.R;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BeerOnTopList;
import com.ratebeer.android.gui.lists.BeerOnTopListAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.ItemClickSupport;

import rx.Observable;

public final class TopListActivity extends RateBeerActivity {

	private AutoCompleteTextView filterEdit;
	private RecyclerView beersList;
	private ProgressBar loadingProgress;

	public static Intent start(Context context, @Mode int mode) {
		return new Intent(context, TopListActivity.class).putExtra("mode", mode);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toplist);

		@Mode int mode = getIntent().getIntExtra("mode", Mode.OVERALL);

		// Set up toolbar
		Toolbar mainToolbar = setupDefaultUpButton();

		filterEdit = (AutoCompleteTextView) findViewById(R.id.filter_edit);
		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
		beersList = (RecyclerView) findViewById(R.id.beers_list);
		beersList.setLayoutManager(new LinearLayoutManager(this));

		ItemClickSupport.addTo(beersList).setOnItemClickListener((parent, pos, v) ->
				openBeer(((BeerOnTopListAdapter) beersList.getAdapter()).get(pos)));

		switch (mode) {
			case Mode.OVERALL:
				mainToolbar.setTitle(R.string.top_topoverall);
				filterEdit.setVisibility(View.GONE);
				refresh(Api.get().getTopOverall());
				break;
			case Mode.BY_COUNTRY:
				mainToolbar.setTitle(R.string.top_topbycountry);
				break;
			case Mode.BY_STYLE:
				mainToolbar.setTitle(R.string.top_topoverall);
				break;
		}

	}

	private void refresh(Observable<BeerOnTopList> topBeers) {
		Animations.fadeFlip(loadingProgress, beersList);
		topBeers.toList().compose(onIoToUi()).compose(bindToLifecycle()).subscribe(beers -> beersList.setAdapter(new BeerOnTopListAdapter(beers)),
				e -> Snackbar.show(this, R.string.error_unexpectederror), () -> Animations.fadeFlip(beersList, loadingProgress));
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
