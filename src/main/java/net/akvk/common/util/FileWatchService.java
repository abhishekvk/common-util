package net.akvk.common.util;

import java.io.IOException;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatchService implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileWatchService.class);

  private Path dir;
  private WatchService watcher;
  private Map<String, FileWatcher> fileWatchers;

  public FileWatchService(String path) {
    if (path == null || path.trim().length() == 0) {
      dir = null;
      LOGGER.error("Got null or empty path");
      throw new InvalidPathException(path, "null or empty path");
    }

    dir = Paths.get(path);
    if (!dir.toFile().exists()) {
      dir = null;
      LOGGER.error("Path does not exist " + path);
      throw new InvalidPathException(path, "Path does not exist.");
    }

    try {
      LOGGER.info("Request to watch " + path);
      watcher = FileSystems.getDefault().newWatchService();
      dir.register(watcher, ENTRY_MODIFY);
      fileWatchers = new ConcurrentHashMap();
    } catch (Exception e) {
      LOGGER.error("FileWatchService - " + e);
    }
  }

  public void watchFile(String filename, FileWatcher watcher) {
    fileWatchers.put(filename, watcher);
  }

  public boolean isFileWatched() {
    return (dir != null && !fileWatchers.isEmpty());
  }

  @Override
  public void run() {
    boolean valid = true;
    WatchKey key;
    while (valid) {
      try {
        key = watcher.take();
        for (WatchEvent event : key.pollEvents()) {
          WatchEvent.Kind kind = event.kind();

          WatchEvent<Path> ev = event;
          Path file = ev.context();
          String filename = file.toString();

          if (kind == ENTRY_MODIFY && fileWatchers.containsKey(filename)) {
            FileWatcher fileWatcher = fileWatchers.get(filename);
            fileWatcher.onFileModified();
          }
        }
        valid = key.reset();
      } catch (InterruptedException ie) {
        LOGGER.error("Thread is interrupted. The process may have stopped. Stop monitoring " + dir + ". " + ie.toString());
        valid = false;
      } catch (Exception e) {
        LOGGER.error("run - " + e);
      }
    }

    try {
      watcher.close();
    } catch (IOException ioe) {
      LOGGER.error(ioe.toString());
    }
  }
}
