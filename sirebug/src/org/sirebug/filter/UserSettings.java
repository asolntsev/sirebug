package org.sirebug.filter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class UserSettings implements Serializable {
  private final String clientIP;
  private boolean enabledForIP = true;

  public UserSettings(String sClientIP) {
    clientIP = sClientIP;
  }

  @Override
  public String toString() {
    return clientIP + (enabledForIP ? " enabled" : " disabled") + " for IP";
  }

  public static boolean isEnabledInCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null || cookies.length == 0)
      return true;

    for (int i = 0; i < cookies.length; i++) {
      if (Consts.COOKIE_HIREBUG_STATUS.equals(cookies[i].getName())) {
        return Consts.STATUS_ENABLED.equalsIgnoreCase(cookies[i].getValue());
      }
    }
    return true;
  }
}

