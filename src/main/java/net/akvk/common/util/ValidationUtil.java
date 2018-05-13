package net.akvk.common.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtil implements FileWatcher {

  private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);

  private static final String VAILDATION_FILE = "validation.properties";
  private static Properties validation = null;

  private static final ReadWriteLock RWL = new ReentrantReadWriteLock();
  private static final Lock RLOCK = RWL.readLock();
  private static final Lock WLOCK = RWL.writeLock();

  static {
    loadValidation();
  }

  private static void loadValidation() {
    try (InputStream is = FileUtil.getConfigStream(VAILDATION_FILE)) {
      validation = new Properties();
      validation.load(is);
    } catch (Exception e) {
      logger.error("loadValidation - " + e.toString());
    }
  }

  public static boolean isValidPattern(String value, ValidationProperty property) {
    boolean retVal = false;
    RLOCK.lock();
    try {
      if (value != null && property != null) {
        if (!(retVal = value.matches(validation.getProperty(property.getKey())))) {
          logger.warn("isValidPattern - Validation error. Value: " + value + " , Pattern(" + property.getKey() + "):" + validation.getProperty(property.getKey()));
        }
      }
    } finally {
      RLOCK.unlock();
    }
    return retVal;
  }

  public static boolean isAllowedValue(String value, ValidationProperty property) {
    boolean retVal = false;
    RLOCK.lock();
    try {
      if (value != null && property != null) {
        String[] allowedValues = validation.getProperty(property.getKey()).split(",");
        for (String allowed : allowedValues) {
          if (value.equalsIgnoreCase(allowed.trim())) {
            retVal = true;
            break;
          }
        }
      }
    } finally {
      RLOCK.unlock();
    }
    return retVal;
  }

  public static boolean isReservedValue(String value, ValidationProperty property) {
    boolean retVal = false;
    RLOCK.lock();
    try {
      if (value != null && property != null) {
        String[] reservedNames = validation.getProperty(property.getKey()).split(",");
        if (reservedNames.length > 0) {
          String val = value.trim();
          for (String name : reservedNames) {
            if (val.equalsIgnoreCase(name.trim())) {
              logger.warn("isReservedName - " + value + " is a reserved name.");
              retVal = true;
              break;
            }
          }
        }
      }
    } finally {
      RLOCK.unlock();
    }
    return retVal;
  }

  public static boolean isNumber(String str) {
    boolean retVal = true;
    try {
      Long.parseLong(str);
    } catch (NumberFormatException e) {
      retVal = false;
      logger.warn("isNumber - " + str + " is not a number. " + e);
    }
    return retVal;
  }

  @Override
  public void onFileModified() {
    logger.info("onFileModified - " + VAILDATION_FILE);
    WLOCK.lock();
    try {
      validation.clear();
      loadValidation();
    } finally {
      WLOCK.unlock();
    }
  }

  @Override
  public String getWatchedFilename() {
    return VAILDATION_FILE;
  }
}
