package ch.plus8.hikr.gappserver.admin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.google.P8CredentialStore;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public abstract class P8CrawlerGoogleServlet extends AbstractAuthorizationCodeServlet {
	private static final long serialVersionUID = 1L;

	public static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/drive", "https://www.googleapis.com/auth/drive.file", "https://docs.google.com/feeds/", "https://docs.googleusercontent.com/", "https://www.googleapis.com/auth/userinfo.profile");

	private String googleUid;

	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		UserUtils.init(req);
		super.service(req, resp);
	}
	
	
	protected final String getGoogleUid() {
		return googleUid;
	}

	protected abstract String processOAuthRedirect(HttpServletRequest request);
	
	protected final String getUserId(HttpServletRequest request) throws ServletException, IOException {
		MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
		googleUid = request.getParameter("googleUid");
		if (!Util.isBlank(request.getParameter(P8CredentialStore.PARAM_OAUTHSEQUENCE))) {
			googleUid = request.getParameter(P8CredentialStore.PARAM_OAUTHSEQUENCE);
			memcacheService.put(P8CredentialStore.PARAM_OAUTHSEQUENCE + ":" + googleUid + "-"+UserUtils.P8_TASK_QUEUE_AUTH, KeyFactory.keyToString(UserUtils.getCurrentKeyFor()));
			memcacheService.put(P8CredentialStore.PARAM_OAUTHSEQUENCE + ":" + googleUid + "-redirect", processOAuthRedirect(request));
		}
		if(Util.isBlank(googleUid)){
			googleUid = UUID.randomUUID().toString();
			memcacheService.put(P8CredentialStore.PARAM_OAUTHSEQUENCE + ":" + googleUid + "-"+UserUtils.P8_TASK_QUEUE_AUTH, KeyFactory.keyToString(UserUtils.getCurrentKeyFor()));
			memcacheService.put(P8CredentialStore.PARAM_OAUTHSEQUENCE + ":" + googleUid + "-redirect", processOAuthRedirect(request));
		}
		

		return googleUid;

	}


	@Override
	protected String getRedirectUri(HttpServletRequest request) throws ServletException, IOException {
		GenericUrl url = new GenericUrl(request.getRequestURL().toString());
		url.setRawPath("/oauth2callback");
		return url.build();
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws IOException {
		return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new GsonFactory(), Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET, SCOPES)
		.setCredentialStore(new P8CredentialStore())
		.setAccessType("offline")
		.build();
	}

	@Override
	protected void onAuthorization(HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeRequestUrl authorizationUrl) throws ServletException, IOException {
		authorizationUrl.setState(googleUid);
		super.onAuthorization(req, resp, authorizationUrl);
	}

}
