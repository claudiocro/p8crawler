package ch.plus8.hikr.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.plus8.hikr.gappserver.FeedItemBasic;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public interface FeedRepository {
	
	void storeFeed(FeedItemBasic entry, Collection<String> categories);

	void storeFeed(FeedItemBasic entry, Collection<String> categories, Integer statusOverwrite);
	
	void updateCategories(Key key, Entity entity, List<String> supCategories);

	void addToCategories(Key key, Entity entity, String supCategory);

	void storeFeed(FeedItemBasic entry, Collection<String> categories, Integer statusOverwrite, Map<String, Object> additionalProperties);

	void storeFeed(FeedItemBasic entry, Collection<String> categories, Map<String, Object> additionalProperties);

	void storeFeed(FeedItemBasic entry, String id, Collection<String> categories, Integer statusOverwrite, Map<String, Object> additionalProperties);


}
