package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Datastore;
import ch.plus8.hikr.gappserver.DatastoreFactory;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class ImageDownloadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(ImageDownloadServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		UserUtils.init(req);
		
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
	
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();

		logger.info("ImageDownloadServlet called");

		Query query = new Query("FeedItem");

		query.setFilter(CompositeFilterOperator.and(
				new Query.FilterPredicate("status", Query.FilterOperator.GREATER_THAN_OR_EQUAL, Util.ITEM_STATUS_IMAGE_LINK_PROCESS),
				new Query.FilterPredicate("status", Query.FilterOperator.LESS_THAN, Util.ITEM_STATUS_IMAGE_LINK_PROCESS + 5)));
		
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
			
		PreparedQuery prepare = dataStore.prepare(query);
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		for (Entity entity : resultList) {
			logger.log(Level.FINE, "Create images from: " + entity.getProperty("link"));
			try {
				String bigImageUrl = entity.getProperty("imageLink").toString();
				
				HTTPResponse bigImageResp = urlFetchService.fetch(new URL(bigImageUrl));
				Image orgImageB = ImagesServiceFactory.makeImage(bigImageResp.getContent());
				
				Datastore datastore = DatastoreFactory.createDatastore(new Long((Long)entity.getProperty("img2A")*-1), entity);
				if(datastore == null) {
					logger.log(Level.FINE, "Use appengine as default datastore for img2");
					datastore = DatastoreFactory.createDatastore(new Long(Util.DATASTORE_APPENGINE), entity);
				}
				
				if(datastore == null) {
					throw new IllegalArgumentException("No processor for image upload found:" +entity.getProperty("img2A"));
				}
				
				if(datastore.createImage2(entity, orgImageB)) {
					feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
				} else {
					feedRepository.increaseStatus(entity, true);
				}
				
/*				if (new Long(Util.DATASTORE_DROPBOX*-1).equals(entity.getProperty("img2A"))) {
					String dropboxUid = entity.getProperty("author").toString();
					DropboxAPI dropboxAPI = dropboxApiCache.get(dropboxUid);
					if (dropboxAPI == null) {
						dropboxAPI = DropboxUtil.createDropboxApi(dropboxUid);
						dropboxApiCache.put(dropboxUid, dropboxAPI);
					}
					String[] fileInfo = DropboxUtil.fileName(entity.getProperty("link").toString());
					String thumbName = fileInfo[1] + "-img2." + fileInfo[2];
					Image thumb = ImageUtil.thumb(thumbName, 350, 350, imagesService, orgImageB);

					DropboxEntity thumbDropboxEntity = dropboxAPI.uploadImage(fileInfo[0], "/thumbs/" + thumbName, thumb);
					DropboxLink media = dropboxAPI.media(thumbDropboxEntity.path);

					entity.setProperty("img2A", Util.DATASTORE_DROPBOX);
					entity.setUnindexedProperty("img2", thumbDropboxEntity.path);
					entity.setUnindexedProperty("img2Link", media.url);

					entity.setUnindexedProperty("dropboxThumb", null);
					feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
				} else if(new Long(Util.DATASTORE_APPENGINE*-1).equals(entity.getProperty("img2A"))){

					Datastore ds= DatastoreFactory.createDatastore((Long)entity.getProperty("img2A"), entity);
					if(ds != null)
						ds.deleteImage2(entity);
					
					BlobKey resizedBlobKey = ImageUtil.transformToImg2(fileService, imagesService, blobstoreService, entity, orgImageB);
					if (resizedBlobKey != null) {
						entity.setProperty("img2A", Util.DATASTORE_APPENGINE);
						entity.setUnindexedProperty("img2", resizedBlobKey);
						entity.setUnindexedProperty("img2Link", imagesService.getServingUrl(resizedBlobKey));
						feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
					} else {
						throw new IllegalStateException("Could not transform img2.");
					}

				} else {
					throw new IllegalArgumentException("No processor for image download found:" +entity.getProperty("img2A"));
				}
*/
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