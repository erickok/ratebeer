package com.ratebeer.android.gui.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.ratebeer.android.R;
import com.ratebeer.android.db.Db;
import com.ratebeer.android.db.RBLog;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class SyncService extends IntentService {

	private static final BehaviorSubject<Boolean> SYNC_STATUS = BehaviorSubject.create(false);
	private static final int NOTIFY_SYNC = 0;
	private static final int REQUEST_SYNC = 0;

	public static Intent start(Context context) {
		return new Intent(context, SyncService.class);
	}

	public static Observable<Boolean> getSyncStatus() {
		return SYNC_STATUS.asObservable();
	}

	public SyncService() {
		super("RateBeer SyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (SYNC_STATUS.toBlocking().first()) {
			return; // Already syncing

		}

		// Perform sync by loading and then storing all of the user's ratings
		final AtomicInteger i = new AtomicInteger();
		Db.syncRatings(this, progress -> reportProgress(true, null, progress))
				.toBlocking()
				.subscribe(
						r -> RBLog.d("R:" + i.getAndIncrement()),
						e -> reportProgress(false, e, null),
						() -> reportProgress(false, null, 100F));

	}

	private void reportProgress(boolean isActive, Throwable error, Float progress) {

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Broadcast sync status for external listeners
		SYNC_STATUS.onNext(isActive);

		// Remove persistent notification when done
		if (!isActive && error == null) {
			notificationManager.cancel(NOTIFY_SYNC);
			return;
		}

		// Show error (and allow retry) when sync failed
		if (error != null) {
			PendingIntent retryIntent = PendingIntent.getService(this, REQUEST_SYNC, start(this), PendingIntent.FLAG_CANCEL_CURRENT);
			notificationManager
					.notify(NOTIFY_SYNC, new NotificationCompat.Builder(this)
							.setPriority(NotificationCompat.PRIORITY_DEFAULT)
							.setCategory(NotificationCompat.CATEGORY_ERROR)
							.setAutoCancel(true)
							.setSmallIcon(R.drawable.ic_stat_error)
							.setContentTitle(getString(R.string.sync_error))
							.setContentText(getString(R.string.sync_tryagain))
							.setContentIntent(retryIntent)
					.build());
			return;
		}

		// Show ongoing sync progress
		notificationManager.notify(NOTIFY_SYNC,
				new NotificationCompat.Builder(this)
						.setPriority(NotificationCompat.PRIORITY_LOW)
						.setCategory(NotificationCompat.CATEGORY_SERVICE)
						.setOngoing(true)
						.setSmallIcon(R.drawable.ic_stat_default)
						.setProgress(100, progress.intValue(), false)
						.setContentTitle(getString(R.string.sync_insync))
						.build());

	}

}
