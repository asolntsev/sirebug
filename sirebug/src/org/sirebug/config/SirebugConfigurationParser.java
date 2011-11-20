package org.sirebug.config;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
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
        for (Element method : getMethods(el)) {
          watch.addMethod(extractMethod(el, method));
        }

        config.addWatch(watch);
      }

      return config;
    } catch (DocumentException e) {
      throw new RuntimeException("Error while parsing sirebug.cfg.xml", e);
    }
  }

  private static TrackedMethod extractMethod(Element el, Element method) {
    String className = getAttribute(method, "class");
    String methodName = getAttribute(method, "method");
    String pattern = el.getText();

    return new TrackedMethod(className, methodName, pattern);
  }

  private static String getAttribute(Element method, String attributeName) {
    Attribute attribute = method.attribute(attributeName);
    if (attribute == null) {
      throw new IllegalArgumentException("Missing attribute " + attributeName + " in Sirebug configuration");
    }
    return attribute.getText();
  }

  @SuppressWarnings("unchecked")
  private static List<Element> getMethods(Element el) {
    return el.elements("method");
  }

  private static String getOptionalAttribute(Element el, String attrName) {
    Attribute attr = el.attribute(attrName);
    return (attr == null) ? null : attr.getText();
  }
}
