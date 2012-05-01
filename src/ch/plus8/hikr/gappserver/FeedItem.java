package ch.plus8.hikr.gappserver;

import java.util.Collection;
import java.util.Date;

import com.google.gson.Gson;

public class FeedItem extends FeedItemBasic {
	
	
	
	public String img1Link;
	public String img2Link;
	
	public Date publishedDate;
	public Date storeDate;
	public long status;
	public Collection categories;

	public FeedItem(){};
	
	public FeedItem(Date publishedDate, String source, String author, String authorName, String authorLink, String link, String title, 
			String feedLink, String imageLink, Long img1A, Date storeDate) {
		super(publishedDate, source, author, authorName, authorLink, link, title, feedLink,imageLink, img1A);
	
		this.storeDate = storeDate;
		
	}
	
	
	public String toJson() {
    	return new Gson().toJson(this);
    }
	
    public static FeedItem constructFromJson(String json) {
    	return new Gson().fromJson(json, FeedItem.class);
    }
}
