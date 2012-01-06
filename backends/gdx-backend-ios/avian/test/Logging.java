import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Logging {
  private static final Logger log = Logger.getLogger("Logging");

  private static class MyHandler extends Handler {
    private static final int NAME_WIDTH = 18;
    private static final int METHOD_WIDTH = 20;
    private static final int LEVEL_WIDTH = 8;

    public Object clone() { return this; }
    public void close() { }
    public void flush() { }

    private void maybeLogThrown(StringBuilder sb, Throwable t) {
      if (t != null) {
        sb.append("\nCaused by: ");
        sb.append(t.getClass().getName());
        sb.append(": ");
        sb.append(t.getMessage());
        sb.append('\n');

        for (StackTraceElement elt : t.getStackTrace()) {
          sb.append('\t');
          sb.append(elt.getClassName());
          sb.append('.');
          sb.append(elt.getMethodName());
          sb.append('(');
          sb.append(elt.getFileName());
          sb.append(':');
          sb.append(elt.getLineNumber());
          sb.append(')');
          sb.append('\n');
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
      String methodName = r.getSourceMethodName();
      if (methodName == null) {
        methodName = "<unknown>";
      }
      sb.append(methodName);
      indent(sb, METHOD_WIDTH - methodName.length());
      sb.append(r.getLevel().getName());
      indent(sb, LEVEL_WIDTH - r.getLevel().getName().length());
      sb.append(r.getMessage());
      maybeLogThrown(sb, r.getThrown());
      System.out.println(sb.toString());
    }
  }

  public void run() {
    log.info("Started run");
    a();
    log.info("Ended run");
  }

  private void a() {
    log.fine("Started a()");
    b();
  }

  private void b() {
    log.info("Started b()");
    c();
  }

  private void c() {
    log.warning("Started c()");
    try {
      d();
    } catch (Exception ex) {
      log.log(Level.SEVERE, "Exception caught in c", ex);
    }
  }

  private void d() throws Exception {
    e();
  }

  private void e() throws Exception {
    throw new Exception("Started here");
  }

  private static final boolean useCustomHandler = true;
  public static void main(String args[]) {
    if (useCustomHandler) {
      Logger root = Logger.getLogger("");
      root.addHandler(new MyHandler());
      for (Handler h : root.getHandlers()) root.removeHandler(h);
      root.addHandler(new MyHandler());
    }

    Logging me = new Logging();
    me.run();
  }
}
