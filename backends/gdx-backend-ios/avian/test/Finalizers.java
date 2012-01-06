public class Finalizers {
  private static final Object lock = new Object();
  private static boolean finalized = false;

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  protected void finalize() {
    synchronized (lock) {
      finalized = true;
      lock.notifyAll();
    }
  }

  public static void main(String[] args) throws Exception {
    new Finalizers();

    expect(! finalized);
    
    synchronized (lock) {
      System.gc();
      lock.wait(5000);
    }

    expect(finalized);

    new Finalizers2();
    
    finalized = false;

    expect(! finalized);
    
    synchronized (lock) {
      System.gc();
      lock.wait(5000);
    }

    expect(finalized);
  }

  private static class Finalizers2 extends Finalizers { }

}
