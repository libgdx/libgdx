public class GC {
  private static final Integer cache[] = new Integer[100];
  private static final Integer MAX_INT_OBJ = new Integer(Integer.MAX_VALUE);

  private static Integer valueOf(int i) {
    try {
      return cache[i];
    } catch (ArrayIndexOutOfBoundsException e) {
      return (i == Integer.MAX_VALUE) ? MAX_INT_OBJ : new Integer(i);
    }
  }

  private static void small() {
    for (int i = 0; i < 1024; ++i) {
      byte[] a = new byte[4 * 1024];
    }
  }

  private static void medium() {
    for (int i = 0; i < 8; ++i) {
      Object[] array = new Object[32];
      for (int j = 0; j < 32; ++j) {
        array[j] = new byte[32 * 1024];
      }
    }
  }

  private static void large() {
    for (int i = 0; i < 8; ++i) {
      byte[] a = new byte[16 * 1024 * 1024];
    }

    for (int i = 0; i < 8; ++i) {
      byte[] a = new byte[16 * 1024 * 1024];
      for (int j = 0; j < 32; ++j) {
        byte[] b = new byte[32 * 1024];
      }
    }
  }

  private static void stackMap1(boolean predicate) {
    if (predicate) {
      Object a = null;
    }

    System.gc();
  }

  private static void stackMap2(boolean predicate) {
    if (predicate) {
      int a = 42;
    } else {
      Object a = null;
    }

    System.gc();
  }

  private static void stackMap3(boolean predicate) {
    if (predicate) {
      Object a = null;
    } else {
      int a = 42;
    }

    System.gc();
  }

  private static void stackMap4(boolean predicate) {
    int i = 2;
    if (predicate) {
      Object a = null;
    } else {
      Object a = null;
    }

    do {
      System.gc();
      int a = 42;
      -- i;
    } while (i >= 0);
  }

  private static void noop() { }

  private static void stackMap5(boolean predicate) {
    if (predicate) {
      noop();
    }

    if (predicate) {
      noop();
    } else {
      Object a = null;
    }

    System.gc();
  }

  private static void stackMap6(boolean predicate) {
    if (predicate) {
      int a = 42;
    } else {
      Object a = null;
    }

    if (predicate) {
      noop();
    } else {
      Object a = null;
    }

    noop();
    System.gc();
  }

  private static void stackMap7(boolean predicate) {
    try {
      if (predicate) {
        Object a = null;
      } else {
        Object a = null;
      }

      try {
        int a = 42;
        throw new DummyException();
      } finally {
        System.gc();
      }
    } catch (DummyException e) {
      e.toString();
    }
  }

  private static void stackMap8(boolean predicate) {
    try {
      Object x = new Object();
      if (predicate) {
        Object a = null;
      } else {
        Object a = null;
      }

      try {
        int a = 42;
        throw new DummyException();
      } finally {
        System.gc();
        x.toString();
      }
    } catch (DummyException e) {
      e.toString();
    }
  }

  public static void main(String[] args) {
    valueOf(1000);

    Object[] array = new Object[1024 * 1024];
    array[0] = new Object();

    small();

    array[1] = new Object();

    medium();

    array[2] = new Object();

    large();

    array[0].toString();
    array[1].toString();
    array[2].toString();

    stackMap1(true);
    stackMap1(false);

    stackMap2(true);
    stackMap2(false);

    stackMap3(true);
    stackMap3(false);

    stackMap4(true);
    stackMap4(false);

    stackMap5(true);
    stackMap5(false);

    stackMap6(true);
    stackMap6(false);

    stackMap7(true);
    stackMap7(false);

    stackMap8(true);
    stackMap8(false);
  }

  private static class DummyException extends RuntimeException { }
}
