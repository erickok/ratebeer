package com.ratebeer.android.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.ratebeer.android.R;
import com.ratebeer.android.ShareHelper;
import com.ratebeer.android.api.Api;
import com.ratebeer.android.api.model.BreweryBeer;
import com.ratebeer.android.db.Brewery;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.gui.lists.BreweryPropertiesBeersAdapter;
import com.ratebeer.android.gui.lists.Property;
import com.ratebeer.android.gui.widget.Animations;
import com.ratebeer.android.gui.widget.Images;
import com.ratebeer.android.gui.widget.ItemClickSupport;

import java.util.ArrayList;
import java.util.List;

public final class BreweryActivity extends RateBeerActivity {

	private Toolbar mainToolbar;
	private ProgressBar loadingProgress;
	private View detailsLayout;
	private RecyclerView propertiesBeersList;
	private BreweryPropertiesBeersAdapter propertiesBeersAdapter;

	public static Intent start(Context context, long breweryId) {
		return new Intent(context, BreweryActivity.class).putExtra("breweryId", breweryId);
	}

	@SuppressLint("PrivateResource")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_brewery);

		mainToolbar = setupDefaultUpButton();
		mainToolbar.inflateMenu(R.menu.menu_link);

		loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
		detailsLayout = findViewById(R.id.details_layout);
		propertiesBeersList = (RecyclerView) findViewById(R.id.properties_beers_list);
		propertiesBeersList.setLayoutManager(new LinearLayoutManager(this));
		propertiesBeersAdapter = new BreweryPropertiesBeersAdapter();
		propertiesBeersList.setAdapter(propertiesBeersAdapter);

		// Load place from database or live
		long breweryId = getIntent().getLongExtra("breweryId", 0);
		Db.getBrewery(this, breweryId, false)
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(this::showBrewery, e -> Snackbar.show(this, R
						.string.error_connectionfailure));

		// Load beers made by this brewery (which is always live data)
		Api.get().getBreweryBeers(breweryId)
				.toSortedList()
				.compose(onIoToUi())
				.compose(bindToLifecycle())
				.subscribe(this::showBeers, Throwable::printStackTrace);

	}

	private void showBrewery(Brewery brewery) {

		ImageView photoImage = (ImageView) findViewById(R.id.backdrop_image);
		Images.with(this).loadBrewery(brewery._id, true).fit().centerCrop().noPlaceholder().into(photoImage);

		if (!TextUtils.isEmpty(brewery.city)) {
			String typeText = brewery.getTypeName(this);
			String typeLocationText = getString(R.string.brewery_typelocation, typeText, brewery.city);
			SpannableStringBuilder typeLocationMarkup = new SpannableStringBuilder(typeLocationText);
			int typeStart = typeLocationText.indexOf(" ") + 1;
			int typeEnd = typeText.length();
			int locationStart = typeLocationText.indexOf(brewery.city);
			int locationEnd = locationStart + brewery.city.length();
			typeLocationMarkup.setSpan(new StyleSpan(Typeface.BOLD), typeStart, typeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			typeLocationMarkup.setSpan(new StyleSpan(Typeface.BOLD), locationStart, locationEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			((TextView) findViewById(R.id.type_place_text)).setText(typeLocationMarkup);
		} else {
			String typeText = brewery.getTypeName(this);
			SpannableStringBuilder typeMarkup = new SpannableStringBuilder(typeText);
			int typeStart = typeText.indexOf(" ") + 1;
			int typeEnd = typeText.length();
			typeMarkup.setSpan(new StyleSpan(Typeface.BOLD), typeStart, typeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			((TextView) findViewById(R.id.type_place_text)).setText(typeMarkup);
		}

		((TextView) findViewById(R.id.brewery_name_text)).setText(brewery.name);
		if (brewery.isRetired != null && brewery.isRetired)
			findViewById(R.id.retired_badge).setVisibility(View.VISIBLE);

		// Show additional data as (clickable) properties in a list
		List<Property> properties = new ArrayList<>();

		if (!TextUtils.isEmpty(brewery.website)) {
			Property websiteProperty = new Property(R.drawable.ic_prop_website, brewery.website, v -> {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, brewery.getWebsiteUri()));
				} catch (Exception e) {
					Snackbar.show(this, R.string.error_cannotopenurl);
				}
			});
			properties.add(websiteProperty);
		}
		if (!TextUtils.isEmpty(brewery.phoneNumber)) {
			properties.add(new Property(R.drawable.ic_prop_phone, brewery.phoneNumber, v -> {
				try {
					startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + brewery.phoneNumber.replaceAll("[^0-9|\\+]", ""))));
				} catch (Exception e) {
					Snackbar.show(this, R.string.error_cannotopenurl);
				}
			}));
		}
		ItemClickSupport.addTo(propertiesBeersList).setOnItemClickListener((parent, pos, v) -> {
			Object clicked = ((BreweryPropertiesBeersAdapter) propertiesBeersList.getAdapter()).getItem(pos);
			if (clicked instanceof BreweryBeer) {
				startActivity(BeerActivity.start(this, ((BreweryBeer) clicked).beerId));
			} else if (clicked instanceof Property) {
				((Property) clicked).clickListener.onClick(v);
			}
		});
		propertiesBeersAdapter.setProperties(properties);

		RxToolbar.itemClicks(mainToolbar).subscribe(item -> {
			new ShareHelper(this).shareBrewery(brewery._id, brewery.name);
		});

		Animations.fadeFlipIn(detailsLayout, propertiesBeersList, loadingProgress);

	}

	private void showBeers(List<BreweryBeer> beers) {
		propertiesBeersAdapter.setBeers(beers);
	}

}
