package extra;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class Memory {
  private static final int ITERATION_COUNT=1;

  private static class Item {
    private static int instanceCount=0;
    private final int index;
    private final int val;
    public Item(int i) { val = i; index = instanceCount++; }
    public int value() { return val; }
    public int index() { return index; }
  }

  private static void traceFunc(String s) {
    if (false) {
      System.out.println(s);
    }
  }

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static int runningSum(Item[] items) {
    int sum=0;
    for (Item item : items) {
      sum += item.value();
    }
    return sum;
  }

  private static int runningSum(Collection<Item> items) {
    int sum=0;
    for (Item item : items) {
      sum += item.value();
    }
    return sum;
  }

  private static final void testArray() {
    traceFunc("testArray()");
    Item[] items = new Item[1750];

    for (int iter=0; iter < ITERATION_COUNT; iter++) {
      for (int i=0; i < 1000; i++) {
        items[i] = new Item(1);
      }
      for (int i=0; i < 500; i++) {
        items[i+1000] = new Item(4);
      }
      for (int i=0; i < 250; i++) {
        items[i+1500] = new Item(9);
      }
      expect(runningSum(items) == (1000*1 + 500*4 + 250*9));
      Item[] zeroItems = new Item[300];
      for (int i=0; i < 300; i++) {
        zeroItems[i] = new Item(0);
      }
      System.arraycopy(zeroItems, 0, items, 900, zeroItems.length);
      for (int i=0; i < 10000; i++) {
        items[0] = new Item(1);
      }
      expect(runningSum(items) == (900*1 + 300*4 + 250*9));
      for (int i=0; i < 300; i++) {
        zeroItems[i] = new Item((i+900) < 1000 ? 1 : 4);
      }
      for (int i=0; i < 10000; i++) {
        items[0] = new Item(1);
      }
      expect(runningSum(items) == (900*1 + 300*4 + 250*9));
      System.arraycopy(zeroItems, 0, items, 900, zeroItems.length);
      expect(runningSum(items) == (1000*1 + 500*4 + 250*9));
      for (int i=0; i < 1750; i++) {
        items[i] = null;
      }
    }
  }

  private static final void testHashMap() {
    traceFunc("testHashMap()");
    HashMap<Integer, Item> items = new HashMap<Integer, Item>();
    for (int iter=0; iter < ITERATION_COUNT; iter++) {
      for (int i=0; i < 1000; i++) {
        items.put(i, new Item(1));
      }
      for (int i=0; i < 500; i++) {
        items.put(i+1000, new Item(4));
      }
      for (int i=0; i < 250; i++) {
        items.put(i+1500, new Item(9));
      }
      expect(runningSum(items.values()) == (1000*1 + 500*4 + 250*9));
      for (int i = 900; i < 1200; i++) {
        items.remove(i);
      }
      expect(runningSum(items.values()) == (900*1 + 300*4 + 250*9));
      for (int i = 900; i < 1200; i++) {
        items.put(i, new Item(i < 1000 ? 1 : 4));
      }
      expect(runningSum(items.values()) == (1000*1 + 500*4 + 250*9));
      items.clear();
    }
  }

  private static final void testLinkedList() {
    traceFunc("testLinkedList()");
    LinkedList<Item> items = new LinkedList<Item>();
    for (int iter=0; iter < ITERATION_COUNT; iter++) {
      for (int i=0; i < 1000; i++) {
        items.add(new Item(1));
      }
      for (int i=0; i < 500; i++) {
        items.add(new Item(4));
      }
      for (int i=0; i < 250; i++) {
        items.add(new Item(9));
      }
      expect(runningSum(items) == (1000*1 + 500*4 + 250*9));
      for (int i = 1199; i >= 900; i--) {
        items.remove(i);
      }
      expect(runningSum(items) == (900*1 + 300*4 + 250*9));
      for (int i = 900; i < 1200; i++) {
        items.add(new Item(i < 1000 ? 1 : 4));
      }
      expect(runningSum(items) == (1000*1 + 500*4 + 250*9));
      items.clear();
    }
  }

  private static final void testTreeSet() {
    traceFunc("testTreeSet()");
    TreeSet<Item> items = new TreeSet<Item>(new Comparator<Item>() {
      public int compare(Item i1, Item i2) {
        int r = i1.value() - i2.value();
        if (r == 0) {
          return i1.index() - i2.index();
        }
        return r;
      }
    });
    for (int iter=0; iter < ITERATION_COUNT; iter++) {
      for (int i=0; i < 1000; i++) {
        items.add(new Item(1));
      }
      for (int i=0; i < 500; i++) {
        items.add(new Item(4));
      }
      for (int i=0; i < 250; i++) {
        items.add(new Item(9));
      }
      expect(runningSum(items) == (1000*1 + 500*4 + 250*9));
      items.clear();
    }
  }

  public static void main(String args[]) {
    for (int i=0; i < 10; i++) {
      testArray();
      testHashMap();
      testLinkedList();
      testTreeSet();
    }
  }
}
