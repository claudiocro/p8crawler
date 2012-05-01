package ch.plus8.hikr.gappserver.admin.client.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.PagedResponse;
import ch.plus8.hikr.gappserver.admin.UserUtils;
import ch.plus8.hikr.gappserver.admin.client.Datastore;
import ch.plus8.hikr.gappserver.dropbox.DropboxSyncher;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("serial")
public class DatastoreServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(DatastoreServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		if("1".equals(req.getParameter("store"))) 
			store(req, resp);
		else
			findAll(req, resp);
		
	}
	
	private void findAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query(DropboxSyncher.DROPBOXUSER_KIND);
		query.setAncestor(UserUtils.getCurrentKeyFor());
		
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(20);
		fetchOptions.prefetchSize(20);
		PreparedQuery prepare = datastore.prepare(query);
		
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		List<Datastore> datastores = new ArrayList<Datastore>();
		for(Entity entity : resultList) {
			if(entity.getProperty("dropbboxUid") != null) {
				entity.setProperty("dropboxUid", entity.getProperty("dropbboxUid"));
				entity.setUnindexedProperty("dropbboxUid", null);
				datastore.put(entity);
			}
			
			datastores.add(new Datastore(entity.getKey().getName(), entity.getKind(), (String)entity.getProperty("dropboxUid"), (String)entity.getProperty("title")));
		}
		logger.info("Found datastores: "+datastores.size());
		
		PagedResponse response = new PagedResponse();
		response.response = datastores;
		
		Gson gson = new Gson();
		String json = gson.toJson(response);

		resp.setContentType("application/json");
		resp.getWriter().write(json);

	}
	
	private void store(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String datastoreP = req.getParameter("datastore");
		
		Gson gson = new Gson();
		Datastore datastoreToSave = gson.fromJson(datastoreP, new TypeToken<Datastore>(){}.getType());
		
		try {
			Key key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), datastoreToSave.kind, datastoreToSave.key);
			Entity entity = datastore.get(key);
			entity.setProperty("title", datastoreToSave.title);
			datastore.put(entity);
			
			PagedResponse response = new PagedResponse();
			response.response = new Datastore(entity.getKey().getName(), entity.getKind(), (String)entity.getProperty("dropboxUid"), (String)entity.getProperty("title"));
			
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
