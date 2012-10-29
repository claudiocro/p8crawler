package ch.plus8.hikr.gappserver.gplus;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class GPlusPersonImporterServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(GPlusPersonImporterServlet.class.getName());

	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.info("Request import of person: " + req.getParameter("person"));
		
		String personid = req.getParameter("person");
		if(personid == null) {
			logger.info("no person provided");
			resp.getWriter().write("no person provided");
			return;
		}
		
		String categories = req.getParameter("categories");
		String[] cats = categories.split(",");
		
		
		
		
		try {
			Plus.Builder builder = new Plus.Builder(new UrlFetchTransport(), new GsonFactory(), null);
			Plus plus = builder.setJsonHttpRequestInitializer(new PlusRequestInitializer()).build();
			Plus.People.Get search = plus.people().get(personid);
			
			Person person = search.execute();
			
			DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
			Entity entity = new Entity(KeyFactory.createKey("gplus:person", person.getId()));
			entity.setUnindexedProperty("id", person.getId());
			entity.setUnindexedProperty("displayName", person.getDisplayName());
			entity.setUnindexedProperty("url", person.getUrl());
			entity.setProperty("categories", Arrays.asList(cats));
			dataStore.put(entity);
			resp.getWriter().write("person added");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error request google+ person: " + personid,e);
		}
		
	}
	

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
