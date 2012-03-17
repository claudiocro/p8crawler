package ch.plus8.hikr.gappserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.admin.ImageDownloadServlet;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

public class AppengineDatastore extends Datastore {

	private static final Logger logger = Logger.getLogger(AppengineDatastore.class.getName());
	
	private BlobstoreService blobstoreService;
	private FileService fileService;
	private ImagesService imagesService;

	public AppengineDatastore() {
		blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		fileService = FileServiceFactory.getFileService();
		imagesService = ImagesServiceFactory.getImagesService();
	}
	
	@Override
	protected boolean deleteImageItem(Entity entity) {
		if(Util.DATASTORE_APPENGINE.equals(entity.getProperty("img1A"))) {
			BlobKey img1Key = (BlobKey)entity.getProperty("img1");
	    	if(img1Key != null) {
	    		blobstoreService.delete(img1Key);
	    	}
	    	return true;
		}
		throw new IllegalArgumentException("Datastore type is wrong:"+entity.getProperty("img1A"));
	}
	
	
	

	@Override
	protected boolean deleteImg2(Entity entity) {
		if(Util.DATASTORE_APPENGINE.equals(entity.getProperty("img2A"))) {
			BlobKey img2Key = (BlobKey)entity.getProperty("img2");
	    	if(img2Key != null) {
	    		blobstoreService.delete(img2Key);
	    	}
	    	return true;
		}
		throw new IllegalArgumentException("Datastore type is wrong:"+entity.getProperty("img2"));
		
	}

	
	@Override
	protected boolean uploadImg2(Entity entity, Image orgImageB) {
		try {
			BlobKey resizedBlobKey = ImageUtil.transformToImg2(fileService, imagesService, blobstoreService, entity, orgImageB);
			if (resizedBlobKey != null) {
				entity.setProperty("img2A", Util.DATASTORE_APPENGINE);
				entity.setUnindexedProperty("img2", resizedBlobKey);
				entity.setUnindexedProperty("img2Link", imagesService.getServingUrl(resizedBlobKey));
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"could not create img2 for appengine store", e);
			return false;
		}
	}
	
	
	protected boolean delete(Entity entity, String type, String val) {
		if(Util.DATASTORE_APPENGINE.equals(entity.getProperty(type))) {
			BlobKey imgKey = (BlobKey)entity.getProperty(val);
	    	if(imgKey != null) {
	    		blobstoreService.delete(imgKey);
	    	}
	    	return true;
		}
		throw new IllegalArgumentException("Datastore type is wrong:"+entity.getProperty(type));
	}
}
