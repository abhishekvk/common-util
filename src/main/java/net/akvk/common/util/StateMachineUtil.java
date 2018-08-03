package net.akvk.common.util;

import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachine;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMachineUtil implements FileWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineUtil.class);

  private static final String STATE_MACHINE_FILE = "state-machine.ini";

  private static Ini stateMachineProperty;

  private static List<String> configuredStates = new ArrayList();

  private static final ReadWriteLock RWL = new ReentrantReadWriteLock();
  private static final Lock RLOCK = RWL.readLock();
  private static final Lock WLOCK = RWL.writeLock();

  static {
    loadStateMachine();
  }

  private static void loadStateMachine() {
    try (InputStream in = FileUtil.getConfigStream(STATE_MACHINE_FILE)) {
      stateMachineProperty = new Ini();
      stateMachineProperty.load(in);
      LOGGER.info("Number of state machines : " + stateMachineProperty.size());
    } catch (IOException ex) {
      LOGGER.error("loadStateMachine - Unable to load state machine file. " + ex);
    }
  }

  public synchronized static StateMachine stateMachine(String section, String initialState, ActionProvider actionProvider) throws Exception {
    StateMachine<String, String> stateMachine = new StateMachine(initialState);
    LOGGER.info("Create state machine for " + section + " with initial state " + initialState);
    Map<String, String> smSection = null;
    RLOCK.lock();
    try {
      smSection = stateMachineProperty.get(section);
    } finally {
      RLOCK.unlock();
    }
    if (smSection == null) {
      LOGGER.error("No section found for " + section);
      return stateMachine;
    }
    configuredStates.clear();
    updateStateMachine(stateMachine, initialState, smSection, actionProvider);
    return stateMachine;
  }

  private static void updateStateMachine(StateMachine stateMachine, String state, Map<String, String> section, ActionProvider provider) throws Exception {
    if (!configuredStates.contains(state)) {
      String entry = section.get("state." + state + ".entry");
      String exit = section.get("state." + state + ".exit");
      String permits = section.get("state." + state + ".permit");
      StateConfiguration cfg = stateMachine.configure(state);
      configuredStates.add(state);
      if (entry != null) {
        cfg.onEntry(provider.getAction(entry));
      }

      if (exit != null) {
        cfg.onExit(provider.getAction(exit));
      }

      if (permits != null) {
        String[] permitsArray = permits.split(",");
        for (String permit : permitsArray) {
          LOGGER.info("Permit :" + permit + ", state : " + section.get("state." + state + "." + permit));
          cfg.permit(permit, section.get("state." + state + "." + permit));
          updateStateMachine(stateMachine, section.get("state." + state + "." + permit), section, provider);
        }
      }
    }
  }

  @Override
  public void onFileModified() {
    LOGGER.info("onFileModified - " + STATE_MACHINE_FILE);
    WLOCK.lock();
    try {
      stateMachineProperty.clear();
      loadStateMachine();
    } finally {
      WLOCK.unlock();
    }
  }

  @Override
  public String getWatchedFilename() {
    return STATE_MACHINE_FILE;
  }
}
