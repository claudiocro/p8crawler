package ch.plus8.hikr.gappserver;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

public class ImageUtil {
	private static final Logger logger = Logger.getLogger(ImageUtil.class.getName());

	public static boolean transformToImageLink(FileService fileService, ImagesService imagesService, BlobstoreService blobstoreService, Entity entity, Image orgImage) throws IOException, InterruptedException {
		logger.log(Level.FINE, "Create images for format img1: " + entity.getProperty("link"));

		if (entity.getProperty("img1") != null) {
			try {
				blobstoreService.delete(new BlobKey[] { (BlobKey) entity.getProperty("img1") });
				entity.setProperty("img1", null);
				entity.setProperty("img1A", Util.ZERO);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not delete img2 for: " + entity.getKey(), e);
			}

		}

		String thumbName = entity.getProperty("imageLink") + "-img1";
		Image thumb = thumb(thumbName, 1600, 1600, imagesService, orgImage);

		AppEngineFile resizedFile = fileService.createNewBlobFile("image/jpeg", thumbName);
		FileWriteChannel resizedFileWriteChannel1 = fileService.openWriteChannel(resizedFile, true);
		resizedFileWriteChannel1.write(ByteBuffer.wrap(thumb.getImageData()));
		resizedFileWriteChannel1.closeFinally();

		BlobKey resizedBlobKey = fileService.getBlobKey(resizedFile);
		for (int ri = 0; (resizedBlobKey == null) && (ri < 7); ri++) {
			logger.warning("Waiting img1 to be resized: " + thumbName);
			Thread.sleep(1000L);
			resizedBlobKey = fileService.getBlobKey(resizedFile);
		}
		if (resizedBlobKey != null) {
			entity.setProperty("img1A", Util.DATASTORE_APPENGINE);
			entity.setUnindexedProperty("img1", resizedBlobKey);
			entity.setUnindexedProperty("imageLink", imagesService.getServingUrl(resizedBlobKey));
			return true;
		}

		return false;
	}

	public static BlobKey transformToImg2(FileService fileService, ImagesService imagesService, BlobstoreService blobstoreService, Entity entity, Image orgImage) throws IOException, InterruptedException {
		logger.log(Level.FINE, "Create images for format img1: " + entity.getProperty("link"));
		
		String thumbName = entity.getProperty("imageLink") + "-img2";
		Image newImage1 = thumb(thumbName, 350, 350, imagesService, orgImage);

		AppEngineFile resizedFile = fileService.createNewBlobFile("image/jpeg", thumbName);
		FileWriteChannel resizedFileWriteChannel1 = fileService.openWriteChannel(resizedFile, true);
		resizedFileWriteChannel1.write(ByteBuffer.wrap(newImage1.getImageData()));
		resizedFileWriteChannel1.closeFinally();

		BlobKey resizedBlobKey = fileService.getBlobKey(resizedFile);
		for (int ri = 0; (resizedBlobKey == null) && (ri < 7); ri++) {
			logger.warning("Waiting img2 to be resized: " + thumbName);
			Thread.sleep(1000L);
			resizedBlobKey = fileService.getBlobKey(resizedFile);
		}
		
		return resizedBlobKey;
	}

	public static Image thumb(String name, int height, int width, ImagesService imagesService, Image orgImage) throws IOException, InterruptedException {
		logger.log(Level.FINE, "Create images for format: " + name);

		double orgH = orgImage.getHeight();
		double orgW = orgImage.getWidth();

		if (orgH >= orgW) {
			double calc = orgH / orgW;
			BigDecimal bd = new BigDecimal(width * calc);
			bd = bd.setScale(0, 0);
			width = bd.intValue();
		} else {
			double calc = orgW / orgH;
			BigDecimal bd = new BigDecimal(height * calc);
			bd = bd.setScale(0, 0);
			height = bd.intValue();
		}

		Transform resize = ImagesServiceFactory.makeResize(height, width);
		Image thumb = imagesService.applyTransform(resize, orgImage, ImagesService.OutputEncoding.JPEG);

		return thumb;
	}
}