package org.sirebug.config;

import javassist.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Instrumentator {
  public static void instrumentClasses(SirebugConfiguration config) throws NotFoundException, IOException, CannotCompileException {
    ClassPool parentClassPool = ClassPool.getDefault();
    CtClass exceptionType = parentClassPool.get("java.lang.Throwable");
    ClassPool pool = createClassPool(parentClassPool);

    Set<CtClass> instrumentedClasses = instrumentClasses(config, exceptionType, pool);

    detachClasses(instrumentedClasses);
  }

  private static Set<CtClass> instrumentClasses(SirebugConfiguration config, CtClass exceptionType, ClassPool pool) throws NotFoundException, CannotCompileException {
    final Set<CtClass> instrumentedClasses = new HashSet<CtClass>();

    for (Watch watch : config.getWatches()) {
      for (TrackedMethod m : watch.getTrackedMethods()) {

        CtClass cc = pool.get(m.getClassName());
        cc.stopPruning(true);  // I am not sure this is a good idea... :(

        List<CtMethod> methods = findMethods(cc, m);
        for (CtMethod cm : methods) {
          instrumentMethod(exceptionType, watch, m, cm);
        }

        instrumentedClasses.add(cc);
      }
    }
    return instrumentedClasses;
  }

  private static List<CtMethod> findMethods(CtClass cc, TrackedMethod m) throws NotFoundException {
    List<CtMethod> foundMethods = new ArrayList<CtMethod>();
    CtMethod[] cms = cc.getDeclaredMethods();
    for (CtMethod cm : cms) {
      if (cm.getName().equals(m.getMethodName())) {
        foundMethods.add(cm);
      }
    }
    return foundMethods;
  }

  private static void instrumentMethod(CtClass exceptionType, Watch watch, TrackedMethod m, CtMethod cm) throws CannotCompileException {
    String sClassPlusName = m.getClassName() + ":" + m.getMethodName();
    cm.insertAfter("{org.sirebug.result.ExecutionContext.addExecution(\"" +
        watch.getName() + "\", \"" + watch.getSignal() + "\", \"" + sClassPlusName + "\", $args, ($r)$_);}");

    cm.addCatch("{org.sirebug.result.ExecutionContext.addExecution(\"" +
        watch.getName() + "\", \"" + watch.getSignal() + "\", \"" + sClassPlusName + "\", $args, $e); throw $e;}", exceptionType);
  }

  private static ClassPool createClassPool(ClassPool parentClassPool) throws NotFoundException {
    // This is needed in servlet containers
    ClassPool pool = new ClassPool(parentClassPool);
    pool.appendSystemPath();         // the same class path as the default one.
    pool.childFirstLookup = true;    // changes the behavior of the child.

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    while (cl != null) {
      addClassloaderToPool(pool, cl);
      cl = cl.getParent();
    }
    return pool;
  }

  private static void addClassloaderToPool(ClassPool pool, ClassLoader cl) throws NotFoundException {
    if (cl instanceof URLClassLoader) {
      URLClassLoader urlCL = (URLClassLoader) cl;
      for (URL urlCPEntry : urlCL.getURLs()) {
        String fileName = urlCPEntry.getFile().replaceAll("%20", " ");
        pool.insertClassPath(fileName);
      }
    }
  }

  private static void detachClasses(Set<CtClass> instrumentedClasses) throws NotFoundException, IOException, CannotCompileException {
    for (CtClass cc : instrumentedClasses) {
      cc.writeFile();

      // Make ClassLoader load the modified class
      // If you receive CannotCompileException (caused by LinkageError) here,
      // you probably initialize SirebugFilter too late. Please initialize it before
      // any other classes (Filters,Servlets) of your application.
      Class<?> c = cc.toClass();

      cc.detach();
    }
  }
}
