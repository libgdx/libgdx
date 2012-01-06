/* Copyright (c) 2009-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import avian.Cell;

public class ThreadGroup implements Thread.UncaughtExceptionHandler {
  private final ThreadGroup parent;
  private final String name;
  private Cell<ThreadGroup> subgroups;

  public ThreadGroup(ThreadGroup parent, String name) {
    this.parent = parent;
    this.name = name;

    synchronized (parent) {
      parent.subgroups = new Cell(this, subgroups);
    }
  }

  public ThreadGroup(String name) {
    this(Thread.currentThread().getThreadGroup(), name);
  }

  public void uncaughtException(Thread t, Throwable e) {
    if (parent != null) {
      parent.uncaughtException(t, e);
    } else {
      Thread.UncaughtExceptionHandler deh
        = Thread.getDefaultUncaughtExceptionHandler();
      if (deh != null) {
        deh.uncaughtException(t, e);
      } else if (! (e instanceof ThreadDeath)) {
        e.printStackTrace();
      }
    }
  }

  public ThreadGroup getParent() {
    return parent;
  }

  public String getName() {
    return name;
  }

  public int activeCount() {
    int allCount = Thread.activeCount();
    Thread[] all = new Thread[allCount];
    allCount = Thread.enumerate(all);

    int count = 0;
    for (int i = 0; i < allCount; ++i) {
      if (parentOf(all[i].getThreadGroup())) {
        ++ count;
      }
    }

    return count;
  }

  public int enumerate(Thread[] threads) {
    return enumerate(threads, true);
  }

  public int enumerate(Thread[] threads, boolean recurse) {
    int allCount = Thread.activeCount();
    Thread[] all = new Thread[allCount];
    allCount = Thread.enumerate(all);

    int count = 0;
    for (int i = 0; i < allCount && count < threads.length; ++i) {
      Thread t = all[i];
      ThreadGroup g = t.getThreadGroup();
      if (g == this || (recurse && parentOf(g))) {
        threads[count++] = t;
      }
    }

    return count;
  }  

  public boolean parentOf(ThreadGroup g) {
    for (; g != null; g = g.parent) {
      if (g == this) {
        return true;
      }
    }

    return false;
  }

  public int enumerate(ThreadGroup[] groups, boolean recurse) {
    return enumerate(groups, recurse, 0);
  }

  private int enumerate(ThreadGroup[] groups, boolean recurse, int count) {
    for (Cell<ThreadGroup> c = subgroups; c != null && count < groups.length;
         c = c.next)
    {
      ThreadGroup g = c.value;
      groups[count++] = g;
      if (recurse) {
        count = g.enumerate(groups, true, count);
      }
    }
    return count;
  }
}
