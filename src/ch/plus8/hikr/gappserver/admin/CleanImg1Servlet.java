package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

@SuppressWarnings("serial")
public class CleanImg1Servlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(CleanImg1Servlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		
		List<Filter> filters = new ArrayList<Filter>();
		Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
		filters.add(new Query.FilterPredicate("img1A", FilterOperator.EQUAL, 1));
		filters.add(new Query.FilterPredicate("img2A", FilterOperator.EQUAL, 1));
		
		query.setFilter(CompositeFilterOperator.and(filters));
		query.addSort("publishedDate", SortDirection.ASCENDING);
		
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(20);
		if (req.getParameter("cursor") != null) {
			try {
				fetchOptions.startCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
				logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Could not validate cursor string", e);
				resp.getWriter().write("Could not validate cursor string");
				return;
			}
		}

		PreparedQuery prepare = datastore.prepare(query);
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		for (Entity entity : resultList) {
			try {

				BlobKey img1Key = (BlobKey) entity.getProperty("img1");
				if (img1Key != null)
					blobstoreService.delete(img1Key);

				entity.setProperty("img1A", 2);
				entity.setProperty("img1", null);
				datastore.put(entity);

				logger.info("removed img1 from : " + entity.getKey());

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error when delete: " + entity.getKey(), e);
			}
		}
		
		if(!resultList.isEmpty()) {
			Queue queue = QueueFactory.getDefaultQueue();
			TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/cleanImg1");
			param.param("cursor", resultList.getCursor().toWebSafeString());
			
			queue.add(param);
		}
	}
	
	

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}


}
