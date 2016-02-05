package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.ImageUrls;
import com.ratebeer.android.api.model.BeerRating;
import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.gui.lists.BeerRatingsAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import rx.Observable;

public final class BeerActivity extends RateBeerActivity {

	public static Intent start(Context context, long beerId) {
		return new Intent(context, BeerActivity.class).putExtra("beerId", beerId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beer);

		// Set up toolbar
		Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		mainToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		RxToolbar.navigationClicks(mainToolbar).subscribe(ignore -> onBackPressed());
		FloatingActionButton rateButton = (FloatingActionButton) findViewById(R.id.rate_button);

		// Load beer and ratings from database or live
		long beerId = getIntent().getLongExtra("beerId", 0);
		Db.getBeer(this, beerId).compose(onIoToUi()).compose(bindToLifecycle())
				.subscribe(this::showBeer, e -> Snackbar.show(this, R.string.error_connectionfailure));

		Observable<BeerRating> ratings = Api.get().getBeerRatings(beerId);
		if (Session.get().isLoggedIn())
			ratings = ratings.startWith(Db.getRating(this, beerId, Session.get().getUserId()).map(this::localToBeerRating));
		else
			rateButton.setVisibility(View.GONE);
		ratings.toList().compose(onIoToUi()).compose(bindToLifecycle()).subscribe(this::showRatings, e -> {});
	}

	private void showBeer(Beer beer) {
		Picasso.with(this).load(ImageUrls.getBeerPhotoHighResUrl(beer._id)).fit().centerCrop().placeholder(R.color.grey_light).into((ImageView) findViewById(R.id.backdrop_image));
		((TextView) findViewById(R.id.beer_name_text)).setText(beer.name);
		((TextView) findViewById(R.id.brewer_name_text)).setText(getString(R.string.beer_stylebrewer, beer.styleName, beer.brewerName));
		((TextView) findViewById(R.id.mark_overall_text)).setText(beer.getOverallPercentileString());
		((TextView) findViewById(R.id.mark_style_text)).setText(beer.getStylePercentileString());
		((TextView) findViewById(R.id.mark_count_text)).setText(beer.getRateCountString());
		((TextView) findViewById(R.id.mark_abv_text)).setText(beer.getAlcoholString());
		((TextView) findViewById(R.id.mark_ibu_text)).setText(beer.getIbuString());
		((TextView) findViewById(R.id.mark_calories_text)).setText(beer.getCaloriesString());
	}

	private void showRatings(List<BeerRating> ratings) {
		RecyclerView ratingsList = (RecyclerView) findViewById(R.id.ratings_list);
		ratingsList.setLayoutManager(new LinearLayoutManager(this));
		ratingsList.setAdapter(new BeerRatingsAdapter(ratings));
	}

	private BeerRating localToBeerRating(Rating rating) {
		// Mimic a recent rating object to show in the latest beer ratings list
		BeerRating beerRating = new BeerRating();
		// Take the stored rating of the logged in user
		beerRating.ratingId = rating._id.intValue();
		beerRating.aroma = rating.aroma;
		beerRating.flavor = rating.flavor;
		beerRating.appearance = rating.appearance;
		beerRating.mouthfeel = rating.mouthfeel;
		beerRating.overall = rating.overall;
		beerRating.total = rating.total;
		beerRating.comments = rating.comments;
		// Combine with the up-to-date user details from the session
		beerRating.userId = Session.get().getUserId();
		beerRating.userName = Session.get().getUserName();
		beerRating.userRateCount = Session.get().getUserRateCount();
		return beerRating;
	}

}
