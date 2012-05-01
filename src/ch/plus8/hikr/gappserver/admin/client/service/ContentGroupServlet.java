package ch.plus8.hikr.gappserver.admin.client.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.PagedResponse;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.admin.client.PageGroup;
import ch.plus8.hikr.gappserver.admin.client.SimpleContent;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("serial")
public class ContentGroupServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GalleryServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if("1".equals(req.getParameter("store"))) 
			store(req, resp);
		else 
			findContentGroups(req, resp);
	}
	
	
	private void findContentGroups(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query(GAEFeedRepository.CNT_GROUP_KIND);
		query.setAncestor(UserUtils.getCurrentKeyFor());
		
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(20);
		fetchOptions.prefetchSize(100);
		PreparedQuery prepare = datastore.prepare(query);
		
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		List<PageGroup> pageGroups = new ArrayList<PageGroup>();
		for(Entity entity : resultList) {
			pageGroups.add(new PageGroup(entity.getKey().getName(), entity.getKind(), (String)entity.getProperty("groupId"),(String)entity.getProperty("title")));
		}
		logger.info("Found content groups: "+pageGroups.size());
		
		PagedResponse response = new PagedResponse();
		response.response = pageGroups;
		
		Gson gson = new Gson();
		String json = gson.toJson(response);

		resp.setContentType("application/json");
		resp.getWriter().write(json);
	}

	
	private void store(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String pageGroupP = req.getParameter("pageGroup");
		
		Gson gson = new Gson();
		PageGroup pageGroupToSave = gson.fromJson(pageGroupP, new TypeToken<PageGroup>(){}.getType());
		
		try {
			Key key = null;
			Entity entity = null;
			boolean createPages = false;
			if(Util.isBlank(pageGroupToSave.key)) { 
				key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), GAEFeedRepository.CNT_GROUP_KIND, UUID.randomUUID().toString());
				entity = new Entity(key);
				createPages = true;
			}
			else {
				key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), pageGroupToSave.kind, pageGroupToSave.key);
				entity = datastore.get(key);
			}
			
			entity.setUnindexedProperty("groupId", pageGroupToSave.groupId);
			entity.setUnindexedProperty("title", pageGroupToSave.title);
			datastore.put(entity);
			
			PagedResponse response = new PagedResponse();
			response.response = new PageGroup(
					entity.getKey().getName(), 
					entity.getKind(), 
					(String)entity.getProperty("groupId"),
					(String)entity.getProperty("title"));
			
			if(createPages) {
				for(int i=1; i<=8; i++) {
					Entity pentity = new Entity(KeyFactory.createKey(
							UserUtils.getCurrentKeyFor(), 
							GAEFeedRepository.CNT_SIMPLE_CONTENT_KIND, 
							UUID.randomUUID().toString()));
					
					pentity.setProperty("group", pageGroupToSave.groupId);
					pentity.setProperty("sort", i);
					pentity.setUnindexedProperty("menu1_idx",  i);
					
					datastore.put(pentity);
				}
			}
			
			String json = gson.toJson(response);
			resp.setContentType("application/json");
			resp.getWriter().write(json);
			
		} catch (EntityNotFoundException e) {
			
			e.printStackTrace();
		}
		
	}

	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}

