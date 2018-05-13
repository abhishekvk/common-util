package net.akvk.common.util;

public interface FileWatcher {
  String getWatchedFilename();
  void onFileModified();
}
