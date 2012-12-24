package ch.plus8.hikr.gappserver.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxAccount;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxContent;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class DropboxSyncher extends HttpServlet {

	private static final Logger logger = Logger.getLogger(DropboxSyncher.class.getName());
	
	public final static String APP_KEY = "i9zcbjp57s5wm0w";
	public final static String APP_SECRET = "fmzbmikip81rj4s";
	
	public final static String DROPBOXUSER_KIND = "dropbox:user";
	
	
	final static String PARAM_OAUTHSEQUENCE = "oauthsequence";
	final static String PARAM_CREATE_TOKEN = "createToken";
	final static String PARAM_CREATE_ALBUM = "createAlbum";
	final static String PARAM_CREATE_ALBUM_OFFSET = "offset";
	static final String PARAM_CREATE_ALL_ALBUMS = "createAllAlbums";
	final static String PARAM_IMPORT_IMAGE = "importImage";
	final static String PARAM_ADD_USER = "addUser";
	private final static int RETRIVE_COUNT = 100;
	
	private GAEFeedRepository feedRepository;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		this.feedRepository = feedRepository;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserUtils.init(req);
		
		try {
			
			OAuthProvider provider = new DefaultOAuthProvider("https://api.dropbox.com/1/oauth/request_token", "https://api.dropbox.com/1/oauth/access_token", "https://www.dropbox.com/1/oauth/authorize");
			
			if ("1".equals(req.getParameter(PARAM_ADD_USER))) {
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				Entity dropboxUserEntity = new Entity(KeyFactory.createKey(
						UserUtils.getCurrentKeyFor(),
						DROPBOXUSER_KIND, req.getParameter("uid")));
				
				dropboxUserEntity.setProperty("dropboxUid", req.getParameter("uid"));
				dropboxUserEntity.setProperty("token", req.getParameter("token"));
				dropboxUserEntity.setProperty("tokenSecret", req.getParameter("tokenSecret"));
				datastoreService.put(dropboxUserEntity);
				
			} if ("1".equals(req.getParameter(PARAM_CREATE_TOKEN))) {
				OAuthConsumer consumer = new DefaultOAuthConsumer(APP_KEY, APP_SECRET);
				
				String oauthsequence = UUID.randomUUID().toString();
				String authUrl = provider.retrieveRequestToken(consumer, "http://photo.plus8.ch/dropbox/dropboxSyncher?"+PARAM_OAUTHSEQUENCE+"="+oauthsequence);
				if(!Util.isProductionServer())
					authUrl = provider.retrieveRequestToken(consumer, "http://localhost:8888/dropbox/dropboxSyncher?"+PARAM_OAUTHSEQUENCE+"="+oauthsequence);
				
				Map<String, Object> sequenceParams = new HashMap<String, Object>();
				sequenceParams.put("consumer", consumer);
				sequenceParams.put("email", UserServiceFactory.getUserService().getCurrentUser().getEmail());
				MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
				memcacheService.put(PARAM_OAUTHSEQUENCE+":"+oauthsequence, sequenceParams);
				
				resp.getWriter().print("<script type=\"text/javascript\">window.location = \""+authUrl+"\";</script>");
			} else if (req.getParameter(PARAM_OAUTHSEQUENCE) != null) {
				
				MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
				Map<String, Object> sequenceParams = (Map<String, Object>)memcacheService.get(PARAM_OAUTHSEQUENCE+":"+req.getParameter(PARAM_OAUTHSEQUENCE));
				OAuthConsumer consumer = (OAuthConsumer)sequenceParams.get("consumer");
				
				provider.retrieveAccessToken(consumer, null);
				
				Entity userEntity = UserUtils.getCurrentEntityFor();
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				Entity dropboxUserEntity = new Entity(KeyFactory.createKey(userEntity.getKey(), DROPBOXUSER_KIND, req.getParameter("uid")));
				dropboxUserEntity.setProperty("dropboxUid", req.getParameter("uid"));
				dropboxUserEntity.setProperty("token", consumer.getToken());
				dropboxUserEntity.setProperty("tokenSecret", consumer.getTokenSecret());
				datastoreService.put(dropboxUserEntity);
				
								
				resp.getWriter().print("<script type=\"text/javascript\">window.opener.App.datastoresController.load();window.close();</script>");
			} else if("1".equals(req.getParameter(PARAM_CREATE_ALBUM))) {
				String dropboxUid = req.getParameter("dropboxUid");
				String path = req.getParameter("path"); //public-upload/paris
				
				
				Key dropboxKey = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), DROPBOXUSER_KIND, dropboxUid);
				
				DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
				
				Entity gallery = feedRepository.findGalleryByRef(datastoreService, dropboxKey.getKind(), path, UserUtils.getCurrentKeyFor());
				if(gallery == null) {
					gallery = new Entity(KeyFactory.createKey(UserUtils.getCurrentKeyFor(), GAEFeedRepository.USER_GALLERY_KIND, UUID.randomUUID().toString()));
					gallery.setProperty("kind", dropboxKey.getKind());
					gallery.setProperty("key", dropboxKey);
					gallery.setProperty("ref", path);
					gallery.setUnindexedProperty("title", req.getParameter("title"));
					gallery.setUnindexedProperty("desc", new Text(req.getParameter("desc")));
					datastoreService.put(gallery);
				}
				
				DropboxAPI dropboxAPI = DropboxUtil.createDropboxApi(UserUtils.getCurrentKeyFor(), dropboxUid);
				DropboxAccount accountInfo = dropboxAPI.accountInfo();
				DropboxEntity metadata = dropboxAPI.metadata(path);
				if(metadata == null ) 
					logger.log(Level.WARNING, "No data is imported because feed is empty: " + path);
				else {
					resp.getWriter().write(metadata.path);
					resp.getWriter().write("<br>");
					
					int offset = (req.getParameter(PARAM_CREATE_ALBUM_OFFSET) == null) ? 0 :  Integer.valueOf(req.getParameter(PARAM_CREATE_ALBUM_OFFSET));
					Set<String> presentKeys = new HashSet<String>();
					if(metadata.contents != null) {
						List<String> categories = new ArrayList<String>();
						String dropboxCat = gallery.getKey().getName(); 
						categories.add(dropboxCat);
						categories.add("dropbox-"+dropboxUid+"-"+path); //old style
						
						int cntIndex = -1;
						for(DropboxContent cnt : metadata.contents) {
							String id = dropboxUid+cnt.path;
							presentKeys.add(id);
							cntIndex++;
							
							if(cntIndex < offset) {
								continue;
							}
								
								
							if(cntIndex >= offset + RETRIVE_COUNT)
								continue;

							
							FeedItemBasic item = new FeedItemBasic();
							Map<String, Object> additional = new HashMap<String, Object>();
							String revision = cnt.rev;
							
							if(DropboxUtil.fillEntity(item, additional, accountInfo, metadata, cnt, req.getParameter("authorName"))) {
								if(!feedRepository.storeFeed(item, id, categories, Util.ITEM_STATUS_IMAGE_LINK_EVAL, additional, UserUtils.getCurrentKeyFor(), dropboxKey)) {
									Key key = GAEFeedRepository.createKey(UserUtils.getCurrentKeyFor(), id);
									Entity feEntity = datastoreService.get(key);
									if(!revision.equals(feEntity.getProperty(DropboxUtil.PROP_DROPBOX_REVISION))) {
										for(Entry<String, Object> en : additional.entrySet()) {
											feEntity.setUnindexedProperty(en.getKey(), en.getValue());
										}
										
										logger.info("Delete dropbox image because of wrong revision 1 ");
										feedRepository.deleteImagesFromEntityItem(feEntity, false, true);
										feEntity.setProperty("status",Util.ITEM_STATUS_IMAGE_LINK_EVAL);
										datastoreService.put(feEntity);
										
									}
									
									
								}
								presentKeys.add(id);
							}
						}
						
						if(cntIndex+1 < metadata.contents.size()) {
							Scheduler.scheduleDropboxGallery(
									dropboxUid,
									path,
									offset+RETRIVE_COUNT,
									req.getParameter("title"), 
									req.getParameter("desc"),
									req.getParameter("authorName"));
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
						query.setFilter(new Query.FilterPredicate("categories",FilterOperator.EQUAL, dropboxCat));
						PreparedQuery prepare = datastoreService.prepare(query);
						QueryResultIterable<Entity> results = prepare.asQueryResultIterable();
						for(Entity oldEntity : results) {
							if(!presentKeys.contains(oldEntity.getProperty("author").toString()+oldEntity.getProperty("link"))) {
								Scheduler.scheduleDeleteItem(KeyFactory.keyToString(oldEntity.getKey()), true, true, true);
							}
						}
						
						if(metadata.contents.size() == cntIndex) {
							Scheduler.scheduleImageEvaluator();
						}
					}
					
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
						Scheduler.addUserIfExists(param);
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
	
	

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
	
}
