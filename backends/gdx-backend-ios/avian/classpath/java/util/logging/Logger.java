/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.logging;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Logger {
  private final String name;
  private Level levelValue = null;
  private static final ArrayList<Handler> handlers;
  private static Logger rootLogger;
  private Logger parent;

  static {
    rootLogger = new Logger("");
    rootLogger.setLevel(Level.INFO);
    handlers = new ArrayList<Handler>();
    handlers.add(new DefaultHandler());
  }

  public static Logger getLogger(String name) {
    if (name.equals("")) return rootLogger;
    Logger logger = new Logger(name);
    logger.parent = rootLogger;
    return logger;
  }

  private Logger(String name) {
    this.name = name;
  }

  public Handler[] getHandlers() {
    return handlers.toArray(new Handler[handlers.size()]);
  }

  public void addHandler(Handler handler) {
    handlers.add(handler);
  }

  public void removeHandler(Handler handler) {
    handlers.remove(handler);
  }

  public Logger getParent() {
    return parent;
  }

  public void fine(String message) {
    log(Level.FINE, Method.getCaller(), message, null);
  }

  public void info(String message) {
    log(Level.INFO, Method.getCaller(), message, null);
  }

  public void warning(String message) {
    log(Level.WARNING, Method.getCaller(), message, null);
  }

  public void severe(String message) {
    log(Level.SEVERE, Method.getCaller(), message, null);
  }

  public void log(Level level, String message) {
    log(level, Method.getCaller(), message, null);
  }

  public void log(Level level, String message, Throwable exception) {
    log(level, Method.getCaller(), message, exception);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
	if (!isLoggable(level)) {
		return;
	}
    publish(new LogRecord(name, sourceMethod, level, msg, null));
  }
  
  public void logp(Level level, String sourceClass, String sourceMethod,
          String msg, Throwable thrown) {
	if (!isLoggable(level)) {
	  return;
	}
	publish(new LogRecord(name, sourceMethod, level, msg, thrown));
  }

  public Level getLevel() {
    return levelValue;
  }

  private Level getEffectiveLevel() {
    Logger logger = this;

    while (logger.levelValue == null) {
      logger = logger.getParent();
    }
    return logger.getLevel();
  }
      
  private void log(Level level, avian.VMMethod caller, String message,
                   Throwable exception) {
    
    if (level.intValue() < getEffectiveLevel().intValue()) {
      return;
    }
    LogRecord r = new LogRecord
      (name, caller == null ? "<unknown>" : Method.getName(caller), level,
       message, exception);
    publish(r);
  }

  private void publish(LogRecord logRecord) {
    for (Handler h : handlers) {
      h.publish(logRecord);
    }
  }

  public void setLevel(Level level) {
    levelValue = level;
  }
  
  public boolean isLoggable(Level level) {
    return level.intValue() >= levelValue.intValue();
  }
  
  private static class DefaultHandler extends Handler {
    private static final int NAME_WIDTH = 14;
    private static final int METHOD_WIDTH = 15;
    private static final int LEVEL_WIDTH = 8;
    private final String newline;

    public DefaultHandler() {
      newline = System.getProperty("line.separator");
    }

    public Object clone() { return this; }
    public void close() { }
    public void flush() { }

    private void maybeLogThrown(StringBuilder sb, Throwable t) {
      if (t != null) {
        sb.append("\nCaused by: ");
        sb.append(t.getClass().getName());
        sb.append(": ");
        sb.append(t.getMessage());
        sb.append(newline);

        for (StackTraceElement elt : t.getStackTrace()) {
          sb.append('\t');
          sb.append(elt.getClassName());
          sb.append('.');
          sb.append(elt.getMethodName());
          sb.append("(line");
          sb.append(':');
	  int lineNumber = elt.getLineNumber();
	  if (lineNumber == -2) {
	    sb.append("unknown");
	  } else if (lineNumber == -1) {
	    sb.append("native");
	  } else {
	    sb.append(lineNumber);
	  }
          sb.append(')');
          sb.append(newline);
        }
        maybeLogThrown(sb, t.getCause());
      }
    }

    private void indent(StringBuilder sb, int amount) {
      do {
        sb.append(' ');
      } while (--amount > 0);
    }

    public void publish(LogRecord r) {
      StringBuilder sb = new StringBuilder();
      sb.append(r.getLoggerName());
      indent(sb, NAME_WIDTH - r.getLoggerName().length());
      sb.append(r.getSourceMethodName());
      indent(sb, METHOD_WIDTH - r.getSourceMethodName().length());
      sb.append(r.getLevel().getName());
      indent(sb, LEVEL_WIDTH - r.getLevel().getName().length());
      sb.append(r.getMessage());
      maybeLogThrown(sb, r.getThrown());
      System.out.println(sb.toString());
    }
  }

}
