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
import ch.plus8.hikr.gappserver.admin.client.Gallery;
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
public class GalleryServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GalleryServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if("1".equals(req.getParameter("store"))) 
			store(req, resp);
		else
			findAll(req, resp);
	}
	
	private void findAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query query = new Query(GAEFeedRepository.USER_GALLERY_KIND);
		query.setAncestor(UserUtils.getCurrentKeyFor());
		
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(100);
		fetchOptions.prefetchSize(100);
		PreparedQuery prepare = datastore.prepare(query);
		
		QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
		List<Gallery> datastores = new ArrayList<Gallery>();
		for(Entity entity : resultList) {
			datastores.add(new Gallery(entity.getKey().getName(), entity.getKind(), (String)entity.getProperty("ref"),(String)entity.getProperty("title"),(Text)entity.getProperty("desc")));
		}
		logger.info("Found galleries: "+datastores.size());
		
		PagedResponse response = new PagedResponse();
		response.response = datastores;
		
		Gson gson = new Gson();
		String json = gson.toJson(response);

		resp.setContentType("application/json");
		resp.getWriter().write(json);
	}
	
	private void store(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String galleryP = req.getParameter("gallery");
		
		Gson gson = new Gson();
		Gallery galleryToSave = gson.fromJson(galleryP, new TypeToken<Gallery>(){}.getType());
		
		try {
			Key key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), galleryToSave.kind, galleryToSave.key);
			Entity entity = datastore.get(key);
			entity.setUnindexedProperty("title", galleryToSave.title);
			entity.setUnindexedProperty("desc", new Text(galleryToSave.desc));
			datastore.put(entity);
			
			PagedResponse response = new PagedResponse();
			response.response = new Gallery(entity.getKey().getName(), entity.getKind(), (String)entity.getProperty("ref"),(String)entity.getProperty("title"),(Text)entity.getProperty("desc"));
			
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
