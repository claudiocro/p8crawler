package ch.plus8.hikr.gappserver.lomo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.lomo.Lomo.Asset;
import ch.plus8.hikr.gappserver.lomo.Lomo.Photo;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

@SuppressWarnings("serial")
public class LomoImporterServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(LomoImporterServlet.class.getName());

	private final static String LOMO_API_KEY = "bcc6757d03818aa8ddc0ceea37fd92";
	private final static String LOMO_FOTO_FEED = "http://api.lomography.com/v1/";
	
	private final JsonObjectParser parser = new JsonObjectParser(new GsonFactory());
	
	private GAEFeedRepository feedRepository;
	
	
	//lomo film id: 871936831 ->  PX 680 Color Shade
	
	@Override
    public void init(ServletConfig config) throws ServletException {
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		this.feedRepository = feedRepository;
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String type = null;
		if(!Util.isBlank(req.getParameter("filmId")))
			type = req.getParameter("type");
		
		
		if("recent".equals(type) || "popular".equals(type) || "selected".equals(type)) {
			logger.info("Request import of lomo activities for: "+ "recent");
			
			int page = 0;
			if(req.getParameter("page") != null)
				page = Integer.valueOf(req.getParameter("page"));
			
			//if(page != 0)
			//	return;
				
			DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
				
			
			try {
				String url = LOMO_FOTO_FEED;
				
				if(!Util.isBlank(req.getParameter("filmId")))
					url += "films/"+req.getParameter("filmId")+"/";
				
				url += "photos/";
				url +=type;
				url += "?api_key="+LOMO_API_KEY;
				
				if(page >0)
					url += "&page="+page;
				
				
				HttpHeaders headers = new HttpHeaders();
				headers.setAccept("*/*");
				UrlFetchTransport transport = new UrlFetchTransport();
				HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(url));
				request.setHeaders(headers);
				request.setParser(parser);
				
				Lomo lomo = request.execute().parseAs(Lomo.class);
				if(lomo == null ) 
					logger.log(Level.WARNING, "No data is imported because feed is empty: " + url);
				else {
					
					if(lomo.photos != null) {
						for(Photo photo : lomo.photos) {
							if(photo.assets.large != null) {
								Asset asset = photo.assets.large;
								Key key = GAEFeedRepository.createKey(photo.url);
								try {
									Entity entity;
									try {
										entity = dataStore.get(key);
										List newCategories = new ArrayList();
										String supCat = supcategory(type);
										if(supCat != null)
											newCategories.add(supCat);
										
										if(!Util.isBlank(req.getParameter("filmId")))
											newCategories.add("lomo:film:"+req.getParameter("filmId"));
										
										
										feedRepository.updateCategories(key, entity, newCategories);
																			
									}catch (EntityNotFoundException e) {
										FeedItemBasic item = new FeedItemBasic();
										if(LomoUtil.fillEntity(item,lomo,photo,asset)) {	
	//										if(photo.camera != null && !Util.isBlank(photo.camera.name))
	//											entity.setProperty("camera", photo.camera.name);
											
											List cats = categories(type);
											if(!Util.isBlank(req.getParameter("filmId")))
												cats.add("lomo:film:"+req.getParameter("filmId"));
											
											feedRepository.storeFeed(item, cats);
										}
									}
									
								}catch(Exception e1) {
									logger.log(Level.SEVERE, "could not store lomo feed: " + photo.id ,e1);
								}
								
							}
						}
					}
					
					
					
					
					
					
					List categories = new ArrayList();
					categories.add("lomo");
					
					//feedRepository.storeFeed("lomography", lomo, categories);
				}
				
			
				if(++page<=10)
					Scheduler.scheduleLomoImporter(type, page);
				//else
				//	Util.scheduleImageFetcer();
				
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error request lomo feed: " + LOMO_FOTO_FEED,e);
				
			}
		} else {
			resp.getWriter().write("no type");
		}
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List categories(String type) {
		List categories = new ArrayList();
		categories.add("lomo");
		String sup = supcategory(type);
		if(sup != null)
			categories.add(sup);
		
		return categories;
	}
	
	protected String supcategory(String type) {
		if("popular".equals(type))
			return "lomo:popular";
		else if("selected".equals(type))
			return "lomo:selected";
		
		return null;
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
