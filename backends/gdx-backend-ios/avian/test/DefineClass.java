import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

public class DefineClass {
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

  private static Class loadClass(String name) throws Exception {
    return new MyClassLoader(DefineClass.class.getClassLoader()).defineClass
      (name, read(findClass(name, new File(System.getProperty("user.dir")))));
  }

  private static void testStatic() throws Exception {
    loadClass("DefineClass$Hello")
      .getMethod("main", String[].class).invoke(null, (Object) new String[0]);
  }

  private static void testDerived() throws Exception {
    System.out.println
      (String.valueOf
       (((Base) loadClass("DefineClass$Derived").newInstance()).zip()));
  }

  public static void main(String[] args) throws Exception {
    testStatic();
    testDerived();
  }

  private static class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }

    public Class defineClass(String name, byte[] bytes) {
      return defineClass(name, bytes, 0, bytes.length);
    }
  }

  public static class Hello {
    public static void main(String[] args) {
      System.out.println("hello, world!");
    }
  }

  public abstract static class Base {
    public int foo;
    public int[] array;
    
    public void bar() { }

    public abstract int zip();
  }

  public static class Derived extends Base {
    public int zip() {
      return 42;
    }
  }
}
