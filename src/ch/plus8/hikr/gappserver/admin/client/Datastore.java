package ch.plus8.hikr.gappserver.admin.client;

public class Datastore {
	
	public String key;
	public String kind;
	public String uid;
	public String title;
	
	public Datastore() {}
	
	public Datastore(String key, String kind, String uid, String title) {
		this.key = key;
		this.kind = kind;
		this.uid = uid;
		this.title = title;
	}
	
	
}
