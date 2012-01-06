public class Initializers {
  private static class Static2 {
    public static String foo = "Static2.foo";

    static {
      System.gc();
      new Exception().printStackTrace();
    }
  }

  private static class Static1 {
    public static String foo = "Static1.foo";

    static {
      System.out.println(Static2.foo);
    }
  }

  public static void main(String[] args) {
    Object x = new Object();
    System.out.println(Static1.foo);
    x.toString();
  }
}
