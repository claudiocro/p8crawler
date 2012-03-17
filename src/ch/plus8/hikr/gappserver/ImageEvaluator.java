package ch.plus8.hikr.gappserver;

import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.datastore.Entity;

public abstract class ImageEvaluator {

	public abstract boolean evaluate(FeedRepository feedRepository, Entity entity) throws Exception;
}
