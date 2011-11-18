package org.sirebug.result;

import java.util.*;

public class ThreadExecutionHistory {
  private Map<String, List<MethodExecution>> methodsExecutions = new HashMap<String, List<MethodExecution>>();

  public Map<String, List<MethodExecution>> getMethodsExecutions() {
    return methodsExecutions;
  }

  public List<MethodExecution> getMethodExecutions(String watchName) {
    return methodsExecutions.get(watchName);
  }

  private final List<MethodExecution> executions(String watchName) {
    if (!methodsExecutions.containsKey(watchName)) {
      methodsExecutions.put(watchName, new ArrayList<MethodExecution>());
    }
    return methodsExecutions.get(watchName);
  }

  /**
   * Method returns Set of watch names which has some records.
   *
   * @return
   */
  public Set<String> getWatchNames() {
    return methodsExecutions.keySet();
  }

  public int numberOfErrors() {
    return numberOfSignals("error");
  }

  public int numberOfWarnings() {
    return numberOfSignals("warning");
  }

  private int numberOfSignals(String signal) {
    int counter = 0;
    for (Map.Entry<String, List<MethodExecution>> entry : methodsExecutions.entrySet()) {
      for (MethodExecution m : entry.getValue()) {
        if (m.getSignal() != null && m.getSignal().equals(signal)) {
          counter++;
        }
      }
    }
    return counter;
  }

  public void addExecution(String watchName, String signal, String classPlusMethod, Object[] params, Object result) {
    executions(watchName).add(new MethodExecution(watchName, signal, classPlusMethod, params, result));
  }

  public void addExecution(String watchName, String signal, String classPlusMethod, Object[] params, Throwable exception) {
    executions(watchName).add(new MethodExecution(watchName, signal, classPlusMethod, params, exception));
  }
}