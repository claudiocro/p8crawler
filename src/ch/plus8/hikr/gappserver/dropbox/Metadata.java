package ch.plus8.hikr.gappserver.dropbox;

import java.util.List;

import com.google.api.client.util.Key;

public class Metadata {

	public static class DropboxEntity {
		@Key("size")
		public String size;
		
		@Key("bytes")
		public int bytes;

		@Key("hash")
		public String hash;

		@Key("thumb_exists")
		public boolean thumb_exists;

		@Key("rev")
		public String rev;

		@Key("modified")
		public String modified;

		@Key("path")
		public String path;

		@Key("is_dir")
		public boolean is_dir;

		@Key("icon")
		public String icon;

		@Key("root")
		public String root;

		@Key("contents")
		public List<DropboxContent> contents;
	}

	public static class DropboxContent {

		@Key("size")
		public String size;

		@Key("rev")
		public String rev;

		@Key("thumb_exists")
		public boolean thumb_exists;

		@Key("bytes")
		public long bytes;

		@Key("modified")
		public String modified;
		
		@Key("client_mtime")
		public String client_mtime;

		@Key("path")
		public String path;

		@Key("is_dir")
		public boolean is_dir;

		@Key("icon")
		public String icon;

		@Key("root")
		public String root;

		@Key("mime_type")
		public String mime_type;

		@Key("revision")
		public int revision;

	}
	
	
	public static class DropboxAccount {
		@Key("country")
		public String country;
		
		@Key("displayName")
		public String displayName;
		
		@Key("uid")
		public long uid;
		
		@Key("referralLink")
		public String referralLink;
		
		@Key("quota_info")
		public DropboxQuotaInfo quota_info;
	}
	
	public static class DropboxQuotaInfo {
		
		@Key("quota")
		public long quota;
		
		@Key("normal")
		public long normal;
		
		@Key("shared")
		public long shared;
		
	}
	
	public static class DropboxLink {
		
		@Key("url")
		public String url;
		
		@Key("expires")
		public String expires;
		
	}
}
