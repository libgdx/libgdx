import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Proxies {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static void main(String[] args) {
    Foo foo = (Foo) Proxy.newProxyInstance
      (Proxies.class.getClassLoader(), new Class[] { Foo.class },
       new InvocationHandler() {
         public Object invoke(Object proxy, Method method, Object[] arguments)
         {
           if (method.getName().equals("bar")) {
             return "bam";
           } else if (method.getName().equals("baz")) {
             return ((Integer) arguments[0]) + 1;
           } else if (method.getName().equals("bim")) {
             return ((Long) arguments[0]) - 1L;
           } else if (method.getName().equals("boom")) {
             return ((String) arguments[0]).substring(1);
           } else {
             throw new IllegalArgumentException();
           }
         }
       });

    expect(foo.bar().equals("bam"));
    expect(foo.baz(42) == 43);
    expect(foo.bim(42L) == 41L);
    expect(foo.boom("hello").equals("ello"));
  }

  private interface Foo {
    public String bar();
    public int baz(int v);
    public long bim(long v);
    public String boom(String s);
  }
}
