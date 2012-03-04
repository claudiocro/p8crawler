package ch.plus8.hikr.gappserver.gplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.FeedItem;
import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.Plus.Activities.Search;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.api.services.plus.model.ActivityObjectAttachments;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

@SuppressWarnings("serial")
public class GPlusHashTagActivitiesImporter extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GPlusHashTagActivitiesImporter.class.getName());
	
	private GAEFeedRepository feedRepository;

	@Override
    public void init(ServletConfig config) throws ServletException {
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		this.feedRepository = feedRepository;
    }
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		
		String hashTag = req.getParameter("hashtag");
		if(hashTag == null) {
			logger.severe("no parameter hashtag defined");
			resp.getWriter().write("no parameter hashtag defined");
		}
		logger.info("Request import of gplus activities for: "+hashTag);
		
		String nextPageToken = req.getParameter("nextPage");
		
		int page = 0;
		if(req.getParameter("page") != null)
			page = Integer.valueOf(req.getParameter("page"));
		
		
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
			
		
		Plus plus = new Plus(new UrlFetchTransport(), new GsonFactory());
		plus.setKey(Util.GOOGLE_API_KEY);
		//Person person = plus.people.get("110416871235589164413").execute();
		Search search = plus.activities.search();
			
		search.setQuery("#"+hashTag);
		if(nextPageToken != null)
			search.setPageToken(nextPageToken);
		
		ActivityFeed feed = search.execute();
		
		
		if(feed.getItems() == null) {
			logger.warning("no feed for: " + "#"+hashTag);
			return;
		}
		
		
		for(Activity act :  feed.getItems()) {
			if(act.getPlusObject().getAttachments() == null) 
				continue;
			
			//only ONE!!!!!
			for(ActivityObjectAttachments att : act.getPlusObject().getAttachments()) {
				logger.fine("Parse attachement: " + att.getId());
				if(att.getUrl() == null || att.getUrl().length() == 0 ||  !"photo".equals(att.getObjectType())) {
					continue;
				}

				
				Key key = GAEFeedRepository.createKey(att.getUrl());
				Entity entity = null;
				try {
					entity = dataStore.get(key);
					feedRepository.addToCategories(key, entity, hashTag);
				} catch (EntityNotFoundException e) {
					try {
						FeedItemBasic item = new FeedItemBasic();
						List<String> cats = new ArrayList<String>();
						cats.add(hashTag);
						
						if(GPlusUtil.fillEntity(item,feed,act,att,750,750)) {
							feedRepository.storeFeed(item, cats);
						}
					}catch(Exception e1) {
						logger.log(Level.SEVERE, "could not store gplus feed: "+hashTag+" / "+att.getDisplayName()+" - "+att.getUrl() + " / " + feed.getSelfLink(),e1);
					}
				}
				
				//Only one attachement
				break;
			}
		}
		
		String nextPage = feed.getNextPageToken();
		if(page<=20 && nextPage != null)
			Scheduler.scheduleGPlusHashTagActivity(hashTag, page+1, nextPage);
		//else
		//	Util.scheduleImageFetcer();
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
