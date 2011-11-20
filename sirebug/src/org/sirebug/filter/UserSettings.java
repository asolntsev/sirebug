package org.sirebug.filter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class UserSettings implements Serializable {
  public static boolean isEnabledInCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null || cookies.length == 0)
      return true;

    for (Cookie cookie : cookies) {
      if (Consts.COOKIE_SIREBUG_STATUS.equals(cookie.getName())) {
        return Consts.STATUS_ENABLED.equalsIgnoreCase(cookie.getValue());
      }
    }
    return true;
  }
}

