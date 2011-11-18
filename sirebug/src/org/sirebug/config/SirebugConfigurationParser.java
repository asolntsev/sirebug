package org.sirebug.config;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class SirebugConfigurationParser {
  public static SirebugConfiguration parseXml(URL urlConfigXml) {
    try {
      SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(urlConfigXml);

      SirebugConfiguration config = new SirebugConfiguration();

      @SuppressWarnings("unchecked")
      List<Element> list = document.selectNodes("//sirebug/watch");
      for (Element el : list) {
        String name = el.attribute("name").getText();
        String signal = getOptionalAttribute(el, "signal");

        Watch watch = new Watch(name, signal);
        for (Iterator<Element> it = el.elementIterator("method"); it.hasNext(); ) {
          // TODO Check for null and throw appropriate exception
          Element method = it.next();
          String className = method.attribute("class").getText();
          String methodName = method.attribute("method").getText();
          String pattern = el.getText();

          watch.addMethod(new TrackedMethod(className, methodName, pattern));
        }

        config.addWatch(watch);
      }

      return config;
    } catch (DocumentException e) {
      throw new RuntimeException("Error while parsing sirebug.cfg.xml", e);
    }
  }

  private static String getOptionalAttribute(Element el, String attrName) {
    Attribute attr = el.attribute(attrName);
    return (attr == null) ? null : attr.getText();
  }
}
