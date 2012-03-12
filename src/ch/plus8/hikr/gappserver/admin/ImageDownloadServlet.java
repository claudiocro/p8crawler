package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.ImageUtil;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.DropboxAPI;
import ch.plus8.hikr.gappserver.dropbox.DropboxUtil;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxLink;
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
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class ImageDownloadServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(HikrImageFetcher.class.getName());
	private static final int MAX_COUNT = 5;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		FileService fileService = FileServiceFactory.getFileService();
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		
		logger.info("ImageDownloadServlet called");

		Query query = new Query("FeedItem");

		query.addFilter("status", Query.FilterOperator.GREATER_THAN_OR_EQUAL, Util.ITEM_STATUS_IMAGE_LINK_PROCESS);
		query.addFilter("status", Query.FilterOperator.LESS_THAN, Util.ITEM_STATUS_IMAGE_LINK_PROCESS + 5);

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

		if (req.getParameter("cursor") != null) {
			try {
				fetchOptions = FetchOptions.Builder.withStartCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
				logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Could not validate cursor string", e);
				resp.getWriter().write("Could not validate cursor string");
				return;
			}
		}
		fetchOptions.limit(5);

		HashMap<String, DropboxAPI> dropboxApiCache = new HashMap<String, DropboxAPI>();
		
		PreparedQuery prepare = dataStore.prepare(query);
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		for (Entity entity : resultList) {
			logger.log(Level.FINE, "Create images from: " + entity.getProperty("link"));
			try {
				String bigImageUrl = entity.getProperty("imageLink").toString();

				HTTPResponse bigImageResp = urlFetchService.fetch(new URL(bigImageUrl));
				Image orgImageB = ImagesServiceFactory.makeImage(bigImageResp.getContent());
				if(new Long(1).equals(entity.getProperty("dropboxThumb"))) {
					String dropboxUid = entity.getProperty("author").toString();
					DropboxAPI dropboxAPI = dropboxApiCache.get(dropboxUid);
					if(dropboxAPI == null) {
						dropboxAPI = DropboxUtil.createDropboxApi(dropboxUid);
						dropboxApiCache.put(dropboxUid, dropboxAPI);
					}
					String[] fileInfo = DropboxUtil.fileName(entity.getProperty("link").toString());
					String thumbName = fileInfo[1]+"-img2."+fileInfo[2];
					Image thumb = ImageUtil.thumb(thumbName, 350, 350, imagesService, orgImageB);
					
					DropboxEntity thumbDropboxEntity = dropboxAPI.uploadImage(fileInfo[0], "/thumbs/"+thumbName, thumb);
					DropboxLink media = dropboxAPI.media(thumbDropboxEntity.path);
					
			        entity.setProperty("img2A", Integer.valueOf(1));
			        entity.setUnindexedProperty("img2Link", media.url);
			        
					feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
					dataStore.put(entity);
				} else if (ImageUtil.transformToImg2(fileService, imagesService, blobstoreService, entity, orgImageB)) {
					feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
				} else {
					throw new IllegalStateException("Could not transform img2.");
				}
			} catch (Exception e) {
				resp.getWriter().write("Could not process image download: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString());
				logger.log(Level.SEVERE, "Could not process image download: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString(), e);

				try {
					feedRepository.increaseStatus(entity, true);
				} catch (Exception ex) {
					resp.getWriter().write("Could not store process error to db: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString());
					logger.log(Level.SEVERE, "Could not store process error to db: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString(), ex);
				}
			}
		}

		if (!resultList.isEmpty()) {
			Scheduler.scheduleImageFetcher(resultList.getCursor().toWebSafeString());
		}

		resp.getWriter().write("DONE");
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}