package ch.plus8.hikr.gappserver;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class Scheduler
{
  private static final Logger logger = Logger.getLogger(Scheduler.class.getName());

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

  public static final void scheduleImageFetcher() {
    scheduleImageFetcher(null);
  }

  public static final void scheduleImageFetcher(String cursor) {
    logger.info("Schedule: /p8admin/fetchImage: " + cursor);
    TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/fetchImage");

    if (cursor != null) {
      param.param("cursor", cursor);
    }
    Queue queue = QueueFactory.getQueue("fetchImage-queue");
    queue.add(param);
  }

  public static final void scheduleDeleteItem(String key, boolean forceDelete)
  {
    logger.info("Schedule: /p8admin/fetchImage: " + key);
    TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/deleteItem");
    param.param("key", key);
    if(forceDelete)
    	param.param("delete", "1");

    Queue queue = QueueFactory.getQueue("deleteItem-queue");
    queue.add(param);
  }

  public static void scheduleDeleteOldFeedItems(String cursor, String source, String cat, String timeType, String timeValue, boolean forceDelete)
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

    if (cursor != null) {
      param.param("cursor", cursor);
    }
    Queue queue = QueueFactory.getDefaultQueue();
    queue.add(param);
  }

  public static void scheduleFeedCacher(String param, String paramV, Integer[] page, boolean forceCache) {
    Queue queue = QueueFactory.getQueue("feedCacher-queue");
    for (int i = 0; i < page.length; i++) {
      logger.info("Schedule: /feed:" + param + " / " + paramV + " / " + page[i]);

      TaskOptions options = 
        TaskOptions.Builder.withUrl("/feed")
        .param("nocache", forceCache ? "1" : "0")
        .param("cache-request", "1")
        .param(param, paramV)
        .param("page", String.valueOf(page[i]));

      queue.add(options);
    }
  }

  public static void cleanCache(String param, String paramV)
  {
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService("feedItems");
    logger.info("Clean memcache: feedItems:" + param + ":" + paramV);
    memcacheService.put("feedItems:" + param + ":" + paramV, new HashMap());
  }

  public static void scheduleFeedCacher(boolean forceCache) {
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService("feedItems");

    Queue queue = QueueFactory.getQueue("feedCacher-queue");

    if (forceCache) {
      logger.info("scheduleFeedCacher with force cache remove");
    }

    for (int i = 0; i < Util.categories.length; i++) {
      if (forceCache)
        memcacheService.put("feedItems:cat:" + Util.categories[i], new HashMap());
      scheduleFeedCacher("cat", Util.categories[i], new Integer[] { Integer.valueOf(0) }, forceCache);
    }
  }

  
}