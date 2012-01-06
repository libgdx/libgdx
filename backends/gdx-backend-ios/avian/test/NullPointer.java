public class NullPointer {
  private int x;
  private Object y;

  private static void throw_(Object o) {
    o.toString();
  }

  private static void throwAndCatch(Object o) {
    try {
      o.toString();
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      throw_(null);
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    throwAndCatch(null);

    // invokeinterface
    try {
      ((Runnable) null).run();
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // invokevirtual
    try {
      ((Object) null).toString();
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // arraylength
    try {
      int a = ((byte[]) null).length;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // iaload
    try {
      int a = ((byte[]) null)[42];
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // aaload
    try {
      Object a = ((Object[]) null)[42];
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // getfield (int)
    try {
      int a = ((NullPointer) null).x;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // getfield (Object)
    try {
      Object a = ((NullPointer) null).y;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // iastore
    try {
      ((byte[]) null)[42] = 42;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // aastore
    try {
      ((Object[]) null)[42] = null;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // putfield (int)
    try {
      ((NullPointer) null).x = 42;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // putfield (Object)
    try {
      ((NullPointer) null).y = null;
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    // monitorenter
    try {
      synchronized ((Object) null) {
        int a = 42;
      }
      throw new RuntimeException();
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }
}
