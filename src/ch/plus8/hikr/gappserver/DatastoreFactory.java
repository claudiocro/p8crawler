package ch.plus8.hikr.gappserver;

import java.util.HashMap;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.dropbox.DropboxDatastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.images.Image;

public class DatastoreFactory {

	private static final Logger logger = Logger.getLogger(DatastoreFactory.class.getName());
	private static final HashMap<String, Datastore> cache =new HashMap<String, Datastore>();

	public static Datastore createDatastore(Long datastore, Entity entity) {
		if(datastore.equals(Util.DATASTORE_APPENGINE)) {
			if(cache.containsKey("appengine"))
				return cache.get("appengine");
			else {
				Datastore ds = new AppengineDatastore();
				cache.put("appengine", ds);
				return ds;
			}
		} else if(datastore.equals(Util.DATASTORE_DROPBOX)) {
			String author = (String)entity.getProperty("author");
			String key = "dropbox-"+author;
			if(cache.containsKey(key))
				return cache.get(key);
			else {
				Datastore ds = new DropboxDatastore(author);
				cache.put(key, ds);
				return ds;
			}
		} else if(datastore.equals(Util.DATASTORE_UNKNOWN)) {
			return null;
		} else if(datastore.equals(Util.ZERO)) {
			return null;
		}
		
		logger.severe("Could not found datastore for type: " + datastore);
		throw new IllegalArgumentException("No datastore found for type:"+datastore);
		
	}
	
	private static class ZeroDatastore extends Datastore{

		@Override
		protected boolean deleteImageItem(Entity entity) {
			return true;
		}

		@Override
		protected boolean deleteImg2(Entity entity) {
			return true;
		}

		@Override
		protected boolean uploadImg2(Entity entity, Image orgImageB) {
			return true;
		}
		
	}
}