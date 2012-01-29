package ch.plus8.hikr.gappserver.lomo;

import java.text.ParseException;
import java.util.Calendar;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.lomo.Lomo.Asset;
import ch.plus8.hikr.gappserver.lomo.Lomo.Photo;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

public class LomoUtil {
	
	private static final Logger logger = Logger.getLogger(LomoUtil.class.getName());

	public static boolean fillEntity(Entity entity, Lomo lomo, Photo photo, Asset asset) throws ParseException {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(lomo.dateFormat.parse(photo.createdAt));
		//cal.add(Calendar.MINUTE, act.getPublished().getTimeZoneShift());
		entity.setProperty("publishedDate", cal.getTime());
		entity.setProperty("source", "lomo");
		entity.setProperty("link", photo.url);
		entity.setUnindexedProperty("title", photo.title!= null ? Util.truncate(photo.title, 499) : null);
		entity.setUnindexedProperty("feedLink", new Text(photo.url));
		
		
		entity.setUnindexedProperty("author", photo.user.username);
		entity.setUnindexedProperty("authorName", photo.user.username);
		entity.setUnindexedProperty("authorLink", photo.user.url);
		
		
		
		boolean store = true;
		if((asset.height >=370 && asset.width >=370)) {
			entity.setProperty("imageLink", asset.url);
			entity.setProperty("imageLinkA", 1);
		} else {
			store = false;
			logger.warning("Skip image because it's to small:" + asset.url + " / " + asset.width + ":"+asset.height);
		}
		
		return store;
	}
	
}
