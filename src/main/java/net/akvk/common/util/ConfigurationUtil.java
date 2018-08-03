package net.akvk.common.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationUtil implements FileWatcher {

  private static final String CONFIG_FILE = "config.properties";
  private static Properties properties = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtil.class);

  private static final ReadWriteLock RWL = new ReentrantReadWriteLock();
  private static final Lock RLOCK = RWL.readLock();
  private static final Lock WLOCK = RWL.writeLock();

  static {
    loadProperties();
  }

  private static void loadProperties() {
    try (InputStream is = FileUtil.getConfigStream(CONFIG_FILE)) {
      properties = new Properties();
      properties.load(is);
      is.close();
    } catch (Exception e) {
      LOGGER.error("loadProperties - " + e.toString());
    }
  }

  public static String getProperty(String propertyName, String defaultValue) {
    String retVal = defaultValue;
    RLOCK.lock();
    try {
      if (properties.containsKey(propertyName)) {
        retVal = properties.getProperty(propertyName);
      }
    } finally {
      RLOCK.unlock();
    }
    return retVal;
  }

  public static String getProperty(String propertyName) {
    return getProperty(propertyName, null);
  }

  public static String[] getMultiValuedProperty(String propertyName, String separator) {
    String value = getProperty(propertyName);
    String[] retVal = {};
    if (value != null) {
      retVal = value.split(separator);
    }
    return retVal;
  }

  public static String[] getMultiValuedProperty(String propertyName) {
    return getMultiValuedProperty(propertyName, ",");
  }

  @Override
  public void onFileModified() {
    LOGGER.info("onFileModified - " + CONFIG_FILE);
    WLOCK.lock();
    try {
      properties.clear();
      loadProperties();
    } finally {
      WLOCK.unlock();
    }
  }

  @Override
  public String getWatchedFilename() {
    return CONFIG_FILE;
  }
}
