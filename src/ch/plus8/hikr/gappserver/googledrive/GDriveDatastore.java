package ch.plus8.hikr.gappserver.googledrive;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.Datastore;
import ch.plus8.hikr.gappserver.ImageUtil;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.DropboxUtil;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

public class GDriveDatastore extends Datastore {

	private static final Logger logger = Logger.getLogger(GDriveDatastore.class.getName());
	
	public static final String MIME_FOLDER = "application/vnd.google-apps.folder";
	
	private final ImagesService imagesService;

	private GDriveApi gDriveApi;
	//private final Drive drive;
	//private final DocsService docs;

	public GDriveDatastore(Key credentialKey) throws IOException {
		gDriveApi = new GDriveApi();
		gDriveApi.loadById(credentialKey.getName());
		/*Credential credential = new GoogleAuthorizationCodeFlow.Builder(
			new NetHttpTransport(), new GsonFactory(), Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET, GDriveGalleryServlet.SCOPES).
			setCredentialStore(new P8CredentialStore()).
		build().loadCredential(credentialKey.getName());
		
		drive = Drive.builder(new UrlFetchTransport(), new GsonFactory()).
			setHttpRequestInitializer(credential).
			setApplicationName("photography-stream/1.1.1").
		build();
			
		docs = new DocsService("photography-stream/1.1.1");
		docs.setOAuth2Credentials(credential);
			*/
		imagesService = ImagesServiceFactory.getImagesService();
	}
	public GDriveDatastore(Credential credential) throws IOException {
		gDriveApi = new GDriveApi();
		gDriveApi.loadByCredentials(credential);
		/*drive = Drive.builder(new UrlFetchTransport(), new GsonFactory()).
			setHttpRequestInitializer(credential).
			setApplicationName("photography-stream/1.1.1").
		build();
		
		docs = new DocsService("photography-stream/1.1.1");
		docs.setOAuth2Credentials(credential);
		*/
		imagesService = ImagesServiceFactory.getImagesService();
	}
	
	@Override
	protected boolean uploadImg2(Entity entity, Image orgImageB) {
		String[] fileInfo = DropboxUtil.fileNameNoPath(entity.getProperty("title").toString());
		String thumbName = fileInfo[0] + "-img2." + fileInfo[1];
		
		try {
			Image thumb = ImageUtil.thumb(thumbName, 350, 350, imagesService, orgImageB);
			//FileEntry thumbGFile = gDriveApi.addFile(entity.getProperty(GDriveUtil.PROP_IMG2_GDRIVE_PID).toString(),"image/jpeg", thumbName, thumb.getImageData());
			File thumbGFile = gDriveApi.addFile(entity.getProperty(GDriveUtil.PROP_IMG2_GDRIVE_PID).toString(),"image/jpg", thumbName, thumb.getImageData());
			
			entity.setProperty("img2A", Util.DATASTORE_GDRIVE);
			entity.setUnindexedProperty("img2", thumbGFile.getId());
			entity.setUnindexedProperty("img2Link", GDriveUtil.createDownloadLink(thumbGFile.getId()));
			return true;
		} catch(Exception e) {
			logger.log(Level.SEVERE,"could not create img2 for gdrive", e);
			return false;
		}
		
		
	}
	
	@Override
	public boolean deleteImageItem(Entity entity) {
		return delete(entity, "img1A", "img1");
	}
	
	@Override
	protected boolean deleteImg2(Entity entity) {	
		return delete(entity, "img2A", "img2");
	}
	
	protected boolean delete(Entity entity, String type, String val) {
		if(Util.DATASTORE_GDRIVE.equals(entity.getProperty(type))) {
			String imgKey = (String)entity.getProperty(val);
			if(imgKey != null) {
	    		return delete(imgKey);
			} else {
				logger.info("Image key is null "+type+" :"+val);
				return true;
			}
		}
		
		throw new IllegalArgumentException("Datastore type is wrong:"+entity.getProperty(type));
	}
	
	protected boolean delete(String id) {
		
		try {
			/*URL docURL = new URL("https://docs.google.com/feeds/default/private/full/"+id);
			DocumentListEntry sd=docs.getEntry(docURL, DocumentListEntry.class);
			if(sd != null) {
				sd.delete();
				return true;
			}
			return false;*/
			
			gDriveApi.deleteFileById(id);
			return true;
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not delete image from gdrive: "+id,e);
		}
	}
}
