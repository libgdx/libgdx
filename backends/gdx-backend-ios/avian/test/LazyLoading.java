import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LazyLoading {
  private static boolean loadLazy;

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static File findClass(String name, File directory) {
    for (File file: directory.listFiles()) {
      if (file.isFile()) {
        if (file.getName().equals(name + ".class")) {
          return file;
        }
      } else if (file.isDirectory()) {
        File result = findClass(name, file);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private static byte[] read(File file) throws IOException {
    byte[] bytes = new byte[(int) file.length()];
    FileInputStream in = new FileInputStream(file);
    try {
      if (in.read(bytes) != (int) file.length()) {
        throw new RuntimeException();
      }
      return bytes;
    } finally {
      in.close();
    }
  }

  public static void main(String[] args) throws Exception {
    Class c = new MyClassLoader(LazyLoading.class.getClassLoader()).loadClass
      ("LazyLoading$Test");

    c.getMethod("test").invoke(null);
  }

  private static class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
      try {
        return defineClass
          (name, read
           (LazyLoading.findClass
            (name, new File(System.getProperty("user.dir")))));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Class loadClass(String name) throws ClassNotFoundException {
      if ("LazyLoading$Test".equals(name)) {
        return findClass(name);
      } else if ("LazyLoading$Lazy".equals(name)
                 || "LazyLoading$Interface".equals(name))
      {
        if (loadLazy) {
          return findClass(name);
        } else {
          throw new ClassNotFoundException();
        }
      } else {
        return super.loadClass(name);
      }
    }

    private Class defineClass(String name, byte[] bytes) {
      return defineClass(name, bytes, 0, bytes.length);
    }
  }

  public static class Test {
    public static void test() {
      doTest();
      loadLazy = true;
      doTest();
    }

    private static void doTest() {
      if (loadLazy) {
        // anewarray
        Lazy[] array = new Lazy[1];
        
        // new and invokespecial
        Object lazy = new Lazy();
        
        // checkcast
        array[0] = (Lazy) lazy;

        // instanceof
        expect(lazy instanceof Lazy);

        // invokeinterface
        Interface i = array[0];
        expect(i.interfaceMethod() == 42);

        // invokestatic
        expect(Lazy.staticMethod() == 43);

        // invokevirtual
        expect(array[0].virtualMethod() == 44);

        // ldc
        expect(Lazy.class == lazy.getClass());

        // multianewarray
        Lazy[][] multiarray = new Lazy[5][6];
        multiarray[2][3] = array[0];
        expect(multiarray[2][3] == array[0]);

        // getfield
        expect(array[0].intField == 45);

        // getstatic
        expect(Lazy.intStaticField == 46);

        // putfield int
        array[0].intField = 47;
        expect(array[0].intField == 47);

        // putfield long
        array[0].longField = 48;
        expect(array[0].longField == 48);

        // putfield object
        Object x = new Object();
        array[0].objectField = x;
        expect(array[0].objectField == x);

        // putstatic int
        array[0].intStaticField = 49;
        expect(array[0].intStaticField == 49);

        // putstatic long
        array[0].longStaticField = 50;
        expect(array[0].longStaticField == 50);

        // putstatic object
        Object y = new Object();
        array[0].objectStaticField = y;
        expect(array[0].objectStaticField == y);
      }
    }
  }

  private interface Interface {
    public int interfaceMethod();
  }

  private static class Lazy implements Interface {
    public static int intStaticField = 46;
    public static long longStaticField;
    public static Object objectStaticField;

    public int intField = 45;
    public long longField;
    public Object objectField;

    public int interfaceMethod() {
      return 42;
    }

    public static int staticMethod() {
      return 43;
    }

    public int virtualMethod() {
      return 44;
    }
  }
}
