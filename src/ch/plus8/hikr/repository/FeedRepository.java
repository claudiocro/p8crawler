package ch.plus8.hikr.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.plus8.hikr.gappserver.FeedItemBasic;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public interface FeedRepository {
	
	boolean storeFeed(FeedItemBasic entry, Collection<String> categories);

	boolean storeFeed(FeedItemBasic entry, Collection<String> categories, Long statusOverwrite);
	
	void updateCategories(Key key, Entity entity, List<String> supCategories);

	void addToCategories(Key key, Entity entity, String supCategory);

	boolean storeFeed(FeedItemBasic entry, Collection<String> categories, Long statusOverwrite, Map<String, Object> additionalProperties);

	boolean storeFeed(FeedItemBasic entry, Collection<String> categories, Map<String, Object> additionalProperties);

	boolean storeFeed(FeedItemBasic entry, String id, Collection<String> categories, Long statusOverwrite, Map<String, Object> additionalProperties, Key userKey, Key sourceAuth);

	void updateImageLinkAndProcess(Entity entity, String imageLink, Long img1A, String img1, boolean save);

	void increaseStatus(Entity entity, boolean b);

	void setStatus(Entity entity, Long status, boolean save);


}
