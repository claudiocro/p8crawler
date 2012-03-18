package ch.plus8.hikr.gappserver.hikr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ch.plus8.hikr.gappserver.ImageEvaluator;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class HikrImageEvaluator extends ImageEvaluator {

	private URLFetchService urlFetchService;

	public HikrImageEvaluator() {
		urlFetchService = URLFetchServiceFactory.getURLFetchService();
	}
	
	@Override
	public boolean evaluate(FeedRepository feedRepository, Entity entity) throws Exception {
		String imageLink = evalImageFrom(urlFetchService, entity.getProperty("link").toString());
		if(imageLink != null) {
			entity.setProperty("img2A", Util.DATASTORE_APPENGINE*-1);
			feedRepository.updateImageLinkAndProcess(entity, imageLink, Util.DATASTORE_UNKNOWN, null, true);
			return true;
		}
		else {
			return false;
		}
	}
	
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
