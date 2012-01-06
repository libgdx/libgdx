import java.io.IOException;

public class Processes {
  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    try {
      final Process p = Runtime.getRuntime().exec("sleep 10");
      new Thread() {
        public void run() {
          try {
            Thread.sleep(100);
          } catch(InterruptedException e) {
            // ignore
          }
          p.destroy();
        }
      }.start();
      try {
        p.waitFor();
      } catch(InterruptedException e) {
        // ignore
      }
      long stop = System.currentTimeMillis();
      if(stop - start > 5000) {
        throw new RuntimeException("test failed; we didn't kill the process...");
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
