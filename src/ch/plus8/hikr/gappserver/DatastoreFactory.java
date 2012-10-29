package ch.plus8.hikr.gappserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.dropbox.DropboxDatastore;
import ch.plus8.hikr.gappserver.googledrive.GDriveDatastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class DatastoreFactory {

	private static final Logger logger = Logger.getLogger(DatastoreFactory.class.getName());
	private static final HashMap<String, Datastore> cache =new HashMap<String, Datastore>();

	public static Datastore createDatastore(Long datastore, Entity entity) {
		logger.info("createDatastore for: "+datastore);
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
				Datastore ds = new DropboxDatastore((Key)entity.getProperty("sourceAuth"));
				cache.put(key, ds);
				return ds;
			}
		} else if(datastore.equals(Util.DATASTORE_GDRIVE)) {
			String author = (String)entity.getProperty("author");
			String key = "gdrive-"+author;
			if(cache.containsKey(key))
				return cache.get(key);
			else {
				try {
				Datastore ds = new GDriveDatastore((Key)entity.getProperty("sourceAuth"));
				cache.put(key, ds);
				return ds;
				}catch (IOException e) {
					logger.severe("Error creating datastore for type: " + datastore);
					throw new IllegalArgumentException("Error creating datastore for type:"+datastore);
				}
			}
		} else if(datastore.equals(Util.DATASTORE_UNKNOWN)) {
			return null;
		} else if(datastore.equals(Util.ZERO)) {
			return null;
		}
		
		logger.warning("Could not found datastore for type: " + datastore);
		throw new IllegalArgumentException("No datastore found for type:"+datastore);
		
	}
	
}
