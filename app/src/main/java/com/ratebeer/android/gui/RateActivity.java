package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.db.Rating;
import com.ratebeer.android.gui.widget.Animations;

import java.util.Date;
import java.util.Locale;

import rx.Observable;

import static com.ratebeer.android.db.CupboardDbHelper.database;

public final class RateActivity extends RateBeerActivity {

	private static final int REQUEST_PICK_BEER = 0;
	private static final int COMMENTS_LENGTH_MIN = 80;

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
	private View deleteButton;
	private Button actionButton;
	private View uploadProgress;

	private Rating rating;

	public static Intent start(Context context) {
		return new Intent(context, RateActivity.class);
	}

	public static Intent start(Context context, Rating rating) {
		return new Intent(context, RateActivity.class).putExtra("ratingId", rating._id.longValue());
	}

	public static Intent start(Context context, long beerId) {
		return new Intent(context, RateActivity.class).putExtra("beerId", beerId);
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
		deleteButton = findViewById(R.id.delete_button);
		actionButton = (Button) findViewById(R.id.upload_button);
		uploadProgress = findViewById(R.id.upload_progress);

		Observable<Rating> ratingObservable;
		if (getIntent().hasExtra("ratingId")) {
			// Load existing rating
			ratingObservable = Db.getOfflineRating(this, getIntent().getLongExtra("ratingId", 0)).map(existing -> {
				// Upgrade legacy data fields
				if (existing.beerId != null && existing.beerId <= 0)
					existing.beerId = null;
				return existing;
			});
		} else if (getIntent().hasExtra("beerId")) {
			// Start rating for a beer, perhaps based on an existing rating
			long beerId = getIntent().getLongExtra("beerId", 0);
			ratingObservable = Observable
					.combineLatest(Db.getBeer(this, beerId), Db.getRating(this, beerId, Session.get().getUserId()).firstOrDefault(null),
							(beer, existing) -> {
								if (existing == null) {
									existing = new Rating();
									existing.beerId = beer._id;
									existing.beerName = beer.name;
								} else {
									// Upgrade legacy data fields
									if (existing.beerId != null && existing.beerId <= 0)
										existing.beerId = null;
									if (existing.ratingId != null && existing.ratingId <= 0)
										existing.ratingId = null;
								}
								return existing;
							});
		} else {
			ratingObservable = Observable.just(new Rating());
		}
		ratingObservable.compose(onIoToUi()).subscribe(beerRating -> {
			beerNameText.setVisibility(beerRating.beerId == null ? View.GONE : View.VISIBLE);
			beerNameEntry.setVisibility(beerRating.beerId == null ? View.VISIBLE : View.GONE);
			beerNameText.setText(beerRating.beerName);
			beerNameEdit.setText(beerRating.beerName);
			aromaText.setText(getNumberString(beerRating.aroma));
			appearanceText.setText(getNumberString(beerRating.appearance));
			tasteText.setText(getNumberString(beerRating.flavor));
			palateText.setText(getNumberString(beerRating.mouthfeel));
			overallText.setText(getNumberString(beerRating.overall));
			commentsEdit.setText(beerRating.comments);
			actionButton.setText(beerRating.beerId == null ? R.string.rate_findbeer : R.string.rate_upload);
			updateTotalWith(beerRating.calculateTotal());
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_BEER && resultCode == RESULT_OK && data != null) {
			// Picked a beer: update the ongoing rating and show the beer name as title
			rating.beerId = data.getLongExtra(SearchActivity.EXTRA_BEERID, 0);
			rating.beerName = data.getStringExtra(SearchActivity.EXTRA_BEERNAME);
			beerNameText.setText(rating.beerName);
			beerNameEdit.setText(rating.beerName);
			actionButton.setText(R.string.rate_upload);
			Animations.fadeFlip(beerNameText, beerNameEntry);
			updateRating();
		}
	}

	private String getNumberString(Integer number) {
		if (number == null)
			return null;
		return Integer.toString(number);
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
		rating.timeEntered = null;
		rating.timeCached = new Date();
		updateTotalWith(rating.total);
		database(this).put(rating);
		getIntent().putExtra("ratingId", rating._id);
		RBLog.d("STORE:" + rating);
	}

	private void updateTotalWith(Float total) {
		totalText.setText(total == null ? "-" : String.format(Locale.getDefault(), "%1$.1f", total));
	}

	public void uploadFindBeer(View view) {
		if (rating == null)
			return;

		if (rating.beerId == null) {
			// Allow picking of the beer that the user is rating
			startActivityForResult(SearchActivity.start(this, beerNameEdit.getText().toString(), true), REQUEST_PICK_BEER);
			return;
		}

		// Validate input
		if (rating.total == null) {
			new AlertDialog.Builder(this).setMessage(R.string.rate_error_norating).setPositiveButton(android.R.string.ok, null).show();
			return;
		}
		if (rating.comments == null || rating.comments.length() < COMMENTS_LENGTH_MIN) {
			new AlertDialog.Builder(this).setMessage(R.string.rate_error_commentlength).setPositiveButton(android.R.string.ok, null).show();
			return;
		}

		// Upload the rating directly to RB
		Animations.fadeFlipOut(uploadProgress, actionButton, deleteButton);
		Db.postRating(this, rating, Session.get().getUserId()).compose(onIoToUi()).compose(bindToLifecycle()).subscribe(saved -> finish(), e -> {
			Animations.fadeFlipIn(actionButton, deleteButton, uploadProgress);
			Snackbar.show(this, R.string.error_connectionfailure);
		});

	}

	public void deleteRating(View view) {
		new AlertDialog.Builder(this).setMessage(R.string.rate_discard_confirm)
				.setPositiveButton(rating.ratingId == null ? R.string.rate_discard_rating : R.string.rate_discard_changes,
						(di, i) -> deleteOfflineRating()).setNegativeButton(android.R.string.cancel, null).show();
	}

	private void deleteOfflineRating() {
		Animations.fadeFlipOut(uploadProgress, actionButton, deleteButton);
		Db.deleteOfflineRating(this, rating, Session.get().getUserId()).compose(onIoToUi()).compose(bindToLifecycle())
				.subscribe(refreshed -> {}, e -> {
					Animations.fadeFlipIn(deleteButton, actionButton, uploadProgress);
					Snackbar.show(this, R.string.error_connectionfailure);
				}, this::finish);
	}

}
