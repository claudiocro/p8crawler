package ch.plus8.hikr.gappserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.google.api.client.util.Key;

public class GoogleReaderFeed {

	public static final DateFormat dateFormat = new SimpleDateFormat("EE, dd MMM yyyy hh:mm:ss Z", Locale.ENGLISH);
	

	@Key("responseData")
	public ResponseData responseData;

	public List<Entries> entries() {
		return responseData.feed.entries;
	}
	
	public String feedTitle() {
		return responseData.feed.title;
	}
	
	public String feedLink() {
		return responseData.feed.link;
	}
	
	public static class ResponseData {
		
		@Key("feed")
		public Feed feed;
	}
	
	public static class Feed {
	
		 @Key("title")
		 public String title;
		 
		 @Key("link")
		 public String link;
		 
		 @Key("entries")
		 public List<Entries> entries;
	}
	 
	 
	 public static class Entries {

		 @Key("title")
		 public String title;
		 
		 @Key("link")
		 public String link;
		 
		 @Key("publishedDate")
		 public String publishedDate;
		 
		 @Key("author")
		 public String author;
		 
		 @Key("contentSnippet")
		 public String contentSnippet;
		 
		 @Key("content")
		 public String content;
		 
	}
}
