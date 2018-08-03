package net.akvk.common.util;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlUtil implements FileWatcher {

  private static final String SPARQL_FILE = "sparql.properties";
  private static Properties sparql = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(SparqlUtil.class);

  private static final ReadWriteLock RWL = new ReentrantReadWriteLock();
  private static final Lock RLOCK = RWL.readLock();
  private static final Lock WLOCK = RWL.writeLock();

  static {
    loadSparql();
  }

  private static void loadSparql() {

    try (InputStream is = FileUtil.getConfigStream(SPARQL_FILE)) {
      sparql = new Properties();
      sparql.load(is);
    } catch (Exception e) {
      LOGGER.error("loadSparql - " + e.toString());
    }
  }

  public static String getQuery(String queryKey) {
    RLOCK.lock();
    try {
      return sparql.getProperty(queryKey);
    } finally {
      RLOCK.unlock();
    }
  }

  public static String getQuery(String queryKey, Map<String, String> substitution) {
    String query = getQuery(queryKey);
    if (query != null && substitution != null) {
      Set<String> targets = substitution.keySet();
      for (String target : targets) {
        query = query.replaceAll(target, substitution.get(target));
      }
    }
    return query;
  }

  public static String getQuery(String queryKey, String target, String replacement) {
    String query = getQuery(queryKey);
    if (query != null && target != null) {
      query = query.replaceAll(target, replacement);
    }
    return query;
  }

  @Override
  public void onFileModified() {
    LOGGER.info("onFileModified - " + SPARQL_FILE);
    WLOCK.lock();
    try {
      sparql.clear();
      loadSparql();
    } finally {
      WLOCK.unlock();
    }

  }

  @Override
  public String getWatchedFilename() {
    return SPARQL_FILE;
  }
}
