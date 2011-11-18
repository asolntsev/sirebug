package org.sirebug.config;

import junit.framework.TestCase;

import java.net.URL;
import java.util.List;

public class SirebugConfigurationParserTest extends TestCase {
  public void testParseXml() {
    URL urlConfigXml = Thread.currentThread().getContextClassLoader().getResource("sirebug.cfg.xml");
    assertNotNull(urlConfigXml);

    SirebugConfiguration config = SirebugConfigurationParser.parseXml(urlConfigXml);
    assertNotNull(config);

    List<Watch> watches = config.getWatches();
    assertEquals(2, watches.size());

    {
      List<TrackedMethod> trackedMethods = watches.get(0).getTrackedMethods();
      assertEquals(1, trackedMethods.size());

      assertEquals("debug-logs", watches.get(0).getName());
      assertEquals("org.apache.log4j.Category", trackedMethods.get(0).getClassName());
      assertEquals("debug", trackedMethods.get(0).getMethodName());
      assertEquals("none", watches.get(0).getSignal());
    }

    {
      List<TrackedMethod> trackedMethods = watches.get(1).getTrackedMethods();
      assertEquals(3, trackedMethods.size());
      assertEquals("HelloWorld", watches.get(1).getName());
      assertEquals("org.sirebug.HelloWorldServlet", trackedMethods.get(1).getClassName());
      assertEquals("doGet", trackedMethods.get(0).getMethodName());
      assertEquals("doPost", trackedMethods.get(1).getMethodName());
      assertEquals("service", trackedMethods.get(2).getMethodName());
      assertNull(watches.get(1).getSignal());
    }
  }
}
