package com.ratebeer.android.gui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BeerRating;
import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.CustomList;
import com.ratebeer.android.db.CustomListBeer;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.gui.lists.BeerRatingsAdapter;
import com.ratebeer.android.gui.lists.CustomListsPopupAdapter;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.CheckableImageButton;
import com.ratebeer.android.gui.widget.Images;
import com.ratebeer.android.gui.widget.ImeUtils;
import com.ratebeer.android.gui.widget.ItemClickSupport;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.database;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class BeerActivity extends RateBeerActivity {

	private RecyclerView ratingsList;
	private ProgressBar loadingProgress;
	private View detailsLayout;
	private View numbersLayout;
	private View moreLayout;
	private CheckableImageButton listAddButton;
	private TextView brewerNameText;
	private FloatingActionButton rateButton;
	private PopupWindow addListPopup;

	private long beerId;

	public static Intent start(Context context, long beerId) {
		return new Intent(context, BeerActivity.class).putExtra("beerId", beerId);
	}

	@SuppressLint("PrivateResource")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beer);

		// Set up toolbar
		Toolbar mainToolbar = setupDefaultUpButton();
		mainToolbar.inflateMenu(R.menu.menu_refresh);
		RxToolbar.itemClicks(mainToolbar).filter(item -> item.getItemId() == R.id.menu_refresh).subscribe(item -> {
			Animations.fadeFlip(loadingProgress, detailsLayout);
			refresh(true);
		});

		ratingsList = (RecyclerView) findViewById(R.id.ratings_list);
		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
		detailsLayout = findViewById(R.id.details_layout);
		numbersLayout = findViewById(R.id.numbers_layout);
		listAddButton = (CheckableImageButton) findViewById(R.id.list_add_button);
		moreLayout = findViewById(R.id.more_layout);
		brewerNameText = (TextView) findViewById(R.id.brewer_name_text);
		rateButton = (FloatingActionButton) findViewById(R.id.rate_button);
		rateButton.setVisibility(View.GONE);

		beerId = getIntent().getLongExtra("beerId", 0);
		if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null) {
			List<String> segments = getIntent().getData().getPathSegments();
			if (segments != null && segments.size() > 1) {
				try {
					beerId = Integer.parseInt(segments.get(1));
				} catch (NumberFormatException e) {
					// Not a supported URL; start the url in a browser instead (via missing beerId)
				}
			}
		}
		if (beerId == 0) {
			startActivity(new Intent(Intent.ACTION_VIEW, getIntent().getData()));
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh(false);
	}

	private void refresh(boolean forceFresh) {

		// Load beer from database or live, with a fallback on the bare beer name taken from an offline rating
		Db.getBeer(this, beerId, forceFresh)
				.onErrorResumeNext(Db.getOfflineRatingForBeer(this, beerId)
						.map(this::ratingToBeer))
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(this::showBeer, e -> Snackbar.show(this, R.string.error_connectionfailure));

		// Load beer ratings (always live) and the user's rating, if any exists in the database or live
		Observable<BeerRating> ratings = Api.get().getBeerRatings(beerId)
				.filter(beerRating -> !Session.get().isLoggedIn() || beerRating.userId != Session.get().getUserId());
		if (Session.get().isLoggedIn())
			ratings = ratings.onErrorResumeNext(Observable.empty())
					.startWith(Db.getRating(this, beerId, Session.get().getUserId())
							.map(this::localToBeerRating));
		ratings.toList()
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(this::showRatings, Throwable::printStackTrace);

	}

	@TargetApi(Build.VERSION_CODES.M)
	private void showBeer(Beer beer) {

		ImageView photoImage = (ImageView) findViewById(R.id.backdrop_image);
		Images.with(this).loadBeer(beer._id, true).fit().centerCrop().noPlaceholder().into(photoImage);

		if (!TextUtils.isEmpty(beer.styleName)) {
			String brewerStyleText = getString(R.string.beer_stylebrewer, beer.styleName, beer.brewerName);
			SpannableStringBuilder brewerStyleMarkup = new SpannableStringBuilder(brewerStyleText);
			int styleStart = brewerStyleText.indexOf(beer.styleName);
			int styleEnd = styleStart + beer.styleName.length();
			int brewerStart = brewerStyleText.indexOf(beer.brewerName);
			int brewerEnd = brewerStart + beer.brewerName.length();
			brewerStyleMarkup.setSpan(new StyleSpan(Typeface.BOLD), styleStart, styleEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			brewerStyleMarkup.setSpan(new StyleSpan(Typeface.BOLD), brewerStart, brewerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			brewerNameText.setText(brewerStyleMarkup);
		} else {
			String brewerText = getString(R.string.beer_brewer, beer.brewerName);
			SpannableStringBuilder brewerMarkup = new SpannableStringBuilder(brewerText);
			int brewerStart = brewerText.indexOf(beer.brewerName);
			int brewerEnd = brewerStart + beer.brewerName.length();
			brewerMarkup.setSpan(new StyleSpan(Typeface.BOLD), brewerStart, brewerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			brewerNameText.setText(brewerMarkup);
		}
		brewerNameText.setOnClickListener(v -> startActivity(BreweryActivity.start(this, beer.brewerId)));

		((TextView) findViewById(R.id.beer_name_text)).setText(beer.name);

		if (beer.isAlias()) {
			numbersLayout.setVisibility(View.GONE);
			findViewById(R.id.alias_layout).setVisibility(View.VISIBLE);
		} else if (beer.overallPercentile == null && beer.stylePercentile == null && beer.rateCount == 0 && beer.alcohol == null) {
			// No additional beer numbers available at all: hide the numbers bar
			numbersLayout.setVisibility(View.GONE);
		} else {
			numbersLayout.setVisibility(View.VISIBLE);
			TextView markOverallText = (TextView) findViewById(R.id.mark_overall_text);
			markOverallText.setText(beer.getOverallPercentileString());
			((TextView) findViewById(R.id.mark_style_text)).setText(beer.getStylePercentileString());
			((TextView) findViewById(R.id.mark_count_text)).setText(beer.getRateCountString());
			((TextView) findViewById(R.id.mark_abv_text)).setText(beer.getAlcoholString());
			((TextView) findViewById(R.id.mark_ibu_text)).setText(beer.getIbuString());

			((TextView) findViewById(R.id.mark_avg_real_text)).setText(beer.getRealRatingString());
			((TextView) findViewById(R.id.mark_avg_weighted_text)).setText(beer.getWeightedRatingString());
			((TextView) findViewById(R.id.mark_calories_text)).setText(beer.getCaloriesString());
			TextView descriptionText = (TextView) findViewById(R.id.description_text);
			descriptionText.setText(beer.description);
			descriptionText.setVisibility(TextUtils.isEmpty(beer.description) ? View.GONE : View.VISIBLE);
			ImageView expandCollapseImage = (ImageView) findViewById(R.id.expand_collapse_image);
			numbersLayout.setOnClickListener(v -> {
				if (expandCollapseImage.isActivated()) {
					expandCollapseImage.setActivated(false);
					Animations.collapseToTop(moreLayout);
				} else {
					expandCollapseImage.setActivated(true);
					Animations.expandFromTop(moreLayout);
				}
			});

			// Load lists when tapping the add to list button
			RxView.clicks(listAddButton)
					.compose(onUi())
					.switchMap(click -> Db.getCustomLists(this, beerId).toList())
					.compose(toIo())
					.compose(toUi())
					.compose(bindToLifecycle())
					.subscribe(lists -> {

						// Prepare popup view
						ViewGroup content = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_select_list, null);
						addListPopup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
						RecyclerView listsList = (RecyclerView) content.findViewById(R.id.lists_list);
						EditText createNameEdit = (EditText) content.findViewById(R.id.create_name_edit);
						ImageButton createAddButton = (ImageButton) content.findViewById(R.id.create_add_button);
						listsList.setLayoutManager(new LinearLayoutManager(this));
						listsList.setAdapter(new CustomListsPopupAdapter(lists));

						// Handle new list creation and existing list clicks
						RxTextView.textChanges(createNameEdit)
								.map(name -> !TextUtils.isEmpty(name))
								.compose(RxLifecycleAndroid.bindView(createNameEdit))
								.subscribe(
										createAddButton::setEnabled,
										e -> Snackbar.show(this, R.string.error_unexpectederror));
						RxView.clicks(createAddButton)
								.compose(RxLifecycleAndroid.bindView(createAddButton))
								.subscribe(
										name -> addBeerToNewCustomList(beer, createNameEdit.getText().toString(), addListPopup),
										e -> Snackbar.show(this, R.string.error_unexpectederror));
						ItemClickSupport.addTo(listsList)
								.setOnItemClickListener((recyclerView, position, v) ->
										addBeerToCustomList(beer, ((CustomListsPopupAdapter) recyclerView.getAdapter()).get(position)._id,
												addListPopup));

						/// Show the lists as popup (in the top corner so there is place for the software keyboard)
						//noinspection deprecation Hack to have the background transparent
						addListPopup.setBackgroundDrawable(new BitmapDrawable());
						addListPopup.setOutsideTouchable(true);
						int popupMargin = (int) getResources().getDimension(R.dimen.static_popup_margin);
						addListPopup.showAtLocation(listAddButton, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, popupMargin);
						createNameEdit.requestFocus();
						Observable.timer(100, TimeUnit.MILLISECONDS).subscribe(event -> ImeUtils.showIme(createNameEdit));

					}, e -> Snackbar.show(this, R.string.error_unexpectederror));
		}

		Animations.fadeFlip(detailsLayout, loadingProgress);
		rateButton.setVisibility(Session.get().isLoggedIn() ? View.VISIBLE : View.GONE);
		RxView.clicks(rateButton).subscribe(clicked -> startActivity(RateActivity.start(this, beer._id)));
		RxView.clicks(photoImage).subscribe(clicked -> startActivity(PhotoActivity.start(this, beer._id)));

	}

	private void showRatings(List<BeerRating> ratings) {
		ratingsList.setLayoutManager(new LinearLayoutManager(this));
		ratingsList.setAdapter(new BeerRatingsAdapter(ratings));
	}

	private BeerRating localToBeerRating(Rating rating) {
		// Mimic a recent rating object to show in the latest beer ratings list
		BeerRating beerRating = new BeerRating();
		// Take the stored rating of the logged in user
		beerRating.ratingId = rating.ratingId == null ? 0 : rating.ratingId.intValue();
		beerRating.aroma = rating.aroma;
		beerRating.flavor = rating.flavor;
		beerRating.appearance = rating.appearance;
		beerRating.mouthfeel = rating.mouthfeel;
		beerRating.overall = rating.overall;
		beerRating.total = rating.total;
		beerRating.comments = rating.comments;
		beerRating.timeEntered = rating.timeEntered;
		beerRating.timeUpdated = rating.timeUpdated;
		// Combine with the up-to-date user details from the session
		beerRating.userId = Session.get().getUserId();
		beerRating.userName = Session.get().getUserName();
		beerRating.userRateCount = Session.get().getUserRateCount();
		return beerRating;
	}

	private Beer ratingToBeer(Rating rating) {
		Beer beer = new Beer();
		beer._id = rating.beerId;
		beer.name = rating.beerName;
		beer.brewerName = rating.brewerName;
		return beer;
	}

	private void addBeerToNewCustomList(Beer beer, String listName, PopupWindow popup) {
		CustomList newList = new CustomList();
		newList.name = listName;
		database(this).put(newList);
		addBeerToCustomList(beer, newList._id, popup);
	}

	private void addBeerToCustomList(Beer beer, long listId, PopupWindow popup) {
		Db.getCustomListBeer(this, listId, beer._id)
				.compose(bindToLifecycle())
				.defaultIfEmpty(null)
				.subscribe(existingBeerOnList -> {
					if (existingBeerOnList == null) {
						CustomListBeer addBeer = new CustomListBeer();
						addBeer.listId = listId;
						addBeer.beerId = beer._id;
						addBeer.beerName = beer.name;
						rxdb(this).put(addBeer);
						listAddButton.setChecked(true);
					} else {
						rxdb(this).delete(existingBeerOnList);
						listAddButton.setChecked(false);
					}
				});
		popup.dismiss();
	}

	public void openAlias(View view) {
		// Look up the aliased beer id and open this beer instead (closing the current view)
		Api.get().getBeerAlias(beerId)
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(aliasBeerId -> {
					startActivity(BeerActivity.start(this, aliasBeerId));
					finish();
				}, e -> Snackbar.show(this, R.string.error_connectionfailure));
	}

}
