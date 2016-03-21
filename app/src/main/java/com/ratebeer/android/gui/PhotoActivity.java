package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ImageUrls;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class PhotoActivity extends RateBeerActivity {

	public static Intent start(Context context, String userName) {
		return new Intent(context, PhotoActivity.class).putExtra("userName", userName);
	}

	public static Intent start(Context context, long beerId) {
		return new Intent(context, PhotoActivity.class).putExtra("beerId", beerId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);

		// Set up toolbar
		Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		mainToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		mainToolbar.inflateMenu(R.menu.menu_refresh);
		RxToolbar.navigationClicks(mainToolbar).subscribe(ignore -> navigateUp());
		RxToolbar.itemClicks(mainToolbar).filter(item -> item.getItemId() == R.id.menu_refresh).subscribe(item -> {
			showPhoto(true);
		});

		showPhoto(false);

	}

	private void showPhoto(boolean refresh) {
		// Load high res photo into the single image view
		ImageView photoImage = (ImageView) findViewById(R.id.photo_image);
		String url = null;
		if (getIntent().hasExtra("userName")) {
			url = ImageUrls.getUserPhotoHighResUrl(getIntent().getStringExtra("userName"));
		} else if (getIntent().hasExtra("beerId")) {
			url = ImageUrls.getBeerPhotoHighResUrl(getIntent().getLongExtra("beerId", 0));
		}
		if (refresh) {
			Picasso.with(this).invalidate(url);
			Picasso.with(this).load(url).networkPolicy(NetworkPolicy.NO_CACHE).into(photoImage);
		} else {
			Picasso.with(this).load(url).into(photoImage);
		}
	}

}
