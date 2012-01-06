import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class Reflection {
  public static boolean booleanMethod() {
    return true;
  }

  public static byte byteMethod() {
    return 1;
  }

  public static char charMethod() {
    return '2';
  }

  public static short shortMethod() {
    return 3;
  }

  public static int intMethod() {
    return 4;
  }

  public static float floatMethod() {
    return 5.0f;
  }

  public static long longMethod() {
    return 6;
  }

  public static double doubleMethod() {
    return 7.0;
  }

  public static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static void main(String[] args) throws Exception {
    Class system = Class.forName("java.lang.System");
    Field out = system.getDeclaredField("out");
    Class output = Class.forName("java.io.PrintStream");
    Method println = output.getDeclaredMethod("println", String.class);

    println.invoke(out.get(null), "Hello, World!");

    expect((Boolean) Reflection.class.getMethod("booleanMethod").invoke(null));

    expect(1 == (Byte) Reflection.class.getMethod("byteMethod").invoke(null));

    expect('2' == (Character) Reflection.class.getMethod
           ("charMethod").invoke(null));

    expect(3 == (Short) Reflection.class.getMethod
           ("shortMethod").invoke(null));

    expect(4 == (Integer) Reflection.class.getMethod
           ("intMethod").invoke(null));

    expect(5.0 == (Float) Reflection.class.getMethod
           ("floatMethod").invoke(null));

    expect(6 == (Long) Reflection.class.getMethod
           ("longMethod").invoke(null));

    expect(7.0 == (Double) Reflection.class.getMethod
           ("doubleMethod").invoke(null));
  }
}
