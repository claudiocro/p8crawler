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
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.dropbox.DropboxDatastore;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class GAEFeedRepository implements FeedRepository {

	private static final Logger logger = Logger.getLogger(GAEFeedRepository.class.getName());

	public final static String FEED_ITEM_KIND = "FeedItem";
	public final static String USER_GALLERY_KIND = "user:gallery";
	public final static String CNT_GROUP_KIND = "cnt:group";
	public final static String CNT_SIMPLE_CONTENT_KIND = "cnt:simple";
	

	//public final static Integer STATUS_DELETED = new Integer(-999);

	private DatastoreService dataStore;
	private BlobstoreService blobstoreService;

	public void init() {
		dataStore = DatastoreServiceFactory.getDatastoreService();
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	}

	public static final Key createKey(String link) {
		return createKey(null, link);
	}

	public static final Key createKey(Key parent, String link) {
		if (parent != null)
			return KeyFactory.createKey(parent, FEED_ITEM_KIND, link);
		else
			return KeyFactory.createKey(FEED_ITEM_KIND, link);
	}

	protected final void initEntity(Entity entity) {
		entity.setUnindexedProperty("img1", null);
		entity.setProperty("img1A", 0);
		entity.setUnindexedProperty("img2", null);
		entity.setProperty("img2A", 0);
	}

	@Override
	public boolean storeFeed(FeedItemBasic entry, Collection<String> categories) {
		return storeFeed(entry, categories, null, null);
	}

	@Override
	public boolean storeFeed(FeedItemBasic entry, Collection<String> categories, Long statusOverwrite) {
		return storeFeed(entry, categories, statusOverwrite, null);
	}

	@Override
	public boolean storeFeed(FeedItemBasic entry, Collection<String> categories, Map<String, Object> additionalProperties) {
		return storeFeed(entry, categories, null, additionalProperties);
	}

	@Override
	public boolean storeFeed(FeedItemBasic entry, Collection<String> categories, Long statusOverwrite, Map<String, Object> additionalProperties) {
		return storeFeed(entry, null, categories, statusOverwrite, additionalProperties, null, null);
	}

	@Override
	public boolean storeFeed(FeedItemBasic entry, String id, Collection<String> categories, Long statusOverwrite, Map<String, Object> additionalProperties, Key userKey, Key sourceAuth) {
		if (id == null)
			id = entry.link;

		Key key = createKey(userKey, id);
		Entity entity;
		try {
			entity = dataStore.get(key);
			logger.log(Level.FINE, "Skip store new feed because it already exists: " + id);
			return false;
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
			if (entry.imageLink != null) {
				entity.setProperty("status", Util.ITEM_STATUS_IMAGE_LINK_PROCESS);
			} else {
				entity.setProperty("status", Util.ITEM_STATUS_NEW);
			}

			entity.setProperty("img1A", entry.img1A);

			if (statusOverwrite != null)
				entity.setProperty("status", statusOverwrite);

			entity.setUnindexedProperty("authorName", entry.authorName);
			entity.setUnindexedProperty("authorLink", entry.authorLink);

			entity.setProperty("storeDate", new Date());

			entity.setProperty("categories", categories);

			if (additionalProperties != null) {
				for (Entry<String, Object> en : additionalProperties.entrySet()) {
					entity.setUnindexedProperty(en.getKey(), en.getValue());
				}
			}

			if (sourceAuth != null) {
				entity.setUnindexedProperty("sourceAuth", sourceAuth);
			}

			dataStore.put(entity);
			logger.log(Level.FINE, "Stored new feed : " + entry.link);
			return true;
		}

		//		logger.info("Imported " + newEntities.size() + " from: " + feed.responseData.feed.link);
	}

	public void updateImageLinkAndProcess(Entity entity, String imageLink, Long img1A, String img1, boolean save) {
		entity.setProperty("imageLink", imageLink);
		entity.setProperty("img1A", img1A);
		entity.setUnindexedProperty("img1", img1);
		entity.setProperty("status", Util.ITEM_STATUS_IMAGE_LINK_PROCESS);
		if (save)
			dataStore.put(entity);
	}

	public void updateImg2(String entity, String imageLink, int img2type) {

	}

	@Override
	public void updateCategories(Key key, Entity entity, List<String> supCategories) {
		if (supCategories == null || Util.ITEM_STATUS_DELETED.equals(entity.getProperty("status")))
			return;

		Object cats = entity.getProperty("categories");
		boolean update = false;
		for (String supCat : supCategories) {
			if (updateFeedCategories(cats, entity, (String) supCat))
				update = true;
		}

		if (update) {
			dataStore.put(entity);
			logger.log(Level.FINE, "Only update categories: " + key.getName());
		}
	}

	@Override
	public void addToCategories(Key key, Entity entity, String supCategory) {
		Object cats = entity.getProperty("categories");
		if (updateFeedCategories(cats, entity, supCategory)) {
			dataStore.put(entity);
			logger.log(Level.FINE, "Only update categories: " + key.getName());
		}
	}

	public void increaseStatus(Entity entity, boolean save) {
		entity.setProperty("status", ((Number) entity.getProperty("status")).intValue() + 1);
		if (save)
			dataStore.put(entity);
	}

	@Override
	public void setStatus(Entity entity, Long status, boolean save) {
		entity.setProperty("status", status);
		if (save)
			dataStore.put(entity);
	}

	public boolean deleteByKey(String name, boolean delete, boolean deleteImage, boolean deleteImg2) {
		try {
			logger.info("Delete FeedItem: " + name +" delete:"+delete+" deleteImage:"+deleteImage+" deleteImg2:"+deleteImg2);
			Key key = KeyFactory.stringToKey(name);
			Entity entity = dataStore.get(key);
			logger.fine("Delete FeedItem - imageLink: " + entity.getProperty("imageLink"));
			
			if(deleteImage && !deleteImageFromEntity(entity)) {
				delete = false;
				logger.severe("Delete image failed, force delete status: " + name);
			}
			
			if(deleteImg2 && !deleteImg2FromEntity(entity)) {
				delete = false;
				logger.severe("Delete img2 failed, force delete status: " + name);
			}
		
			if (delete) {
				dataStore.delete(key);
				logger.fine("Deleted: " + name);
				return true;
			} else if (!Util.ITEM_STATUS_DELETED.equals(entity.getProperty("status"))) {
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
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error deleting: " + name, e);
			return false;
		}
	}

	public void deleteImagesFromEntityItem(Entity entity, boolean deleteImage, boolean deleteImg2) {
		if (deleteImage) {
			deleteImageFromEntity(entity);
		}

		if (deleteImg2) {
			deleteImg2FromEntity(entity);
		}
	}

	public boolean deleteImageFromEntity(Entity entity) {
		boolean deleted = true;
		Datastore ds = DatastoreFactory.createDatastore((Long) entity.getProperty("img1A"), entity);
		if (ds != null && !ds.deleteImage(entity))
			deleted = false;

		return deleted;
	}

	public boolean deleteImg2FromEntity(Entity entity) {
		boolean deleted = true;

		Datastore ds = DatastoreFactory.createDatastore((Long) entity.getProperty("img2A"), entity);
		if (ds != null && !ds.deleteImage2(entity))
			deleted = false;

		return deleted;
	}

	public Entity getDatatoreByKey(Key key) {
		Query query = new Query("user:datastore");
		query.setAncestor(UserUtils.getCurrentKeyFor());
		query.addFilter("key", FilterOperator.EQUAL, key);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
		fetchOptions.prefetchSize(1);
		PreparedQuery pquery = dataStore.prepare(query);
		return pquery.asSingleEntity();

	}
	
	public Entity findGalleryByRef(DatastoreService ds, String kind, String ref, Key userKey) {
		Query q = new Query(GAEFeedRepository.USER_GALLERY_KIND);
		q.setAncestor(userKey);
		q.addFilter("kind", FilterOperator.EQUAL, kind);
		q.addFilter("ref", FilterOperator.EQUAL, ref);
		PreparedQuery pq = ds.prepare(q);
		return pq.asSingleEntity();
	}

	protected final boolean updateFeedCategories(Object cats, Entity entity, String hashTag) {
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
				for (Object o : (Collection) cats) { //TODO: optimize
					if (o.equals(hashTag)) {
						add = false;
						break;
					}
				}
				if (add) {
					((Collection) cats).add(hashTag);
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
