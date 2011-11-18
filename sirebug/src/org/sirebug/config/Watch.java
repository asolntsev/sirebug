package org.sirebug.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Watch {
  private final String name;
  private final String signal;
  private final List<TrackedMethod> trackedMethods;

  public Watch(String name, String signal) {
    this.name = name;
    this.signal = signal;
    this.trackedMethods = new ArrayList<TrackedMethod>();
  }

  public String getName() {
    return name;
  }

  public String getSignal() {
    return signal;
  }

  protected Watch addMethod(TrackedMethod method) {
    this.trackedMethods.add(method);
    return this;
  }

  public List<TrackedMethod> getTrackedMethods() {
    return Collections.unmodifiableList(trackedMethods);
  }
}
