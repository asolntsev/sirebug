package org.sirebug.filter;

import org.sirebug.result.MethodExecution;
import org.sirebug.result.ThreadExecutionHistory;

import java.util.List;
import java.util.Map;

public class ThreadExecutionHistoryPrinter {
  public static void printLink(StringBuilder sbResponse, String name,
                               String sServletPath, boolean bOpenInNewWindow, String sAction) {
    printLink(sbResponse, null, name, -1, sServletPath, bOpenInNewWindow, sAction);
  }

  public static void printLink(StringBuilder sbResponse, String category, String name, int step,
                               String sServletPath, boolean bOpenInNewWindow) {
    printLink(sbResponse, category, name, step, sServletPath, bOpenInNewWindow, null);
  }

  public static void printLink(StringBuilder sbResponse, String category, String name, int step,
                               String sServletPath, boolean bOpenInNewWindow, String sAction) {
    final String sTarget = bOpenInNewWindow ? "target=\"_blank\"" : "";
    char delim = '?';

    sbResponse.append("<td> <a ").append(sTarget).append(" href=\"").append(sServletPath);
    if (category != null) {
      sbResponse.append(delim).append(Consts.PARAM_CATEGORY).append("=").append(category);
      delim = '&';
    }

    if (step >= 0) {
      sbResponse.append(delim).append(Consts.PARAM_STEP).append("=").append(step).append("\"");
      delim = '&';
    }

    if (sAction != null) {
      sbResponse.append(delim).append(Consts.PARAM_ACTION).append("=").append(sAction).append("\"");
      delim = '&';
    }

    sbResponse.append(">").append(name).append("</a> </td>");
  }

  /**
   * Show different pictures depending on page status (number of errors?)
   *
   * @param t
   * @return
   */
  private static String getIconFileName(ThreadExecutionHistory t) {
    if (t.numberOfErrors() > 0) {
      return Consts.IMG_ERRORS;
    } else if (t.numberOfWarnings() > 0) {
      return Consts.IMG_WARNING;
    }
    // TODO Add data checker (which performs SELECT's)
    else {
      return Consts.IMG_OK;
    }
  }

  private static String getIconHtmlCode(ThreadExecutionHistory t, int step,
                                        String contextName, String sServletPath, String sImagePath) {
    StringBuilder sb = new StringBuilder();
    sb.append("<a target=\"_blank\"");
    sb.append(" href=\"/").append(contextName).append('/').append(sServletPath);
    sb.append("?");
    //sb.append(Consts.PARAM_CATEGORY).append("=").append(Consts.CATEGORY_LOGS);

    if (step >= 0) {
      sb.append("&").append(Consts.PARAM_STEP).append("=").append(step);
    }

    sb.append("\">");

    sb.append("\t<img border=\"0\" align=\"right\" src=\"");
    sb.append(sImagePath).append(getIconFileName(t)).append("\" />");

    sb.append("</a>");
    return sb.toString();
  }

  public static void printThreadIcon(StringBuilder sbResponse, ThreadExecutionHistory t, int step,
                                     String contextName, String sServletPath, String sImagePath) {
    String sIconHtmlCode = getIconHtmlCode(t, step, contextName, sServletPath, sImagePath);

    sbResponse.append("");
    // printColouredLayer(sbResponse, t, sIconHtmlCode);
    sbResponse.append("<div style=\"position: absolute; z-index: 0; opacity: 0.7; right:5px; top:5px;\">")
        .append(sIconHtmlCode).append("</div>");
  }

  public static void printThreadExecutionHistory(StringBuilder sbResponse, ThreadExecutionHistory t, int step,
                                                 String sServletPath, String sImagePath,
                                                 boolean bOpenInNewWindow) {
    // boolean bShowPanelImmediatelly = (t.numberOfErrors() > 0);

    // sbResponse.append("<div align=\"right\"><tt> "); /*printClientIP(sbResponse)*/sbResponse.append("XXXXX</tt></div>");

    sbResponse.append("<center><div style=\"width:700px; top:5px;\" id=\"hirebug_panel\"");
    //if (bShowPanelImmediatelly || !bOpenInNewWindow)
    //	sbResponse.append(" style=\"visibility: visible;\"");
    //else
    sbResponse.append(" style=\"visibility: hidden;\"");
    sbResponse.append("><center>");
    sbResponse.append("<table align=\"center\" width=\"650\"><tr>");
    sbResponse.append("<td><img src=\"").append(sImagePath).append(Consts.IMG_LOGO).append("\" /></td>");

    // TODO Wouldn't it be more correct to take list of method names from SirebugConfiguration?
    Map<String, List<MethodExecution>> methods = t.getMethodsExecutions();
    for (String watchName : methods.keySet()) // TODO Sort alphabetically?
    {
      List<MethodExecution> executions = methods.get(watchName);
      printLink(sbResponse, watchName, watchName + ": " + executions.size(), step,
          sServletPath, bOpenInNewWindow);
    }

    final String sTarget = bOpenInNewWindow ? "target=\"_blank\"" : "";
    // sbResponse.append("<td> <a ").append(sTarget).append(" href=\"").append(sShowSessionURL).append("\">Show session</a> </td>");
    // sbResponse.append("<td> <a ").append(sTarget).append(" href=\"").append(sShowSessionHistoryURL).append("\">Show history</a> </td>");
    //sbResponse.append("<td> <a ").append(sTarget).append(" href=\"").append(sObjectCacheURL).append("\">Object Cache</a> </td>");

    if (bOpenInNewWindow) {
      // printLink(sbResponse, "Close", sServletPath, bOpenInNewWindow, Consts.ACTION_TURN_OFF);
      sbResponse.append("<td> <a target=\"_blank\" href=\"").append(sServletPath);
      sbResponse.append("?").append(Consts.PARAM_ACTION).append("=").append(Consts.ACTION_DISABLE_FOR_SESSION).append("\"");
      sbResponse.append("><img alt=\"Disable HireBug for current session\" border=\"0\" src=\"").append(sImagePath).append("close.gif\"/>").append("</a> </td>");
    }

    sbResponse.append("</tr></table>");

    sbResponse.append("</center></div></center>");
  }

  public static void printMethodExecutions(StringBuilder sbResponse, List<MethodExecution> methodExecutions) {
    if (methodExecutions == null || methodExecutions.isEmpty())
      return;

    sbResponse.append("<hr/>Method executions: <table border=\"1\">");

    MethodExecution first = methodExecutions.get(0);
    int numOfParams = first.getParameters().length;

    sbResponse.append("<thead><tr>");
    sbResponse.append("<td>").append("Result").append("</td>");
    for (int i = 1; i <= numOfParams; i++)
      sbResponse.append("<td>").append("Param [").append(i).append("]</td>");
    sbResponse.append("<td>").append("Exception").append("</td>");
    sbResponse.append("</tr></thead>");

    for (MethodExecution m : methodExecutions) {
      sbResponse.append("<tr>");
      sbResponse.append("<td>").append(nvl(m.getResult(), "&#160;")/*.replaceAll("\n", "<br/>")*/).append("</td>");

      for (int i = 0; i < numOfParams; i++) {
        sbResponse.append("<td><pre>");
        sbResponse.append(truncate(m.getParameters()[i], 3000));
        sbResponse.append("</pre></td>");
      }
      sbResponse.append("<td>").append(nvl(m.getException(), "&#160;")).append("</td>");
      sbResponse.append("</tr>");
    }

    sbResponse.append("</table><hr/>");
  }

  public static String nvl(Object obj, String defaultValue) {
    return obj == null ? defaultValue : obj.toString();
  }

  public static String truncate(String sMessage, int nMaxLength) {
    if (sMessage != null && sMessage.length() > nMaxLength)
      return sMessage.substring(0, nMaxLength);

    return sMessage;
  }

  public static String truncate(Object message, int nMaxLength) {
    if (message == null)
      return "";

    return truncate(message.toString(), nMaxLength);
  }

}
