package ch.plus8.hikr.gappserver;

import java.util.Date;

import com.google.gson.Gson;

public class FeedItem {
	
	public Date publishedDate;
	public String source;
	public String author;
	public String link;
	public String title;
	public String feedLink;
	public String imageLink;
	public String img1Link;
	public String img2Link;
	public String authorName;
	public String authorLink;

	public FeedItem(Date publishedDate, String source, String author, String authorName, String authorLink, String link, String title, String feedLink,String imageLink,
			String img1Link, String img2Link) {
		this.publishedDate = publishedDate;
		this.source = source;
		this.author = author;
		this.authorName = authorName;
		this.authorLink = authorLink;
		this.link = link;
		this.title = title;
		this.feedLink = feedLink;
		this.imageLink = imageLink;
		this.img1Link = img1Link;
		this.img2Link = img2Link;
		
	}
	
	
	public String toJson() {
    	return new Gson().toJson(this);
    }
	
    public static FeedItem constructFromJson(String json) {
    	return new Gson().fromJson(json, FeedItem.class);
    }
}
