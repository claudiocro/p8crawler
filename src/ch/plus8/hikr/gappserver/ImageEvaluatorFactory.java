package ch.plus8.hikr.gappserver;

import java.util.HashMap;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.dropbox.DropboxImageEvaluator;
import ch.plus8.hikr.gappserver.googledrive.GDriveApi;
import ch.plus8.hikr.gappserver.googledrive.GDriveImageEvaluator;
import ch.plus8.hikr.gappserver.hikr.HikrImageEvaluator;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ImageEvaluatorFactory {
	private static final Logger logger = Logger.getLogger(ImageEvaluatorFactory.class.getName());
	
	private static final HashMap<String, ImageEvaluator> cache =new HashMap<String, ImageEvaluator>();

	public static ImageEvaluator createImageEvaluator(String source, Entity entity) {
		if("hikr".equals(source)) {
			if(cache.containsKey("hikr"))
				return cache.get("hikr");
			else {
				ImageEvaluator ds = new HikrImageEvaluator();
				cache.put("hikr", ds);
				return ds;
			}
		} else if("dropbox".equals(source)) {
			
			if(cache.containsKey("dropbox"))
				return cache.get("dropbox");
			else {
				ImageEvaluator ds = new DropboxImageEvaluator();
				cache.put("dropbox", ds);
				return ds;
			}
		}  else if("gdrive".equals(source)) {
			if(cache.containsKey("gdrive"))
				return cache.get("gdrive");
			else {
				ImageEvaluator ds = new GDriveImageEvaluator();
				cache.put("gdrive", ds);
				return ds;
			}
		} 
		return null;	
	}

}
