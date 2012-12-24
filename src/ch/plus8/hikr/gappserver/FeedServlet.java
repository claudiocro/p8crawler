package ch.plus8.hikr.gappserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class FeedServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(FeedServlet.class.getName());
	
	private static final int MAX_COUNT = 40;
	public static final String MEM_FEED_PAGE_PREFIX = "feedItems:";
	
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	
		logger.info("/feed called");
		
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService("feedItems");
		
		String source = req.getParameter("source");
		if("".equals(source))
			source = null;
		
		String cat = req.getParameter("cat");
		if("".equals(cat))
			cat = null;
		
		if(cat == null && source == null)
			cat = "photographer";
		
		int page = -1;
		if(req.getParameter("page") != null) {
			try {
				page = Integer.valueOf(req.getParameter("page"));
			}catch(IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Could not page parameter",e);
				return;
			}
		}
		
		boolean cacheRequest = false;
		if("1".equals(req.getParameter("cache-request")))
			cacheRequest = true;
		
		boolean nocache = false;
		if("1".equals(req.getParameter("nocache")))
			nocache = true;
		
		String sortCol = "storeDate";
		if("publishedDate".equals(req.getParameter("sort")))
			sortCol = "publishedDate";
		
		
		int memCachePage = (page==-1)?0:page;
		String memcacheKey = null;
		
		if(source != null)
			memcacheKey = MEM_FEED_PAGE_PREFIX+"source:"+source;
		else if(cat != null)
			memcacheKey = MEM_FEED_PAGE_PREFIX+"cat:"+cat;
		
		memcacheKey += ":"+sortCol;
		
		if(!nocache) {
			String cachedFeed = getCachedFeed(memcacheService, memcacheKey, memCachePage);
			if(/*req.getParameter("cursor") == null && */cachedFeed != null) {
				resp.setContentType("application/json");
				
				logger.info("Returned from memcache: "+memcacheKey + " / "+memCachePage);
				
				
				
				resp.getWriter().write(cachedFeed);
				return;
			}
		}
			
		PreparedQuery prepare = null;
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(MAX_COUNT);
		fetchOptions.prefetchSize(MAX_COUNT);
		//if(req.getParameter("type") == null) {
		//	logger.fine("FeedServlet with no type called");
			
		List<Filter> filters = new ArrayList<Filter>();
		Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
		if(cat != null)
			filters.add(new Query.FilterPredicate("categories", FilterOperator.EQUAL, cat));
		else if(source != null)
			filters.add(new Query.FilterPredicate("source", FilterOperator.EQUAL, source));
		
		
		filters.add(new Query.FilterPredicate("status", FilterOperator.EQUAL, Util.ITEM_STATUS_READY));
		
		query.setFilter(CompositeFilterOperator.and(filters));
		query.addSort(sortCol, SortDirection.DESCENDING);
		
		if(req.getParameter("cursor") != null) {
			try {
				fetchOptions.startCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
				logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
			}catch(IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Could not validate cursor string",e);
				resp.getWriter().write("Could not validate cursor string");
				return;
			}
		} 
		
		if(fetchOptions.getStartCursor() == null && page > 0)
			fetchOptions.offset(page*MAX_COUNT);
		
		prepare = dataStore.prepare(query);
		
		
		List<FeedItem> items = new ArrayList<FeedItem>();
		String cursor = iterateItems(dataStore, prepare, fetchOptions, items);
	
		logger.fine("FeedServlet returns json feed total: " + items.size());
		resp.setContentType("application/json");
		
		PagedResponse response = new PagedResponse();
		response.response = items;
		response.cursor = cursor;
		response.page = page;
		
		Gson gson = new Gson();
		String json = gson.toJson(response);
		
		if(page != -1) {
			response.cursor = null;
			logger.info("put records to memcache: " + memcacheKey);
			
			String param = null;
			String paramV = null;
			if(cat != null) {
				param="cat";
				paramV=cat;
			} else if(source != null) {
				param="source";
				paramV=source;
			}
			
			
			storeCachedPage(memcacheService, memcacheKey, memCachePage, sortCol, gson.toJson(response), param, paramV,!cacheRequest, nocache);
		}
		logger.info("Returned from datastore for page "+memCachePage);
		if(req.getParameter("callback") != null) {
			logger.info("JSONP");
			resp.getWriter().write(req.getParameter("callback")+"("+json+")");
		} else
			resp.getWriter().write(json);
	}
	
	
	@SuppressWarnings("rawtypes")
	private String getCachedFeed(MemcacheService memcacheService, String memcacheKey, int page) {
		Map cFeed =((Map)memcacheService.get(memcacheKey));
		if(cFeed != null && cFeed.containsKey(page)) {
			return (String)cFeed.get(page);
		}
		
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private void storeCachedPage(MemcacheService memcacheService, String memcacheKey, int page, String sortCol, String feed, String param, String paramV, boolean preCache, boolean forceCache) {
		Map map = (Map)memcacheService.get(memcacheKey);
		if(map == null)
			map = new HashMap();
		
		logger.info("put to cache: "+memcacheKey+" / "+page+" / precache:"+preCache + "/ forceCache:" + forceCache);
		memcacheService.put(memcacheKey, map,Expiration.byDeltaSeconds(10800));

		if(preCache) {
			List<Integer> pages = new ArrayList<Integer>(3);
			for(int i=0;i<3; i++) {
				if(!map.containsKey(page+i+1)){
					pages.add(page+i+1);
				}
				
			}
			Scheduler.scheduleFeedCacher(param, paramV, pages.toArray(new Integer[pages.size()]), sortCol, forceCache);
		}
		
	}
	
	protected String iterateItems(DatastoreService datastoreService, PreparedQuery prepare, FetchOptions fetchOptions, List<FeedItem> items) {
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		for(Entity entity : resultList) {
			try {
				FeedItem feedItem = FeedItem.createFromEntity(entity);
	
				if(feedItem.img2Link == null) { //TODO: only for img2 that resides in the appengine blobstore
					feedItem.img2Link =  imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey((BlobKey)entity.getProperty("img2")));
					entity.setUnindexedProperty("img2Link", feedItem.img2Link);
					datastoreService.put(entity);
				}
				
					
				items.add(feedItem);
			} catch( Exception e) {
				logger.log(Level.SEVERE, "Could not deserialize feed entity: "+entity.getKey(), e);
			}
		}
		
		if(!resultList.isEmpty()) {
			return resultList.getCursor().toWebSafeString();
		}
		
		return null;
		
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
