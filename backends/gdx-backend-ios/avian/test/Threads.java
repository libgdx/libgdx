public class Threads implements Runnable {
  public static void main(String[] args) {
    { Threads test = new Threads();
      Thread thread = new Thread(test);

      try {
        synchronized (test) {
          thread.start();
          test.wait();
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    { Thread thread = new Thread() {
        public void run() {
          while (true) {
            System.out.print(".");
            try {
              sleep(1000);
            } catch (Exception e) {
              System.out.println("thread interrupted? " + interrupted());
              break;
            }
          }
        }
      };
      thread.start();

      System.out.println("\nAbout to interrupt...");
      thread.interrupt();
      System.out.println("\nInterrupted!");
    }

    System.out.println("finished");
  }

  public void run() {
    synchronized (this) {
      int i = 0;
      try {
        System.out.println("I'm running in a separate thread!");

        final int arrayCount = 16;
        final int arraySize = 4;
        System.out.println("Allocating and discarding " + arrayCount +
                           " arrays of " + arraySize + "MB each");
        for (; i < arrayCount; ++i) {
          byte[] array = new byte[arraySize * 1024 * 1024];
        }

        long nap = 5;
        System.out.println("sleeping for " + nap + " seconds");
        Thread.sleep(nap * 1000);
      } catch (Throwable e) {
        System.err.println("caught something in second thread after " + i +
                           " iterations");
        e.printStackTrace();
      } finally {
        notifyAll();
      }
    }
  }
}
