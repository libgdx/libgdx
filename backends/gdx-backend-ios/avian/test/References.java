import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.PhantomReference;
import java.util.WeakHashMap;

public class References {
  public static void main(String[] args) {
    Object a = new Object();
    Object b = new Object();
    Object c = new Object();
    Object d = new Object();
    Object e = new Object();
    Object f = new Object();

    ReferenceQueue q = new ReferenceQueue();

    Reference ar = new WeakReference(a);
    Reference br = new WeakReference(b, q);
    Reference cr = new WeakReference(c, q);
    Reference dr = new PhantomReference(d, q);
    Reference er = new MyReference(e, q, "foo");
    
    WeakHashMap<Key,Object> map = new WeakHashMap();
    map.put(new Key("foo"), f);

    a = b = c = d = e = cr = null;
    
    System.out.println("a: " + ar.get());
    System.out.println("b: " + br.get());
    System.out.println("d: " + dr.get());
    System.out.println("e: " + er.get());
    System.out.println("f: " + map.get(new Key("foo")));

    System.gc();

    System.out.println("a: " + ar.get());
    System.out.println("b: " + br.get());
    System.out.println("d: " + dr.get());
    System.out.println("e: " + er.get());
    System.out.println("f: " + map.get(new Key("foo")));

    for (Reference r = q.poll(); r != null; r = q.poll()) {
      System.out.println("polled: " + r.get());      
    }
  }

  private static class MyReference extends WeakReference {
    private final Object foo;

    public MyReference(Object target, ReferenceQueue queue, Object foo) {
      super(target, queue);
      this.foo = foo;
    }
  }

  private static class Key {
    private final String name;

    public Key(String name) {
      this.name = name;
    }

    public int hashCode() {
      return name.hashCode();
    }

    public boolean equals(Object o) {
      return o instanceof Key && ((Key) o).name.equals(name);
    }
  }
}
