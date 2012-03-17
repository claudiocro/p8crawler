package ch.plus8.hikr.gappserver;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.images.Image;

public abstract class Datastore {

	protected abstract boolean uploadImg2(Entity entity, Image orgImageB);
	
	protected abstract boolean deleteImageItem(Entity entity);
	protected abstract boolean deleteImg2(Entity entity);

	public boolean deleteImage(Entity entity) {
		if(deleteImageItem(entity)) {
			entity.setUnindexedProperty("imageLink", null);
			entity.setUnindexedProperty("img1", null);
			entity.setProperty("img1A", Util.ITEM_STATUS_DELETED);
			return true;
		} 

		return false;
	}
	
	public boolean deleteImage2(Entity entity) {
		if(deleteImg2(entity)) {
			entity.setUnindexedProperty("img2", null);
			entity.setProperty("img2A", Util.ITEM_STATUS_DELETED);
			return true;
		} 
		return false;
	}
	
	public boolean createImage2(Entity entity, Image orgImageB) {
		Datastore ds= DatastoreFactory.createDatastore((Long)entity.getProperty("img2A"), entity);
		if(ds != null)
			ds.deleteImage2(entity);
		
		if(uploadImg2(entity, orgImageB)) {
			return true;
		}
		return false;
			
	}
}
