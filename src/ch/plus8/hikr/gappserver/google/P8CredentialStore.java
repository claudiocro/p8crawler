package ch.plus8.hikr.gappserver.google;

import java.io.IOException;

import ch.plus8.hikr.gappserver.admin.UserUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class P8CredentialStore implements CredentialStore {

	public static final String PARAM_OAUTHSEQUENCE = "oauthsequence";
	public static final String KIND = "google:user";

	@Override
	public void store(String userId, Credential credential) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(KIND, userId, UserUtils.getCurrentKeyFor());
		Userinfo userinfo = getUserInfo(credential);
		entity.setProperty("accessToken", credential.getAccessToken());
		entity.setProperty("refreshToken", credential.getRefreshToken());
		entity.setProperty("expirationTimeMillis", credential.getExpirationTimeMilliseconds());
		entity.setProperty("userid", userinfo.getId());
		entity.setProperty("name", userinfo.getName());
		datastore.put(entity);
	}

	@Override
	public void delete(String userId, Credential credential) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), KIND, userId);
		datastore.delete(key);
	}

	@Override
	public boolean load(String userId, Credential credential) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(UserUtils.getCurrentKeyFor(), KIND, userId);
		try {
			Entity entity = datastore.get(key);
			credential.setAccessToken((String) entity.getProperty("accessToken"));
			credential.setRefreshToken((String) entity.getProperty("refreshToken"));
			credential.setExpirationTimeMilliseconds((Long) entity.getProperty("expirationTimeMillis"));
			return true;
		} catch (EntityNotFoundException exception) {
			return false;
		}
	}

	protected Userinfo getUserInfo(Credential credentials) {

		Oauth2 userInfoService = new Oauth2.Builder(new NetHttpTransport(), new GsonFactory(), credentials).build();
		Userinfo userInfo = null;

		try {
			userInfo = userInfoService.userinfo().get().execute();
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
		}

		if (userInfo != null && userInfo.getId() != null)
			return userInfo;
		else
			return null;
	}

}
