package org.sirebug;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;

public class HelloWorldServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(HelloWorldServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.debug("HelloWorldServlet.doGet: " + new Date() + " : " + req.getQueryString());

    String message = req.getParameter("msg");
    if (message == null)
      message = "Hello, World!";

    HttpSession session = req.getSession(true);
    session.setAttribute("message", message);

    logErrors(req);
    logWarnings(req);

    String html =
        "<html><head><title>Hello World</title></head>" +
            "<body>" + message +
            "	<br/>&#160;<br/>" +
            "	<a href=\"?errors=3&warnings=7\">Hello real world</a><br/>" +
            "	<a href=\"?\">Hello ideal world</a><br/>" +
            "</body></html>";
    resp.setContentType("text/html");
    resp.getWriter().print(html);
  }

  private void logErrors(HttpServletRequest req) {
    String errors = req.getParameter("errors");
    if (errors != null) {
      int nErrorsCount = Integer.parseInt(errors);
      for (int i = 0; i < nErrorsCount; i++) {
        Exception e = new RuntimeException("Hello Error #" + i);
        log.error(e.getMessage(), e);
      }
    }
  }

  private void logWarnings(HttpServletRequest req) {
    String warnings = req.getParameter("warnings");
    if (warnings != null) {
      int nWarningsCount = Integer.parseInt(warnings);
      for (int i = 0; i < nWarningsCount; i++) {
        Exception e = new RuntimeException("Hello Warning #" + i);
        log.warn(e.getMessage(), e);
      }
    }
  }
}
