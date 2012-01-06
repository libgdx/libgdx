/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.util.Comparator;

public class PersistentSet <T> implements Iterable <T> {
  private static final Node NullNode = new Node(null);

  static {
    NullNode.left = NullNode;
    NullNode.right = NullNode;
  }

  private final Node<T> root;
  private final Comparator<T> comparator;
  private final int size;

  public PersistentSet() {
    this(NullNode, new Comparator<T>() {
        public int compare(T a, T b) {
          return ((Comparable<T>) a).compareTo(b);
        }
      }, 0);
  }

  public PersistentSet(Comparator<T> comparator) {
    this(NullNode, comparator, 0);
  }

  private PersistentSet(Node<T> root, Comparator<T> comparator, int size) {
    this.root = root;
    this.comparator = comparator;
    this.size = size;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (java.util.Iterator it = iterator(); it.hasNext();) {
      sb.append(it.next());
      if (it.hasNext()) {
        sb.append(",");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  public Comparator<T> comparator() {
    return comparator;
  }

  public PersistentSet<T> add(T value) {
    return add(value, false);
  }

  public int size() {
    return size;
  }

  public PersistentSet<T> add(T value, boolean replaceExisting) {
    Path<T> p = find(value);
    if (! p.fresh) {
      if (replaceExisting) {
        return p.replaceWith(value);
      } else {
        return this;
      }
    }

    return add(p);
  }

  private PersistentSet<T> add(Path<T> p) {
    if (! p.fresh) throw new IllegalArgumentException();

    Node<T> new_ = p.node;
    Node<T> newRoot = p.root.root;
    Cell<Node<T>> ancestors = p.ancestors;

    // rebalance
    new_.red = true;
    while (ancestors != null && ancestors.value.red) {
      if (ancestors.value == ancestors.next.value.left) {
        if (ancestors.next.value.right.red) {
          ancestors.value.red = false;
          ancestors.next.value.right = new Node(ancestors.next.value.right);
          ancestors.next.value.right.red = false;
          ancestors.next.value.red = true;
          new_ = ancestors.next.value;
          ancestors = ancestors.next.next;
        } else {
          if (new_ == ancestors.value.right) {
            new_ = ancestors.value;
            ancestors = ancestors.next;

            Node<T> n = leftRotate(new_);
            if (ancestors.value.right == new_) {
              ancestors.value.right = n;
            } else {
              ancestors.value.left = n;
            }
            ancestors = new Cell(n, ancestors);
          }
          ancestors.value.red = false;
          ancestors.next.value.red = true;

          Node<T> n = rightRotate(ancestors.next.value);
          if (ancestors.next.next == null) {
            newRoot = n;
          } else if (ancestors.next.next.value.right == ancestors.next.value) {
            ancestors.next.next.value.right = n;
          } else {
            ancestors.next.next.value.left = n;
          }
          // done
        }
      } else {
        if (ancestors.next.value.left.red) {
          ancestors.value.red = false;
          ancestors.next.value.left = new Node(ancestors.next.value.left);
          ancestors.next.value.left.red = false;
          ancestors.next.value.red = true;
          new_ = ancestors.next.value;
          ancestors = ancestors.next.next;
        } else {
          if (new_ == ancestors.value.left) {
            new_ = ancestors.value;
            ancestors = ancestors.next;

            Node<T> n = rightRotate(new_);
            if (ancestors.value.right == new_) {
              ancestors.value.right = n;
            } else {
              ancestors.value.left = n;
            }
            ancestors = new Cell(n, ancestors);
          }
          ancestors.value.red = false;
          ancestors.next.value.red = true;

          Node<T> n = leftRotate(ancestors.next.value);
          if (ancestors.next.next == null) {
            newRoot = n;
          } else if (ancestors.next.next.value.right == ancestors.next.value) {
            ancestors.next.next.value.right = n;
          } else {
            ancestors.next.next.value.left = n;
          }
          // done
        }
      }
    }

    newRoot.red = false;

    return new PersistentSet(newRoot, comparator, size + 1);
  }

  private static <T> Node<T> leftRotate(Node<T> n) {
    Node<T> child = new Node(n.right);
    n.right = child.left;
    child.left = n;
    return child;
  }

  private static <T> Node<T> rightRotate(Node<T> n) {
    Node<T> child = new Node(n.left);
    n.left = child.right;
    child.right = n;
    return child;
  }

  public PersistentSet<T> remove(T value) {
    Path<T> p = find(value);
    if (! p.fresh) {
      return remove(p);
    }

    return this;
  }

  private PersistentSet<T> remove(Path<T> p) {
    if (size == 1) {
      if (p.node != root) {
        throw new IllegalArgumentException();
      }
      return new PersistentSet(NullNode, comparator, 0);
    }

    Node<T> new_ = p.node;
    Node<T> newRoot = p.root.root;
    Cell<Node<T>> ancestors = p.ancestors;

    Node<T> dead;
    if (new_.left == NullNode || new_.right == NullNode) {
      dead = new_;
    } else {
      Cell<Node<T>> path = successor(new_, ancestors);
      dead = path.value;
      ancestors = path.next;
    }
    
    Node<T> child;
    if (dead.left != NullNode) {
      child = new Node(dead.left);
    } else if (dead.right != NullNode) {
      child = new Node(dead.right);
    } else {
      child = NullNode;
    }

    if (ancestors == null) {
      child.red = false;
      return new PersistentSet(child, comparator, 1);
    } else if (dead == ancestors.value.left) {
      ancestors.value.left = child;
    } else {
      ancestors.value.right = child;
    }

    if (dead != new_) {
      new_.value = dead.value;
    }

    if (! dead.red) {
      // rebalance
      while (ancestors != null && ! child.red) {
        if (child == ancestors.value.left) {
          Node<T> sibling = ancestors.value.right
            = new Node(ancestors.value.right);
          if (sibling.red) {
            sibling.red = false;
            ancestors.value.red = true;
            
            Node<T> n = leftRotate(ancestors.value);
            if (ancestors.next == null) {
              newRoot = n;
            } else if (ancestors.next.value.right == ancestors.value) {
              ancestors.next.value.right = n;
            } else {
              ancestors.next.value.left = n;
            }
            ancestors.next = new Cell(n, ancestors.next);

            sibling = ancestors.value.right;
          }

          if (! (sibling.left.red || sibling.right.red)) {
            sibling.red = true;
            child = ancestors.value;
            ancestors = ancestors.next;
          } else {
            if (! sibling.right.red) {
              sibling.left = new Node(sibling.left);
              sibling.left.red = false;

              sibling.red = true;
              sibling = ancestors.value.right = rightRotate(sibling);
            }

            sibling.red = ancestors.value.red;
            ancestors.value.red = false;

            sibling.right = new Node(sibling.right);
            sibling.right.red = false;
            
            Node<T> n = leftRotate(ancestors.value);
            if (ancestors.next == null) {
              newRoot = n;
            } else if (ancestors.next.value.right == ancestors.value) {
              ancestors.next.value.right = n;
            } else {
              ancestors.next.value.left = n;
            }

            child = newRoot;
            ancestors = null;
          }
        } else {
          Node<T> sibling = ancestors.value.left
            = new Node(ancestors.value.left);
          if (sibling.red) {
            sibling.red = false;
            ancestors.value.red = true;
            
            Node<T> n = rightRotate(ancestors.value);
            if (ancestors.next == null) {
              newRoot = n;
            } else if (ancestors.next.value.left == ancestors.value) {
              ancestors.next.value.left = n;
            } else {
              ancestors.next.value.right = n;
            }
            ancestors.next = new Cell(n, ancestors.next);

            sibling = ancestors.value.left;
          }

          if (! (sibling.right.red || sibling.left.red)) {
            sibling.red = true;
            child = ancestors.value;
            ancestors = ancestors.next;
          } else {
            if (! sibling.left.red) {
              sibling.right = new Node(sibling.right);
              sibling.right.red = false;

              sibling.red = true;
              sibling = ancestors.value.left = leftRotate(sibling);
            }

            sibling.red = ancestors.value.red;
            ancestors.value.red = false;

            sibling.left = new Node(sibling.left);
            sibling.left.red = false;
            
            Node<T> n = rightRotate(ancestors.value);
            if (ancestors.next == null) {
              newRoot = n;
            } else if (ancestors.next.value.left == ancestors.value) {
              ancestors.next.value.left = n;
            } else {
              ancestors.next.value.right = n;
            }

            child = newRoot;
            ancestors = null;
          }
        }
      }

      child.red = false;
    }

    return new PersistentSet(newRoot, comparator, size - 1);
  }

  private static <T> Cell<Node<T>> minimum(Node<T> n,
                                           Cell<Node<T>> ancestors)
  {
    while (n.left != NullNode) {
      n.left = new Node(n.left);
      ancestors = new Cell(n, ancestors);
      n = n.left;
    }

    return new Cell(n, ancestors);
  }

  private static <T> Cell<Node<T>> successor(Node<T> n,
                                             Cell<Node<T>> ancestors)
  {
    if (n.right != NullNode) {
      n.right = new Node(n.right);
      return minimum(n.right, new Cell(n, ancestors));
    }

    while (ancestors != null && n == ancestors.value.right) {
      n = ancestors.value;
      ancestors = ancestors.next;
    }

    return ancestors;
  }

  public Path<T> find(T value) {
    Node<T> newRoot = new Node(root);
    Cell<Node<T>> ancestors = null;

    Node<T> old = root;
    Node<T> new_ = newRoot;
    while (old != NullNode) {
      ancestors = new Cell(new_, ancestors);

      int difference = comparator.compare(value, old.value);
      if (difference < 0) {
        old = old.left;
        new_ = new_.left = new Node(old);
      } else if (difference > 0) {
        old = old.right;
        new_ = new_.right = new Node(old);
      } else {
        return new Path(false, new_,
                        new PersistentSet(newRoot, comparator, size),
                        ancestors.next);
      }
    }

    new_.value = value;
    return new Path(true, new_,
                    new PersistentSet(newRoot, comparator, size),
                    ancestors);
  }

  public Path<T> first() {
    if (root == NullNode) return null;

    Node<T> newRoot = new Node(root);
    Cell<Node<T>> ancestors = null;

    Node<T> old = root;
    Node<T> new_ = newRoot;
    while (old.left != NullNode) {
      ancestors = new Cell(new_, ancestors);

      old = old.left;
      new_ = new_.left = new Node(old);
    }

    return new Path(true, new_,
                    new PersistentSet(newRoot, comparator, size),
                    ancestors);
  }

  public Path<T> last() {
    if (root == NullNode) return null;

    Node<T> newRoot = new Node(root);
    Cell<Node<T>> ancestors = null;

    Node<T> old = root;
    Node<T> new_ = newRoot;
    while (old.right != NullNode) {
      ancestors = new Cell(new_, ancestors);

      old = old.right;
      new_ = new_.right = new Node(old);
    }

    return new Path(true, new_,
                    new PersistentSet(newRoot, comparator, size),
                    ancestors);
  }

  public java.util.Iterator<T> iterator() {
    return new Iterator(first());
  }

  private Path<T> successor(Path<T> p) {
    Cell<Node<T>> s = successor(p.node, p.ancestors);
    if (s == null) {
      return null;
    } else {
      return new Path(false, s.value, p.root, s.next);
    }
  }

  private static class Node <T> {
    public T value;
    public Node left;
    public Node right;
    public boolean red;
    
    public Node(Node<T> basis) {
      if (basis != null) {
        value = basis.value;
        left = basis.left;
        right = basis.right;
        red = basis.red;
      }
    }
  }

  public static class Path <T> {
    private final boolean fresh;
    private final Node<T> node;
    private final PersistentSet<T> root;
    private final Cell<Node<T>> ancestors;
    
    public Path(boolean fresh, Node<T> node, PersistentSet<T> root,
                Cell<Node<T>> ancestors)
    {
      this.fresh = fresh;
      this.node = node;
      this.root = root;
      this.ancestors = ancestors;
    }

    public T value() {
      return node.value;
    }

    public boolean fresh() {
      return fresh;
    }

    public PersistentSet<T> root() {
      return root;
    }

    public Path<T> successor() {
      return root.successor(this);
    }

    public PersistentSet<T> remove() {
      if (fresh) throw new IllegalStateException();

      return root.remove(this);
    }

    public PersistentSet<T> add() {
      if (! fresh) throw new IllegalStateException();

      return root.add(this);
    }

    public PersistentSet<T> replaceWith(T value) {
      if (fresh) throw new IllegalStateException();
      if (root.comparator.compare(node.value, value) != 0)
        throw new IllegalArgumentException();

      node.value = value;
      return root;
    }
  }
  
  public class Iterator <T> implements java.util.Iterator <T> {
    private PersistentSet.Path<T> path;

    private Iterator(PersistentSet.Path<T> path) {
      this.path = path;
    }

    private Iterator(Iterator<T> start) {
      path = start.path;
    }

    public boolean hasNext() {
      return path != null;
    }

    public T next() {
      PersistentSet.Path<T> p = path;
      path = path.successor();
      return p.value();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
