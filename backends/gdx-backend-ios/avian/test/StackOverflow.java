public class StackOverflow {
  private static int test1() {
    return test1() + 1;
  }

  private static int test2() {
    return test3() + 1;
  }

  private static int test3() {
    return test2() + 1;
  }

  public static void main(String[] args) {
    try {
      test1();
      throw new RuntimeException();
    } catch (StackOverflowError e) {
      e.printStackTrace();
    }

    try {
      test2();
      throw new RuntimeException();
    } catch (StackOverflowError e) {
      e.printStackTrace();
    }
  }
}
