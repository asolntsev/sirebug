package org.sirebug.result;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ExecutionContext {
  private static ThreadLocal<Boolean> recordingNow = new ThreadLocal<Boolean>();

  private static ThreadLocal<ThreadExecutionHistory> threadHistory = new ThreadLocal<ThreadExecutionHistory>();

  public static void startRecording() {
    recordingNow.set(TRUE);
    threadHistory.set(new ThreadExecutionHistory());
  }

  public static ThreadExecutionHistory finishRecording() {
    recordingNow.set(FALSE);
    ThreadExecutionHistory result = threadHistory.get();
    threadHistory.set(null);
    return result;
  }

  public static void addExecution(String watchName, String signal, String classPlusMethod, Object[] params, Object result) {
    if (threadHistory.get() != null)  // TODO when it is null?
      threadHistory.get().addExecution(watchName, signal, classPlusMethod, params, result);
  }

  public static void addExecution(String watchName, String signal, String classPlusMethod, Object[] params, int result) {
    if (threadHistory.get() != null)  // TODO when it is null?
      threadHistory.get().addExecution(watchName, signal, classPlusMethod, params, result);
  }

  public static void addExecution(String watchName, String signal, String classPlusMethod, Object[] params, Throwable exception) {
    if (threadHistory.get() != null)  // TODO when it is null?
      threadHistory.get().addExecution(watchName, signal, classPlusMethod, params, exception);
  }
}

