package ch.plus8.hikr.gappserver.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.HTML;

import org.apache.commons.codec.net.URLCodec;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxAccount;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxContent;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.UserServiceFactory;

public class DropboxSyncher extends HttpServlet {

	private static final Logger logger = Logger.getLogger(DropboxSyncher.class.getName());
	
	public final static String APP_KEY = "i9zcbjp57s5wm0w";
	public final static String APP_SECRET = "fmzbmikip81rj4s";
	
	public final static String DROPBOXUSER_KIND = "dropbox:user";
	
	
	final static String PARAM_OAUTHSEQUENCE = "oauthsequence";
	final static String PARAM_CREATE_TOKEN = "createToken";
	final static String PARAM_CREATE_ALBUM = "createAlbum";
	static final String PARAM_CREATE_ALL_ALBUMS = "createAllAlbums";
	final static String PARAM_IMPORT_IMAGE = "importImage";
	final static String PARAM_ADD_USER = "addUser";

	
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
			
			if ("1".equals(req.getParameter(PARAM_ADD_USER))) {
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				Entity dropboxUserEntity = new Entity(KeyFactory.createKey(
						UserUtils.getCurrentKeyFor(),
						DROPBOXUSER_KIND, req.getParameter("uid")));
				
				dropboxUserEntity.setProperty("dropbboxUid", req.getParameter("uid"));
				dropboxUserEntity.setProperty("token", req.getParameter("token"));
				dropboxUserEntity.setProperty("tokenSecret", req.getParameter("tokenSecret"));
				datastoreService.put(dropboxUserEntity);
				
			} if ("1".equals(req.getParameter(PARAM_CREATE_TOKEN))) {
				OAuthConsumer consumer = new DefaultOAuthConsumer(APP_KEY, APP_SECRET);
				
				String oauthsequence = UUID.randomUUID().toString();
				String authUrl = provider.retrieveRequestToken(consumer, "http://photo.plus8.ch/dropbox/dropboxSyncher?"+PARAM_OAUTHSEQUENCE+"="+oauthsequence);
				
				Map<String, Object> sequenceParams = new HashMap<String, Object>();
				sequenceParams.put("consumer", consumer);
				sequenceParams.put("email", UserServiceFactory.getUserService().getCurrentUser().getEmail());
				MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
				memcacheService.put(PARAM_OAUTHSEQUENCE+":"+oauthsequence, sequenceParams);
				
						
				resp.getWriter().print("<a href=\""+authUrl+"\">"+authUrl+"</a>");
			} else if (req.getParameter(PARAM_OAUTHSEQUENCE) != null) {
				
				MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
				Map<String, Object> sequenceParams = (Map<String, Object>)memcacheService.get(PARAM_OAUTHSEQUENCE+":"+req.getParameter(PARAM_OAUTHSEQUENCE));
				OAuthConsumer consumer = (OAuthConsumer)sequenceParams.get("consumer");
				String email = (String)sequenceParams.get("email");
				
				provider.retrieveAccessToken(consumer, null);
				
				Entity userEntity = UserUtils.getCurrentEntityFor();
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				Entity dropboxUserEntity = new Entity(KeyFactory.createKey(userEntity.getKey(), DROPBOXUSER_KIND, req.getParameter("uid")));
				dropboxUserEntity.setProperty("dropbboxUid", req.getParameter("uid"));
				dropboxUserEntity.setProperty("token", consumer.getToken());
				dropboxUserEntity.setProperty("tokenSecret", consumer.getTokenSecret());
				datastoreService.put(dropboxUserEntity);
				
								
				resp.getWriter().write("dropbox user: " + req.getParameter("uid") + " added.");
			} else if("1".equals(req.getParameter(PARAM_CREATE_ALBUM))) {
				String dropboxUid = req.getParameter("dropboxUid");
				String path = req.getParameter("path"); //public-upload/paris
				String userKey = req.getParameter("userKey"); 
				Key uKey = null;
				if(userKey != null)
					uKey = KeyFactory.stringToKey(userKey);
				else
					uKey = UserUtils.getCurrentKeyFor();
				
				Key dropboxKey = KeyFactory.createKey(uKey, DROPBOXUSER_KIND, dropboxUid);
				
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				
				Entity gallery = findGalleryByRef(datastoreService, path, uKey);
				if(gallery == null) {
					gallery = new Entity(KeyFactory.createKey(uKey, GAEFeedRepository.USER_GALLERY_KIND, UUID.randomUUID().toString()));
					gallery.setProperty("kind", dropboxKey.getKind());
					gallery.setProperty("key", dropboxKey);
					gallery.setProperty("ref", path);
					datastoreService.put(gallery);
				}
				
				DropboxAPI dropboxAPI = DropboxUtil.createDropboxApi(uKey, dropboxUid);
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
						String dropboxCat = gallery.getKey().getName(); 
						categories.add(dropboxCat);
						categories.add("dropbox-"+dropboxUid+"-"+path); //old style
						
						for(DropboxContent cnt : metadata.contents) {
							FeedItemBasic item = new FeedItemBasic();
							Map<String, Object> additional = new HashMap<String, Object>();
							String revision = cnt.rev;
							if(DropboxUtil.fillEntity(item, additional, accountInfo, metadata, cnt, req.getParameter("authorName"))) {
								String id = dropboxUid+cnt.path;
								if(!feedRepository.storeFeed(item, id, categories, Util.ITEM_STATUS_IMAGE_LINK_EVAL, additional, uKey, dropboxKey)) {
									Key key = GAEFeedRepository.createKey(uKey, id);
									Entity feEntity = datastoreService.get(key);
									if(!revision.equals(feEntity.getProperty(DropboxUtil.PROP_DROPBOX_REVISION))) {
										for(Entry<String, Object> en : additional.entrySet()) {
											feEntity.setUnindexedProperty(en.getKey(), en.getValue());
										}
										
										feedRepository.deleteImagesFromEntityItem(feEntity, false, true);
										feEntity.setProperty("status",Util.ITEM_STATUS_IMAGE_LINK_EVAL);
										datastoreService.put(feEntity);
										
									}
									
									
								}
								presentKeys.add(id);
							}
						}
						
						/*Entity dropboxUserEntity = datastoreService.get(KeyFactory.createKey(DropboxSyncher.DROPBOXUSER_KIND, dropboxUid));
						List<String> albums = (List<String>)dropboxUserEntity.getProperty("albums");
						if(albums == null)
							albums = new ArrayList<String>();
						
						if(!albums.contains(path)) {
							albums.add(path);
							dropboxUserEntity.setUnindexedProperty("albums", albums);
							datastoreService.put(dropboxUserEntity);
						}
						*/
						
						Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
						query.addFilter("categories",FilterOperator.EQUAL, dropboxCat);
						PreparedQuery prepare = datastoreService.prepare(query);
						QueryResultIterable<Entity> results = prepare.asQueryResultIterable();
						for(Entity oldEntity : results) {
							if(!presentKeys.contains(oldEntity.getProperty("author").toString()+oldEntity.getProperty("link"))) {
								Scheduler.scheduleDeleteItem(KeyFactory.keyToString(oldEntity.getKey()), true, true, true);
							}
						}
						
					}
					
					Scheduler.scheduleImageEvaluator();
				}
			} /*else if(req.getParameter("addCategories") != null) {
				DatastoreService d = DatastoreServiceFactory.getDatastoreService();
				Key dropboxKey = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), DROPBOXUSER_KIND, "5031239");
				String[] categories = req.getParameter("addCategories").split(",");
				
				for(int i=0;i<categories.length; i++) {
					
					Key galleryKey = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), "user:gallery", UUID.randomUUID().toString());
					try {
						d.get(galleryKey);
					}catch(EntityNotFoundException ee) {
						
						Entity entity = new Entity(galleryKey);
						entity.setProperty("kind", dropboxKey.getKind());
						entity.setProperty("key", dropboxKey);
						entity.setUnindexedProperty("ref", categories[i]);
						d.put(entity);
					}
				}
				
			} */
			else if("1".equals(req.getParameter(PARAM_CREATE_ALL_ALBUMS))) {
				 Queue queue = QueueFactory.getDefaultQueue();
				
				DatastoreService d = DatastoreServiceFactory.getDatastoreService();
				Query q = new Query(GAEFeedRepository.USER_GALLERY_KIND);
				q.setAncestor(UserUtils.getCurrentKeyFor());
				PreparedQuery pq = d.prepare(q);
				QueryResultIterable<Entity> qr = pq.asQueryResultIterable(FetchOptions.Builder.withLimit(100));
				for(Entity o : qr) {
					
					if(o.getProperty("kind").equals(DROPBOXUSER_KIND)) {
						TaskOptions param = TaskOptions.Builder.withUrl("/dropbox/dropboxSyncher");
						param.param("createAlbum", String.valueOf(1));
						param.param("dropboxUid", ((Key)o.getProperty("key")).getName());
						param.param("path", o.getProperty("ref").toString());
						param.param("userKey", KeyFactory.keyToString(o.getParent()));
						queue.add(param);
						resp.getWriter().write(Util.stringToHTMLString(o.toString())+"<br><br>");
					}
				}
			
			} else {
				DatastoreService d = DatastoreServiceFactory.getDatastoreService();
				Query q = new Query(GAEFeedRepository.USER_GALLERY_KIND);
				PreparedQuery pq = d.prepare(q);
				QueryResultIterable<Entity> qr = pq.asQueryResultIterable(FetchOptions.Builder.withLimit(100));
				for(Entity o : qr) {
					o.setProperty("ref", o.getProperty("ref"));
					d.put(o);
					resp.getWriter().write(Util.stringToHTMLString(o.toString())+"<br><br>");
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error", e);
		}

	}
	
	protected Entity findGalleryByRef(DatastoreService ds, String ref, Key userKey) {
		Query q = new Query(GAEFeedRepository.USER_GALLERY_KIND);
		q.setAncestor(userKey);
		q.addFilter("kind", FilterOperator.EQUAL, DROPBOXUSER_KIND);
		q.addFilter("ref", FilterOperator.EQUAL, ref);
		PreparedQuery pq = ds.prepare(q);
		return pq.asSingleEntity();
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
	
}
