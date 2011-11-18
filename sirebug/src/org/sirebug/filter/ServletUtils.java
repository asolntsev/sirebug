package org.sirebug.filter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;

public class ServletUtils {
  public static final String CONTENT_TYPE_HTML = "text/html";

  protected static final String HTTP_HEADER_LOCATION = "Location";

  public static void printHtml(ServletResponse res, String sResponse) throws IOException {
    res.setContentType(CONTENT_TYPE_HTML);
    res.setContentLength(sResponse.length());
    PrintWriter out = res.getWriter();
    try {
      out.write(sResponse);
    }
    finally {
      out.close();
    }
  }

  public static void printText(ServletResponse response, String sContent) throws IOException {
    response.setContentLength(sContent.length());
    PrintWriter out = new PrintWriter(response.getOutputStream());
    try {
      out.print(sContent);
    } finally {
      out.close();
    }
  }

  public static void printBinary(ServletResponse res, byte[] baResponse) throws IOException {
    if (baResponse != null && baResponse.length > 0) { // content can be empty, for example, when default servlet returns only http header ("not modified")
      OutputStream out = res.getOutputStream();
      try {
        out.write(baResponse);
      } finally {
        out.close();
      }
    }
  }

  public static void printBinary(HttpServletResponse res, byte[] baResponse, String sContentType, String sFileName) throws IOException {
    res.setContentType(sContentType);
    if (sFileName != null && sFileName.length() > 0)
      res.setHeader("Content-Disposition", "attachment;filename=" + sFileName);

    printBinary(res, baResponse);
  }

  public static final void closeSilently(OutputStream out) {
    if (out != null) {
      try {
        out.close();
      } catch (IOException ioe) {
        // keep silence
      }
    }
  }

  public static final void closeSilently(InputStream in) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException ioe) {
        // keep silence
      }
    }
  }

  public static final void closeSilently(InputStreamReader in) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException ioe) {
        // keep silence
      }
    }
  }

  public static final void closeSilently(Writer out) {
    if (out != null) {
      try {
        out.close();
      } catch (IOException ioe) {
        // keep silence
      }
    }
  }

  public static byte[] readBytes(URL in) throws IOException {
    return readBytes(in.openStream(), 512);
  }

  public static byte[] readBytes(InputStream in) throws IOException {
    return readBytes(in, 512);
  }

  public static byte[] readBytes(InputStream in, int initialSize) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(initialSize);
    writeBytes(in, out);
    return out.toByteArray();
  }

  public static boolean writeBytes(InputStream in, OutputStream out) throws IOException {
    boolean isEmptyFile = true;

    try {
      byte[] inputBuffer = new byte[1024];

      int bytesRead;
      while ((bytesRead = in.read(inputBuffer)) != -1) {
        out.write(inputBuffer, 0, bytesRead);
        isEmptyFile = false;
      }
    } finally {
      closeSilently(in);
      closeSilently(out);
    }

    return isEmptyFile;
  }

  public static boolean isHtml(String sContentType) {
    return sContentType != null && sContentType.startsWith("text/html");
  }

  /*@SuppressWarnings("unchecked")
   public static Map<String, Object> collectHttpParameters(HttpServletRequest httpRequest)
   {
     Map<String, Object> valuesHolder = new HashMap<String, Object>();
     Enumeration<String> eNames = httpRequest.getParameterNames();
     while (eNames.hasMoreElements())
     {
       String szKey = eNames.nextElement();
       String[] aszValues = httpRequest.getParameterValues(szKey);

       if (aszValues.length > 1)
         valuesHolder.put(szKey, aszValues);	// put the whole array
       else
         valuesHolder.put(szKey, aszValues[0]);	// aszValues[0]);	// put a single value ?
     }
     return valuesHolder;
   }*/
}

