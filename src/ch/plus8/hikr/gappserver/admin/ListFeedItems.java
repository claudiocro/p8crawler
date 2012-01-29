package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

@SuppressWarnings("serial")
public class ListFeedItems extends HttpServlet {  
  
	
	
    @Override  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  
          throws ServletException, IOException {  
  
    	String source = req.getParameter("source");
    	ImagesService imagesService = ImagesServiceFactory.getImagesService();
    	
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
        Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
        query.addFilter("source", FilterOperator.EQUAL, source);
		query.addFilter("img1A", FilterOperator.EQUAL, 1);
		query.addFilter("img2A", FilterOperator.EQUAL, 1);
		
		query.addSort("publishedDate", SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(query);  
        int pageSize = 30;  
  
        resp.setContentType("text/html");  
        resp.getWriter().println(" <ul>");  
  
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);  
        String startCursor = req.getParameter("cursor");  
  
        // If this servlet is passed a cursor parameter, let's use it  
        if (startCursor != null) {  
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));  
        }  
  
        QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);  
        for (Entity entity : results) {  
            resp.getWriter().println("<li>" + entity.getProperty("imageLink")+" / " + imagesService.getServingUrl((BlobKey)entity.getProperty("img2")) + " / <img height=40 width=40 src=\""+ imagesService.getServingUrl((BlobKey)entity.getProperty("img2")) + "\" />");  
        }  
        
        resp.getWriter().println("</li>  </entity></ul>  ");  
  
        String cursor = results.getCursor().toWebSafeString();  
  
        // Assuming this servlet lives at '/people'  
        resp.getWriter().println(            "<a href=\"/p8admin/ListFeedItems?cursor=" + cursor + "&source="+source+"\">Next page</a>");  
    }  
}
