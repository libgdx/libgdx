import avian.Stream;
import avian.ConstantPool;
import avian.ConstantPool.PoolEntry;
import avian.Assembler;
import avian.Assembler.MethodData;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Subroutine {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static byte[] makeTestCode(List pool) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Stream.write2(out, 1); // max stack
    Stream.write2(out, 1); // max locals
    Stream.write4(out, 0); // length (we'll set the real value later)
    
    //  0:
    Stream.write1(out, Assembler.ldc_w);
    Stream.write2(out, ConstantPool.addString(pool, "foo") + 1);

    //  3:
    Stream.write1(out, Assembler.astore_0);

    //  4:
    Stream.write1(out, Assembler.invokestatic);
    Stream.write2(out, ConstantPool.addMethodRef
                  (pool, "java/lang/System", "gc", "()V") + 1);

    //  7:
    Stream.write1(out, Assembler.goto_);
    Stream.write2(out, 9); // 16

    // 10:
    Stream.write1(out, Assembler.astore_0);

    // 11:
    Stream.write1(out, Assembler.invokestatic);
    Stream.write2(out, ConstantPool.addMethodRef
                  (pool, "java/lang/System", "gc", "()V") + 1);

    // 14:
    Stream.write1(out, Assembler.ret);
    Stream.write1(out, 0);

    // 16:
    Stream.write1(out, Assembler.jsr);
    Stream.write2(out, -6); // 10
    
    // 19:
    Stream.write1(out, Assembler.invokestatic);
    Stream.write2(out, ConstantPool.addMethodRef
                  (pool, "java/lang/System", "gc", "()V") + 1);

    // 22:
    Stream.write1(out, Assembler.return_);

    Stream.write2(out, 0); // exception handler table length
    Stream.write2(out, 0); // attribute count

    byte[] result = out.toByteArray();
    Stream.set4(result, 4, result.length - 12);

    return result;
  }

  private static Class makeTestClass() throws IOException {
    List pool = new ArrayList();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String name = "$SubroutineTest$";

    Assembler.writeClass
      (out, pool, ConstantPool.addClass(pool, name),
       ConstantPool.addClass(pool, "java/lang/Object"),
       new int[0], new MethodData[]
       { new MethodData(Assembler.ACC_STATIC | Assembler.ACC_PUBLIC,
                        ConstantPool.addUtf8(pool, "test"),
                        ConstantPool.addUtf8(pool, "()V"),
                        makeTestCode(pool)) });

    return new MyClassLoader(Subroutine.class.getClassLoader())
      .defineClass(name, out.toByteArray());
  }

  // These tests are intended to cover the jsr and ret instructions.
  // However, recent Sun javac versions avoid generating these
  // instructions by default, so we must compile this class using
  // -source 1.2 -target 1.1 -XDjsrlimit=0.
  //
  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4381996
  //

  private static void test(boolean throw_, boolean predicate) {
    int x = 42;
    int y = 99;
    int a = 0;
    try {
      try {
        int z = x + y;
        if (throw_) throw new DummyException();
        if (predicate) {
          return;
        }
        Integer.valueOf(z).toString();
      } finally {
        a = x + y;
        System.gc();
      }
      expect(a == x + y);
    } catch (DummyException e) {
      e.printStackTrace();
    }
  }

  private static Object test2(int path) {
    try {
      try {
        switch (path) {
        case 1:
          return new Object();

        case 2: {
          int a = 42;
          return Integer.valueOf(a);
        }

        case 3:
          throw new DummyException();
        }
      } finally {
        System.gc();
      }
      return null;
    } catch (DummyException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Object test3(int path1, int path2, int path3) {
    try {
      try {
        switch (path1) {
        case 1:
          return new Object();

        case 2: {
          int a = 42;
          return Integer.valueOf(a);
        }

        case 3:
          throw new DummyException();
        }
      } finally {
        try {
          switch (path2) {
          case 1:
            return new Object();

          case 2: {
            int a = 42;
            return Integer.valueOf(a);
          }

          case 3:
            throw new DummyException();
          }
        } finally {
          try {
            switch (path3) {
            case 1:
              return new Object();

            case 2: {
              int a = 42;
              return Integer.valueOf(a);
            }

            case 3:
              throw new DummyException();
            }
          } finally {
            System.gc();
          }
        }
      }
      return null;
    } catch (DummyException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static long test4(int path) {
    try {
      try {
        switch (path) {
        case 1:
          return 0xFABFABFABFL;

        case 2: {
          int a = 42;
          return 52L;
        }

        case 3:
          throw new DummyException();
        }
      } finally {
        System.gc();
      }
      return 0L;
    } catch (DummyException e) {
      e.printStackTrace();
      return 0L;
    }
  }

  private boolean test5(boolean predicate) {
    try {
      if (predicate) {
        return false;
      }
    } finally {
      synchronized (this) {
        notifyAll();
      }
    }
    return true;
  }

  public static void main(String[] args) throws Exception {
    test(false, false);
    test(false, true);
    test(true, false);

    String.valueOf(test2(1));
    String.valueOf(test2(2));
    String.valueOf(test2(3));

    String.valueOf(test3(1, 1, 1));
    String.valueOf(test3(2, 1, 1));
    String.valueOf(test3(3, 1, 1));

    String.valueOf(test3(1, 2, 1));
    String.valueOf(test3(2, 2, 1));
    String.valueOf(test3(3, 2, 1));

    String.valueOf(test3(1, 3, 1));
    String.valueOf(test3(2, 3, 1));
    String.valueOf(test3(3, 3, 1));

    String.valueOf(test3(1, 1, 2));
    String.valueOf(test3(2, 1, 2));
    String.valueOf(test3(3, 1, 2));

    String.valueOf(test3(1, 2, 2));
    String.valueOf(test3(2, 2, 2));
    String.valueOf(test3(3, 2, 2));

    String.valueOf(test3(1, 3, 2));
    String.valueOf(test3(2, 3, 2));
    String.valueOf(test3(3, 3, 2));

    String.valueOf(test3(1, 1, 3));
    String.valueOf(test3(2, 1, 3));
    String.valueOf(test3(3, 1, 3));

    String.valueOf(test3(1, 2, 3));
    String.valueOf(test3(2, 2, 3));
    String.valueOf(test3(3, 2, 3));

    String.valueOf(test3(1, 3, 3));
    String.valueOf(test3(2, 3, 3));
    String.valueOf(test3(3, 3, 3));

    String.valueOf(test4(1));
    String.valueOf(test4(2));
    String.valueOf(test4(3));

    expect(test4(1) == 0xFABFABFABFL);

    new Subroutine().test5(true);
    new Subroutine().test5(false);

    makeTestClass().getMethod("test", new Class[0]).invoke
      (null, new Object[0]);
  }

  private static class DummyException extends RuntimeException { }

  private static class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }

    public Class defineClass(String name, byte[] bytes) {
      return super.defineClass(name, bytes, 0, bytes.length);
    }
  }
}
