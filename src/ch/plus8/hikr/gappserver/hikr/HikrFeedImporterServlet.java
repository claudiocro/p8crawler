package ch.plus8.hikr.gappserver.hikr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.Scheduler;
import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.googlefeed.GoogleFeedUtil;
import ch.plus8.hikr.gappserver.googlefeed.GoogleReaderFeed;
import ch.plus8.hikr.gappserver.googlefeed.GoogleReaderFeed.Entries;
import ch.plus8.hikr.gappserver.repository.GAEFeedRepository;
import ch.plus8.hikr.repository.FeedRepository;

import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.gson.GsonFactory;

@SuppressWarnings("serial")
public class HikrFeedImporterServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(HikrFeedImporterServlet.class.getName());

	//private final static String HIKR_FOTO_FEED = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=20&q=http%3A//www.hikr.org/gallery/%3Fmode%3Drss";
	private final static String HIKR_FOTO_FEED = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&num=20&q=http://www.hikr.org/gallery/%3Fmode%3Drss%26photo_order%3Dphoto_hot%26key%3DAIzaSyD6FWIhhEskZwN2E_uTsrxZT-vs67px8-Y";
	
	private final JsonHttpParser parser = JsonHttpParser.builder(new GsonFactory()).setContentType("text/javascript").build();
	private FeedRepository feedRepository;

	
	@Override
    public void init(ServletConfig config) throws ServletException {
		GAEFeedRepository feedRepository = new GAEFeedRepository();
		feedRepository.init();
		this.feedRepository = feedRepository;
    }
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.info("Request feed for import: " + HIKR_FOTO_FEED);
		try {
			UrlFetchTransport transport = new UrlFetchTransport();
			HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(HIKR_FOTO_FEED));

			request.addParser(parser);
			GoogleReaderFeed feed = request.execute().parseAs(GoogleReaderFeed.class);
			if(feed.responseData == null || feed.responseData.feed == null || feed.responseData.feed.entries == null) 
				logger.log(Level.WARNING, "No data is imported because feed is empty: " + HIKR_FOTO_FEED);
			else {
				List<String> categories = new ArrayList<String>();
				categories.add("mountain");
				categories.add("nature");
				
				for(Entries entry : feed.entries()) {
					FeedItemBasic item = new FeedItemBasic();
					
					if(GoogleFeedUtil.fillEntity(item, entry, feed.feedLink(), "hikr")) {
						resp.getWriter().write("store<br>");
						feedRepository.storeFeed(item, categories, Util.ITEM_STATUS_IMAGE_LINK_EVAL);
					}
				}
				
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error request hikr feed: " + HIKR_FOTO_FEED,e);
		}
		
		resp.getWriter().write("scedule<br>");
		Scheduler.scheduleImageEvaluator();
		resp.getWriter().write("DONE<br>");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
}
