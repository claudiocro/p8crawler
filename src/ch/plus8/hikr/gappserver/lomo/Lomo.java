package ch.plus8.hikr.gappserver.lomo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.api.client.util.Key;

public class Lomo {

	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
	
	@Key("page")
	public int page;
	
	@Key("perPage")
	public int perPage;
	
	@Key("totalEntries")
	public int totalEntries;
	
	@Key("photos")
	public List<Photo> photos;
	
	
	public List<Asset> getBigImages(){
		List<Asset> bigImages = new ArrayList<Asset>();
		if(photos != null) {
			for(Photo photo : photos) {
				if(photo.assets != null && photo.assets.large != null) {
					bigImages.add(photo.assets.large);
				}
			}
		}
		
		return bigImages;
	}
	
	public static final class Photo {
		@Key("id")
		public int id;
		
		@Key("title")
		public String title;
		
		@Key("description")
		public String description;
		
		@Key("url")
		public String url;
		
		@Key("assets")
		public Assets assets;
		
		@Key("user")
		public User user;
		
		@Key("created_at")
		public String createdAt;
		
		@Key("camera")
		public Camera camera;
	}
	
	public static final class Camera {
		
		@Key("id")
		public int id;
		
		@Key("name")
		public String name;
		
	}
	
	public static final class Assets {
		@Key("small")
		public Asset small;
		
		@Key("large")
		public Asset large;
	}
	
	public static final class Asset {
		@Key("url")
		public String url;
		
		@Key("width")
		public int width;
		
		@Key("height")
		public int height;
	}
	
	public static final class User {
		
		@Key("username")
		public String username;
		
		@Key("url")
		public String url;
	}
	
}
