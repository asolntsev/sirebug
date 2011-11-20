package org.sirebug.config;

import org.junit.Test;

import static java.lang.Thread.currentThread;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;

public class SirebugConfigurationParserTest {
  @Test
  public void parsesConfigurationXml() {
    SirebugConfiguration config = parse("sirebug.cfg.xml");

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

  @Test(expected = IllegalArgumentException.class)
  public void classAttributeIsRequired() {
    parse("sirebug.cfg.with-missing-class.xml");
  }

  @Test(expected = IllegalArgumentException.class)
  public void methodAttributeIsRequired() {
    parse("sirebug.cfg.with-missing-method.xml");
  }

  private SirebugConfiguration parse(String configurationFile) {
    URL urlConfigXml = currentThread().getContextClassLoader().getResource(configurationFile);
    assertNotNull(urlConfigXml);

    SirebugConfiguration config = SirebugConfigurationParser.parseXml(urlConfigXml);
    assertNotNull(config);
    return config;
  }
}
