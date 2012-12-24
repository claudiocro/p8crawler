package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

@SuppressWarnings("serial")
public class ListFeedItems extends HttpServlet {  
  
	
	
    @Override  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  
          throws ServletException, IOException {  
  
    	
    	String source = req.getParameter("source");
    	ImagesService imagesService = ImagesServiceFactory.getImagesService();
    	
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        
        List<Filter> filters = new ArrayList<Filter>();
		Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
		filters.add(new Query.FilterPredicate("source", FilterOperator.EQUAL, source));
		filters.add(new Query.FilterPredicate("img1A", FilterOperator.EQUAL, 1));
		filters.add(new Query.FilterPredicate("img2A", FilterOperator.EQUAL, 1));
		
		query.setFilter(CompositeFilterOperator.and(filters));
		
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
            resp.getWriter().println("<li>" + entity.getProperty("imageLink")+" / " + imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey((BlobKey)entity.getProperty("img2"))) + " / <img height=40 width=40 src=\""+ imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey((BlobKey)entity.getProperty("img2"))) + "\" />");
        }  
        
        resp.getWriter().println("</li>  </entity></ul>  ");  
  
        String cursor = results.getCursor().toWebSafeString();  
  
        // Assuming this servlet lives at '/people'  
        resp.getWriter().println(            "<a href=\"/p8admin/ListFeedItems?cursor=" + cursor + "&source="+source+"\">Next page</a>");  
    }  
}
