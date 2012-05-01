package ch.plus8.hikr.gappserver.admin.client;

import com.google.appengine.api.datastore.Text;

public class Gallery {

	public Gallery(){}
	
	public Gallery(String key, String kind, String ref, String title, Text desc) {
		super();
		this.key = key;
		this.kind = kind;
		this.ref = ref;
		this.title = title;
		this.desc = (desc != null) ? desc.getValue() : null;
	}

	public String key;
	public String kind;
	public String ref;
	public String title;
	public String desc;
	
}
