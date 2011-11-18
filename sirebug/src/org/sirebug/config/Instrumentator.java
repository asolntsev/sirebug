package org.sirebug.config;

import javassist.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class Instrumentator {
  public static void instrumentClasses(SirebugConfiguration config) throws NotFoundException, IOException, CannotCompileException {
    final ClassPool parentClassPool = ClassPool.getDefault();
    final CtClass etype = parentClassPool.get("java.lang.Throwable");

    // This is needed in servlet containers
    ClassPool pool = new ClassPool(parentClassPool);
    pool.appendSystemPath();         // the same class path as the default one.
    pool.childFirstLookup = true;    // changes the behavior of the child.

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    while (cl != null) {
      if (cl instanceof URLClassLoader) {
        URLClassLoader urlCL = (URLClassLoader) cl;
        for (URL urlCPEntry : urlCL.getURLs()) {
          String fileName = urlCPEntry.getFile().replaceAll("%20", " ");
          pool.insertClassPath(fileName);
        }
      }
      cl = cl.getParent();
    }

    final Set<CtClass> instrumentedClasses = new HashSet<CtClass>();

    for (Watch watch : config.getWatches()) {
      for (TrackedMethod m : watch.getTrackedMethods()) {
        CtClass cc = pool.get(m.getClassName());
        cc.stopPruning(true);  // I am not sure this is a good idea... :(

        CtMethod[] cms = cc.getDeclaredMethods();
        for (CtMethod cm : cms) {
          if (cm.getName().equals(m.getMethodName())) {
            // TODO How to differentiate method with the same name, but different parameters?

            String sClassPlusName = m.getClassName() + ":" + m.getMethodName();
            cm.insertAfter("{org.sirebug.result.ExecutionContext.addExecution(\"" +
                watch.getName() + "\", \"" + watch.getSignal() + "\", \"" + sClassPlusName + "\", $args, ($r)$_);}");

            cm.addCatch("{org.sirebug.result.ExecutionContext.addExecution(\"" +
                watch.getName() + "\", \"" + watch.getSignal() + "\", \"" + sClassPlusName + "\", $args, $e); throw $e;}", etype);
          }
        }

        instrumentedClasses.add(cc);
      }
    }

    for (CtClass cc : instrumentedClasses) {
      cc.writeFile();

      // Make ClassLoader load the modified class
      // If you receive CannotCompileException (caused by LinkageError) here,
      // you probably initialize SirebugFilter too late. Please initialize it before
      // any other classes (Filters,Servlets) of your application.
      Class<?> c = cc.toClass();

      cc.detach();
    }

    instrumentedClasses.clear();
  }
}
