package com.ratebeer.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.ratebeer.android.api.Api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ShareHelper {

	private static final String URL_BEER = Api.DOMAIN + "/b/%1$d/";
	private static final String URL_BREWERY = Api.DOMAIN + "/brewers/b/%1$d/";
	private static final String URL_PLACE = Api.DOMAIN + "/p/p/%1$d/";

	private final Context context;

	public ShareHelper(Context context) {this.context = context;}

	public void shareBeer(long id, String name) {
		shareLink(name, String.format(Locale.US, URL_BEER, id));
	}

	public void shareBrewery(long id, String name) {
		shareLink(name, String.format(Locale.US, URL_BREWERY, id));
	}

	public void sharePlace(long id, String name) {
		shareLink(name, String.format(Locale.US, URL_PLACE, id));
	}

	private void shareLink(String name, String url) {
		Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		List<Intent> browserIntents = new ArrayList<>();
		for (ResolveInfo openInfo : context.getPackageManager().queryIntentActivities(openIntent, 0)) {
			if (!openInfo.activityInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
				Intent browserIntent = new Intent();
				browserIntent.setComponent(new ComponentName(openInfo.activityInfo.packageName, openInfo.activityInfo.name));
				browserIntent.setAction(Intent.ACTION_VIEW);
				browserIntent.setData(Uri.parse(url));
				browserIntents.add(browserIntent);
			}
		}
		String shareText = context.getString(R.string.app_sharetext, name, url);
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
		chooserIntent.putExtra(Intent.EXTRA_INTENT, shareIntent);
		chooserIntent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.app_openorshare));
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, browserIntents.toArray(new Intent[browserIntents.size()]));
		context.startActivity(chooserIntent);
	}

}
