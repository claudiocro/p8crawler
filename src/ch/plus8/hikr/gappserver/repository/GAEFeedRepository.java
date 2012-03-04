package ch.plus8.hikr.gappserver.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

public class GAEFeedRepository implements FeedRepository {

	private static final Logger logger = Logger.getLogger(GAEFeedRepository.class.getName());
	
	public final static String FEED_ITEM_KIND = "FeedItem";
	private DatastoreService dataStore;
	
	public void init() {
		dataStore = DatastoreServiceFactory.getDatastoreService();
	}

	public static final Key createKey(String link) {
		return KeyFactory.createKey(FEED_ITEM_KIND, link);
	}
	
	protected final void initEntity(Entity entity) {
		entity.setUnindexedProperty("img1", null);
		entity.setProperty("img1A", 0);
		entity.setUnindexedProperty("img2", null);
		entity.setProperty("img2A", 0);
	}
	
	@Override
	public void storeFeed(FeedItemBasic entry, Collection<String> categories) {
		
		Key key = createKey(entry.link);
		Entity entity;
		try {
			entity = dataStore.get(key);
			logger.log(Level.FINE, "Skip store new feed because it already exists: "+entry.link);
		} catch (EntityNotFoundException e) {
			entity = new Entity(key);
			initEntity(entity);
			entity.setProperty("link", entry.link);
			entity.setProperty("publishedDate", entry.publishedDate);
			entity.setProperty("source", entry.source);
			entity.setUnindexedProperty("author", entry.author);
			entity.setUnindexedProperty("title", entry.title);
			entity.setUnindexedProperty("feedLink", new Text(entry.feedLink));
			
			entity.setUnindexedProperty("imageLink", entry.imageLink);
			entity.setProperty("imageLinkA", entry.imageLinkA);
			
			entity.setUnindexedProperty("authorName", entry.authorName);
			entity.setUnindexedProperty("authorLink", entry.authorLink);
			
			entity.setProperty("storeDate", new Date());
			
			entity.setProperty("categories", categories);
			
			dataStore.put(entity);
			logger.log(Level.FINE, "Stored new feed : "+entry.link);
		}
		
//		logger.info("Imported " + newEntities.size() + " from: " + feed.responseData.feed.link);
	}
	
	@Override
	public void updateCategories(Key key, Entity entity, List<String> supCategories) {
		if(supCategories == null)
			return;
		
		Object cats = entity.getProperty("categories");
		boolean update = false;
		for(String supCat : supCategories) {
			if(updateFeedCategories(cats,entity,(String)supCat))
				update = true;
		}
		
		if(update) {
			dataStore.put(entity);
			logger.log(Level.FINE, "Only update categories: "+key.getName());
		}
	}
	
	@Override
	public void addToCategories(Key key, Entity entity, String supCategory) {
		Object cats = entity.getProperty("categories");
		if(updateFeedCategories(cats,entity, supCategory)) {
			dataStore.put(entity);
			logger.log(Level.FINE, "Only update categories: "+key.getName());
		}
	}
	
	
	protected final boolean updateFeedCategories(Object cats, Entity entity, String hashTag)
	  {
	    if (cats != null) {
	      if ((cats instanceof String)) {
	        List ncats = new ArrayList();
	        ncats.add(cats);
	        if (!cats.equals(hashTag)) {
	          ncats.add(hashTag);
	        }
	        entity.setProperty("categories", ncats);
	        return true;
	      }
	      
	      if ((cats instanceof Collection)) {
	        boolean add = true;
	        for (Object o : (Collection)cats) { //TODO: optimize
	          if (o.equals(hashTag)) {
	            add = false;
	            break;
	          }
	        }
	        if (add) {
	          ((Collection)cats).add(hashTag);
	          entity.setProperty("categories", cats);
	          return true;
	        }
	        return false;
	      }
	    }
	    
	    
	    else {
	      List ncats = new ArrayList();
	      ncats.add(hashTag);
	      entity.setProperty("categories", ncats);
	      return true;
	    }

	    return false;
	  }

}
