package com.ratebeer.android.api.model;

import android.text.TextUtils;

import com.ratebeer.android.db.RBLog;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class BeerAliasIdConverter implements Converter<ResponseBody, BeerAliasId> {

	private static final String STRING_ISALIAS = "Also known as";
	private static final String STRING_LINK_START = "<A HREF=\"/beer/";
	private static final String STRING_LINK_ID_START = "/";
	private static final String STRING_LINK_ID_END = "/\"";

	@Override
	public BeerAliasId convert(ResponseBody value) throws IOException {

		String string = value.string();
		int isAliasString = string.indexOf(STRING_ISALIAS);
		if (isAliasString <= 0) {
			RBLog.e("This beer is not an alias or the returned HTML has some error");
			return null;
		}

		// Find the link that looks like <A HREF="/beer/amstel-100-malta-lager/38/"
		int linkStart = string.indexOf(STRING_LINK_START, isAliasString);
		if (linkStart <= 0)
			return null;
		int linkIdStart = string.indexOf(STRING_LINK_ID_START, linkStart + STRING_LINK_START.length());
		if (linkIdStart <= 0)
			return null;
		int linkIdEnd = string.indexOf(STRING_LINK_ID_END, linkStart + STRING_LINK_ID_START.length());
		if (linkIdEnd <= 0)
			return null;

		// Parse as long
		long aliasId;
		String aliasIdString = string.substring(linkIdStart + STRING_LINK_ID_START.length(), linkIdEnd);
		if (TextUtils.isEmpty(aliasIdString)) {
			RBLog.e("Tried to parse the beer alias id, but the link text has an empty id");
			return null;
		}
		try {
			aliasId = Long.parseLong(aliasIdString);
		} catch (NumberFormatException e) {
			RBLog.e("Tried to parse the beer alias id from '" + aliasIdString + "' but it is no number", e);
			return null;
		}

		BeerAliasId alias = new BeerAliasId();
		alias.id = aliasId;
		return alias;
	}

}
