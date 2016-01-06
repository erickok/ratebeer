package com.ratebeer.android.api;

public class Session {

	private static class Holder {
		// Holder with static instance which implements a thread safe lazy loading singleton
		static final Session INSTANCE = new Session();
	}

	static Session get() {
		return Holder.INSTANCE;
	}

}
