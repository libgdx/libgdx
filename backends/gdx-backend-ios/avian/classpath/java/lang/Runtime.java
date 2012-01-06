/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.util.StringTokenizer;

public class Runtime {
  private static final Runtime instance = new Runtime();

  private Runtime() { }

  public static Runtime getRuntime() {
    return instance;
  }

  public void load(String path) {
    if (path != null) {
      load(path, false);
    } else {
      throw new NullPointerException();
    }
  }

  public void loadLibrary(String path) {
    if (path != null) {
      load(path, true);
    } else {
      throw new NullPointerException();
    }
  }

  public Process exec(String command) throws IOException {
    StringTokenizer t = new StringTokenizer(command);
    String[] cmd = new String[t.countTokens()];
    for (int i = 0; i < cmd.length; i++)
      cmd[i] = t.nextToken();
    
    return exec(cmd);
  }

  public Process exec(final String[] command) throws IOException {
    final MyProcess[] process = new MyProcess[1];
    final Throwable[] exception = new Throwable[1];

    synchronized (process) {
      Thread t = new Thread() {
          public void run() {
            synchronized (process) {
              try {
                long[] info = new long[5];
                exec(command, info);
                process[0] = new MyProcess
                  (info[0], info[1], (int) info[2], (int) info[3],
                   (int) info[4]);
              } catch (Throwable e) {
                exception[0] = e;
              } finally {          
                process.notifyAll();
              }
            }

            MyProcess p = process[0];
            if (p != null) {
              synchronized (p) {
                try {
                  if (p.pid != 0) {
                    p.exitCode = Runtime.waitFor(p.pid, p.tid);
                    p.pid = 0;
                    p.tid = 0;
                  }
                } finally {
                  p.notifyAll();
                }
              }
            }
          }
        };
      t.setDaemon(true);
      t.start();

      while (process[0] == null && exception[0] == null) {
        try {
          process.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    if (exception[0] != null) {
      if (exception[0] instanceof IOException) {
        String message = "Failed to run \"" + command[0] + "\": " + exception[0].getMessage();
        throw new IOException(message);
      } else {
        throw new RuntimeException(exception[0]);
      }
    }

    return process[0];
  }

  public native void addShutdownHook(Thread t);

  private static native void exec(String[] command, long[] process)
    throws IOException;

  private static native int waitFor(long pid, long tid);

  private static native void load(String name, boolean mapName);

  private static native void kill(long pid);

  public native void gc();

  public native void exit(int code);

  public native long freeMemory();

  public native long totalMemory();

  private static class MyProcess extends Process {
    private long pid;
    private long tid;
    private final int in;
    private final int out;
    private final int err;
    private int exitCode;

    public MyProcess(long pid, long tid, int in, int out, int err) {
      this.pid = pid;
      this.tid = tid;
      this.in = in;
      this.out = out;
      this.err = err;
    }

    public void destroy() {
			if (pid != 0) {
      	kill(pid);
			}
    }

    public InputStream getInputStream() {
      return new FileInputStream(new FileDescriptor(in));
    }

    public OutputStream getOutputStream() {
      return new FileOutputStream(new FileDescriptor(out));
    }

    public InputStream getErrorStream() {
      return new FileInputStream(new FileDescriptor(err));
    }

    public synchronized int exitValue() {
      if (pid != 0) {
        throw new IllegalThreadStateException();
      }

      return exitCode;
    }

    public synchronized int waitFor() throws InterruptedException {
      while (pid != 0) {
        wait();
      }

      return exitCode;
    }
  }
}
