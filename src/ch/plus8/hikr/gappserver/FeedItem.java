package ch.plus8.hikr.gappserver;

import java.util.Collection;
import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;

public class FeedItem extends FeedItemBasic {
	
	
	public String key;
	public String img1Link;
	public String img2Link;
	
	public Date publishedDate;
	public Date storeDate;
	public long status;
	public Collection categories;

	public FeedItem(){};
	
	public FeedItem(String key, Date publishedDate, String source, String author, String authorName, String authorLink, String link, String title, 
			String feedLink, String imageLink, Long img1A, Date storeDate) {
		super(publishedDate, source, author, authorName, authorLink, link, title, feedLink,imageLink, img1A);
	
		this.key = key;
		this.storeDate = storeDate;
	}
	
	
	public String toJson() {
    	return new Gson().toJson(this);
    }
	
    public static FeedItem constructFromJson(String json) {
    	return new Gson().fromJson(json, FeedItem.class);
    }
    
    public static FeedItem createFromEntity(Entity entity) {
    	FeedItem feedItem = new FeedItem(
    			entity.getKey().getName(),
				(Date)entity.getProperty("publishedDate"),
				Util.translateSource(entity.getProperty("source").toString()), 
				entity.getProperty("author").toString(),
				(entity.getProperty("authorName") != null) ? entity.getProperty("authorName").toString() : entity.getProperty("author").toString(),
				(entity.getProperty("authorLink") != null) ? entity.getProperty("authorLink").toString() : null,
				entity.getProperty("link").toString(),
				(entity.getProperty("title") != null)?entity.getProperty("title").toString():"",
				entity.getProperty("feedLink").toString(),
				entity.getProperty("imageLink").toString(),
				Long.valueOf(entity.getProperty("img1A").toString()),
				(Date)entity.getProperty("storeDate"));
		
		feedItem.categories = ((Collection)entity.getProperty("categories"));
		feedItem.publishedDate = ((Date)entity.getProperty("publishedDate"));
		feedItem.status = ((Number)entity.getProperty("status")).longValue();
		feedItem.img1Link = (String)entity.getProperty("img1Link");
		feedItem.img2Link = (String)entity.getProperty("img2Link");
		
		return feedItem;
    }
}
