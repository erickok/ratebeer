package com.ratebeer.android.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.ratebeer.android.R;
import com.ratebeer.android.db.CustomList;
import com.ratebeer.android.db.CustomListBeer;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.RBLog;
import com.ratebeer.android.gui.lists.CustomListBeersAdapter;
import com.ratebeer.android.rx.RecyclerAdapterDataEvent;
import com.ratebeer.android.rx.RxRecyclerViewAdapter;

import static com.ratebeer.android.db.CupboardDbHelper.database;
import static com.ratebeer.android.db.CupboardDbHelper.rxdb;

public final class CustomListActivity extends RateBeerActivity {

	private static final int REQUEST_PICK_BEER = 0;

	private RecyclerView beersList;
	private TextView emptyText;

	private CustomList list;
	private CustomListBeersAdapter customListBeersAdapter;

	public static Intent start(Context context) {
		return new Intent(context, CustomListActivity.class);
	}

	public static Intent start(Context context, long listId) {
		return new Intent(context, CustomListActivity.class).putExtra("listId", listId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customlist);

		setupDefaultUpButton();

		EditText listNameEdit = (EditText) findViewById(R.id.list_name_edit);
		beersList = (RecyclerView) findViewById(R.id.beers_list);
		emptyText = (TextView) findViewById(R.id.empty_text);

		customListBeersAdapter = new CustomListBeersAdapter();
		beersList.setLayoutManager(new LinearLayoutManager(this));
		beersList.setAdapter(customListBeersAdapter);

		long listId = getIntent().getLongExtra("listId", 0);
		if (listId > 0) {
			list = database(this).get(CustomList.class, listId);
			listNameEdit.setText(list.name);
		} else {
			// Create and directly save, such that we have an id to bind beers to
			list = new CustomList();
			rxdb(this).put(list);
		}

		// Directly persist name changes
		RxTextView.textChanges(listNameEdit).map(name -> {
			list.name = name.toString();
			return list;
		}).compose(bindToLifecycle()).subscribe(rxdb(this).put(), e -> RBLog.e("Error persisting the list name", e));

		// Directly visualize item changes
		Db.getCustomListBeerChanges(this, list._id).compose(bindToLifecycle()).subscribe(change -> customListBeersAdapter.change(change), e -> RBLog
				.e("Error handling a beer list change", e));

		// Load current beers list and always have it update the empty view visibility when adding/removing beers
		Db.getCustomListBeers(this, list._id).toList().compose(onIoToUi()).compose(bindToLifecycle()).subscribe(beers -> customListBeersAdapter.init
				(beers), e -> Snackbar.show(this, R.string.error_unexpectederror));
		RxRecyclerViewAdapter.dataEvents(customListBeersAdapter).filter(event -> event.getKind() != RecyclerAdapterDataEvent.Kind.RANGE_CHANGE)
				.subscribe(event -> {
			emptyText.setVisibility(customListBeersAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
			beersList.setVisibility(customListBeersAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
		}, e -> RBLog.e("Unexpected error when handling list addition/removal", e));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_BEER && resultCode == RESULT_OK && data != null) {
			// Picked a beer: save to the database and add it to the list
			CustomListBeer addBeer = new CustomListBeer();
			addBeer.listId = list._id;
			addBeer.beerId = data.getLongExtra(SearchActivity.EXTRA_BEERID, 0);
			addBeer.beerName = data.getStringExtra(SearchActivity.EXTRA_BEERNAME);
			rxdb(this).put(addBeer);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Remove the list if it is empty and has no name
		if (TextUtils.isEmpty(list.name) && customListBeersAdapter.getItemCount() == 0) {
			rxdb(this).delete(list);
		}
	}

	private void openListing(CustomListBeer customListBeer) {
		// TODO
	}

	public void quickAddBeer(View view) {
		startActivityForResult(SearchActivity.start(this, null, true), REQUEST_PICK_BEER);
	}

}
