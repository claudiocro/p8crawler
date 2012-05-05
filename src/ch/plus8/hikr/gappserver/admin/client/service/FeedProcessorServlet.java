package ch.plus8.hikr.gappserver.admin.client.service;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Datastore;
import ch.plus8.hikr.gappserver.DatastoreFactory;
import ch.plus8.hikr.gappserver.FeedItem;
import ch.plus8.hikr.gappserver.ImageEvaluatorFactory;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class FeedProcessorServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(FeedProcessorServlet.class.getName());
	private GAEFeedRepository feedRepository;
	private URLFetchService urlFetchService;
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		logger.info("ImageEvaluator called");
		
		String link = (String)req.getParameter("key");
		
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		urlFetchService = URLFetchServiceFactory.getURLFetchService();
		
		feedRepository = new GAEFeedRepository();
		feedRepository.init();
		
		
		Key key = GAEFeedRepository.createKey(UserUtils.getCurrentKeyFor(), link);
		try {
			Entity entity = dataStore.get(key);
			long status = ((Long)entity.getProperty("status")).longValue();
			if((status >= Util.ITEM_STATUS_IMAGE_LINK_EVAL && status <= Util.ITEM_STATUS_IMAGE_LINK_EVAL+10) || 
					status == Util.ITEM_STATUS_IMAGE_LINK_NO_EVAL_PROC) {
				try {
					evaluateImageLink(entity);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not evaluate image download: "+link, e);
				}
			}
			
			entity = dataStore.get(key);
			status = (Long)entity.getProperty("status");
			if(status >= Util.ITEM_STATUS_IMAGE_LINK_PROCESS && status <= Util.ITEM_STATUS_IMAGE_LINK_PROCESS+10) {
				try {
					processImageLink(entity);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Could not process image link: "+link, e);
					entity.setProperty("status", Util.ITEM_STATUS_IMAGE_LINK_PROCESS);
					dataStore.put(entity);
				}
			}
			
			entity = dataStore.get(key);
			FeedItem feedItem = FeedItem.createFromEntity(entity);
			
			Gson gson = new Gson();
			String json = gson.toJson(feedItem);

			resp.setContentType("application/json");
			resp.getWriter().write(json);
			
		} catch (EntityNotFoundException e) {
			logger.log(Level.SEVERE, "could not find entity for:"+link);
		}
	
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}	

	
	protected boolean evaluateImageLink(Entity entity) throws Exception {
		ch.plus8.hikr.gappserver.ImageEvaluator imageEvaluator = ImageEvaluatorFactory.createImageEvaluator((String)entity.getProperty("source"), entity);
		return imageEvaluator.evaluate(feedRepository, entity);
	}
	
	protected boolean processImageLink(Entity entity) throws Exception {
		String bigImageUrl = entity.getProperty("imageLink").toString();

		HTTPResponse bigImageResp = urlFetchService.fetch(new URL(bigImageUrl));
		Image orgImageB = ImagesServiceFactory.makeImage(bigImageResp.getContent());
		
		Datastore datastore = DatastoreFactory.createDatastore(new Long((Long)entity.getProperty("img2A")*-1), entity);
		if(datastore == null) {
			throw new IllegalArgumentException("No processor for image upload found:" +entity.getProperty("img2A"));
		}
		
		return datastore.createImage2(entity, orgImageB);
	}
}
