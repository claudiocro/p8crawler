package ch.plus8.hikr.gappserver;

import java.util.Date;

public class FeedItemBasic {

	public String link; //key
	public Date publishedDate;
	public String source;
	public String author;
	public String title;
	public String feedLink;
	
	public String imageLink = null;
	//public int imageLinkA = 0; 
	
	public String authorName;
	public String authorLink;
	
	
	
	public FeedItemBasic(){}
	
	public FeedItemBasic(Date publishedDate, String source, String author, String authorName, 
			String authorLink, String link, String title, String feedLink,String imageLink, int imageLinkA) {
		
		this.link = link;
		this.publishedDate = publishedDate;
		this.source = source;
		this.author = author;
		this.title = title;
		this.feedLink = feedLink;
		
		this.imageLink = imageLink;
		//this.imageLinkA = imageLinkA;

		this.authorName = authorName;
		this.authorLink = authorLink;
	}
	
}
