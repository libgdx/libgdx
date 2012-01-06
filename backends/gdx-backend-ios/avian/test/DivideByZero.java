public class DivideByZero {
  private static int divide(int n, int d) {
    return n / d;
  }

  private static int modulo(int n, int d) {
    return n % d;
  }

  private static long divide(long n, long d) {
    return n / d;
  }

  private static long modulo(long n, long d) {
    return n % d;
  }

  private static float divide(float n, float d) {
    return n / d;
  }

  private static float modulo(float n, float d) {
    return n % d;
  }

  private static double divide(double n, double d) {
    return n / d;
  }

  private static double modulo(double n, double d) {
    return n % d;
  }

  public static void main(String[] args) {
    try {
      int x = 1 / 0;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      int x = 1 % 0;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      int y = 2;
      int x = y / 0;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      int y = 2;
      int x = y % 0;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      int z = 0;
      int y = 2;
      int x = y / z;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      int z = 0;
      int y = 2;
      int x = y % z;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      long z = 0;
      long y = 2;
      long x = y / z;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      long z = 0;
      long y = 2;
      long x = y % z;
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      divide(5, 0);
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      modulo(6, 0);
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      divide(5L, 0L);
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    try {
      modulo(6L, 0L);
      throw new RuntimeException();
    } catch (ArithmeticException e) {
      e.printStackTrace();
    }

    divide(5F, 0F);
    modulo(6F, 0F);

    divide(5D, 0D);
    modulo(6D, 0D);
  }
}
