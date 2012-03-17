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
	public Long img1A = Util.ZERO; 
	
	public String authorName;
	public String authorLink;
	
	
	
	public FeedItemBasic(){}
	
	public FeedItemBasic(Date publishedDate, String source, String author, String authorName, 
			String authorLink, String link, String title, String feedLink,String imageLink, Long img1A) {
		
		this.link = link;
		this.publishedDate = publishedDate;
		this.source = source;
		this.author = author;
		this.title = title;
		this.feedLink = feedLink;
		
		this.imageLink = imageLink;
		this.img1A = img1A;

		this.authorName = authorName;
		this.authorLink = authorLink;
	}
	
}
