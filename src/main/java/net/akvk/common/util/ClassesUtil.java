package net.akvk.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassesUtil implements FileWatcher {

    private static final String CLASSES_FILE = "classes.properties";
    private static Properties classes = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassesUtil.class);

    private static final ReadWriteLock RWL = new ReentrantReadWriteLock();
    private static final Lock RLOCK = RWL.readLock();
    private static final Lock WLOCK = RWL.writeLock();

    static {
        loadClasses();
    }

    private static void loadClasses() {
        try (InputStream is = FileUtil.getConfigStream(CLASSES_FILE)) {
            classes = new Properties();
            classes.load(is);
        } catch (IOException e) {
            classes = null;
            LOGGER.error("loadClasses - " + e.toString());
        }
    }

    public static String getClassName(String propertyName) {
        String retVal = null;
        RLOCK.lock();
        try {
            if (classes.containsKey(propertyName)) {
                retVal = classes.getProperty(propertyName);
            }
        } finally {
            RLOCK.unlock();
        }
        return retVal;
    }

    public static String[] getClassesName(String propertyName) {
        String[] retVal = null;
        String value = getClassName(propertyName);
        if (value != null) {
            retVal = value.split(",");
        } else {
            retVal = new String[0];
        }
        return retVal;
    }

    public static Class getClassFromName(String propertyName) throws Exception {
        String value = getClassName(propertyName);
        return Class.forName(value);
    }

    public static List<Class> getClassesFromName(String propertyName) throws Exception {
        List<Class> retVal = new ArrayList();
        String value = getClassName(propertyName);
        if (value != null) {
            String[] clazz = value.split(",");
            for (String cls : clazz) {
                retVal.add(Class.forName(cls.trim()));
            }
        }
        return retVal;
    }

    public static Object getObjectFromName(String propertyName) throws Exception {
        Class clazz = getClassFromName(propertyName);
        return clazz.getConstructor().newInstance();
    }

    public static Object getObjectFromName(String propertyName, Class argType, Object value) throws Exception {
        Class clazz = getClassFromName(propertyName);
        return clazz.getConstructor(argType).newInstance(value);
    }

    public static List<Object> getObjectsFromName(String propertyName) throws Exception {
        List<Class> clazzez = getClassesFromName(propertyName);
        List<Object> objects = new ArrayList(clazzez.size());
        for(Class clazz : clazzez) {
            objects.add(clazz.getConstructor().newInstance());
        }
        return objects;
    }

    @Override
    public void onFileModified() {
        LOGGER.info("onFileModified - " + CLASSES_FILE);
        WLOCK.lock();
        try {
            classes.clear();
            loadClasses();
        } finally {
            WLOCK.unlock();
        }
    }

    @Override
    public String getWatchedFilename() {
        return CLASSES_FILE;
    }
}
