public class OutOfMemory {
  // assume a 128MB heap size:
  private static final int Padding = 120 * 1024 * 1024;

  private static class Node {
    Object value;
    Node next;
  }

  private static void bigObjects() {
    Object[] root = null;
    while (true) {
      Object[] x = new Object[1024 * 1024];
      x[0] = root;
      root = x;
    }
  }

  private static void littleObjects() {
    byte[] padding = new byte[Padding];
    Node root = null;
    while (true) {
      Node x = new Node();
      x.next = root;
      root = x;
    }
  }

  private static void bigAndLittleObjects() {
    byte[] padding = new byte[Padding];
    Node root = null;
    while (true) {
      Node x = new Node();
      x.value = new Object[1024 * 1024];
      x.next = root;
      root = x;
    }
  }

  public static void main(String[] args) {
    try {
      bigObjects();
      throw new RuntimeException();
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    }

    try {
      littleObjects();
      throw new RuntimeException();
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    }

    try {
      bigAndLittleObjects();
      throw new RuntimeException();
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    }
  }
}
