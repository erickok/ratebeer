package com.ratebeer.android.db;

public final class CustomListBeer {

	public Long _id;
	public long listId;
	public long beerId;
	public String beerName;
	public Integer stars;
	public String note;

	@Override
	public boolean equals(Object o) {
		return this == o || !(o == null || getClass() != o.getClass()) && _id.equals(((CustomListBeer) o)._id);
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

}
