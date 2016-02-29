package com.ratebeer.android.gui.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.RBLog;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class SyncService extends IntentService {

	private static final PublishSubject<SyncProgress> SYNC_PROGRESS = PublishSubject.<SyncProgress>create();

	public static Intent start(Context context) {
		return new Intent(context, SyncService.class);
	}

	public static Observable<SyncProgress> getSyncProgress() {
		return SYNC_PROGRESS.asObservable();
	}

	public SyncService() {
		super("RateBeer SyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// Perform sync by loading and then storing all of the user's ratings
		final AtomicInteger i = new AtomicInteger();
		Db.syncUserRatings(this, progress -> SYNC_PROGRESS.onNext(new SyncProgress(true, progress))).toBlocking()
				.subscribe(r -> RBLog.d("R:" + i.getAndIncrement()), e -> SYNC_PROGRESS.onNext(new SyncProgress(false, e)),
						() -> SYNC_PROGRESS.onNext(new SyncProgress(false, 100F)));

	}

	public static final class SyncProgress {

		public boolean isWorking;
		public float progress;
		public Throwable error;

		SyncProgress(boolean isWorking, float progress) {
			this.isWorking = isWorking;
			this.progress = progress;
		}

		SyncProgress(boolean isWorking, Throwable error) {
			this.isWorking = isWorking;
			this.error = error;
		}

	}

}
