package ch.plus8.hikr.gappserver.dropbox;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oauth.signpost.OAuthConsumer;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.ImageUtil;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxAccount;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxContent;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.hikr.HikrImageFetcher;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;
import ch.plus8.hikr.gappserver.signpost.AppEngineOAuthConsumer;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;

public class DropboxUtil {

	private static final Logger logger = Logger.getLogger(HikrImageFetcher.class.getName());
	
	public static void main(String args[]) {
		String[] f = fileName("/sfaf/asdfa /sadfsdfa/dfa dsfasdfasdf-sdfasdf.jpg");
		System.out.println(f);
	}
	
	public static String[] fileName(String path) {
		Pattern p = Pattern.compile("^(.*)/(.*)\\.(.*)$");
		Matcher m = p.matcher(path);
		
		if(m.find()) {
			return new String[]{m.group(1),m.group(2),m.group(3)};
		}
		return null;
	}
	
	public static boolean fillEntity(FeedItemBasic entity, Map<String, Object> additional, DropboxAccount account, DropboxEntity dentity, DropboxContent cnt, String authorName) throws ParseException {
		if(cnt.is_dir)
			return false;
		else if(!"image/jpeg".equals(cnt.mime_type))
			return false;
		
		//Calendar cal = Calendar.getInstance();
		//cal.setTime(lomo.dateFormat.parse(photo.createdAt));
		//entity.publishedDate = cal.getTime();
		entity.source = "dropbox";
		entity.link = cnt.path;
		entity.title = null;
		entity.feedLink = dentity.path;

		entity.author = String.valueOf(account.uid);
		if(authorName == null)
			entity.authorName = account.displayName;
		else
			entity.authorName = authorName;
		
		entity.authorLink = account.referralLink;

		additional.put("dropboxRev", cnt.rev);

		return true;
	}

	public static boolean createImg1(Entity entity, DropboxAPI dropboxAPI, URLFetchService urlFetchService, FileService fileService, BlobstoreService blobstoreService, ImagesService imagesService) throws IOException {

		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();

		logger.log(Level.FINE, "Convert dropbox image from: " + entity.getProperty("link"));
		try {
			String url = dropboxAPI.media(entity.getProperty("link").toString()).url;

			HTTPResponse bigImageResp = urlFetchService.fetch(new URL(url));
			Image orgImageB = ImagesServiceFactory.makeImage(bigImageResp.getContent());

			if (ImageUtil.transformToImageLink(fileService, imagesService, blobstoreService, entity, orgImageB))
				return true;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not process image download: " + entity.getProperty("link").toString(), e);

			try {
				feedRepository.increaseStatus(entity, true);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Could not store process error to db: " + entity.getProperty("link").toString(), ex);
			}
		}

		return false;
	}
	
	public static DropboxAPI createDropboxApi(String dropboxUid) {
		OAuthConsumer consumer = new AppEngineOAuthConsumer(DropboxSyncher.APP_KEY, DropboxSyncher.APP_SECRET);
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		
		try {
			Entity dropboxUserEntity = datastoreService.get(KeyFactory.createKey(DropboxSyncher.DROPBOXUSER_KIND, dropboxUid));
			consumer.setTokenWithSecret((String)dropboxUserEntity.getProperty("token"), (String)dropboxUserEntity.getProperty("tokenSecret"));
			
			
			
			return new DropboxAPI(consumer);
		} catch (EntityNotFoundException e) {
			logger.severe("Dropbox user: " + dropboxUid + " not found.");
		}
		
		/*consumer.setTokenWithSecret("fc4vpaho6wuo1wj","lm19zvmjxloibzl");
		return new DropboxAPI(consumer);*/
		return null;
	}
	
	public static String getDropboxUidFromCategories(Object categories) {
		List<String> cats; 
		if(categories instanceof String) {
			cats = new ArrayList<String>();
			cats.add((String)categories);
		} else {
			cats = (List<String>)categories; 
		}
		
		Pattern p = Pattern.compile("^dropbox-(.*?)-(.*?)$");
		for(String c : cats) {
			Matcher m = p.matcher(c);
			if(m.find())
				return m.group(1);
		}
		return null;
	}

}
