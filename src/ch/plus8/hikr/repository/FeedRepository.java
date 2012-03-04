package ch.plus8.hikr.repository;

import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import ch.plus8.hikr.gappserver.FeedItem;
import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.googlefeed.GoogleReaderFeed;

public interface FeedRepository {
	
	void storeFeed(FeedItemBasic entry, Collection<String> categories);

	void updateCategories(Key key, Entity entity, List<String> supCategories);

	void addToCategories(Key key, Entity entity, String supCategory);
}
