package ch.plus8.hikr.gappserver.admin.client;

public class Datastore {
	
	public String key;
	public String kind;
	public String dropboxUid;
	public String title;
	
	public Datastore() {}
	
	public Datastore(String key, String kind, String dropboxUid, String title) {
		this.key = key;
		this.kind = kind;
		this.dropboxUid = dropboxUid;
		this.title = title;
	}
	
	
}
