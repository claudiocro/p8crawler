package ch.plus8.hikr.gappserver.gplus;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.Activity.PlusObject.Attachments;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;

@SuppressWarnings("serial")
public class GPlusPersonsActivitiesImporterServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GPlusPersonsActivitiesImporterServlet.class.getName());

	private static final int MAX_COUNT = 10;

	private GAEFeedRepository feedRepository;
	
	
	@Override
    public void init(ServletConfig config) throws ServletException {
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		this.feedRepository = feedRepository;
    }
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.info("Request import of gplus activities cursor: " + req.getParameter("cursor"));
		
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(GPlusUtil.PERSON_KIND);
		
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
		for(Entity personEntity : resultList) {
			Plus plus = new Plus(new UrlFetchTransport(), new GsonFactory());
			Plus.Activities.List activities = plus.activities().list((String)personEntity.getProperty("id"), "public");
			activities.setKey(Util.GOOGLE_API_KEY);
			//Person person = plus.people.get("110416871235589164413").execute();
			ActivityFeed feed = activities.execute();
			
			
			if(feed.getItems() == null) {
				logger.warning("no feed for: " + personEntity.getProperty("id"));
				return;
			}
			
			
			for(Activity act :  feed.getItems()) {
				if(act.getObject().getAttachments() == null) 
					continue;
				
				for(Attachments att : act.getObject().getAttachments()) {
					logger.fine("Parse attachement: " + att.getId());
					if(att.getUrl() == null || att.getUrl().length() == 0 ||  !"photo".equals(att.getObjectType())) {
						continue;
					}

					
					Key key = GAEFeedRepository.createKey(att.getUrl());
					Entity entity = null;
					try {
						entity = dataStore.get(key);
						feedRepository.updateCategories(key, entity, (List<String>)personEntity.getProperty("categories"));
					} catch (EntityNotFoundException e) {
						try {
							FeedItemBasic item = new FeedItemBasic();
							if(GPlusUtil.fillEntity(item,feed,act,att)) {
								feedRepository.storeFeed(item, (List<String>)personEntity.getProperty("categories"));
							}
						}catch(Exception e1) {
							logger.log(Level.SEVERE, "could not store gplus feed: " + personEntity.getProperty("id") + " / "+att.getDisplayName(),e1);
						}
					}
				}
			}
		}
		
		if(!resultList.isEmpty()) {
			Scheduler.scheduleGplusPersonsActivitiesImport(resultList.getCursor().toWebSafeString());
		}else {
			//logger.info("try to schedule image fetcher");
			//Util.scheduleImageFetcer();
		}
		
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
