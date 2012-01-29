package ch.plus8.hikr.gappserver.gplus;

import java.util.Calendar;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.Util;

import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.api.services.plus.model.ActivityObjectAttachments;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

public class GPlusUtil {

	private static final Logger logger = Logger.getLogger(GPlusUtil.class.getName());
	
	public static final String PERSON_KIND = "gplus:person";
	
	public static boolean fillEntity(Entity entity, ActivityFeed feed, Activity act, ActivityObjectAttachments att) {
		return fillEntity(entity, feed, act, att, 800, 1000);
	}
	
	public static boolean fillEntity(Entity entity, ActivityFeed feed, Activity act, ActivityObjectAttachments att, int maxImgSize, int maxImgSize1) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(act.getPublished().getValue());
		cal.add(Calendar.MINUTE, act.getPublished().getTimeZoneShift());
		entity.setProperty("publishedDate", cal.getTime());
		entity.setProperty("source", "gplus");
		entity.setProperty("link", att.getUrl());
		entity.setUnindexedProperty("title", att.getDisplayName()!= null ? Util.truncate(att.getDisplayName(), 499) : null);
		entity.setUnindexedProperty("feedLink", new Text(act.getUrl()));
		
		if(act.getPlusObject() != null && act.getPlusObject().getActor() != null) {
			entity.setUnindexedProperty("author", act.getPlusObject().getActor().getId());
			entity.setUnindexedProperty("authorName", act.getPlusObject().getActor().getDisplayName());
			entity.setUnindexedProperty("authorLink", act.getPlusObject().getActor().getUrl());
		} else {
			entity.setUnindexedProperty("author", act.getActor().getId());
			entity.setUnindexedProperty("authorName", act.getActor().getDisplayName());
			entity.setUnindexedProperty("authorLink", act.getActor().getUrl());
		}
		
		
		boolean store = true;
		if(att.getImage() != null && 
				att.getImage().getHeight() != null && att.getImage().getHeight()>=maxImgSize && 
				att.getImage().getWidth() != null && att.getImage().getWidth() >=maxImgSize ||
				
				(att.getImage().getHeight() != null && att.getImage().getHeight()>=maxImgSize1 || 
				att.getImage().getWidth() != null && att.getImage().getWidth() >=maxImgSize1)) {
			entity.setProperty("imageLink", att.getImage().getUrl());
			entity.setProperty("imageLinkA", 1);
		}
		
		if(att.getFullImage() != null && 
				(att.getFullImage().getHeight() != null && att.getFullImage().getHeight()>=maxImgSize && 
				att.getFullImage().getWidth() != null && att.getFullImage().getWidth() >=maxImgSize) ||
				
				(att.getFullImage().getHeight() != null && att.getFullImage().getHeight()>=maxImgSize1 || 
				att.getFullImage().getWidth() != null && att.getFullImage().getWidth() >=maxImgSize1)) {
			entity.setProperty("imageLink", att.getFullImage().getUrl());
			entity.setProperty("imageLinkA", 1);
		} else {
			store = false;
			
			if(att.getImage() != null && 
					att.getImage().getHeight() != null && 
					att.getImage().getWidth() != null) {
				logger.info("Skip image because it's to small:" + att.getFullImage().getWidth()+"x"+att.getFullImage().getHeight());
			}
			if(att.getFullImage() != null && 
					att.getFullImage().getHeight() != null && 
					att.getFullImage().getWidth() != null ) {
				logger.info("Skip image because it's to small:" + att.getFullImage().getWidth()+"x"+att.getFullImage().getHeight());
			}
			logger.warning("Skip image because it's to small:" + att.getUrl());
		}
		
		return store;
	}
	
}
