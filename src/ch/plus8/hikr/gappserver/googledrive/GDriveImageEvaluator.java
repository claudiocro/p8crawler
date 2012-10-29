package ch.plus8.hikr.gappserver.googledrive;

import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.ImageEvaluator;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.dropbox.DropboxUtil;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.gdata.data.docs.DocumentListFeed;

public class GDriveImageEvaluator extends ImageEvaluator {

	private static final Logger logger = Logger.getLogger(GDriveImageEvaluator.class.getName());
	
	public boolean evaluate(FeedRepository feedRepository, Entity entity) throws Exception {
		logger.info("GDriveImageEvaluator evaluate");
		Key sourceAuth = (Key)entity.getProperty("sourceAuth");
		GDriveApi gDriveApi = new GDriveApi();
		gDriveApi.loadById(sourceAuth.getName());
		String link = entity.getProperty("link").toString();
		String title = entity.getProperty("title").toString();
		String[] fileName = DropboxUtil.fileNameNoPath(title);
		DocumentListFeed childEntries = gDriveApi.getContentFromEntryByTitle(entity.getProperty(GDriveUtil.PROP_IMG2_GDRIVE_PID).toString(), fileName[0]+"-img2.jpg");
		boolean thumbFound = false;
		if(childEntries.getEntries().size() == 1) {
			entity.setProperty("img2A", Util.DATASTORE_GDRIVE);
			entity.setUnindexedProperty("img2", childEntries.getEntries().get(0).getId());
			entity.setUnindexedProperty("img2Link", GDriveUtil.createDownloadLink(childEntries.getEntries().get(0).getDocId()));
			thumbFound = true;
		} else {
			entity.setProperty("img2A", Util.DATASTORE_GDRIVE*-1);
		}
		
		feedRepository.updateImageLinkAndProcess(entity, GDriveUtil.createDownloadLink(link), Util.DATASTORE_GDRIVE, link, !thumbFound);
		
		
		if(thumbFound) { 
			feedRepository.setStatus(entity, Util.ITEM_STATUS_READY, true);
		}
		
		return true;
	}
	
}
