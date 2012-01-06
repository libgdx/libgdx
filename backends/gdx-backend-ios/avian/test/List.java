import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class List {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static String printList(ArrayList<Integer> list) {
    StringBuilder sb = new StringBuilder();

    for (Integer i : list) {
      sb.append(i);
      sb.append(", ");
    }
    sb.setLength(sb.length()-2);
    return sb.toString();
  }

  private static void isEqual(String s1, String s2) {
    System.out.println(s1);
    expect(s1.equals(s2));
  }

  private static void testIterators(java.util.List<Integer> l) {
    l.add(1);
    l.add(2);
    l.add(3);
    
    ListIterator<Integer> it = l.listIterator();
    expect(it.next().equals(Integer.valueOf(1)));
    expect(it.next().equals(Integer.valueOf(2)));
    expect(it.next().equals(Integer.valueOf(3)));
    expect(! it.hasNext());

    it = l.listIterator(1);
    expect(it.next().equals(Integer.valueOf(2)));
    expect(it.next().equals(Integer.valueOf(3)));
    expect(! it.hasNext());

    it = l.listIterator(2);
    expect(it.next().equals(Integer.valueOf(3)));
    expect(! it.hasNext());

    it = l.listIterator(3);
    expect(it.previous().equals(Integer.valueOf(3)));
    expect(it.previous().equals(Integer.valueOf(2)));
    expect(it.previous().equals(Integer.valueOf(1)));
    expect(! it.hasPrevious());

    it = l.listIterator(2);
    expect(it.previous().equals(Integer.valueOf(2)));
    expect(it.previous().equals(Integer.valueOf(1)));
    expect(! it.hasPrevious());

    it = l.listIterator(1);
    expect(it.previous().equals(Integer.valueOf(1)));
    expect(! it.hasPrevious());
  }

  private static void testIterators2(java.util.List<Integer> l) {
    l.add(1);
    l.add(2);
    l.add(3);
    
    ListIterator<Integer> it = l.listIterator();
    expect(it.next().equals(Integer.valueOf(1)));
    it.remove();
    expect(it.next().equals(Integer.valueOf(2)));
    it.remove();
    expect(it.next().equals(Integer.valueOf(3)));
    it.remove();
    expect(! it.hasNext());
    expect(l.isEmpty());

    l.add(1);
    l.add(2);
    l.add(3);
    
    it = l.listIterator(1);
    expect(it.next().equals(Integer.valueOf(2)));
    it.remove();
    expect(it.next().equals(Integer.valueOf(3)));
    it.remove();
    expect(! it.hasNext());
    expect(l.size() == 1);

    l.add(2);
    l.add(3);

    it = l.listIterator(2);
    expect(it.next().equals(Integer.valueOf(3)));
    it.remove();
    expect(! it.hasNext());
    expect(l.size() == 2);

    l.add(3);

    it = l.listIterator(3);
    expect(it.previous().equals(Integer.valueOf(3)));
    it.remove();
    expect(it.previous().equals(Integer.valueOf(2)));
    it.remove();
    expect(it.previous().equals(Integer.valueOf(1)));
    it.remove();
    expect(! it.hasPrevious());
    expect(l.isEmpty());

    l.add(1);
    l.add(2);
    l.add(3);

    it = l.listIterator(2);
    expect(it.previous().equals(Integer.valueOf(2)));
    it.remove();
    expect(it.previous().equals(Integer.valueOf(1)));
    it.remove();
    expect(! it.hasPrevious());
    expect(l.size() == 1);

    l.clear();
    l.add(1);
    l.add(2);
    l.add(3);

    it = l.listIterator(1);
    expect(it.previous().equals(Integer.valueOf(1)));
    it.remove();
    expect(! it.hasPrevious());
    expect(l.size() == 2);
  }

  public static void main(String args[]) {
    ArrayList<Integer> l = new ArrayList<Integer>();
    l.add(1); l.add(2); l.add(3); l.add(4); l.add(5);
    isEqual(printList(l), "1, 2, 3, 4, 5");
    l.add(0, 6);
    isEqual(printList(l), "6, 1, 2, 3, 4, 5");
    l.add(2, 7);
    isEqual(printList(l), "6, 1, 7, 2, 3, 4, 5");
    l.remove(1);
    isEqual(printList(l), "6, 7, 2, 3, 4, 5");
    l.add(6, 8);
    isEqual(printList(l), "6, 7, 2, 3, 4, 5, 8");
    Integer[] ints = new Integer[15];
    Integer[] z = l.toArray(ints);
    expect(z == ints);
    for (int i=0; i < z.length; i++) {
      System.out.println(z[i]);
    }

    testIterators(new ArrayList());
    testIterators(new LinkedList());

    testIterators2(new ArrayList());
    testIterators2(new LinkedList());
  }
}
