package ch.plus8.hikr.gappserver;

import java.util.logging.Logger;

public class Util {
	private static final Logger logger = Logger.getLogger(Util.class.getName());

	public static final Long ZERO = 0L;
	
	public static final Long ITEM_STATUS_NEW = 0L;
	public static final Long ITEM_STATUS_IMAGE_LINK_EVAL = 100L;
	public static final Long ITEM_STATUS_IMAGE_LINK_NO_EVAL_PROC = 110L;
	public static final Long ITEM_STATUS_IMAGE_LINK_PROCESS = 150L;
	public static final Long ITEM_STATUS_READY = 200L;
	public static final Long ITEM_STATUS_DELETED = -999L;
	
	
	public static final Long DATASTORE_APPENGINE = 1L;
	public static final Long DATASTORE_DROPBOX = 2L;
	public static final Long DATASTORE_GCS = 3L;
	public static final Long DATASTORE_UNKNOWN = 4L;
	

	public static final String GOOGLE_API_KEY = "AIzaSyD6FWIhhEskZwN2E_uTsrxZT-vs67px8-Y";
	
	
	public static final String[] sources = { "gplus", "hikr", "dropbox" };
	public static final String[] categories = { "photographer", "lomo", "lomo:popular", "lomo:selected", "monochromemonday", "mountainmonday" };
	public static final Long[] datastores = { 
		DATASTORE_APPENGINE, //appengine
		DATASTORE_DROPBOX, //dropbox
		DATASTORE_GCS  //google cloud storage
	};
	
	
	public static boolean isInt(String integer) {
		try {
			Integer.valueOf(integer);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isBlank(String str) {
		int strLen = str.length();
		if ((str == null) || (strLen == 0))
			return true;

		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String truncate(String value, int length) {
		if ((value != null) && (value.length() > length))
			value = value.substring(0, length);
		return value;
	}

	public static final String translateSource(String source) {
		if ("gplus".equals(source)) {
			return "google+";
		}
		return source;
	}
	
	
	public static String encodePath(String path) {
		if ((path == null) || (path.length() == 0)) {
			return path;
		}
		StringBuffer buf = encodePath(null, path);
		return ((buf == null) ? path : buf.toString());
	}

	public static StringBuffer encodePath(StringBuffer buf, String path) {
		if (buf == null) {
			for (int i = 0; i < path.length(); ++i) {
				char c = path.charAt(i);
				switch (c) {
				case ' ':
				case '"':
				case '#':
				case '%':
				case '\'':
				case ';':
				case '<':
				case '>':
				case '?':
					buf = new StringBuffer(path.length() << 1);
					break;
				case '!':
				case '$':
				case '&':
				case '(':
				case ')':
				case '*':
				case '+':
				case ',':
				case '-':
				case '.':
				case '/':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case ':':
				case '=':
				}
			}
			if (buf == null) {
				return null;
			}
		}
		synchronized (buf) {
			for (int i = 0; i < path.length(); ++i) {
				char c = path.charAt(i);
				switch (c) {
				case '%':
					buf.append("%25");
					break;
				case '?':
					buf.append("%3F");
					break;
				case ';':
					buf.append("%3B");
					break;
				case '#':
					buf.append("%23");
					break;
				case '"':
					buf.append("%22");
					break;
				case '\'':
					buf.append("%27");
					break;
				case '<':
					buf.append("%3C");
					break;
				case '>':
					buf.append("%3E");
					break;
				case ' ':
					buf.append("%20");
					break;
				case '!':
				case '$':
				case '&':
				case '(':
				case ')':
				case '*':
				case '+':
				case ',':
				case '-':
				case '.':
				case '/':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case ':':
				case '=':
				default:
					buf.append(c);
				}
			}

		}
		return buf;
	}

}