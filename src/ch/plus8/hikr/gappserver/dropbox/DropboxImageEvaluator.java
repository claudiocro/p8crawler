package ch.plus8.hikr.gappserver.dropbox;

import java.util.HashMap;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.ImageEvaluator;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxLink;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class DropboxImageEvaluator extends ImageEvaluator {

	private static final Logger logger = Logger.getLogger(DropboxImageEvaluator.class.getName());
	
	private final HashMap<String, DropboxAPI> dropboxApiCache = new HashMap<String, DropboxAPI>();

	private URLFetchService urlFetchService;
	private FileService fileService;
	private ImagesService imagesService;
	private BlobstoreService blobStoreService;
	
	public DropboxImageEvaluator(String dropboxUid) {
		urlFetchService = URLFetchServiceFactory.getURLFetchService();
		fileService = FileServiceFactory.getFileService();
		blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
		imagesService = ImagesServiceFactory.getImagesService();
	}
	
	public boolean evaluate(FeedRepository feedRepository, Entity entity) throws Exception {
		String dropboxUid = entity.getProperty("author").toString();
		DropboxAPI dropboxAPI = dropboxApiCache.get(dropboxUid);
		if(dropboxAPI == null) {
			dropboxAPI = DropboxUtil.createDropboxApi(dropboxUid);
			dropboxApiCache.put(dropboxUid, dropboxAPI);
		}
		
		
		String link = entity.getProperty("link").toString();
		logger.info("link:"+link + " / " + link.toLowerCase().startsWith("/public/"));
		if(link.toLowerCase().startsWith("/public/")) {
			String[] fileInfo = DropboxUtil.fileName(link);
			String thumbName = fileInfo[0]+"/thumbs/"+fileInfo[1]+"-img2." + fileInfo[2];
			boolean thumbFound = false;
			try {
				DropboxLink thumbLink = dropboxAPI.media(thumbName);
				if(thumbLink != null) {
					entity.setProperty("img2A", Util.DATASTORE_DROPBOX);
					entity.setUnindexedProperty("img2", thumbName);
					entity.setUnindexedProperty("img2Link", thumbLink.url);
					thumbFound = true;
				}
			} catch(Exception e) {
				logger.info("No thumb found on dropbox");
			}
			
			if(!thumbFound) 
				entity.setProperty("img2A", Util.DATASTORE_DROPBOX*-1);
			
			feedRepository.updateImageLinkAndProcess(entity, dropboxAPI.media(link).url, Util.DATASTORE_DROPBOX, link, !thumbFound);
			
			if(thumbFound) { 
				feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
			}
			return true;
		}
		else if(DropboxUtil.createImg1(entity, dropboxAPI, urlFetchService, fileService, blobStoreService, imagesService)) {
			feedRepository.setStatus(entity, Util.ITEM_STATUS_IMAGE_LINK_PROCESS, true);
			return true;
		} else {
			return false;
		}
	}
}
