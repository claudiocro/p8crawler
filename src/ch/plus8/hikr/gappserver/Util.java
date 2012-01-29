package ch.plus8.hikr.gappserver;

public class Util
{
  public static final String GOOGLE_API_KEY = "AIzaSyD6FWIhhEskZwN2E_uTsrxZT-vs67px8-Y";
  public static final String[] sources = { "gplus", "hikr" };
  public static final String[] categories = { "photographer", "lomo", "lomo:popular", "lomo:selected", "monochromemonday", "mountainmonday" };

  public static boolean isInt(String integer)
  {
    try
    {
      Integer.valueOf(integer);
      return true; } catch (Exception e) {
    }
    return false;
  }

  public static boolean isBlank(String str)
  {
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
}