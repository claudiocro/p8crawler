package ch.plus8.hikr.gappserver;

import java.util.HashMap;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.dropbox.DropboxImageEvaluator;
import ch.plus8.hikr.gappserver.hikr.HikrImageEvaluator;

import com.google.appengine.api.datastore.Entity;

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
			String author = (String)entity.getProperty("author");
			String key = "dropbox-"+author;
			if(cache.containsKey(key))
				return cache.get(key);
			else {
				ImageEvaluator ds = new DropboxImageEvaluator(author);
				cache.put(key, ds);
				return ds;
			}
		} 
		return null;	
	}

}
