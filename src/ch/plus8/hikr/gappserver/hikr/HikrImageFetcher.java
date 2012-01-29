package ch.plus8.hikr.gappserver.hikr;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class HikrImageFetcher extends HttpServlet {

	private static final Logger logger = Logger.getLogger(HikrImageFetcher.class.getName());
	
	private static final int MAX_COUNT = 5;
	
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		logger.info("HikrImageFetcher called");
		
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		
		Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
		query.addFilter("source", FilterOperator.EQUAL, "hikr");
		query.addFilter("imageLinkA", FilterOperator.LESS_THAN_OR_EQUAL, 0);
		query.addFilter("imageLinkA", FilterOperator.GREATER_THAN, -5);
		
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		
		if(req.getParameter("cursor") != null) {
			try {
				fetchOptions = FetchOptions.Builder.withStartCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
				logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
			}catch(IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Could not validate cursor string",e);
				resp.getWriter().write("Could not validate cursor string");
				return;
			}
		}
		fetchOptions.limit(MAX_COUNT);
			
		PreparedQuery prepare = dataStore.prepare(query);
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		logger.info("Images to extract from hikr: " + resultList.size());
		for(Entity entity : resultList) {
			logger.log(Level.INFO, "Find image for: " + entity.getProperty("link"));
			try {
				
				if(entity.getProperty("img1") != null) {
					try {
						blobStoreService.delete((BlobKey)entity.getProperty("img1"));
					} catch(Exception e) {
						logger.log(Level.SEVERE, "Could not delete img1 for: " + entity.getKey(),e);
					}
					
				}
				
				HTTPResponse hikrPhotoResp = urlFetchService.fetch(new URL(entity.getProperty("link").toString()));
				String page = new String(hikrPhotoResp.getContent(),"ISO-8859-1");
				int startIndex = page.indexOf("r4zoomPhoto(e1,\"");
				boolean success = false;
				if(startIndex >0) {
					String bigImageUrl = page.substring(startIndex+16);
					int endIndex = bigImageUrl.indexOf("\")");
					if(endIndex >0) {						
						bigImageUrl = bigImageUrl.substring(0,endIndex);
						entity.setProperty("imageLink", bigImageUrl);
						entity.setProperty("imageLinkA", 1);
						dataStore.put(entity);
						success = true;
					}
				}
				if(!success) {
					logger.warning("Parse Image From: "+entity.getProperty("link").toString());
					entity.setProperty("imageLinkA", ((Long)entity.getProperty("imageLinkA")).longValue()-1);
					dataStore.put(entity);
				}
				
			} catch(Exception e) {
				entity.setProperty("imageLinkA", ((Long)entity.getProperty("imageLinkA")).longValue()-1);
				resp.getWriter().write("Could not process hikr image download: "+entity.getProperty("link").toString());
				logger.log(Level.SEVERE, "Could not process hikr image download: "+entity.getProperty("link").toString(), e);
			}
		}
		
		if(!resultList.isEmpty()) {
			Scheduler.scheduleHikrImageFetcher(resultList.getCursor().toWebSafeString());
		}
		else  {
			//Util.scheduleImageFetcer();
		}
		
		resp.getWriter().write("DONE");
		
		//URL url = new URL(productUrl);
		//HTTPRequest searchMethod = new HTTPRequest(url, HTTPMethod.GET);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
