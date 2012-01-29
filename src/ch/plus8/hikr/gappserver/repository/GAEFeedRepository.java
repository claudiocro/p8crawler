package ch.plus8.hikr.gappserver.repository;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.GoogleReaderFeed;
import ch.plus8.hikr.gappserver.GoogleReaderFeed.Entries;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GAEFeedRepository implements FeedRepository {

	private static final Logger logger = Logger.getLogger(GAEFeedRepository.class.getName());
	
	public final static String FEED_ITEM_KIND = "FeedItem";
	private DatastoreService dataStore;
	
	public void init() {
		dataStore = DatastoreServiceFactory.getDatastoreService();
	}

	
	@Override
	public void storeFeed(String source, GoogleReaderFeed feed, Collection categories) {
		if(feed.responseData == null || feed.responseData.feed == null || feed.responseData.feed.entries == null) {
			logger.log(Level.WARNING, "No data is imported because feed is empty");
			return;
		}
		
		Set<Entity> newEntities = new HashSet<Entity>();
		for(Entries entry : feed.entries()) {
			try {
				Key key = createKey(entry.link);
				Entity entity;
				try {
					entity = dataStore.get(key);
					logger.log(Level.FINE, "Skip store new feed because it already exists: "+entry.link);
				} catch (EntityNotFoundException e) {
					entity = new Entity(key);
					initEntity(entity);
					entity.setProperty("publishedDate", GoogleReaderFeed.dateFormat.parse(entry.publishedDate));
					entity.setProperty("source", source);
					entity.setProperty("link", entry.link);
					entity.setUnindexedProperty("title", entry.title);
					entity.setUnindexedProperty("feedLink", feed.feedLink());
					entity.setUnindexedProperty("author", entry.author);
					entity.setProperty("categories", categories);
					newEntities.add(entity);
				}
			} catch (ParseException e) {
				logger.log(Level.SEVERE, "Feed not saved. Could not parse publishedDate: " + entry.publishedDate +" / " + feed.feedLink());
			}
		}
		
		dataStore.put(newEntities);
		logger.info("Imported " + newEntities.size() + " from: " + feed.responseData.feed.link);
	}
	
	public static final Key createKey(String link) {
		return KeyFactory.createKey(FEED_ITEM_KIND, link);
	}
	
	public static final void initEntity(Entity entity) {
		entity.setUnindexedProperty("imageLink", null);
		entity.setProperty("imageLinkA", 0);
		entity.setUnindexedProperty("img1", null);
		entity.setProperty("img1A", 0);
		entity.setUnindexedProperty("img2", null);
		entity.setProperty("img2A", 0);
	}

}
