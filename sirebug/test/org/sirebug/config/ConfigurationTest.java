package org.sirebug.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigurationTest {
  public static SirebugConfiguration createTestConfiguration() {
    SirebugConfiguration config = new SirebugConfiguration();
    config.addWatch(new Watch("say", "none")
        .addMethod(new TrackedMethod("org.sirebug.result.HelloWorld", "say", "say($1)->$_"))
        .addMethod(new TrackedMethod("org.sirebug.result.HelloWorld", "sayHello", "sayHello()->$_")));

    config.addWatch(new Watch("keepSilence", "none")
        .addMethod(new TrackedMethod("org.sirebug.result.HelloWorld", "keepSilence", "keepSilence()->$_")));

    return config;
  }

  @Test
  public void testSize() {
    assertEquals(0, new SirebugConfiguration().getWatches().size());

    assertEquals(2, createTestConfiguration().getWatches().size());
    assertEquals(2, createTestConfiguration().getWatches().get(0).getTrackedMethods().size());
    assertEquals(1, createTestConfiguration().getWatches().get(1).getTrackedMethods().size());
  }
}
