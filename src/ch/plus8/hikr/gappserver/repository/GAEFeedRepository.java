package ch.plus8.hikr.gappserver.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.AppengineDatastore;
import ch.plus8.hikr.gappserver.Datastore;
import ch.plus8.hikr.gappserver.DatastoreFactory;
import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.DropboxDatastore;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
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
	
	//public final static Integer STATUS_DELETED = new Integer(-999);
	
	private DatastoreService dataStore;
	private BlobstoreService blobstoreService;
	
	public void init() {
		dataStore = DatastoreServiceFactory.getDatastoreService();
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
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
		storeFeed(entry, categories, null, null);
	}
	
	@Override
	public void storeFeed(FeedItemBasic entry, Collection<String> categories, Long statusOverwrite) {
		storeFeed(entry, categories, statusOverwrite, null);
	}
	
	@Override
	public void storeFeed(FeedItemBasic entry, Collection<String> categories, Map<String, Object> additionalProperties) {
		storeFeed(entry, categories, null, additionalProperties);
	}
	
	@Override
	public void storeFeed(FeedItemBasic entry, Collection<String> categories, Long statusOverwrite, Map<String, Object> additionalProperties) {
		storeFeed(entry, null, categories, statusOverwrite, additionalProperties);
	}
	
	@Override
	public void storeFeed(FeedItemBasic entry,String id, Collection<String> categories, Long statusOverwrite, Map<String, Object> additionalProperties) {
		if(id == null)
			id = entry.link;
		
		Key key = createKey(id);
		Entity entity;
		try {
			entity = dataStore.get(key);
			logger.log(Level.FINE, "Skip store new feed because it already exists: "+id);
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
			if(entry.imageLink != null) {
				entity.setProperty("status", Util.ITEM_STATUS_IMAGE_LINK_PROCESS);
			} else {
				entity.setProperty("status", Util.ITEM_STATUS_NEW);
			}
			
			entity.setProperty("img1A", entry.img1A);
			
			if(statusOverwrite != null)
				entity.setProperty("status", statusOverwrite);
			
			entity.setUnindexedProperty("authorName", entry.authorName);
			entity.setUnindexedProperty("authorLink", entry.authorLink);
			
			entity.setProperty("storeDate", new Date());
			
			entity.setProperty("categories", categories);
			
			if(additionalProperties != null) {
				for(Entry<String, Object> en : additionalProperties.entrySet()) {
					entity.setUnindexedProperty(en.getKey(), en.getValue());
				}
			}
			
			dataStore.put(entity);
			logger.log(Level.FINE, "Stored new feed : "+entry.link);
		}
		
//		logger.info("Imported " + newEntities.size() + " from: " + feed.responseData.feed.link);
	}
	
	public void updateImageLinkAndProcess(Entity entity, String imageLink, Long img1A, String img1, boolean save) {
		entity.setProperty("imageLink", imageLink);
		entity.setProperty("img1A", img1A);
		entity.setUnindexedProperty("img1", img1);
		entity.setProperty("status", Util.ITEM_STATUS_IMAGE_LINK_PROCESS);
		if(save)
			dataStore.put(entity);
	}
	
	public void updateImg2(String entity, String imageLink, int img2type) {
		
	}
	
	@Override
	public void updateCategories(Key key, Entity entity, List<String> supCategories) {
		if(supCategories == null || Util.ITEM_STATUS_DELETED.equals(entity.getProperty("status")))
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
	
	public void increaseStatus(Entity entity, boolean save) {
		entity.setProperty("status", ((Number)entity.getProperty("status")).intValue()+1);
		if(save)
			dataStore.put(entity);
	}
	
	@Override
	public void setStatus(Entity entity, Long status, boolean save) {
		entity.setProperty("status", status);
		if(save)
			dataStore.put(entity);
	}
	
	
	
	public boolean deleteByKey(String name, boolean delete, boolean deleteImage, boolean deleteImg2) {
		 try {
        	Key key = KeyFactory.createKey(GAEFeedRepository.FEED_ITEM_KIND, name);
        	Entity entity = dataStore.get(key);
        	
        	if(!deleteImagesFromEntityItem(entity, deleteImage, deleteImg2)) {
        		logger.severe("Skip delete beacuse delete image failed: " + name);
        	}
        	
        	if(delete) {
        		dataStore.delete(key);
        		logger.fine("Deleted: " + name);
        		return true;
        	}
        	else if(!Util.ITEM_STATUS_DELETED.equals(entity.getProperty("status"))) {
        		entity.setProperty("link", null);
        		entity.setProperty("publishedDate", null);
        		entity.setUnindexedProperty("author", null);
    			entity.setUnindexedProperty("title", null);
    			entity.setUnindexedProperty("feedLink", null);
    			entity.setProperty("status", Util.ITEM_STATUS_DELETED);
    			
    			entity.setUnindexedProperty("authorName", null);
    			entity.setUnindexedProperty("authorLink", null);
    			entity.setProperty("categories", null);
    			
    			logger.fine("Marked as deleted: " + name);
    			dataStore.put(entity);
    			return true;
        	} else {
        		logger.fine("Item already in delete status: " + name);
        		return false;
        	}
        }catch(Exception e) {
        	logger.log(Level.SEVERE, "Error deleting: " + name);
        	return false;
        }
	}
	
	
	public boolean deleteImagesFromEntityItem(Entity entity, boolean deleteImage, boolean deleteImg2) {
		boolean deleted  = true;
		Datastore ds = null;
		
		if(deleteImage) {
			ds = DatastoreFactory.createDatastore((Long)entity.getProperty("img1A"), entity);
			if(ds == null || !ds.deleteImage(entity))
				deleted = false;
		}
		
		if(deleteImg2) {
			ds = DatastoreFactory.createDatastore((Long)entity.getProperty("img2A"), entity);
			if(ds == null || !ds.deleteImage2(entity))
				deleted = false;
		}
		
		return deleted;
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
