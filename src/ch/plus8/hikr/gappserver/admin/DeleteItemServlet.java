package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class DeleteItemServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(DeleteItemServlet.class.getName());
	
    @Override  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {  
  
    	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        try {
        	Key key = KeyFactory.createKey(GAEFeedRepository.FEED_ITEM_KIND, req.getParameter("key"));
        	Entity entity = datastore.get(key);
        	BlobKey img1Key = (BlobKey)entity.getProperty("img1");
        	if(img1Key != null)
        		blobstoreService.delete(img1Key);
        	
        	BlobKey img2Key = (BlobKey)entity.getProperty("img2");
        	if(img2Key != null)
        		blobstoreService.delete(img2Key);
        	
        	datastore.delete(key);
        }catch(Exception e) {
        	logger.log(Level.SEVERE, "Error deleteing: " + req.getParameter("key"));
        }
        
        
        
    }  
}
