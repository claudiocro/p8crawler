package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Response;
import ch.plus8.hikr.gappserver.gplus.GPlusUtil;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class DeleteUserServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(DeleteUserServlet.class.getName());
	
    @Override  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {  
    	logger.fine("doGet -> delete user servlet");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        try {
        	Key key = KeyFactory.createKey(GPlusUtil.PERSON_KIND, req.getParameter("userid"));
        	Entity entity = datastore.get(key);
        	entity.setProperty("deleted", 1);
        	datastore.put(entity);
        	
        	
        	Response response = new Response();
    		response.response = "All images will be removed within a few hours. <br />There will be no images of this user in future.";
    		
    		Gson gson = new Gson();
    		String json = gson.toJson(response);
        	resp.getWriter().write(json);
        }catch(EntityNotFoundException e) {
        	Response response = new Response();
    		response.response = "User not found.";
    		
    		Gson gson = new Gson();
    		String json = gson.toJson(response);
        	resp.getWriter().write(json);
        }
    }  
}
