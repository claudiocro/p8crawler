package ch.plus8.hikr.gappserver.googledrive;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.auth.AbstractAuthorizationCodeServlet;
import ch.plus8.hikr.gappserver.google.P8CredentialStore;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.ParentsCollection;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.util.ServiceException;

@SuppressWarnings("serial")
public class GDriveGalleryServlet extends AbstractAuthorizationCodeServlet {

	public static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive.file",
			"https://docs.google.com/feeds/",
			"https://docs.googleusercontent.com/", 
			"https://www.googleapis.com/auth/userinfo.profile");
	
	
	public final static String GDRIVEUSER_KIND = "gdrive:user";


	private String googleUid;


	private GDriveApi gDriveApi;


	@Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			GAEFeedRepository feedRepository = new GAEFeedRepository();
			feedRepository.init();
			
			/*DocsService docs = new DocsService("photography-stream/1.1.1");
			docs.setOAuth2Credentials(getCredential());
			
			Drive drive = Drive.builder(new UrlFetchTransport(), new GsonFactory()).
				setHttpRequestInitializer(getCredential()).
				setApplicationName("photography-stream/1.1.1").
			build();
			*/
			String googleUid = request.getParameter("googleUid");
			String path = request.getParameter("path"); //public-upload/paris
			String userKey = request.getParameter("userKey");
			String title = request.getParameter("title");
			String desc = request.getParameter("desc");
			
			gDriveApi = new GDriveApi(googleUid);
			
			Key uKey = null;
			if(userKey != null)
				uKey = KeyFactory.stringToKey(userKey);
			else
				uKey = UserUtils.getCurrentKeyFor();
			
			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
			
			Key credentialsKey = KeyFactory.createKey(uKey, P8CredentialStore.KIND, googleUid);
			
			Entity gallery = feedRepository.findGalleryByRef(datastoreService, credentialsKey.getKind(), path, uKey);
			if(gallery == null) {
				gallery = new Entity(KeyFactory.createKey(uKey, GAEFeedRepository.USER_GALLERY_KIND, UUID.randomUUID().toString()));
				gallery.setProperty("kind", credentialsKey.getKind());
				gallery.setProperty("key", credentialsKey);
				gallery.setProperty("ref", path);
				gallery.setUnindexedProperty("title", title);
				if(desc != null)
					gallery.setUnindexedProperty("desc", new Text(desc));
				datastoreService.put(gallery);
			}

			Set<String> presentKeys = new HashSet<String>();
			List<String> categories = new ArrayList<String>();
			String catkey = gallery.getKey().getName(); 
			categories.add(catkey);
			
			DocumentListEntry imageFolder = getFolderByPath(path);
			DocumentListFeed entries = gDriveApi.getContentsFromEntry(imageFolder.getDocId());
			String thumbsFolderId = getOrCreateThumbsFolder(imageFolder, path+"/thumbs");
			System.out.println("-------------------------------------------------_");
			for(DocumentListEntry entry  : entries.getEntries()) {
				FeedItemBasic item = new FeedItemBasic();
				Map<String, Object> additional = new HashMap<String, Object>();
				
				File file = gDriveApi.getGFileById(entry.getDocId());
				
				String revision = file.getEtag();
				if(GDriveUtil.fillEntity(item, additional, entry, file, credentialsKey, thumbsFolderId)) {

					if(!feedRepository.storeFeed(item, file.getId(), categories, Util.ITEM_STATUS_IMAGE_LINK_EVAL, additional, uKey, credentialsKey)) {
						Key key = GAEFeedRepository.createKey(uKey, file.getId());
						Entity feEntity = datastoreService.get(key);
						if(!revision.equals(feEntity.getProperty(GDriveUtil.PROP_GDRIVE_REVISION))) {
							for(Entry<String, Object> en : additional.entrySet()) {
								feEntity.setUnindexedProperty(en.getKey(), en.getValue());
							}
							
							feedRepository.deleteImagesFromEntityItem(feEntity, false, true);
							feEntity.setProperty("status",Util.ITEM_STATUS_IMAGE_LINK_EVAL);
							datastoreService.put(feEntity);
						}
					}
					presentKeys.add(file.getId());
				}
			}
			
			Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
			query.setAncestor(uKey);
			query.setFilter(new Query.FilterPredicate("categories",FilterOperator.EQUAL, catkey));
			PreparedQuery prepare = datastoreService.prepare(query);
			QueryResultIterable<Entity> results = prepare.asQueryResultIterable();
			for(Entity oldEntity : results) {
				if(!presentKeys.contains(oldEntity.getKey().getName())) {
					Scheduler.scheduleDeleteItem(KeyFactory.keyToString(oldEntity.getKey()), true, true, true);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }
	
	protected String getOrCreateThumbsFolder(DocumentListEntry imageFolder, String path) throws IOException, ServiceException {
		DocumentListEntry thumbFolder = getFolderByPath(path);
		if(thumbFolder == null) {
			String title;
			if(path.indexOf("/")>-1)
				title = path.substring(path.lastIndexOf("/")+1);
			else
				title = path;
			
			return gDriveApi.createFolder(imageFolder.getDocId(), title).getDocId();
			
			
			/*File file = new File();
			file.setMimeType(GDriveDatastore.MIME_FOLDER);
			file.setTitle(path.substring(path.lastIndexOf("/")+1));
			
			ParentsCollection parent = new ParentsCollection();
			parent.setId(imageFolder.getDocId());
			List<ParentsCollection> parents = new ArrayList<File.ParentsCollection>();
			parents.add(parent);
			file.setParentsCollection(parents);
			
			return drive.files().insert(file).execute().getId();
			*/
		} else {
			return thumbFolder.getDocId();
		}
			
	}

	
	protected DocumentListEntry getFolderByPath(String path) throws IOException, ServiceException {
		if(!path.startsWith("/"))
			path = "/"+path;
		
		if(path.endsWith("/"))
			path = path.substring(0, path.length()-1);
		
		path = "root"+path;
		return findFolderByPath(path);
	}
	
	protected DocumentListEntry findFolderByPath(String path) throws IOException, ServiceException {
		String title = null;
		String childTitle = null;
		
		title = path.substring(0, path.indexOf("/"));
		path = path.substring(path.indexOf("/") + 1, path.length());
		if (path.indexOf("/") > -1) {
			childTitle = path.substring(0, path.indexOf("/"));
			path = path.substring(path.indexOf("/") + 1, path.length());
		} else {
			childTitle = path;
			path = null;
		}

		String feedLink = "https://docs.google.com/feeds/default/private/full/folder%3A"+title+"/contents/-/folder";
			
		DocumentQuery query = new DocumentQuery(new URL(feedLink));
		query.setTitleQuery(childTitle);
		query.setTitleExact(true);
		
		DocumentListFeed documentLists = gDriveApi.getFolderFromParentByTitle(title, childTitle);
		if(documentLists.getEntries().size() == 1) {
			DocumentListEntry subEntry = documentLists.getEntries().get(0);
			System.out.println(subEntry.getId());
	    	System.out.println(subEntry.getDocId());
	    	System.out.println(subEntry.getDocumentLink().getHref());
	    	System.out.println(subEntry.getParentLinks().size());
	    	System.out.println(subEntry.getTitle().getPlainText());
	    	
	    	if(path != null)
	    		return findFolderByPath(subEntry.getDocId()+"/"+path);
	    	else
	    		return subEntry;
		}
		
		return null;
	}
	
	@Override
	protected String getState(HttpServletRequest request) throws ServletException, IOException {
		return googleUid;
	}
	
	@Override
	protected String getRedirectUri(HttpServletRequest request) throws ServletException, IOException {
		GenericUrl url = new GenericUrl(request.getRequestURL().toString());
		url.setRawPath("/oauth2callback");
		return url.build();
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws IOException {
		return new GoogleAuthorizationCodeFlow.Builder(
				new NetHttpTransport(), new JacksonFactory(), Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET, SCOPES).
				setCredentialStore(new P8CredentialStore()).
				build();
	}

	@Override
	protected String getUserId(HttpServletRequest request) throws ServletException, IOException {
		googleUid = request.getParameter("googleUid");
		if(Util.isBlank(googleUid))
			googleUid = request.getParameter("oauthstate");
		if(Util.isBlank(googleUid)) {
			googleUid = UUID.randomUUID().toString();
			MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
			NewGallery newGallery = new NewGallery();
			newGallery.path = request.getParameter("path");
			newGallery.userKey = request.getParameter("userKey");
			newGallery.title = request.getParameter("title");
			newGallery.desc = request.getParameter("desc");
			memcacheService.put(googleUid, newGallery);
			memcacheService.put(googleUid+"-redirect", "/gdrive/gdriveAuth");
		}
		
		return googleUid;
		
	}

	
}