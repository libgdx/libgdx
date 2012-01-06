/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.util.Map;
import java.util.WeakHashMap;

public class Thread implements Runnable {
  private long peer;
  private volatile boolean interrupted;
  private volatile boolean unparked;
  private boolean daemon;
  private byte state;
  private byte priority;
  private final Runnable task;
  private Map<ThreadLocal, Object> locals;
  private Object sleepLock;
  private ClassLoader classLoader;
  private UncaughtExceptionHandler exceptionHandler;
  private String name;
  private ThreadGroup group;

  private static UncaughtExceptionHandler defaultExceptionHandler;

  public static final int MIN_PRIORITY = 1;
  public static final int NORM_PRIORITY = 5;
  public static final int MAX_PRIORITY = 10;

  public Thread(ThreadGroup group, Runnable task, String name, long stackSize)
  {
    this.group = (group == null ? Thread.currentThread().group : group);
    this.task = task;
    this.name = name;

    Thread current = currentThread();

    Map<ThreadLocal, Object> map = current.locals;
    if (map != null) {
      for (Map.Entry<ThreadLocal, Object> e: map.entrySet()) {
        if (e.getKey() instanceof InheritableThreadLocal) {
          InheritableThreadLocal itl = (InheritableThreadLocal) e.getKey();
          locals().put(itl, itl.childValue(e.getValue()));
        }
      }
    }

    classLoader = current.classLoader;
  }

  public Thread(ThreadGroup group, Runnable task, String name) {
    this(group, task, name, 0);
  }

  public Thread(ThreadGroup group, String name) {
    this(null, null, name);
  }

  public Thread(Runnable task, String name) {
    this(null, task, name);
  }

  public Thread(Runnable task) {
    this(null, task, "Thread["+task+"]");
  }

  public Thread(String name) {
    this(null, null, name);
  }

  public Thread() {
    this((Runnable) null);
  }

  public synchronized void start() {
    if (peer != 0) {
      throw new IllegalStateException("thread already started");
    }

    state = (byte) State.RUNNABLE.ordinal();

    peer = doStart();
    if (peer == 0) {
      state = (byte) State.NEW.ordinal();
      throw new RuntimeException("unable to start native thread");
    }
  }

  private native long doStart();

  private static void run(Thread t) throws Throwable {
    try {
      t.run();
    } catch (Throwable e) {
      UncaughtExceptionHandler eh = t.exceptionHandler;
      UncaughtExceptionHandler deh = defaultExceptionHandler;
      if (eh != null) {
        eh.uncaughtException(t, e);
      } else if (deh != null) {
        deh.uncaughtException(t, e);
      } else {
        throw e;
      }
    } finally {
      synchronized (t) {
        t.state = (byte) State.TERMINATED.ordinal();
        t.notifyAll();
      }
    }
  }

  public void run() {
    if (task != null) {
      task.run();
    }
  }

  public ClassLoader getContextClassLoader() {
    return classLoader;
  }

  public void setContextClassLoader(ClassLoader v) {
    classLoader = v;
  }

  public Map<ThreadLocal, Object> locals() {
    if (locals == null) {
      locals = new WeakHashMap();
    }
    return locals;
  }

  public static native Thread currentThread();

  public void interrupt() {
    interrupt(peer);
  }

  private static native boolean interrupt(long peer);

  public boolean interrupted() {
    return interrupted(peer);
  }

  private static native boolean interrupted(long peer);

  public static boolean isInterrupted() {
    return currentThread().interrupted;
  }

  public static void sleep(long milliseconds) throws InterruptedException {
    Thread t = currentThread();
    if (t.sleepLock == null) {
      t.sleepLock = new Object();
    }
    synchronized (t.sleepLock) {
      t.sleepLock.wait(milliseconds);
    }
  }

  public static void sleep(long milliseconds, int nanoseconds)
    throws InterruptedException
  {
    if (nanoseconds > 0) {
      ++ milliseconds;
    }

    sleep(milliseconds);
  }

  public StackTraceElement[] getStackTrace() {
    long p = peer;
    if (p == 0) {
      return new StackTraceElement[0];
    }
    return Throwable.resolveTrace(getStackTrace(p));
  }

  private static native Object getStackTrace(long peer);

  public static native int activeCount();

  public static native int enumerate(Thread[] array);
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UncaughtExceptionHandler getUncaughtExceptionHandler() {
    UncaughtExceptionHandler eh = exceptionHandler;
    return (eh == null ? group : eh);
  }

  public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
    return defaultExceptionHandler;
  }

  public void setUncaughtExceptionHandler(UncaughtExceptionHandler h) {
    exceptionHandler = h;
  }

  public static void setDefaultUncaughtExceptionHandler
    (UncaughtExceptionHandler h)
  {
    defaultExceptionHandler = h;
  }

  public State getState() {
    return State.values()[state];
  }

  public boolean isAlive() {
    switch (getState()) {
    case NEW:
    case TERMINATED:
      return false;

    default:
      return true;
    }
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
      throw new IllegalArgumentException();
    }
    this.priority = (byte) priority;
  }

  public boolean isDaemon() {
    return daemon;
  }

  public synchronized void setDaemon(boolean v) {
    if (getState() != State.NEW) {
      throw new IllegalStateException();
    }

    daemon = v;
  }

  public static native void yield();

  public synchronized void join() throws InterruptedException {
    while (getState() != State.TERMINATED) {
      wait();
    }
  }

  public synchronized void join(long milliseconds) throws InterruptedException
  {
    long then = System.currentTimeMillis();
    long remaining = milliseconds;
    while (remaining > 0 && getState() != State.TERMINATED) {
      wait(remaining);

      remaining = milliseconds - (System.currentTimeMillis() - then);
    }
  }

  public void join(long milliseconds, int nanoseconds)
    throws InterruptedException
  {
    if (nanoseconds > 0) {
      ++ milliseconds;
    }

    join(milliseconds);
  }

  public ThreadGroup getThreadGroup() {
    return group;
  }

  public static native boolean holdsLock(Object o);

  public long getId() {
    return peer;
  }

  public interface UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e);
  }

  public enum State {
    NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
  }
  
}
