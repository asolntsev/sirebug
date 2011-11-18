package org.sirebug.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseWrapper extends HttpServletResponseWrapper {
  private final ByteArrayOutputStream output;
  private String m_sContentType;
  private int reportedContentLength;
  private PrintWriter printWriter;

  public ResponseWrapper(HttpServletResponse response) {
    super(response);
    output = new ByteArrayOutputStream();
  }

  public String toString() {
    return output.toString();
  }

  public byte[] toByteArray() {
    return output.toByteArray();
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStream() {
      public void write(int b) throws IOException {
        output.write(b);
      }

      @Override
      public void print(String s) throws IOException {
        output.write(s.getBytes());
      }

      @Override
      public void write(byte[] b) throws IOException {
        output.write(b);
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
      }

      @Override
      public void flush() throws IOException {
        output.flush();
      }

      @Override
      public void close() throws IOException {
        output.close();
      }
    };
  }

  public PrintWriter getWriter() {
    if (printWriter == null) {
      printWriter = new PrintWriter(output, true) {
        @Override
        public void flush() {
          super.flush();
        }

        @Override
        public void write(char[] buf, int off, int len) {
          super.write(buf, off, len);
        }

        @Override
        public PrintWriter append(CharSequence csq) {
          return super.append(csq);
        }

        @Override
        public void close() {
          super.close();
        }
      };
    }
    return printWriter;
  }

  public void setContentType(String sContentType) {
    m_sContentType = sContentType;
    super.setContentType(sContentType);
  }

  public void setContentLength(int length) {
    // Don's pass this value to super.setContentLength()!
    // Later we will set another content length
    reportedContentLength = length;
  }

  public String getContentType() {
    return m_sContentType;
  }

  void close() throws IOException {
    if (printWriter != null) {
      printWriter.flush();
      printWriter.close();
    }

    output.flush();
    output.close();
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    super.sendRedirect(location);
  }

  @Override
  public void flushBuffer() throws IOException {
    super.flushBuffer();
    output.flush();
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {
    super.sendError(sc, msg);
  }

  @Override
  public void sendError(int sc) throws IOException {
    super.sendError(sc);
  }

  @Override
  public void setHeader(String name, String value) {
    super.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    super.addHeader(name, value);
  }

  @Override
  public void setStatus(int sc) {
    super.setStatus(sc);
  }

  @Override
  public void setStatus(int sc, String sm) {
    super.setStatus(sc, sm);
  }
}
