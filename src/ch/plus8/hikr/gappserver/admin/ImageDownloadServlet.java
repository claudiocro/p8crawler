package ch.plus8.hikr.gappserver.admin;

import ch.plus8.hikr.gappserver.ImageUtil;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.hikr.HikrImageFetcher;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImageDownloadServlet extends HttpServlet
{
  private static final Logger logger = Logger.getLogger(HikrImageFetcher.class.getName());
  private static final int MAX_COUNT = 5;

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException
  {
    URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    FileService fileService = FileServiceFactory.getFileService();
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    logger.info("ImageDownloadServlet called");

    Query query = new Query("FeedItem");

    query.addFilter("img1A", Query.FilterOperator.LESS_THAN_OR_EQUAL, Integer.valueOf(0));
    query.addFilter("img1A", Query.FilterOperator.GREATER_THAN, Integer.valueOf(-5));
    query.addFilter("imageLinkA", Query.FilterOperator.EQUAL, Integer.valueOf(1));

    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();

    if (req.getParameter("cursor") != null) {
      try {
        fetchOptions = FetchOptions.Builder.withStartCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
        logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
      } catch (IllegalArgumentException e) {
        logger.log(Level.SEVERE, "Could not validate cursor string", e);
        resp.getWriter().write("Could not validate cursor string");
        return;
      }
    }
    fetchOptions.limit(5);

    PreparedQuery prepare = dataStore.prepare(query);
    QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
    for (Entity entity : resultList)
    {
      logger.log(Level.FINE, "Create images from: " + entity.getProperty("link"));
      try {
        String bigImageUrl = entity.getProperty("imageLink").toString();

        HTTPResponse bigImageResp = urlFetchService.fetch(new URL(bigImageUrl));
        Image orgImageB = ImagesServiceFactory.makeImage(bigImageResp.getContent());

        ImageUtil.transformImageFromImg1ToImg2(fileService, imagesService, blobstoreService, entity, orgImageB);
        entity.setProperty("img1A", Integer.valueOf(2));
        dataStore.put(entity);
      }
      catch (Exception e) {
        resp.getWriter().write("Could not process image download: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString());
        logger.log(Level.SEVERE, "Could not process image download: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString(), e);
        try
        {
          entity.setProperty("img1A", Long.valueOf(((Long)entity.getProperty("img1A")).longValue() - 1L));
          dataStore.put(entity);
        } catch (Exception ex) {
          resp.getWriter().write("Could not store process error to db: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString());
          logger.log(Level.SEVERE, "Could not store process error to db: " + entity.getProperty("imageLink").toString() + " from: " + entity.getProperty("link").toString(), ex);
        }
      }
    }

    if (!resultList.isEmpty()) {
      Scheduler.scheduleImageFetcer(resultList.getCursor().toWebSafeString());
    }

    resp.getWriter().write("DONE");
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException
  {
    doGet(req, resp);
  }
}