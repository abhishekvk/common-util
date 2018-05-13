package net.akvk.common.util;

public interface DirectoryWatcher {
  void onFileCreated(String filename);
  void onFileModified(String filename);
  void onFileDeleted(String filename);
}
