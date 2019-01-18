package net.akvk.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandUtil implements FileWatcher {

    private static final String CMD_FILE = "commands.properties";
    private static Properties commands;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtil.class);

    private static final ReadWriteLock RWL = new ReentrantReadWriteLock();
    private static final Lock RLOCK = RWL.readLock();
    private static final Lock WLOCK = RWL.writeLock();

    static {
        loadCommands();
    }

    private static void loadCommands() {
        try (InputStream is = FileUtil.getConfigStream(CMD_FILE)) {
            commands = new Properties();
            commands.load(is);
        } catch (Exception e) {
            commands = null;
            LOGGER.error("loadCommands - " + e.toString());
        }
    }

    private static String getCommand(String key) {
        if (key == null || key.length() == 0) {
            throw new NullPointerException("Key is null or empty. Please provide a valid command key.");
        }
        RLOCK.lock();
        try {
            return commands.getProperty(key);
        } finally {
            RLOCK.unlock();
        }
    }

    public static String executeCommand(String cmd) throws Exception {
        StringBuilder retVal = new StringBuilder("");
        String line;
        if (cmd != null && cmd.trim().length() > 0) {
            String newLine = System.lineSeparator();
            Process process = Runtime.getRuntime().exec(cmd);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = in.readLine()) != null) {
                    retVal.append(line.trim()).append(newLine);
                }
                LOGGER.info("Exit value : " + process.waitFor());
                process.destroy();
            } catch (Exception e) {
                LOGGER.error("executeCommand - " + e);
                throw e;
            }
        }
        return retVal.toString();
    }

    public static String execute(String cmdKey) throws Exception {
        return executeCommand(getCommand(cmdKey));
    }

    public static String execute(String cmdKey, String target, String replacement) throws Exception {
        String command = getCommand(cmdKey);
        if (command != null && target != null) {
            command = command.replaceAll(target, replacement);
        }
        return execute(command);
    }

    public static String execute(String cmdKey, Map<String, String> substitution) throws Exception {
        String command = getCommand(cmdKey);
        if (command != null && substitution != null) {
            Set<String> targets = substitution.keySet();
            for (String target : targets) {
                command = command.replaceAll(target, substitution.get(target));
            }
        }
        return executeCommand(command);
    }

    public static String execute(String cmdKey, List<String> arguments) throws Exception {
        String command = getCommand(cmdKey);
        if (command != null && arguments != null && arguments.size() > 0) {
            StringBuilder sb = new StringBuilder(command);
            sb.append(" ").append(String.join(" ", arguments));
            command = sb.toString();
        }
        return executeCommand(command);
    }

    public static String execute(String cmdKey, String argument) throws Exception {
        String command = getCommand(cmdKey);
        if (command != null && argument != null && argument.length() > 0) {
            StringBuilder sb = new StringBuilder(command);
            sb.append(" ").append(argument);
            command = sb.toString();
        }
        return executeCommand(command);
    }

    public static Process executeProcess(String command) throws IOException {
        ProcessBuilder pBuilder = new ProcessBuilder(command);
        return pBuilder.start();
    }

    public static Process executeBackgroundProcess(String cmdKey) throws IOException {
        return executeProcess(getCommand(cmdKey));
    }

    public static Process executeBackgroundProcess(String cmdKey, String target, String replacement) throws IOException {
        String command = getCommand(cmdKey);
        if (command != null && target != null) {
            command = command.replaceAll(target, replacement);
        }
        return executeProcess(command);
    }

    public static Process executeBackgroundProcess(String cmdKey, Map<String, String> substitution) throws IOException {
        String command = getCommand(cmdKey);
        if (command != null && substitution != null) {
            Set<String> targets = substitution.keySet();
            for (String target : targets) {
                command = command.replaceAll(target, substitution.get(target));
            }
        }
        return executeProcess(command);
    }

    public static Process executeBackgroundProcess(String cmdKey, List<String> arguments) throws IOException {
        String command = getCommand(cmdKey);
        if (command != null && arguments != null && arguments.size() > 0) {
            StringBuilder sb = new StringBuilder(command);
            sb.append(" ").append(String.join(" ", arguments));
            command = sb.toString();
        }
        return executeProcess(command);
    }

    public static Process executeBackgroundProcess(String cmdKey, String argument) throws IOException {
        String command = getCommand(cmdKey);
        if (command != null && argument != null && argument.length() > 0) {
            StringBuilder sb = new StringBuilder(command);
            sb.append(" ").append(argument);
            command = sb.toString();
        }
        return executeProcess(command);
    }

    @Override
    public void onFileModified() {
        LOGGER.info("onFileModified - " + CMD_FILE);
        WLOCK.lock();
        try {
            commands.clear();
            loadCommands();
        } finally {
            WLOCK.unlock();
        }
    }

    @Override
    public String getWatchedFilename() {
        return CMD_FILE;
    }
}
