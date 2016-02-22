package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.ratebeer.android.R;
import com.ratebeer.android.Session;
import com.ratebeer.android.db.Beer;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.db.Rating;

import java.util.Date;
import java.util.Locale;

import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.database;

public final class RateActivity extends RateBeerActivity {

	private TextView beerNameText;
	private View beerNameEntry;
	private EditText beerNameEdit;
	private View aromaButton;
	private View appearanceButton;
	private View tasteButton;
	private View palateButton;
	private View overallButton;
	private TextView aromaText;
	private TextView appearanceText;
	private TextView tasteText;
	private TextView palateText;
	private TextView overallText;
	private TextView totalText;
	private EditText commentsEdit;

	private Rating rating;

	public static Intent start(Context context) {
		return new Intent(context, RateActivity.class);
	}

	public static Intent start(Context context, Rating rating) {
		return new Intent(context, RateActivity.class).putExtra("ratingId", rating._id.longValue());
	}

	public static Intent start(Context context, Beer beer) {
		return new Intent(context, RateActivity.class).putExtra("beerId", beer._id.longValue());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rate);

		beerNameText = (TextView) findViewById(R.id.beer_name_text);
		beerNameEntry = findViewById(R.id.beer_name_entry);
		beerNameEdit = (EditText) findViewById(R.id.beer_name_edit);
		aromaButton = findViewById(R.id.aroma_button);
		appearanceButton = findViewById(R.id.appearance_button);
		tasteButton = findViewById(R.id.taste_button);
		palateButton = findViewById(R.id.palate_button);
		overallButton = findViewById(R.id.overall_button);
		aromaText = (TextView) findViewById(R.id.aroma_text);
		appearanceText = (TextView) findViewById(R.id.appearance_text);
		tasteText = (TextView) findViewById(R.id.taste_text);
		palateText = (TextView) findViewById(R.id.palate_text);
		overallText = (TextView) findViewById(R.id.overall_text);
		totalText = (TextView) findViewById(R.id.total_text);
		commentsEdit = (EditText) findViewById(R.id.comments_edit);

		Observable<Rating> ratingObservable;
		if (getIntent().hasExtra("ratingId")) {
			// Load existing rating
			ratingObservable = Db.getUserRating(this, getIntent().getLongExtra("ratingId", 0));
		} else if (getIntent().hasExtra("beerId")) {
			// Start rating for a beer, perhaps based on an existing rating
			long beerId = getIntent().getLongExtra("beerId", 0);
			ratingObservable = Observable
					.combineLatest(Db.getBeer(this, beerId), Db.getUserRating(this, beerId, Session.get().getUserId()).firstOrDefault(null),
							(beer, existing) -> {
								if (existing == null) {
									existing = new Rating();
									existing.beerId = beer._id;
									existing.beerName = beer.name;
								} else {
									// Upgrade legacy data fields
									if (existing.ratingId == null)
										existing.ratingId = existing._id;
									if (existing.beerId <= 0)
										existing.beerId = null;
								}
								return existing;
							});
		} else {
			ratingObservable = Observable.just(new Rating());
		}
		ratingObservable.subscribe(beerRating -> {
			beerNameText.setVisibility(beerRating.beerId == null ? View.GONE : View.VISIBLE);
			beerNameEntry.setVisibility(beerRating.beerId == null ? View.VISIBLE : View.GONE);
			beerNameText.setText(beerRating.beerName);
			beerNameEdit.setText(beerRating.beerName);
			aromaText.setText(Integer.toString(beerRating.aroma));
			appearanceText.setText(Integer.toString(beerRating.appearance));
			tasteText.setText(Integer.toString(beerRating.flavor));
			palateText.setText(Integer.toString(beerRating.mouthfeel));
			overallText.setText(Integer.toString(beerRating.overall));
			commentsEdit.setText(beerRating.comments);
			this.rating = beerRating;
		}, e -> Snackbar.show(this, R.string.error_connectionfailure));

		// Store changes in the rating into the database
		RxTextView.textChanges(beerNameEdit).subscribe(updated -> updateRating());
		RxTextView.textChanges(aromaText).subscribe(updated -> updateRating());
		RxTextView.textChanges(appearanceText).subscribe(updated -> updateRating());
		RxTextView.textChanges(tasteText).subscribe(updated -> updateRating());
		RxTextView.textChanges(palateText).subscribe(updated -> updateRating());
		RxTextView.textChanges(overallText).subscribe(updated -> updateRating());
		RxTextView.textChanges(commentsEdit).subscribe(updated -> updateRating());

		// Attach popup menus to the number buttons to enter a rating
		bindPopup(aromaButton, aromaText, R.layout.dialog_pick_10);
		bindPopup(appearanceButton, appearanceText, R.layout.dialog_pick_5);
		bindPopup(tasteButton, tasteText, R.layout.dialog_pick_10);
		bindPopup(palateButton, palateText, R.layout.dialog_pick_5);
		bindPopup(overallButton, overallText, R.layout.dialog_pick_20);

	}

	private void bindPopup(final View button, final TextView text, int layout) {
		RxView.clicks(button).subscribe(clicked -> {
			ViewGroup content = (ViewGroup) getLayoutInflater().inflate(layout, null);
			PopupWindow popup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			applyClickListeners(content, view -> {
				text.setText(((TextView) view).getText());
				popup.dismiss();
			});
			popup.showAsDropDown(button);
		});
	}

	private void applyClickListeners(ViewGroup parent, View.OnClickListener listener) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);
			if (child instanceof Button)
				child.setOnClickListener(listener);
			else if (child instanceof ViewGroup)
				applyClickListeners((ViewGroup) child, listener);
		}
	}

	private void updateRating() {
		if (rating == null)
			return; // Still loading
		rating.beerName = beerNameEdit.getText().toString();
		rating.aroma = aromaText.getText().length() == 0 ? null : Integer.parseInt(aromaText.getText().toString());
		rating.appearance = appearanceText.getText().length() == 0 ? null : Integer.parseInt(appearanceText.getText().toString());
		rating.flavor = tasteText.getText().length() == 0 ? null : Integer.parseInt(tasteText.getText().toString());
		rating.mouthfeel = palateText.getText().length() == 0 ? null : Integer.parseInt(palateText.getText().toString());
		rating.overall = overallText.getText().length() == 0 ? null : Integer.parseInt(overallText.getText().toString());
		rating.total = rating.calculateTotal();
		rating.comments = commentsEdit.getText().toString();
		rating.timeCached = new Date();
		updateTotalWith(rating.total);
		database(this).put(rating);
		RBLog.d("STORE:" + rating);
	}

	private void updateTotalWith(Float total) {
		totalText.setText(total == null ? null : String.format(Locale.getDefault(), "%1$.1f", total));
	}

}
