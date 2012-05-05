package ch.plus8.hikr.gappserver.googledrive;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Util;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class GoogleDriveAuthServlet extends AbstractAppEngineAuthorizationCodeServlet {

	public static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive.file", 
			"https://www.googleapis.com/auth/userinfo.email", 
			"https://www.googleapis.com/auth/userinfo.profile");
	
	
	@Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response)
	      throws IOException {
		
		Drive drive = new Drive(new UrlFetchTransport(), new GsonFactory());
		
		Drive.Files.Get files = drive.files().get("/cro");
		files.setKey(Util.GOOGLE_API_KEY);
		File execute = files.execute();
		
	  }
	
	  @Override
	  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
	    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
	    url.setRawPath("/oauth2callback");
	    return url.build();
	  }

	  @Override
	  protected AuthorizationCodeFlow initializeFlow() throws IOException {
	    return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(),
	        Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET,
	        SCOPES).setCredentialStore(
	        new AppEngineCredentialStore()).build();
	  }

	}