package ch.plus8.hikr.gappserver.admin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import ch.plus8.hikr.gappserver.Util;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UserUtils {

	private static final Logger logger = Logger.getLogger(UserUtils.class.getName());

	public static final String P8_TASK_QUEUE_AUTH = "p8TaskQueueAuth";
	
	private static Key CURRENT_USER_KEY = null;
	
	public static boolean isUserLoggedIn() {
		if(CURRENT_USER_KEY != null)
			return true;
		else {
			UserService userService = UserServiceFactory.getUserService();
			return userService.isUserLoggedIn();
		}
	}
	public static Entity createUser() {
		UserService userService = UserServiceFactory.getUserService();
		if(!userService.isUserLoggedIn()) {
			throw new IllegalArgumentException("User not logged in");
		}
		
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey("user", userService.getCurrentUser().getEmail());
		try {
				Entity entity = ds.get(key);
			} catch (EntityNotFoundException e) {
			Entity entity = new Entity(key);
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				entity.setProperty("id", Util.md5Checksum(UUID.randomUUID().toString()));
			} catch (NoSuchAlgorithmException e1) {
				throw new IllegalArgumentException(e1);
			}
			entity.setProperty("email", userService.getCurrentUser().getEmail());
			entity.setUnindexedProperty("nickname", userService.getCurrentUser().getNickname());
			ds.put(entity);
			return entity;
		}
		return null;
	}

	public static Key getCurrentKeyFor() {
		if(CURRENT_USER_KEY != null)
			return CURRENT_USER_KEY;
		else
			return getUserKeyFor(UserServiceFactory.getUserService().getCurrentUser().getEmail());
	}
	
	public static Entity getCurrentEntityFor() {
		if(CURRENT_USER_KEY != null) {
			return getUserEntityFor(CURRENT_USER_KEY.getName());
		} else
			return getUserEntityFor(UserServiceFactory.getUserService().getCurrentUser().getEmail());
	}
	
	public static Key getUserKeyFor(String email) {
		return KeyFactory.createKey("user", email);
	}
	
	public static Key getUserKeyById(String id) {
		Entity entity = getUserEntityById(id);
		if(entity == null)
			return null;
		else
			return entity.getKey();
	}
	
	public static String getUserIdByCurrent() {
		return getUserIdByKey(getCurrentKeyFor());
	}
	
	public static String getUserIdByKey(Key userKey) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try {
			return (String)ds.get(userKey).getProperty("id");
		} catch (EntityNotFoundException e) {
			return null;
		}
	}
	
	public static Entity getUserEntityFor(String email) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key key = getUserKeyFor(email);
		try {
			return ds.get(key);
		} catch (EntityNotFoundException e) {
			logger.severe("User not found;"+email);
			return createUser();
		}
	}
	
	public static Entity getUserEntityById(String id) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("user");
		query.setFilter(new Query.FilterPredicate("id", FilterOperator.EQUAL, id));
		try {
			Entity entity = ds.prepare(query).asSingleEntity();
			return entity;
		} catch (Exception e) {
			logger.severe("User not found by id: "+id);
			return null;
		}
	}
	public static void init(HttpServletRequest req) {
		if(!Util.isBlank(req.getParameter(P8_TASK_QUEUE_AUTH))) {
			CURRENT_USER_KEY = KeyFactory.stringToKey(req.getParameter(P8_TASK_QUEUE_AUTH));
		}
		
	}
	
}
