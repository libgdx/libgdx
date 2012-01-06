import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

public class Annotations {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static void main(String[] args) throws Exception {
    Method m = Annotations.class.getMethod("foo");

    expect(m.isAnnotationPresent(Test.class));

    expect(((Test) m.getAnnotation(Test.class)).value().equals("couscous"));

    expect(((TestEnum) m.getAnnotation(TestEnum.class)).value()
           .equals(Color.Red));

    expect(((TestInteger) m.getAnnotation(TestInteger.class)).value() == 42);
  }

  @Test("couscous")
  @TestEnum(Color.Red)
  @TestInteger(42)
  public static void foo() {
    
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface Test {
    public String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface TestEnum {
    public Color value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface TestInteger {
    public int value();
  }

  private static enum Color {
    Red, Yellow, Blue
  }

}
