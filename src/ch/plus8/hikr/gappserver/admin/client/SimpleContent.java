package ch.plus8.hikr.gappserver.admin.client;

import com.google.appengine.api.datastore.Text;

public class SimpleContent {

	
	public SimpleContent(){}
	
	
	public SimpleContent(String key, String kind, String group, String title, Number sort, Number menu1_idx, Text content, String image) {
		super();
		this.key = key;
		this.kind = kind;
		this.group = group;
		this.title = title;
		this.image = image;
		this.sort = (sort != null) ? sort.intValue() : 0;
		this.menu1_idx = (menu1_idx != null) ? menu1_idx.intValue() : 0;
		this.content = (content != null) ? content.getValue() : null;
	}


	public String key;
	public String kind;
	public String group;
	public String title;
	public String image;
	public int sort;
	public int menu1_idx;
	public String content;
	
	
}
