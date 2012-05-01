package ch.plus8.hikr.gappserver.admin.client;

public class PageGroup {

	public PageGroup() {}

	public PageGroup(String key, String kind, String groupId, String title) {
		super();
		this.key = key;
		this.kind = kind;
		this.groupId = groupId;
		this.title = title;
	}

	public String key;
	public String kind;
	public String groupId;
	public String title;
	
}
