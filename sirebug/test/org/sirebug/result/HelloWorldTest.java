package org.sirebug.result;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sirebug.config.ConfigurationTest;
import org.sirebug.config.Instrumentator;
import org.sirebug.config.SirebugConfiguration;

import java.io.IOException;
import java.util.List;

public class HelloWorldTest {
  /*@Test
  public void testHelloWorldWithoutTracking()
   {
     HelloWorld hw = new HelloWorld();
     assertEquals(1, hw.say("yop"));
     assertEquals(2, hw.say("yop"));
     assertEquals(3, hw.sayHello());
   }*/

  @Test
  public void testRecording() throws NotFoundException, IOException, CannotCompileException {
    SirebugConfiguration config = ConfigurationTest.createTestConfiguration();
    Instrumentator.instrumentClasses(config);

    ExecutionContext.startRecording();

    HelloWorld hw = new HelloWorld();
    assertEquals(1, hw.say("yop"));
    assertEquals(2, hw.say("yop"));
    assertEquals(3, hw.sayHello());

    ThreadExecutionHistory history = ExecutionContext.finishRecording();
    assertFalse("Execution context is empty", history.getMethodsExecutions().isEmpty());

    List<MethodExecution> sayExecutions = history.getMethodExecutions("say");
    assertEquals(4, sayExecutions.size());

    List<MethodExecution> sayHelloExecutions = history.getMethodExecutions("keepSilence");
    assertNull(sayHelloExecutions);
    // assertEquals(0, sayHelloExecutions.size());
  }
}
