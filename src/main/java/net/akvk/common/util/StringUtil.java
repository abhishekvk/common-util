package net.akvk.common.util;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

  public static String encloseWithin(String str, char charToEnclose) {
    return charToEnclose + str + charToEnclose;
  }

  public static String asJsonPair(String key, String value) {
    return encloseWithin(key, '"') + ":" + encloseWithin(value, '"');
  }

  public static boolean isNumeric(String s) {
    return s.matches("\\d+");
  }

  public static String formatForDisplay(String str) {
    if ((str == null) || (str.length() == 0)) {
      return "";
    }
    return str.toLowerCase().subSequence(0, 1).toString().toUpperCase() + str.toLowerCase().subSequence(1, str.length());
  }

  public static String parseNull(String str) {
    return parseNull(str, "");
  }

  public static String parseNull(String str, String str4Null) {
    return (str == null) ? str4Null : str;
  }

  public static String formatForEventDisplay(String str) {
    if (str == null) {
      str = "NA";
    }
    return str;
  }

  public static String getUUIDForURL() {
    String uuid = UUID.randomUUID().toString();
    while (!uuid.matches("^[a-fA-F].*")) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        LOGGER.error("getUUIDForURL -" + e);
      }
      uuid = UUID.randomUUID().toString();
    }
    return uuid;
  }
}
