package ch.plus8.hikr.gappserver;

import java.util.HashMap;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.admin.UserUtils;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class Scheduler
{
  private static final Logger logger = Logger.getLogger(Scheduler.class.getName());

  public static final void addUserIfExists(TaskOptions param) {
	  try {
		  Key key = UserUtils.getCurrentKeyFor();
		  if(key != null)
			  param.param(UserUtils.P8_TASK_QUEUE_AUTH, KeyFactory.keyToString(key));
	  }catch (Exception e) {
		;
	}
  }
  
  public static final void scheduleImageEvaluator() {
    scheduleImageEvaluator(null);
  }

  public static final void scheduleImageEvaluator(String cursor) {
    logger.info("Schedule: /p8admin/imageEvaluator :" + cursor);
    Queue queue = QueueFactory.getDefaultQueue();
    TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/imageEvaluator");
    if (cursor != null) {
      param.param("cursor", cursor);
    }
    
    addUserIfExists(param);
    
    queue.add(param);
  }

  public static void scheduleLomoImporter(String type, int page) {
    Queue queue = QueueFactory.getDefaultQueue();
    TaskOptions param = TaskOptions.Builder.withUrl("/lomo/lomoImport");
    param.param("type", type);
    if (page != 0) {
      param.param("page", String.valueOf(page));
    }
    queue.add(param);
  }

  public static void scheduleGplusPersonsActivitiesImport(String cursor) {
    logger.info("Schedule: /gplus/personsActivitiesImport :" + cursor);
    Queue queue = QueueFactory.getDefaultQueue();
    TaskOptions param = TaskOptions.Builder.withUrl("/gplus/personsActivitiesImport");
    if (cursor != null) {
      param.param("cursor", cursor);
    }
    queue.add(param);
  }

  public static void scheduleGPlusHashTagActivity(String hashTag, int page, String nextPage)
  {
    logger.info("Schedule: /gplus/hashTagActivitiesImporter :" + hashTag + " / " + nextPage);
    Queue queue = QueueFactory.getDefaultQueue();
    TaskOptions param = TaskOptions.Builder.withUrl("/gplus/hashTagActivitiesImporter");
    param.param("hashtag", hashTag);
    param.param("page", String.valueOf(page));

    if (nextPage != null) {
      param.param("nextPage", nextPage);
    }
    queue.add(param);
  }
  
  
  public static void scheduleDropboxGallery(String dropboxUid, String path, int offset, String title, String desc, String authorName) {
	  logger.info("Schedule: /dropbox/dropboxSyncher :" + dropboxUid + " / " + path + " / " + offset);
	    Queue queue = QueueFactory.getDefaultQueue();
	    TaskOptions param = TaskOptions.Builder.withUrl("/dropbox/dropboxSyncher");
	    param.param("createAlbum", String.valueOf(1));
	    param.param("dropboxUid", dropboxUid);
	    param.param("path", path);
	    param.param("offset", String.valueOf(offset));
	    if(title != null)
	    	param.param("title", title);
	    
	    if(desc != null)
	    	param.param("desc", desc);
	    
	    if(authorName != null)
	    	param.param("authorName", authorName);

	    addUserIfExists(param);
	    
	    queue.add(param);
  }
  
  
  public static void scheduleGDriveGallery(String googleUid, String path, String pageToken, String title, String desc, String authorName) {
	  logger.info("Schedule: /gdrive/createGallery :" + googleUid + " / " + path + " / " + pageToken);
	    Queue queue = QueueFactory.getDefaultQueue();
	    TaskOptions param = TaskOptions.Builder.withUrl("/gdrive/createGallery");
	    param.param("createAlbum", String.valueOf(1));
	    param.param("googleUid", googleUid);
	    param.param("path", path);
	    param.param("pageToken", pageToken);
	    if(title != null)
	    	param.param("title", title);
	    
	    if(desc != null)
	    	param.param("desc", desc);
	    
	    if(authorName != null)
	    	param.param("authorName", authorName);

	    addUserIfExists(param);
	    
	    queue.add(param);
  }

  public static final void scheduleImageFetcher() {
    scheduleImageFetcher(null);
  }

  public static final void scheduleImageFetcher(String cursor) {
    logger.info("Schedule: /p8admin/fetchImage: " + cursor);
    TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/fetchImage");

    if (cursor != null) {
      param.param("cursor", cursor);
    }
    
    addUserIfExists(param);
    
    Queue queue = QueueFactory.getQueue("fetchImage-queue");
    queue.add(param);
  }

  public static final void scheduleDeleteItem(String key, boolean forceDelete, boolean deleteImage, boolean deleteImg2)
  {
    logger.info("Schedule: /p8admin/deleteItem: " + key+" delete:"+forceDelete+" deleteImage:"+deleteImage+" deleteImg2:"+deleteImg2);
    TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/deleteItem");
    param.param("key", key);
    if(forceDelete)
    	param.param("delete", "1");
    
    if(deleteImage)
    	param.param("deleteImage", "1");
    
    if(deleteImg2)
    	param.param("deleteImg2", "1");

    Queue queue = QueueFactory.getQueue("deleteItem-queue");
    queue.add(param);
  }

  public static void scheduleDeleteOldFeedItems(String cursor, String source, String cat, String timeType, String timeValue, boolean forceDelete, boolean deleteImage, boolean deleteImg2)
  {
    logger.info("Schedule: /p8admin/deleteOldItems: " + source);
    TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/deleteOldItems");
    if(source != null)
    	param.param("source", source);
    else if(cat != null)
    	param.param("cat", cat);
    
    param.param("timeType", timeType);
    param.param("timeValue", timeValue);
    if(forceDelete)
    	param.param("delete", "1");

    if(deleteImage)
    	param.param("deleteImage", "1");
    
    if(deleteImg2)
    	param.param("deleteImg2", "1");
    
    if (cursor != null) {
      param.param("cursor", cursor);
    }
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(param);
  }

  public static void scheduleFeedCacher(String param, String paramV, Integer[] page, String sortCol, boolean forceCache) {
    Queue queue = QueueFactory.getQueue("feedCacher-queue");
    for (int i = 0; i < page.length; i++) {
      logger.info("Schedule: /feed:" + param + " / " + paramV + " / " + page[i]);

      TaskOptions options = 
        TaskOptions.Builder.withUrl("/feed")
        .param("nocache", forceCache ? "1" : "0")
        .param("cache-request", "1")
        .param(param, paramV)
        .param("page", String.valueOf(page[i]));
      
      	if(sortCol != null)
      		options.param("sort", sortCol);

      queue.add(options);
    }
  }
  
  @SuppressWarnings("rawtypes")
public static void cleanCache(String param, String paramV)
  {
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService("feedItems");
    logger.info("Clean memcache: feedItems:" + param + ":" + paramV);
    memcacheService.put("feedItems:" + param + ":" + paramV, new HashMap());
  }

  @SuppressWarnings("rawtypes")
public static void scheduleFeedCacher(boolean forceCache) {
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService("feedItems");

    if (forceCache) {
      logger.info("scheduleFeedCacher with force cache remove");
    }

    for (int i = 0; i < Util.categories.length; i++) {
      if (forceCache)
        memcacheService.put("feedItems:cat:" + Util.categories[i], new HashMap());
      scheduleFeedCacher("cat", Util.categories[i], new Integer[] { Integer.valueOf(0) }, null, forceCache);
    }
  }
  
}