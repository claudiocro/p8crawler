package ch.plus8.hikr.gappserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

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
		return delete(entity,"img1A", "img1");
	}
	
	
	

	@Override
	protected boolean deleteImg2(Entity entity) {
		return delete(entity,"img2A", "img2");
	}

	
	@Override
	protected boolean uploadImg2(Entity entity, Image orgImageB) {
		try {
			BlobKey resizedBlobKey = ImageUtil.transformToImg2(fileService, imagesService, blobstoreService, entity, orgImageB);
			if (resizedBlobKey != null) {
				entity.setProperty("img2A", Util.DATASTORE_APPENGINE);
				entity.setUnindexedProperty("img2", resizedBlobKey);
				entity.setUnindexedProperty("img2Link", imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(resizedBlobKey)));
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
		logger.fine("Delete image type: "+type+" / "+entity.getProperty(type));
		if(Util.DATASTORE_APPENGINE.equals(entity.getProperty(type))) {
			BlobKey imgKey = (BlobKey)entity.getProperty(val);
	    	if(imgKey != null) {
	    		logger.fine("Delete "+type+" :"+imgKey);
	    		blobstoreService.delete(imgKey);
	    		return true;
	    	} else {
	    		logger.info("Image key is null "+type+" :"+val);
	    		return true;
	    	}
		}
		throw new IllegalArgumentException("Datastore type is wrong:"+entity.getProperty(type));
	}
}
