package ch.plus8.hikr.repository;

import java.util.Collection;

import ch.plus8.hikr.gappserver.GoogleReaderFeed;

public interface FeedRepository {
	
	void storeFeed(String source, GoogleReaderFeed feed, Collection categories);
}
