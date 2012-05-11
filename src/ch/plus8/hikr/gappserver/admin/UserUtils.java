package ch.plus8.hikr.gappserver.admin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

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
	
	public static boolean isUserLoggedIn() {
		UserService userService = UserServiceFactory.getUserService();
		return userService.isUserLoggedIn();
	}
	public static void createUser() {
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
		}
	}

	public static Key getCurrentKeyFor() {
		return getUserKeyFor(UserServiceFactory.getUserService().getCurrentUser().getEmail());
	}
	
	public static Entity getCurrentEntityFor() {
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
			return null;
		}
	}
	
	public static Entity getUserEntityById(String id) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("user");
		query.addFilter("id", FilterOperator.EQUAL, id);
		try {
			Entity entity = ds.prepare(query).asSingleEntity();
			return entity;
		} catch (Exception e) {
			logger.severe("User not found by id: "+id);
			return null;
		}
	}
	
}