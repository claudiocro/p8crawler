package ch.plus8.hikr.gappserver.hikr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class HikrImageFetcher extends HttpServlet {

	private static final Logger logger = Logger.getLogger(HikrImageFetcher.class.getName());
	
	
	public static String evalImageFrom(URLFetchService urlFetchService, String link) throws MalformedURLException, IOException {
		HTTPResponse hikrPhotoResp = urlFetchService.fetch(new URL(link));
		String page = new String(hikrPhotoResp.getContent(),"ISO-8859-1");
		int startIndex = page.indexOf("r4zoomPhoto(e1,\"");

		if(startIndex >0) {
			String bigImageUrl = page.substring(startIndex+16);
			int endIndex = bigImageUrl.indexOf("\")");
			if(endIndex >0) {						
				bigImageUrl = bigImageUrl.substring(0,endIndex);
				return bigImageUrl;
			}
		}
		return null;
	}
}
