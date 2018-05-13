package net.akvk.common.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatchService implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(DirectoryWatchService.class);

  private Path dir;
  private WatchService watcher;
  private DirectoryWatcher dirWatcher;

  public DirectoryWatchService(String path, DirectoryWatcher dWatcher) {
    if (path == null || path.trim().length() == 0) {
      dir = null;
      logger.error("Got null or empty path");
      throw new InvalidPathException(path, "null or empty path");
    }
    
    if(dWatcher == null) {
      throw new IllegalArgumentException("Directory Watcher is null");
    }
    
    dirWatcher = dWatcher;
    dir = Paths.get(path);
    if (!dir.toFile().exists()) {
      dir = null;
      logger.error("Path does not exist " + path);
      throw new InvalidPathException(path, "Path does not exist.");
    }

    try {
      logger.info("Request to watch " + path);
      watcher = FileSystems.getDefault().newWatchService();
      dir.register(watcher, ENTRY_MODIFY);
    } catch (Exception e) {
      logger.error("DirectoryWatchService - " + e);
    }
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

          if(kind == ENTRY_CREATE) {
            dirWatcher.onFileCreated(filename);
          } else if (kind == ENTRY_MODIFY) {
            dirWatcher.onFileModified(filename);
          } else if(kind == ENTRY_DELETE) {
            dirWatcher.onFileDeleted(filename);
          }
        }
        valid = key.reset();
      } catch (InterruptedException ie) {
        logger.error("Thread is interrupted. The process may have stopped. Stop monitoring " + dir + ". " + ie.toString());
        valid = false;
      } catch (Exception e) {
        logger.error("run - " + e);
      }
    }

    try {
      watcher.close();
    } catch (IOException ioe) {
      logger.error(ioe.toString());
    }
  }
}