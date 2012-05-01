package ch.plus8.hikr.gappserver.admin.client.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.FeedItem;
import ch.plus8.hikr.gappserver.PagedResponse;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class FeedItemServlet  extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GalleryServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		find(req, resp);
	}
	
	private void find(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
	    Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
	    
	    String pcat = request.getParameter("cat");
	    String psource = request.getParameter("source");
	    if(!Util.isBlank(pcat)) {
	    	query.addFilter("categories", FilterOperator.EQUAL, pcat);
	    } else if(!Util.isBlank(psource)) {
	    	query.addFilter("source", FilterOperator.EQUAL, psource);
	    }
	    
		
			
		query.addSort("publishedDate", SortDirection.DESCENDING);
	    PreparedQuery pq = datastore.prepare(query);  
	    int pageSize = 30;  
	  
	    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);  
	    String soffset = request.getParameter("offset");
	    int offset = 0;  
	    if (soffset != null) {
	    	offset = Integer.valueOf(soffset);
	    	fetchOptions.offset(offset);    	
	    }
	      
	  
	    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		List<FeedItem> items = new ArrayList<FeedItem>();
		for(Entity entity : results) {
			
			FeedItem feedItem = new FeedItem(
					(Date)entity.getProperty("publishedDate"),
					Util.translateSource(entity.getProperty("source").toString()), 
					entity.getProperty("author").toString(),
					(entity.getProperty("authorName") != null) ? entity.getProperty("authorName").toString() : entity.getProperty("author").toString(),
					(entity.getProperty("authorLink") != null) ? entity.getProperty("authorLink").toString() : null,
					entity.getProperty("link").toString(),
					(entity.getProperty("title") != null)?entity.getProperty("title").toString():"",
					entity.getProperty("feedLink").toString(),
					entity.getProperty("imageLink").toString(),
					Long.valueOf(entity.getProperty("img1A").toString()),
					(Date)entity.getProperty("storeDate"));
			
			feedItem.categories = ((Collection)entity.getProperty("categories"));
			feedItem.publishedDate = ((Date)entity.getProperty("publishedDate"));
			feedItem.status = ((Number)entity.getProperty("status")).longValue();
			feedItem.img1Link = (String)entity.getProperty("img1Link");
			feedItem.img2Link = (String)entity.getProperty("img2Link");
			
			
			items.add(feedItem);
		}
		logger.info("Found items: "+items.size());
		
		PagedResponse response = new PagedResponse();
		response.response = items;
		
		Gson gson = new Gson();
		String json = gson.toJson(response);

		resp.setContentType("application/json");
		resp.getWriter().write(json);
	}

}
