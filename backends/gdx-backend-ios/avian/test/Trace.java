public class Trace implements Runnable {
  private volatile boolean alive = true;

  private static void throwSomething() {
    throw new RuntimeException();
  }

  private void bar(Object o) {
    o.toString();
  }

  private void foo() {
    { long a = 42;
      long b = 25;
      long c = a / b;
    }

    try {
      long a = 42;
      long b = 0;
      long c = a / b;
    } catch (Exception e) { }

    try {
      throw new Exception();
    } catch (Exception e) { }

    try {
      throwSomething();
    } catch (Exception e) { }

    try {
      Trace.class.getMethod("bar", Object.class).invoke(this, this);
    } catch (Exception e) { }
  }

  private static void dummy() {
    byte[] a = new byte[10];
    byte[] b = new byte[10];
    System.arraycopy(a, 0, b, 0, 10);
  }

  private static void tail1(int a, int b, int c, int d, int e, int f) {
    dummy();
  }

  private static void tail2() {
    tail1(1, 2, 3, 4, 5, 6);
    tail1(1, 2, 3, 4, 5, 6);
  }

  private static void test(Trace trace) {
    tail1(1, 2, 3, 4, 5, 6);
    tail2();
    trace.foo();
  }

  public void run() {
    synchronized (this) {
      notifyAll();
    }

    try {
      for (int i = 0; i < 10000; ++i) {
        test(this);
        
        if (i % 100 == 0) {
          System.out.print("r");
          System.out.flush();
          synchronized (this) {
            notifyAll();
          }
        }
      }
    } finally {
      synchronized (this) {
        alive = false;
        notifyAll();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Trace trace = new Trace();
    Thread thread = new Thread(trace);

    synchronized (trace) {
      thread.start();
      trace.wait();

      int count = 0;
      while (trace.alive) {
        thread.getStackTrace();
        ++ count;
        
        if (count % 100 == 0) {
          trace.wait();
          System.out.print("t");
          System.out.flush();
        }
      }

      System.out.println("\ngot " + count + " traces");
    }
  }
}
