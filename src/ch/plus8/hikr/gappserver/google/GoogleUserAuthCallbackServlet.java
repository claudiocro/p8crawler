package ch.plus8.hikr.gappserver.google;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.googledrive.GDriveCreateDatastoreServlet;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class GoogleUserAuthCallbackServlet extends AbstractAuthorizationCodeCallbackServlet {

	  @Override
	  protected void onSuccess(HttpServletRequest request, HttpServletResponse resp, Credential credential)
	      throws ServletException, IOException {
		  
		  
		MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
		resp.sendRedirect((String)memcacheService.get(P8CredentialStore.PARAM_OAUTHSEQUENCE+":"+request.getParameter("state")+"-redirect")+"?"+P8CredentialStore.PARAM_OAUTHSEQUENCE+"="+request.getParameter("state"));
	  }

	  @Override
	  protected void onError(
	      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
	      throws ServletException, IOException {
	    
		  
		  System.out.println("ERROR");
	  }

	  @Override
	  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
	    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
	    url.setRawPath("/oauth2callback");
	    //url.set("state", req.getParameter("state"));
	    return url.build();
	  }

	  @Override
	  protected AuthorizationCodeFlow initializeFlow() throws IOException {
	    return new GoogleAuthorizationCodeFlow.Builder(new UrlFetchTransport(), new GsonFactory(),
	        Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET,
	        GDriveCreateDatastoreServlet.SCOPES).setCredentialStore(
	        new P8CredentialStore()).build();
	  }

	@Override
	protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
		return req.getParameter("state");
	}
	}