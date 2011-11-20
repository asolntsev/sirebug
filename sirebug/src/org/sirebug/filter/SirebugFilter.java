package org.sirebug.filter;

import org.sirebug.config.Instrumentator;
import org.sirebug.config.SirebugConfiguration;
import org.sirebug.config.SirebugConfigurationParser;
import org.sirebug.result.ExecutionContext;
import org.sirebug.result.ThreadExecutionHistory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;

public class SirebugFilter implements Filter {
  private boolean active = false;
  private String contextName;
  private String servletName;

  public void init(FilterConfig filterConfig) throws ServletException {
    active = false;

    // imagePath = filterConfig.getInitParameter(HRBugConst.PARAM_IMAGE_PATH);	// ends with /

    this.contextName = filterConfig.getServletContext().getServletContextName();

    servletName = filterConfig.getInitParameter(Consts.PARAM_SERVLET_PATH);
    if (servletName == null)
      servletName = "sirebug";

    URL urlConfigXml = Thread.currentThread().getContextClassLoader().getResource("sirebug.cfg.xml");
    if (urlConfigXml == null) {
      // TODO Is it really error? Maybe it's ok for some applications?
      // throw new ServletException("Resource sirebug.cfg.xml is not found in classpath");

      //if (log.isInfoEnabled())
      //	log.info("Resource sirebug.cfg.xml is not found in classpath. Skip Sirebug for " + contextName);
      System.out.println("INFO: Resource sirebug.cfg.xml is not found in classpath. Skip Sirebug for " + contextName);
      return;
    }

    SirebugConfiguration config = SirebugConfigurationParser.parseXml(urlConfigXml);
    try {
      Instrumentator.instrumentClasses(config);
    } catch (Exception e) {
      throw new ServletException("Error while instrumenting classes with sirebug.cfg.xml configuration", e);
    }

    active = true;
  }

  public void destroy() {
  }

  private HttpSession getSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null && request.getRequestedSessionId() == null) {
      // Session doesn't exist = First request
      session = request.getSession(true);
    } else if (session == null) {
      // Session is expired. Don't re-create it.

      session = request.getSession(true); // FIXME temporarily create (otherwise I don't error in the 1st step)
    }

    return session;
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    if (!active) {
      chain.doFilter(req, res);
      return;
    }

    if (!(res instanceof HttpServletResponse)) {
      chain.doFilter(req, res);
      return;
    }

    if (false) // SirebugSettings.isDisabled()
    {
      // It happens on production or staging
      chain.doFilter(req, res);
      return;
    }

    HttpServletRequest request = (HttpServletRequest) req;
    if (!UserSettings.isEnabledInCookies(request)) {
      chain.doFilter(req, res);
      return;
    }

    HttpSession session = getSession(request);
    if (false && session == null) // bugfix: We still should execute SireBug and print its output on page!
    {
      // Session is expired. Don't re-create it, let the application handle this situation by itself
      chain.doFilter(req, res);
      return;
    }

    if (session != null) {
      SirebugSession sbSession = (SirebugSession) session.getAttribute(Consts.KEY_SIREBUG_SESSION);
      if (sbSession == null) {
        sbSession = new SirebugSession();
      } else if (!sbSession.isSirebugEnabled()) {
        // It happens if user clicked button "Close". In this case, Hrebug is disabled for current session.
        chain.doFilter(req, res);
        return;
      }
    }

    ResponseWrapper wrapper = new ResponseWrapper((HttpServletResponse) res);

    ExecutionContext.startRecording();

    int step = -1; // It will stay negative if session is not initialized by application
    final ThreadExecutionHistory t;
    try {
      chain.doFilter(req, wrapper);
    } finally {
      wrapper.close();
      t = ExecutionContext.finishRecording();

      session = getSession(request);
      if (session != null) {
        SirebugSession hbSession = (SirebugSession) session.getAttribute(Consts.KEY_SIREBUG_SESSION);
        if (hbSession == null) {
          hbSession = new SirebugSession();
        }

        step = hbSession.getHistory().addThreadExecutionHistory(t);
        try {
          session.setAttribute(Consts.KEY_SIREBUG_SESSION, hbSession);
        } catch (IllegalStateException e) {
          // Session is expired
        }
      }
    }

    if (ServletUtils.isHtml(wrapper.getContentType())) {
      String sResponse = wrapper.toString();
      /*
          * Unfortunately I cannot replace title because many automation tests use it :(
         if (t.hasErrors())
         {
           sResponse = sResponse.replaceAll( "<title>(.*)</title>", "<title> !!! ERRORS !!! - $1</title>" );
         }
         else if (t.hasWarnings())
         {
           sResponse = sResponse.replaceAll( "<title>(.*)</title>", "<title> ! Warnings ! - $1</title>" );
         }*/

      int indexHead = sResponse.indexOf("</head>");
      if (indexHead > 0) {
        // int nPreviousHeadIndex = 0;
        StringBuilder sbResponse = new StringBuilder(wrapper.toString().length()
            + t.getMethodsExecutions().size() * 100); //?
        /*while (indexHead > 0)
            {
              sbResponse.append(sResponse.substring(nPreviousHeadIndex, indexHead));
              // String sTemp = sResponse.substring(indexHead, indexHead+9);
              if (sResponse.charAt(indexHead+7) != '\'')
                ThreadSummaryPrinter.printSirebugHeader(sbResponse);
              nPreviousHeadIndex = indexHead;
              indexHead = sResponse.indexOf("</head>", 1+nPreviousHeadIndex);
            }*/

        int indexBody = sResponse.lastIndexOf("</body>"); // indexOf("</body>"); ?
        if (indexBody > 0) {
          int nPreviousBodyIndex = 0;
          while (indexBody > 0) {
            sbResponse.append(sResponse.substring(nPreviousBodyIndex, indexBody));
            printSirebugPanel(session, sbResponse, t, step);
            nPreviousBodyIndex = indexBody;
            indexBody = sResponse.indexOf("</body>", 1 + nPreviousBodyIndex);
          }

          sbResponse.append(sResponse.substring(nPreviousBodyIndex));
          sResponse = sbResponse.toString();
        }
      }

      ServletUtils.printHtml(res, sResponse);
    } else {
      ServletUtils.printBinary(res, wrapper.toByteArray());
    }
  }

  public void printSirebugPanel(HttpSession session, StringBuilder sbResponse,
                                ThreadExecutionHistory t, int step) {
    String sServletPath = this.servletName;
    if (session != null) {
      // Add "jsessionid" to the sirebug servlet name
      // sServletPath = CURLRewriter.addSessionKeys(sServletPath, session.getId(), servletPath);
      sServletPath = servletName + ";jsessionid=" + session.getId();
    }

    ThreadExecutionHistoryPrinter.printThreadIcon(sbResponse, t, step,
        contextName, sServletPath, "/" + contextName + '/' + sServletPath + "?image=");
  }

}
