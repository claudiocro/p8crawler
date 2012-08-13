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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("serial")
public class SimpleContentServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(SimpleContentServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if("1".equals(req.getParameter("store"))) 
			store(req, resp);
		else 
			findContentGroups(req, resp);
	}
	
	
	private void findContentGroups(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query(GAEFeedRepository.CNT_SIMPLE_CONTENT_KIND);
		query.setAncestor(UserUtils.getCurrentKeyFor());
		query.setFilter(new Query.FilterPredicate("group", FilterOperator.EQUAL, req.getParameter("group")));
		query.addSort("sort", SortDirection.ASCENDING);
		
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(20);
		fetchOptions.prefetchSize(100);
		PreparedQuery prepare = datastore.prepare(query);
		
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		List<SimpleContent> simpleContents = new ArrayList<SimpleContent>();
		for(Entity entity : resultList) {
			simpleContents.add(new SimpleContent(
				entity.getKey().getName(), entity.getKind(), 
				(String)entity.getProperty("group"),
				(String)entity.getProperty("title"),
				(Number)entity.getProperty("sort"),
				(Number)entity.getProperty("menu1_idx"),
				(Text)entity.getProperty("content"),
				(String)entity.getProperty("image")
			));
		}
		logger.info("Found content groups: "+simpleContents.size());
		
		PagedResponse response = new PagedResponse();
		response.response = simpleContents;
		
		Gson gson = new Gson();
		String json = gson.toJson(response);

		resp.setContentType("application/json");
		resp.getWriter().write(json);
	}

	
	private void store(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String pageGroupP = req.getParameter("pageGroup");
		
		Gson gson = new Gson();
		SimpleContent simpleContentToSave = gson.fromJson(pageGroupP, new TypeToken<SimpleContent>(){}.getType());
		
		try {
			Key key = null;
			Entity entity = null;
			if(Util.isBlank(simpleContentToSave.key)) { 
				key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), GAEFeedRepository.CNT_SIMPLE_CONTENT_KIND, UUID.randomUUID().toString());
				entity = new Entity(key);
			}
			else {
				key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), simpleContentToSave.kind, simpleContentToSave.key);
				entity = datastore.get(key);
			}
			
			entity.setProperty("group", simpleContentToSave.group);
			entity.setProperty("sort", simpleContentToSave.sort);
			entity.setUnindexedProperty("image", simpleContentToSave.image);
			entity.setUnindexedProperty("menu1_idx",  simpleContentToSave.menu1_idx);
			entity.setUnindexedProperty("title",  simpleContentToSave.title);
			entity.setUnindexedProperty("content", new Text( simpleContentToSave.content));

			datastore.put(entity);
			
			PagedResponse response = new PagedResponse();
			response.response = new PageGroup(entity.getKey().getName(), entity.getKind(), (String)entity.getProperty("groupId"),(String)entity.getProperty("title"));
			
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

