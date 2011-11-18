package org.sirebug.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SirebugConfiguration {
  private final List<Watch> watches;

  public SirebugConfiguration() {
    this.watches = new ArrayList<Watch>();
  }

  protected void addWatch(Watch watch) {
    this.watches.add(watch);
  }

  public List<Watch> getWatches() {
    return Collections.unmodifiableList(watches);
  }
}
