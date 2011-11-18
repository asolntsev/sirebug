package org.sirebug.result;

public class MethodExecution {
  private final String watchName;
  private final String signal;
  private final String classPlusMethod;
  private final Object[] parameters;
  private final Object result;
  private final Throwable exception;

  public MethodExecution(String watchName, String signal, String classPlusMethod, Object[] parameters, Object result) {
    this.watchName = watchName;
    this.signal = signal;
    this.classPlusMethod = classPlusMethod;
    this.parameters = parameters;
    this.result = result;
    this.exception = null;
  }

  public MethodExecution(String watchName, String signal, String classPlusMethod, Object[] parameters, Throwable exception) {
    this.watchName = watchName;
    this.signal = signal;
    this.classPlusMethod = classPlusMethod;
    this.parameters = parameters;
    this.result = null;
    this.exception = exception;
  }

  public String getWatchName() {
    return watchName;
  }

  public String getSignal() {
    return signal;
  }

  public String getClassPlusMethod() {
    return classPlusMethod;
  }

  public Object[] getParameters() {
    return parameters;
  }

  public Object getResult() {
    return result;
  }

  public Throwable getException() {
    return exception;
  }
}
