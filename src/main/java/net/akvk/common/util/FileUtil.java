package net.akvk.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

  private static String cfgDir;

  static {
    cfgDir = System.getProperty("app.config.dir");
    if (cfgDir == null || cfgDir.trim().length() == 0) {
      cfgDir = System.getenv("APP_CONFIG_DIR");
      if (cfgDir == null || cfgDir.trim().length() == 0) {
        cfgDir = "";
      }
    }
  }

  public static String getConfigDir() {
    return cfgDir;
  }

  public static List<String> readFileToStringArray(String filePath) throws IOException {
    InputStream input = FileUtil.class.getClassLoader().getResourceAsStream(filePath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    String line = null;
    List<String> content = new ArrayList();
    while ((line = reader.readLine()) != null) {
      content.add(line);
    }
    reader.close();
    input.close();
    return content;

  }

  public boolean createDirectory(String dirPath, boolean writePermission, boolean exePermission) {
    boolean retVal = true;
    try {
      File createDir = new File(dirPath);
      if (!createDir.exists()) {
        createDir.mkdirs();
        Set<PosixFilePermission> perms = new HashSet();
        //add owners permission
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        //add group permissions
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        //add others permissions
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        Files.setPosixFilePermissions(Paths.get(dirPath), perms);

        retVal = createDir.exists();
      }
    } catch (Exception e) {
      LOGGER.error("createDirectory - ", e);
      retVal = false;
    }
    return retVal;
  }

  public boolean createSubFolders(String rootFolder, String[] subFolders, boolean writePermission, boolean exePermission) {
    boolean retVal = true;
    for (String folder : subFolders) {
      if (!createDirectory(rootFolder + "/" + folder, writePermission, exePermission)) {
        retVal = false;
        break;
      }
    }
    return retVal;
  }

  public static String readFileToString(String filePath) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.lineSeparator();

    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(ls);
    }
    return stringBuilder.toString();
  }

  public static String getOSPath(String path) {
    return System.getProperty("os.name").toLowerCase().contains("windows") ? path.replace("/", "\\") : path.replace("\\", "/");
  }

  public static InputStream getConfigStream(String filename) throws FileNotFoundException {
    File file = null;
    if (cfgDir.length() > 0) {
      file = new File(getOSPath(cfgDir + "/" + filename));
    }
    return (file != null && file.exists()) ? new FileInputStream(file) : Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
  }

}
