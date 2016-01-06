package com.ratebeer.android.gui.widget;

import com.quinny898.library.persistentsearch.SearchResult;

public abstract class SearchBoxSearchEvent {

	public enum Kind {
		OPENEND,
		CLOSED,
		CLEARED,
		QUERY_CHANGED,
		QUERY_SUBMITTED,
		RESULT_CLICKED
	}

	private final Kind kind;

	SearchBoxSearchEvent(Kind kind) {
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	public static final class SearchBoxOpenEvent extends SearchBoxSearchEvent {

		SearchBoxOpenEvent() {
			super(Kind.OPENEND);
		}

	}

	public static final class SearchBoxCloseEvent extends SearchBoxSearchEvent {

		SearchBoxCloseEvent() {
			super(Kind.CLOSED);
		}

	}

	public static final class SearchBoxClearEvent extends SearchBoxSearchEvent {

		SearchBoxClearEvent() {
			super(Kind.CLEARED);
		}

	}

	public static final class SearchBoxQueryChangeEvent extends SearchBoxSearchEvent {

		private final String query;

		SearchBoxQueryChangeEvent(String query) {
			super(Kind.QUERY_CHANGED);
			this.query = query;
		}

		public String query() {
			return query;
		}

	}

	public static final class SearchBoxQuerySubmitEvent extends SearchBoxSearchEvent {

		private final String query;

		SearchBoxQuerySubmitEvent(String query) {
			super(Kind.QUERY_SUBMITTED);
			this.query = query;
		}

		public String query() {
			return query;
		}

	}

	public static final class SearchBoxResultClickEvent extends SearchBoxSearchEvent {

		private final SearchResult searchResult;

		SearchBoxResultClickEvent(SearchResult searchResult) {
			super(Kind.QUERY_SUBMITTED);
			this.searchResult = searchResult;
		}

		public SearchResult searchResult() {
			return searchResult;
		}

	}

}
