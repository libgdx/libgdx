package java.util.concurrent;

import avian.Atomic;

public class ConcurrentLinkedQueue<T> {
  private static final long QueueHead;
  private static final long QueueTail;
  private static final long NodeNext;

  static {
    try {
      QueueHead = Atomic.getOffset
        (ConcurrentLinkedQueue.class.getField("head"));

      QueueTail = Atomic.getOffset
        (ConcurrentLinkedQueue.class.getField("tail"));

      NodeNext = Atomic.getOffset
        (Node.class.getField("next"));
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  private volatile Node<T> head = new Node(null, null);
  private volatile Node<T> tail = head;

  public void clear() {
    // todo: can we safely make this O(1)?
    while (poll() != null) { }
  }

  public boolean add(T value) {
    Node<T> n = new Node(value, null);
    while (true) {
      Node<T> t = tail;
      Node<T> next = tail.next;
      if (t == tail) {
        if (next != null) {
          Atomic.compareAndSwapObject(this, QueueTail, t, next);
        } else if (Atomic.compareAndSwapObject(tail, NodeNext, null, n)) {
          Atomic.compareAndSwapObject(this, QueueTail, t, n);
          break;
        }
      }
    }

    return true;
  }

  public T peek() {
    return poll(false);
  }

  public T poll() {
    return poll(true);
  }

  public T poll(boolean remove) {
    while (true) {
      Node<T> h = head;
      Node<T> t = tail;
      Node<T> next = head.next;

      if (h == head) {
        if (h == t) {
          if (next != null) {
            Atomic.compareAndSwapObject(this, QueueTail, t, next);
          } else {
            return null;
          }
        } else {
          T value = next.value;
          if ((! remove)
              || Atomic.compareAndSwapObject(this, QueueHead, h, next))
          {
            return value;
          }
        }
      }
    }
  }

  private static class Node<T> {
    public volatile T value;
    public volatile Node<T> next;

    public Node(T value, Node<T> next) {
      this.value = value;
      this.next = next;
    }
  }
}
