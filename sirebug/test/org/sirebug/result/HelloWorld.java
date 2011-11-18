package org.sirebug.result;

import java.io.Serializable;

public class HelloWorld implements Serializable {
  private int counter = 0;

  public int say(String message) {
    System.out.println(message);
    return ++counter;
  }

  public int sayHello() {
    return say("Hello");
  }
}
