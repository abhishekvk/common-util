package net.akvk.common.util;

import com.github.oxo42.stateless4j.delegates.Action;

public interface ActionProvider {
  public Action getAction(String actionClass) throws Exception;
}
