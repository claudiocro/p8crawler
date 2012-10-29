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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;

@SuppressWarnings("serial")
public class DeleteBySource extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(DeleteBySource.class.getName());
	
    @Override  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {  
  
    	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        try {
        	Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
    		query.setFilter(new Query.FilterPredicate("source", FilterOperator.EQUAL, req.getParameter("source")));
    		
    		PreparedQuery prepare = datastore.prepare(query);
    		QueryResultList<Entity> asQueryResultList = prepare.asQueryResultList(FetchOptions.Builder.withLimit(50));
    		for(Entity entity : asQueryResultList) {
        	
	        	BlobKey img1Key = (BlobKey)entity.getProperty("img1");
	        	if(img1Key != null)
	        		blobstoreService.delete(img1Key);
	        	
	        	BlobKey img2Key = (BlobKey)entity.getProperty("img2");
	        	if(img2Key != null)
	        		blobstoreService.delete(img2Key);
	        	
	        	datastore.delete(entity.getKey());
    		}
        }catch(Exception e) {
        	logger.log(Level.SEVERE, "Error deleteing: " + req.getParameter("key"));
        }
        
        
        
    }  
}
