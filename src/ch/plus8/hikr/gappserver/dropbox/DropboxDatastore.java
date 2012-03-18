package ch.plus8.hikr.gappserver.dropbox;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.Datastore;
import ch.plus8.hikr.gappserver.ImageUtil;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxLink;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

public class DropboxDatastore extends Datastore {

	private static final Logger logger = Logger.getLogger(DropboxDatastore.class.getName());
	
	
	private DropboxAPI dropboxAPI;
	private ImagesService imagesService;

	public DropboxDatastore(String dropboxUid) {
		logger.info("Create dropbox datastore: "+dropboxUid);
		dropboxAPI = DropboxUtil.createDropboxApi(dropboxUid);
		imagesService = ImagesServiceFactory.getImagesService();
	}
	
	@Override
	public boolean deleteImageItem(Entity entity) {
		return delete(entity, "img1A", "img1");
	}
	
	@Override
	protected boolean deleteImg2(Entity entity) {	
		return delete(entity, "img2A", "img2");
	}
	
	@Override
	protected boolean uploadImg2(Entity entity, Image orgImageB) {
		String[] fileInfo = DropboxUtil.fileName(entity.getProperty("link").toString());
		String thumbName = fileInfo[1] + "-img2." + fileInfo[2];
		
		try {
			Image thumb = ImageUtil.thumb(thumbName, 350, 350, imagesService, orgImageB);
	
			DropboxEntity thumbDropboxEntity = dropboxAPI.uploadImage(fileInfo[0], "/thumbs/" + thumbName, thumb);
			DropboxLink media = dropboxAPI.media(thumbDropboxEntity.path);
	
			entity.setProperty("img2A", Util.DATASTORE_DROPBOX);
			entity.setUnindexedProperty("img2", thumbDropboxEntity.path);
			entity.setUnindexedProperty("img2Link", media.url);
			entity.setUnindexedProperty("dropboxThumb", null);
			return true;
		} catch(Exception e) {
			logger.log(Level.SEVERE,"could not create img2 for dropbox", e);
			return false;
		}
		
		
	}
	
	protected boolean delete(Entity entity, String type, String val) {
		if(Util.DATASTORE_DROPBOX.equals(entity.getProperty(type))) {
			String imgKey = (String)entity.getProperty(val);
			if(imgKey != null) {
				try {
	    			dropboxAPI.delete(imgKey);
	    			return true;
				} catch (DropboxException de) {
					if(de.getStatusCode() == DropboxAPI.STATUS_404) //not found
						return true;
					else 
						throw new IllegalArgumentException("Could not delete image from dropbox: "+imgKey,de);
	    		} catch(Exception e) {
	    			throw new IllegalArgumentException("Could not delete image from dropbox: "+imgKey,e);
	    		}
			} else {
				return true;
			}
		}
		
		throw new IllegalArgumentException("Datastore type is wrong:"+entity.getProperty(type));
	}

}
