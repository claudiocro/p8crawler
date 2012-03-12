package ch.plus8.hikr.gappserver.googlefeed;

import java.text.ParseException;
import java.util.logging.Logger;

import ch.plus8.hikr.gappserver.FeedItemBasic;
import ch.plus8.hikr.gappserver.googlefeed.GoogleReaderFeed.Entries;

public class GoogleFeedUtil {

	private static final Logger logger = Logger.getLogger(GoogleFeedUtil.class.getName());
	
	public static boolean fillEntity(FeedItemBasic entity, Entries entry, String feedLink, String source) {
	  	try {
			entity.publishedDate = GoogleReaderFeed.dateFormat.parse(entry.publishedDate);
			entity.source = source;
			entity.link = entry.link;
			entity.title = entry.title;
			entity.feedLink = feedLink;
			entity.author = entry.author;
		} catch (ParseException e) {
			logger.severe("Could not store: wrong publishedDate format:" + entry.publishedDate);
			return false;
		}
		
		return true;
  }
	
}
