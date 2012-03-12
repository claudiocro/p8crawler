package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.DropboxAPI;
import ch.plus8.hikr.gappserver.dropbox.DropboxUtil;
import ch.plus8.hikr.gappserver.hikr.HikrImageFetcher;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

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
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class ImageEvaluator extends HttpServlet {

	private static final Logger logger = Logger.getLogger(ImageEvaluator.class.getName());
	
	private static final int MAX_COUNT = 5;
	
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		logger.info("ImageEvaluator called");
		
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		FileService fileService = FileServiceFactory.getFileService();
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		
		Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
		query.addFilter("status", FilterOperator.GREATER_THAN_OR_EQUAL, Util.ITEM_STATUS_IMAGE_LINK_EVAL);
		query.addFilter("status", FilterOperator.LESS_THAN, Util.ITEM_STATUS_IMAGE_LINK_EVAL+5);
		
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
			
		
		HashMap<String, DropboxAPI> dropboxApiCache = new HashMap<String, DropboxAPI>();
		
		PreparedQuery prepare = dataStore.prepare(query);
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		logger.info("Images to extract: " + resultList.size());
		for(Entity entity : resultList) {
			logger.log(Level.INFO, "Find image for: " + entity.getProperty("link"));
			try {
				
				
				if("hikr".equals(entity.getProperty("source"))) {
					String imageLink = HikrImageFetcher.evalImageFrom(urlFetchService, entity.getProperty("link").toString());
					if(imageLink != null) {
						feedRepository.updateImageLinkAndProcess(entity, imageLink, true);
					}
					else {
						feedRepository.increaseStatus(entity, true);
					}
				} else if("dropbox".equals(entity.getProperty("source"))) {
					String dropboxUid = entity.getProperty("author").toString();
					DropboxAPI dropboxAPI = dropboxApiCache.get(dropboxUid);
					if(dropboxAPI == null) {
						dropboxAPI = DropboxUtil.createDropboxApi(dropboxUid);
						dropboxApiCache.put(dropboxUid, dropboxAPI);
					}
					String link = entity.getProperty("link").toString();
					logger.info("link:"+link + " / " + link.toLowerCase().startsWith("/public/"));
					if(link.toLowerCase().startsWith("/public/")) {
						entity.setProperty("dropboxThumb", 1);
						feedRepository.updateImageLinkAndProcess(entity, dropboxAPI.media(link).url, true);
					}
					else if(DropboxUtil.createImg1(entity, dropboxAPI, urlFetchService, fileService, blobStoreService, imagesService)) {
						feedRepository.setStatus(entity, Util.ITEM_STATUS_IMAGE_LINK_PROCESS, true);
					} else {
						feedRepository.increaseStatus(entity, true);
					}
					
				} else {
					logger.severe("NO IMAGE PROCESSOR FOUND: "+entity.getProperty("source"));
					feedRepository.setStatus(entity, Util.ITEM_STATUS_IMAGE_LINK_NO_EVAL_PROC, true);
				}
				
				
			} catch(Exception e) {
				resp.getWriter().write("Could not process hikr image download: "+entity.getProperty("link").toString());
				logger.log(Level.SEVERE, "Could not process hikr image download: "+entity.getProperty("link").toString(), e);
				feedRepository.increaseStatus(entity, true);
			}
		}
		
		if(!resultList.isEmpty()) {
			Scheduler.scheduleImageEvaluator(resultList.getCursor().toWebSafeString());
		}
		else  {
			Scheduler.scheduleImageFetcher();
		}
		
		resp.getWriter().write("DONE");
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
