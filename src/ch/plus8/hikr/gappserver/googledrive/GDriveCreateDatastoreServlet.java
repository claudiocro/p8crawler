package ch.plus8.hikr.gappserver.googledrive;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.auth.AbstractAuthorizationCodeServlet;
import ch.plus8.hikr.gappserver.google.P8CredentialStore;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class GDriveCreateDatastoreServlet extends AbstractAuthorizationCodeServlet {

	public static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive.file",
			"https://docs.google.com/feeds/",
			"https://docs.googleusercontent.com/", 
			"https://www.googleapis.com/auth/userinfo.profile");
	
	
	
	public static final String GDRIVEUSER_KIND = "gdrive:user";


	private String googleUid;


	@Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			response.getWriter().print("<script type=\"text/javascript\">window.opener.App.datastoresController.load();window.close();</script>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }

	
	@Override
	protected String getState(HttpServletRequest request) throws ServletException, IOException {
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
		return new GoogleAuthorizationCodeFlow.Builder(
				new NetHttpTransport(), new GsonFactory(), Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET, SCOPES).
				setCredentialStore(new P8CredentialStore()).
				build();
	}

	@Override
	protected String getUserId(HttpServletRequest request) throws ServletException, IOException {
		if(!Util.isBlank(request.getParameter(P8CredentialStore.PARAM_OAUTHSEQUENCE))) {
			googleUid = request.getParameter(P8CredentialStore.PARAM_OAUTHSEQUENCE);
		} else {
			googleUid = UUID.randomUUID().toString();
			MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
			memcacheService.put(P8CredentialStore.PARAM_OAUTHSEQUENCE+":"+googleUid + "-redirect", "/gdrive/createDatastore");
		}
		
		return googleUid;
		
	}

	
}