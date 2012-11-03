package ch.plus8.hikr.gappserver.googledrive;

import java.util.Map;

import ch.plus8.hikr.gappserver.FeedItemBasic;

import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.datastore.Key;

public class GDriveUtil {
	
	public static final String PROP_GDRIVE_REVISION = "gdriveRev";
	public static final String PROP_IMG2_GDRIVE_PID = "img2GdrivePid";
	
	public static boolean fillEntity(FeedItemBasic entity, Map<String, Object> additional, ChildReference parent, File file, Key credentialKey, String thumbsFolderId) {
		if(!"drive#file".equals(file.getKind()))
			return false;
		else if(!"image/jpeg".equals(file.getMimeType()))
			return false;

		entity.author = credentialKey.getName();
		entity.source = "gdrive";
		entity.link = file.getId();
		entity.title = file.getTitle();
		entity.feedLink = parent.getSelfLink();

		additional.put(PROP_GDRIVE_REVISION, file.getEtag());
		additional.put(PROP_IMG2_GDRIVE_PID, thumbsFolderId);

		return true;
	}
	
	public static String createDownloadLink(String id) {
		return "https://docs.google.com/uc?export=download&id="+id;
	}
}
