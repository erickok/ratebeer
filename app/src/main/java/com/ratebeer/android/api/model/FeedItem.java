package com.ratebeer.android.api.model;

import java.util.Date;

public final class FeedItem {

	// The different feed item types that are available (with an example LinkText)
	final public static int ITEMTYPE_BEERADDED = 1; // added a new Style Name: <a href="/beer/beer-name/beerid/">Beer Name</a><span class=uaa> (5.0%)
	final public static int ITEMTYPE_BEERRATING = 7; // rated <a href="/beer/beer-name/beerid/userid/">Beer Name</a>
	final public static int ITEMTYPE_PLACERATING = 8; // reviewed <a href="/p/goto/placeid/">Place Name</a>
	final public static int ITEMTYPE_ISDRINKING = 12; // Beer Name
	final public static int ITEMTYPE_EVENTATTENDANCE = 17; // is attending <a href="/event/20713/">Event Name</a> (1/1/2014 in City)
	final public static int ITEMTYPE_AWARD = 18; // Award Name
	final public static int ITEMTYPE_PLACECHECKIN = 20; // checked in at <a href="/p/place-name/placeid/">Place Name, City</a>
	final public static int ITEMTYPE_REACHEDRATINGS = 21; // reached # Style Name ratings!
	final public static int ITEMTYPE_BREWERYADDED = 22; // added a new brewery: <a  href="/brewers/brewer-name/brewerid/">Brewer Name in City</a>

	public int activityId;
	public int userId;
	public String userName;
	public int type;
	public int linkId;
	public String linkText;
	public int activityNumber;
	public Date timeEntered;
	public int numComments;

	public Integer getBeerId() {
		if (type == ITEMTYPE_BEERADDED || type == ITEMTYPE_BEERRATING || type == ITEMTYPE_ISDRINKING) {
			return linkId;
		}
		// Doe snot apply to a specific beer
		return null;
	}

}
