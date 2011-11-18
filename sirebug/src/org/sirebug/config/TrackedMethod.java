package org.sirebug.config;

public class TrackedMethod {
  private final String className;
  private final String methodName;
  private final String outputTemplate;

  public TrackedMethod(String className, String methodName, String outputTemplate) {
    this.className = className;
    this.methodName = methodName;
    this.outputTemplate = outputTemplate;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getOutputTemplate() {
    return outputTemplate;
  }
}
