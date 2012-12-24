package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

@SuppressWarnings("serial")
public class UpdateAvailablePropertiesOnFeed extends HttpServlet {

	private static final Logger logger = Logger.getLogger(UpdateAvailablePropertiesOnFeed.class.getName());
	private DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			
		Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		
		if(req.getParameter("cursor") != null) {
			try {
				fetchOptions = FetchOptions.Builder.withStartCursor(Cursor.fromWebSafeString(req.getParameter("cursor")));
				logger.fine("From websafe-cursor: " + req.getParameter("cursor"));
			}catch(IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Could not validate cursor string",e);
				resp.getWriter().write("Could not validate cursor string");
				return;
			}
		}
		fetchOptions.limit(50);
			
		PreparedQuery prepare = dataStore.prepare(query);
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		for(Entity entity : resultList) {
			boolean update = false;
			update = oldUpdate(entity);
						
			if(update) {
				dataStore.put(entity);
			}
		}
		
		if(!resultList.isEmpty()) {
			String wscursor = resultList.getCursor().toWebSafeString();
			logger.info("Schedule: /p8Admin/updateAvailablePropertiesOnFeed :" +wscursor);
			Queue queue = QueueFactory.getDefaultQueue();
			TaskOptions param = TaskOptions.Builder.withUrl("/p8admin/UpdateAvailablePropertiesOnFeed");
			param.param("cursor", wscursor);
			queue.add(param);
		}
		
		
	}
	
	@SuppressWarnings("unused")
	private boolean updateDropbox(Entity entity) {
		boolean update = false;
		
		if("dropbox".equals(entity.getProperty("source")) && entity.getProperty("user") == null) {
			entity.setProperty("authKey", KeyFactory.createKey("user", "claudiocro@gmail.com"));
			update = true;
		}
		
		return update;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean oldUpdate(Entity entity) {
		boolean update = false;
		
		if(entity.getProperty("imageLink") != null) {
			entity.setProperty("img1A", 1);
			update = true;
		}
		else {
			entity.setProperty("img1A", 0);
			update = true;
		}
		
		if(entity.getProperty("img1") != null) {
			entity.setProperty("img1A", 1);
			update = true;
		}
		else {
			entity.setProperty("img1A", 0);
			update = true;
		}
		
		if(entity.getProperty("img2") != null) {
			entity.setProperty("img2A", 1);
			update = true;
		}
		else {
			entity.setProperty("img2A", 0);
			update = true;
		}
		
		if("hikr".equals(entity.getProperty("source"))) {
			List categories = new ArrayList();
			categories.add("mountain");
			categories.add("nature");
			entity.setProperty("categories", categories);
		} else if("gplus".equals(entity.getProperty("source"))) {
			List categories = new ArrayList();
			categories.add("street");
			categories.add("photographer");
			entity.setProperty("categories", categories);
		}

		return update;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
	
}
