package ch.plus8.hikr.gappserver.googledrive;

import java.util.Map;

import ch.plus8.hikr.gappserver.FeedItemBasic;

import com.google.api.services.drive.model.File;
import com.google.gdata.data.docs.DocumentListEntry;

public class GDriveUtil {
	
	public static final String PROP_GDRIVE_REVISION = "gdriveRev";
	
	public static boolean fillEntity(FeedItemBasic entity, Map<String, Object> additional, DocumentListEntry parent, File file) {
		if(!"drive#file".equals(file.getKind()))
			return false;
		else if(!"image/jpeg".equals(file.getMimeType()))
			return false;

		entity.source = "gdrive";
		entity.link = file.getDownloadUrl();
		entity.title = file.getTitle();
		entity.feedLink = parent.getDocumentLink().getHref();

		additional.put(PROP_GDRIVE_REVISION, file.getEtag());

		return true;
	}
}
