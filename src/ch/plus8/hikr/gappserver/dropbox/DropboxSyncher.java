package ch.plus8.hikr.gappserver.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxAccount;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxContent;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.gplus.GPlusPersonImporterServlet;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class DropboxSyncher extends HttpServlet {

	private static final Logger logger = Logger.getLogger(DropboxSyncher.class.getName());
	
	public final static String APP_KEY = "i9zcbjp57s5wm0w";
	public final static String APP_SECRET = "fmzbmikip81rj4s";
	
	public final static String DROPBOXUSER_KIND = "dropbox:user";
	
	
	final static String PARAM_OAUTHSEQUENCE = "oauthsequence";
	final static String PARAM_CREATE_TOKEN = "createToken";
	final static String PARAM_CREATE_ALBUM = "createAlbum";
	final static String PARAM_IMPORT_IMAGE = "importImage";
	
	private final JsonHttpParser parser = JsonHttpParser.builder(new GsonFactory()).setContentType("text/javascript").build();
	
	private GAEFeedRepository feedRepository;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		this.feedRepository = feedRepository;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			OAuthProvider provider = new DefaultOAuthProvider("https://api.dropbox.com/1/oauth/request_token", "https://api.dropbox.com/1/oauth/access_token", "https://www.dropbox.com/1/oauth/authorize");
			
			if ("1".equals(req.getParameter(PARAM_CREATE_TOKEN))) {
				OAuthConsumer consumer = new DefaultOAuthConsumer(APP_KEY, APP_SECRET);
				
				String oauthsequence = UUID.randomUUID().toString();
				String authUrl = provider.retrieveRequestToken(consumer, "http://photo.plus8.ch/dropbox/dropboxSyncher?"+PARAM_OAUTHSEQUENCE+"="+oauthsequence);
				MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
				memcacheService.put(PARAM_OAUTHSEQUENCE+":"+oauthsequence, consumer);
				
						
				resp.getWriter().print("<a href=\""+authUrl+"\">"+authUrl+"</a>");
			} else if (req.getParameter(PARAM_OAUTHSEQUENCE) != null) {
				
				MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
				OAuthConsumer consumer = (OAuthConsumer)memcacheService.get(PARAM_OAUTHSEQUENCE+":"+req.getParameter(PARAM_OAUTHSEQUENCE));
				
				provider.retrieveAccessToken(consumer, null);
				
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				Entity dropboxUserEntity = new Entity(KeyFactory.createKey(DROPBOXUSER_KIND, req.getParameter("uid")));
				dropboxUserEntity.setProperty("dropbboxUid", req.getParameter("uid"));
				dropboxUserEntity.setProperty("token", consumer.getToken());
				dropboxUserEntity.setProperty("tokenSecret", consumer.getTokenSecret());
				datastoreService.put(dropboxUserEntity);
				
				resp.getWriter().write("dropbox user: " + req.getParameter("uid") + " added.");
			} else if("1".equals(req.getParameter(PARAM_CREATE_ALBUM))) {
				String dropboxUid = req.getParameter("dropboxUid");
				String path = req.getParameter("path"); //public-upload/paris

				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				DropboxAPI dropboxAPI = DropboxUtil.createDropboxApi(dropboxUid);
				
				DropboxAccount accountInfo = dropboxAPI.accountInfo();
				DropboxEntity metadata = dropboxAPI.metadata(path);
				if(metadata == null ) 
					logger.log(Level.WARNING, "No data is imported because feed is empty: " + path);
				else {
					resp.getWriter().write(metadata.path);
					resp.getWriter().write("<br>");
					
					Set<String> presentKeys = new HashSet<String>();
					if(metadata.contents != null) {
						List<String> categories = new ArrayList<String>();
						categories.add("dropbox-"+dropboxUid+"-"+path);
						
						for(DropboxContent cnt : metadata.contents) {
							FeedItemBasic item = new FeedItemBasic();
							Map<String, Object> additional = new HashMap<String, Object>();
							if(DropboxUtil.fillEntity(item, additional, accountInfo, metadata, cnt)) {
								feedRepository.storeFeed(item, dropboxUid+cnt.path, categories, Util.ITEM_STATUS_IMAGE_LINK_EVAL, additional);
								presentKeys.add(cnt.path+"::"+cnt.rev);
							}
						}
						
						Entity dropboxUserEntity = datastoreService.get(KeyFactory.createKey(DropboxSyncher.DROPBOXUSER_KIND, dropboxUid));
						List<String> albums = (List<String>)dropboxUserEntity.getProperty("albums");
						if(albums == null)
							albums = new ArrayList<String>();
						
						if(!albums.contains(path)) {
							albums.add(path);
							dropboxUserEntity.setUnindexedProperty("albums", albums);
							datastoreService.put(dropboxUserEntity);
						}
						
						
						Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
						query.addFilter("categories",FilterOperator.EQUAL, "dropbox-"+dropboxUid+"-"+path);
						PreparedQuery prepare = datastoreService.prepare(query);
						QueryResultIterable<Entity> results = prepare.asQueryResultIterable();
						for(Entity oldEntity : results) {
							if(!presentKeys.contains(oldEntity.getProperty("link")+"::"+oldEntity.getProperty("dropboxRev"))) {
								Scheduler.scheduleDeleteItem(oldEntity.getKey().getName(), true);
							}
						}
						
					}
					
					Scheduler.scheduleImageEvaluator();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error", e);
		}

	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
	
}
