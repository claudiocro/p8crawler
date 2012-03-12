package ch.plus8.hikr.gappserver.hikr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;

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
