package ch.plus8.hikr.gappserver;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageUtil
{
  private static final Logger logger = Logger.getLogger(ImageUtil.class.getName());

  public static void transformImageFromImg1ToImg2(FileService fileService, ImagesService imagesService, BlobstoreService blobstoreService, Entity entity, Image orgImage) throws IOException, InterruptedException {
    logger.log(Level.FINE, "Create images for format img1: " + entity.getProperty("link"));

    if (entity.getProperty("img2") != null) {
      try {
        blobstoreService.delete(new BlobKey[] { (BlobKey)entity.getProperty("img2") });
        entity.setProperty("img2", null);
        entity.setProperty("img2A", Integer.valueOf(0));
        entity.setUnindexedProperty("img2Link", null);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Could not delete img2 for: " + entity.getKey(), e);
      }

    }

    int height = 350;
    int width = 350;
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
    Image newImage1 = imagesService.applyTransform(
      resize, 
      orgImage, 
      ImagesService.OutputEncoding.JPEG);

    AppEngineFile resizedFile = fileService.createNewBlobFile("image/jpeg", entity.getProperty("imageLink") + "-img2");
    FileWriteChannel resizedFileWriteChannel1 = fileService.openWriteChannel(resizedFile, true);
    resizedFileWriteChannel1.write(ByteBuffer.wrap(newImage1.getImageData()));
    resizedFileWriteChannel1.closeFinally();

    BlobKey resizedBlobKey = fileService.getBlobKey(resizedFile);
    for (int ri = 0; (resizedBlobKey == null) && (ri < 7); ri++) {
      logger.warning("Waiting img2 to be resized: " + entity.getProperty("imageLink"));
      Thread.sleep(1000L);
      resizedBlobKey = fileService.getBlobKey(resizedFile);
    }
    if (resizedBlobKey != null) {
      entity.setProperty("img2A", Integer.valueOf(1));
      entity.setUnindexedProperty("img2", resizedBlobKey);
      entity.setUnindexedProperty("img2Link", imagesService.getServingUrl(resizedBlobKey));
    }
  }
}