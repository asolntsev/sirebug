package org.sirebug.filter;

import org.sirebug.result.MethodExecution;
import org.sirebug.result.ThreadExecutionHistory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.List;

public class SirebugServlet implements Filter {
  public static final String CONTENT_TYPE_PLAIN_TEXT = "text/plain";
  public static final String CONTENT_TYPE_HTML = "text/html";

  private String contextName;
  private String servletName;
  private String servletPath;
  private String imagePath;
  // private String webappRootFolder;


  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // webappRootFolder = filterConfig.getServletContext().getRealPath("/");

    this.contextName = filterConfig.getServletContext().getServletContextName();
    if (contextName == null)
      contextName = ""; // I guess it should never happen...

    servletName = filterConfig.getServletContext().getInitParameter(Consts.PARAM_SERVLET_PATH);
    if (servletName == null)
      servletName = "sirebug";

    servletPath = '/' + contextName + '/' + servletName;
    imagePath = servletName + "?image=";
  }

  public void destroy() {
  }

  private HttpSession getSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null && request.getRequestedSessionId() == null) {
      // Session doesn't exist = First request
      session = request.getSession(true);
    }
    /*else if (session == null)
      {
        // Session is expired. Don't re-create it.
        session = null;
      }*/

    return session;
  }

  private static void sendStatusCookie(HttpServletRequest request, HttpServletResponse response, boolean enabled) {
    Cookie cookie = new Cookie(Consts.COOKIE_SIREBUG_STATUS, enabled ?
        Consts.STATUS_ENABLED : Consts.STATUS_DISABLED);
    cookie.setPath(request.getContextPath());
    cookie.setMaxAge(60 * 60 * 24 * 30 * 12);  // 1 year
    response.addCookie(cookie);
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    if (!request.getServletPath().equals("/sirebug")) {
      chain.doFilter(req, res);
      return;
    }

    if (false) // SirebugSettings.isDisabled()
    {
      printText("Sorry, feature is not available on production system", res);
      return;
    }

    final HttpSession session = getSession(request);
    // System.out.println(CClass.getCodeSource(this.getClass()));

    String sAction = request.getParameter(Consts.PARAM_ACTION);
    String sImage = request.getParameter(Consts.PARAM_IMAGE);
    if (sAction == null && sImage != null) {
      URL urlImage = getClass().getClassLoader().getResource(sImage);
      ServletUtils.printBinary(response, ServletUtils.readBytes(urlImage));
      return;
    } else if (sAction == null && (session == null || session.isNew())) {
      printConfigurationPage(request, response);
      return;
    } else if (Consts.ACTION_DISABLE_FOR_CLIENT.equals(sAction)) {
      sendStatusCookie(request, response, false);
      response.sendRedirect(servletPath); // printCondifurationPage(request, response);
      return;
    } else if (Consts.ACTION_ENABLE_FOR_CLIENT.equals(sAction)) {
      sendStatusCookie(request, response, true);
      response.sendRedirect(servletPath); // printCondifurationPage(request, response);
      return;
    }

    if (session == null) {
      printText("Your session seems to be expired", response);
      return; // session expired?
    }

    final SirebugSession sbSession = (SirebugSession) session.getAttribute(Consts.KEY_SIREBUG_SESSION);
    if (sbSession == null) {
      printText("Your session seems to be expired", response);
      return; // session expired?
    }

    if (Consts.ACTION_ENABLE_FOR_SESSION.equals(sAction)) {
      sbSession.setSirebugEnabled(true);
      printText(" Sirebug is enabled for current session :)\nWelcome to the club!\n", response);
      session.setAttribute(Consts.KEY_SIREBUG_SESSION, sbSession);
      return;
    } else if (Consts.ACTION_DISABLE_FOR_SESSION.equals(sAction)) {
      sbSession.setSirebugEnabled(false);
      printDisabledConfirmationPage(request, response);
      session.setAttribute(Consts.KEY_SIREBUG_SESSION, sbSession);
      return;
    }

    String sHistoryStep = request.getParameter(Consts.PARAM_STEP);  // 1,2,3,...
    int step = Integer.parseInt(sHistoryStep);

    SessionHistory history = sbSession.getHistory();
    if (history == null) {
      printText("Your session seems to be expired", response);
      return;
    } else if (step < history.getMinStep()) {
      printText("Sorry, I can keep only last 4 steps. Step #" + step + " is too old.", response);
      return;
    } else if (step > history.getMaxStep()) {
      printText("Sorry, I can not show future steps.", response);
      return;
    }

    ThreadExecutionHistory threadSummary = history.getThreadExecutionHistory(step);

    StringBuilder sbResponse = new StringBuilder();
    printHtmlHeader(sbResponse);
    printThreadSummary(session, sbResponse, threadSummary, step);
    printConfigurationPage(request, sbSession, step, sbResponse);

    sbResponse.append("<div style=\"position:relative; width:100%; top:20px; text-align: left;\" id=\"sirebug_panel\">\n");

    final String sCategory = request.getParameter(Consts.PARAM_CATEGORY);  // "logs" | "sql" | "urls"
    // final String sLogLevel = request.getParameter(Consts.PARAM_LOG_LEVEL);	// "FATAL" : "ERROR" | "WARNING" | "INFO" | "DEBUG"

    List<MethodExecution> methodExecutions = threadSummary.getMethodExecutions(sCategory);
    ThreadExecutionHistoryPrinter.printMethodExecutions(sbResponse, methodExecutions);

    /*if (Consts.CATEGORY_LOGS.equals(sCategory))
      {
        LogsPrinter.printLogs(sbResponse, threadSummary.getLogs(), step, CStringUtils.nvl(sLogLevel, "DEBUG"));
      }
      else if (Consts.CATEGORY_ERRORS.equals(sCategory))
      {
        LogsPrinter.printLogs(sbResponse, threadSummary.getLogs(), step, CStringUtils.nvl(sLogLevel, "ERROR"));
      }
      else if (Consts.CATEGORY_LOG_PAYLOAD.equals(sCategory))
      {
        final String sLogID = request.getParameter(Consts.PARAM_LOG_ID);
        final long nLodID = Long.parseLong(sLogID);
        LogsPrinter.printLogPayload(sbResponse, threadSummary.getLogs(), nLodID);
      }
      else if (Consts.CATEGORY_SQL.equals(sCategory))
      {
        SqlPrinter.printSqlStatements(sbResponse, threadSummary.getSqlStatements());
      }
      else if (Consts.CATEGORY_URLS.equals(sCategory))
      {
        UrlsPrinter.printURLs(sbResponse, threadSummary.getUrls(), step);
      }
      else if (Consts.CATEGORY_SHOW_URL_CONTENT.equals(sCategory))
      {
        final String sUrl = request.getParameter(Consts.PARAM_URL);
        UrlsPrinter.showUrlContent(sbResponse, sUrl);
      }
      else if (Consts.CATEGORY_DB_LOCKS.equals(sCategory))
      {
        DbLocksPrinter.printDbLocks(sbResponse);
      }*/
    /*else
      {
        printText("unknown command: " + sCategory, response);
        return;
      }*/

    sbResponse.append("</div>\n");

    printHtmlFooter(sbResponse);
    printHTML(sbResponse.toString(), response);
  }

  public void printHtmlHeader(StringBuilder sbResponse) {
    sbResponse.append("<html><head><title>SireBug</title>");
    sbResponse.append("<style>body, td{font-family: Lucida Grande, Arial, sans-serif; color: #000000; font-size: 8pt; }\n ");
    // sbResponse.append("a:active,a:hover,#leftcol a:active,#leftcol a:hover {font-family: Verdana, sans-serif; font-size: 15px;  font-weight: normal; color: #737373;}");
    // sbResponse.append("a:hover {font-size: 9px;text-indent: 19px;line-height: 24px; color: #fff !important; height: 25px; width: 161px; overflow: hidden; background: url(/images/sirebug/super.gif) 0 -25px no-repeat;display: block;}");
    sbResponse.append("a {text-decoration: none; color: #737373;}");
    sbResponse.append("a:hover {color: white; background-color: #737373;}"); // text-decoration: underline;
    sbResponse.append("</style>");
    sbResponse.append("<link rel=\"shortcut icon\" href=\"").append(imagePath).append(Consts.IMG_FAVICON).append("\" type=\"image/x-icon\" />");
    //ThreadExectionHistoryPrinter.printSirebugHeader(sbResponse);
    sbResponse.append("</head><body>");
  }

  public void printHtmlFooter(StringBuilder sbResponse) {
    sbResponse.append("</body></html>");
  }

  public void printThreadSummary(HttpSession session, StringBuilder sbResponse,
                                 ThreadExecutionHistory t, int step) {
    String sServletPath = servletPath;
    if (session != null) {
      // TODO Add "jsessionid" to servlet name
      // sServletPath = CURLRewriter.addSessionKeys(sServletPath, session.getId(), servletPath);
    }

    ThreadExecutionHistoryPrinter.printThreadExecutionHistory(sbResponse, t, step,
        sServletPath, imagePath, false);
  }

  private static void printHTML(String sMessage, ServletResponse response) throws IOException {
    print(sMessage, response, CONTENT_TYPE_HTML);
  }

  private static void printText(String sMessage, ServletResponse response) throws IOException {
    print(sMessage, response, CONTENT_TYPE_PLAIN_TEXT);
  }


  private static void print(String sMessage, ServletResponse response, String sContentType) throws IOException {
    PrintWriter out = new PrintWriter(response.getOutputStream());
    try {
      response.setContentType(sContentType);
      out.println(sMessage);
    } finally {
      closeSilently(out);
    }
  }

  private static void closeSilently(Writer writer) {
    try {
      if (writer != null)
        writer.close();
    } catch (IOException ignore) {
    }
  }

  private void printDisabledConfirmationPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    StringBuilder sb = new StringBuilder();
    printHtmlHeader(sb);

    String sHistoryStep = request.getParameter(Consts.PARAM_STEP);  // 1,2,3,...
    String sRequestUri = request.getRequestURI();
    sb.append(" Sirebug is disabled for current session :( <br/>\n");
    sb.append(" You can enable it on <a href=\"").append(sRequestUri);
    sb.append("?").append(Consts.PARAM_STEP).append('=').append(sHistoryStep).append("\">SireBug configuration page</a><br/>\n");
    printHtmlFooter(sb);
    printHTML(sb.toString(), response);
  }

  private void printConfigurationPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    StringBuilder sb = new StringBuilder();
    printHtmlHeader(sb);
    printConfigurationPage(request, null, 0, sb);
    printHtmlFooter(sb);
    printHTML(sb.toString(), response);
  }

  private void printConfigurationPage(HttpServletRequest request, SirebugSession hbSession, int step, StringBuilder sb) {
    String sClientIP = request.getRemoteAddr();
    sb.append("<div style=\"position:absolute; width:230px; top:5px; right: 10px; text-align: left; \">");
    sb.append("<tt>Your IP is ").append(sClientIP).append(".</tt><br/>\n");

    sb.append("<tt>SireBug is ");
    if (UserSettings.isEnabledInCookies(request)) {
      sb.append("<font color='blue'>enabled</font> for this client.</tt><br/>\n");
    } else {
      sb.append("<font color='red'>disabled</font> for this client.</tt><br/>\n");
    }

    sb.append("<ul>\n");
    sb.append("	<li><a href=\"").append(servletPath).append("?");

    if (UserSettings.isEnabledInCookies(request)) {
      sb.append(Consts.PARAM_ACTION).append("=").append(Consts.ACTION_DISABLE_FOR_CLIENT);
      sb.append("\">Disable for client</a></li>\n");
    } else {
      sb.append(Consts.PARAM_ACTION).append("=").append(Consts.ACTION_ENABLE_FOR_CLIENT);
      sb.append("\">Enable for client</a></li>\n");
    }

    if (hbSession != null) {
      sb.append("	<li><a href=\"").append(servletPath).append("?");
      if (hbSession.isSirebugEnabled()) {
        sb.append(Consts.PARAM_ACTION).append("=").append(Consts.ACTION_DISABLE_FOR_SESSION);
        sb.append('&').append(Consts.PARAM_STEP).append('=').append(step);
        sb.append("\">Disable for session</a></li>\n");
      } else if (!hbSession.isSirebugEnabled() && UserSettings.isEnabledInCookies(request)) {
        sb.append(Consts.PARAM_ACTION).append("=").append(Consts.ACTION_ENABLE_FOR_SESSION);
        sb.append('&').append(Consts.PARAM_STEP).append('=').append(step);
        sb.append("\">Enable for session</a></li>\n");
      }
    }

    sb.append("</ul>\n");
    sb.append("</div>");
  }
}
