package org.sirebug.filter;

import java.io.Serializable;

public class SirebugSession implements Serializable {
  // IP Check (used only if IP Check is enabled)
  private boolean ipCheckPerformed = false;
  private String clientIP;
  private String ipMask;
  private boolean ipCheckSucceeded;

  // Current session parameters

  /**
   * False if Sirebug is disabled for the current session.
   * This happens when user clicks button "disable" on UI.
   */
  private boolean sirebugEnabled = true;

  private SessionHistory history;

  public SirebugSession() {
    history = new SessionHistory();
  }

  public SessionHistory getHistory() {
    return history;
  }

  /**
   * @return the bIPCheckPerformed
   */
  public boolean isIpCheckPerformed() {
    return ipCheckPerformed;
  }

  /**
   * @param asCheckPerformed the bIPCheckPerformed to set
   */
  public void setIpCheckPerformed(boolean asCheckPerformed) {
    ipCheckPerformed = asCheckPerformed;
  }

  /**
   * @return the bIPCheckSucceeded
   */
  public boolean isIPCheckSucceeded() {
    return ipCheckSucceeded;
  }

  /**
   * @param asCheckSucceeded the bIPCheckSucceeded to set
   */
  public void setIPCheckSucceeded(boolean asCheckSucceeded) {
    ipCheckSucceeded = asCheckSucceeded;
  }

  public boolean isSirebugEnabled() {
    return sirebugEnabled;
  }

  public void setSirebugEnabled(boolean sirebugEnabled) {
    this.sirebugEnabled = sirebugEnabled;
  }
}

