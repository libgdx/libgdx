package extra;

public class Tails {
  private static final int Limit = 1000000;

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static int staticMethod(Interface i, int n) {
    if (n < Limit) {
      return i.interfaceMethod(n + 1);
    } else {
      return leafMethod(n);
    }
  }

  private static int leafMethod(int n) {
    expect(new Throwable().getStackTrace().length == 2);

    return n;
  }

  public static void main(String[] args) {
    expect(staticMethod(new Foo(), 0) == Limit);
  }

  private interface Interface {
    public int interfaceMethod(int n);
  }

  private static class Foo implements Interface {
    public int interfaceMethod(int n) {
      if (n < Limit) {
        return virtualMethod(n + 1, 1, 2, 3, 4, 5);
      } else {
        return leafMethod(n);
      }
    }

    public int virtualMethod(int n, int a, int b, int c, int d, int e) {
      if (n < Limit) {
        return staticMethod(this, n + 1);
      } else {
        return leafMethod(n);
      }
    }
  }
}
