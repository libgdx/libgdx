package extra;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class QueryDump {
  private static final int Root = 0;
  private static final int Size = 1;
  private static final int ClassName = 2;
  private static final int Push = 3;
  private static final int Pop = 4;

  private static int readInt(InputStream in) throws IOException {
    int b1 = in.read();
    int b2 = in.read();
    int b3 = in.read();
    int b4 = in.read();
    if (b4 == -1) throw new EOFException();
    return (int) ((b1 << 24) | (b2 << 16) | (b3 << 8) | (b4));    
  }

  private static String readString(InputStream in) throws IOException {
    int count = readInt(in);
    byte[] b = new byte[count];
    int offset = 0;
    int c;
    while ((c = in.read(b, offset, b.length - offset)) != -1
           && offset < b.length)
    {
      offset += c;
    }
    if (offset != b.length) throw new EOFException();
    return new String(b);
  }

  private static Record record(Map<Integer, Record> map, int key) {
    Record r = map.get(key);
    if (r == null) {
      map.put(key, r = new Record(key));
    }
    return r;
  }

  private static <T> void push(List<T> stack, T value) {
    stack.add(value);
  }

  private static <T> T pop(List<T> stack) {
    return stack.remove(stack.size() - 1);
  }

  private static <T> T peek(List<T> stack, int offset) {
    return stack.get(stack.size() - 1 - offset);
  }

  private static <T> T peek(List<T> stack) {
    return peek(stack, 0);
  }

  private static Set<Node> nodes(Record record) {
    if (record.nodes == null) {
      record.nodes = new HashSet<Node>(2);
    }
    return record.nodes;
  }

  private static void query(Map<Integer, Node> nodes, Record[] query,
                            List<Instance> stack, int index)
  {
    Node node = nodes.get(peek(stack, index).key);
    if (node != null) {
      int base = node.index();
      for (int i = base + 1; i < query.length; ++i) {
        int peek = index + i - base;
        if (peek < stack.size()) {
          Instance instance = peek(stack, peek);
          if (query[i] == instance.record) {
            TreeNode next = (TreeNode) nodes.get(instance);
            if (next == null) {
              nodes.put(instance.key, next = new TreeNode(instance, i));
            }
            next.children.add(node);
            node = next;
          } else {
            return;
          }
        } else {
          return;
        }
      }

      if (index + query.length - base < stack.size()) {
        nodes(peek(stack, index + query.length - base).record).add(node);
      }
    }
  }

  private static void query(Map<Integer, Node> nodes, Record[] query,
                            List<Instance> stack)
  {
    if (stack.size() > 1) {
      Instance instance = peek(stack, 1);
      if (instance != null && instance.record == query[0]) {
        Node node = nodes.get(instance.key);
        if (node == null) {
          nodes.put(instance.key, new LeafNode(instance));
          query(nodes, query, stack, 1);
        }
        return;
      }
    }

    query(nodes, query, stack, 0);      
  }

  private static Map<Integer, Record> read(InputStream in,
                                           String[] queryClasses)
    throws IOException
  {
    boolean done = false;
    boolean popped = false;
    Map<Integer, Record> records = new HashMap();
    Map<Integer, Node> nodes = new HashMap();
    List<Instance> stack = new ArrayList();
    Record[] query = new Record[queryClasses.length];

    Record roots = new Record(-1, "<roots>");
    records.put(roots.key, roots);

    while (! done) {
      int flag = in.read();
      switch (flag) {
      case Root: {
        stack.clear();
        push(stack, new Instance(readInt(in)));

        query(nodes, query, stack);

        popped = false;
        // System.out.println("root " + last);
      } break;

      case ClassName: {
        String name = readString(in);
        Record r = record(records, peek(stack).key);
        r.name = name;

        for (int i = 0; i < queryClasses.length; ++i) {
          if (queryClasses[i].equals(name)) {
            query[i] = r;
          }
        }

        query(nodes, query, stack);
      } break;

      case Push: {
        int key = readInt(in);

        if (! popped) {
          peek(stack).record = record(records, key);
        }

        push(stack, new Instance(key));

        query(nodes, query, stack);

        popped = false;
      } break;

      case Pop: {
        pop(stack);

        popped = true;
      } break;

      case Size: {
        peek(stack).size = readInt(in);
      } break;

      case -1:
        done = true;
        break;

      default:
        throw new RuntimeException("bad flag: " + flag);
      }
    }

    return records;
  }

  private static String[] copy(String[] array, int offset, int length) {
    String[] copy = new String[length];
    if (length > 0) {
      System.arraycopy(array, offset, copy, 0, length);
    }

    return copy;
  }

  private static void visitLeaves(Set<Node> nodes, LeafVisitor visitor) {
    for (Node n: nodes) {
      n.visitLeaves(visitor);
    }
  }

  private static void usageAndExit() {
    System.err.println("usage: java QueryDump <heap dump> <word size> " +
                       "<query class> ...");
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      usageAndExit();
    }

    Map<Integer, Record> map = read
      (new BufferedInputStream(new FileInputStream(args[0])),
       copy(args, 2, args.length - 2));

    for (Iterator<Record> it = map.values().iterator(); it.hasNext();) {
      final Record r = it.next();
      if (r.nodes == null) {
        it.remove();
      } else {
        visitLeaves(r.nodes, new LeafVisitor() {
            private Set<Instance> set = new HashSet();

            public void visit(LeafNode node) {
              if (! set.contains(node.instance)) {
                r.footprint += node.instance.size;
                ++ r.count;
              }
              set.add(node.instance);
            }
          });
      }
    }

    Record[] array = map.values().toArray(new Record[map.size()]);
    Arrays.sort(array, new Comparator<Record>() {
        public int compare(Record a, Record b) {
          return b.footprint - a.footprint;
        }
      });

    int wordSize = Integer.parseInt(args[1]);

    int footprint = 0;
    int count = 0;
    for (Record r: array) {
      if (r.name == null) {
        r.name = String.valueOf(r.key);
      }
      System.out.println
        (r.name + ": " + (r.footprint * wordSize) + " " + r.count);
      footprint += r.footprint;
      count += r.count;
    }

    System.out.println();
    System.out.println("total: " + (footprint * wordSize) + " " + count);
  }

  private static class Record {
    public final int key;
    public String name;
    public int footprint;
    public int count;
    public Set<Node> nodes;

    public Record(int key) {
      this(key, null);
    }

    public Record(int key, String name) {
      this.key = key;
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  private static class Instance {
    public final int key;
    public int size;
    public Record record;

    public Instance(int key) {
      this.key = key;
    }

    public String toString() {
      return "[" + key + " " + record + "]";
    }
  }

  public interface Node {
    public void visitLeaves(LeafVisitor visitor);
    public int index();
  }

  public static class LeafNode implements Node {
    public final Instance instance;

    public LeafNode(Instance instance) {
      this.instance = instance;
    }

    public void visitLeaves(LeafVisitor visitor) {
      visitor.visit(this);
    }

    public int index() {
      return 0;
    }
  }

  public static class TreeNode implements Node {
    public final Instance instance;
    public final int index;

    public final Set<Node> children = new HashSet(2);

    public TreeNode(Instance instance, int index) {
      this.instance = instance;
      this.index = index;
    }

    public void visitLeaves(LeafVisitor visitor) {
      QueryDump.visitLeaves(children, visitor);
    }

    public int index() {
      return index;
    }
  }

  public interface LeafVisitor {
    public void visit(LeafNode node);
  }
}
