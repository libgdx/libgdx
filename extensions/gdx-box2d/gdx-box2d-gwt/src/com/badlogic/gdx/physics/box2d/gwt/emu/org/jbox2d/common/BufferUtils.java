package org.jbox2d.common;

import java.lang.reflect.Array;

import com.badlogic.gdx.utils.reflect.ArrayReflection;
import com.badlogic.gdx.utils.reflect.ClassReflection;

public class BufferUtils {
  /** Reallocate a buffer. */
  public static <T> T[] reallocateBuffer(Class<T> klass, T[] oldBuffer, int oldCapacity,
      int newCapacity) {
    assert (newCapacity > oldCapacity);
    @SuppressWarnings("unchecked")
    T[] newBuffer = (T[]) ArrayReflection.newInstance(klass, newCapacity);
    if (oldBuffer != null) {
      System.arraycopy(oldBuffer, 0, newBuffer, 0, oldCapacity);
    }
    for (int i = oldCapacity; i < newCapacity; i++) {
      try {
        newBuffer[i] = ClassReflection.newInstance(klass);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return newBuffer;
  }

  /** Reallocate a buffer. */
  public static int[] reallocateBuffer(int[] oldBuffer, int oldCapacity, int newCapacity) {
    assert (newCapacity > oldCapacity);
    int[] newBuffer = new int[newCapacity];
    if (oldBuffer != null) {
      System.arraycopy(oldBuffer, 0, newBuffer, 0, oldCapacity);
    }
    return newBuffer;
  }

  /** Reallocate a buffer. */
  public static float[] reallocateBuffer(float[] oldBuffer, int oldCapacity, int newCapacity) {
    assert (newCapacity > oldCapacity);
    float[] newBuffer = new float[newCapacity];
    if (oldBuffer != null) {
      System.arraycopy(oldBuffer, 0, newBuffer, 0, oldCapacity);
    }
    return newBuffer;
  }

  /**
   * Reallocate a buffer. A 'deferred' buffer is reallocated only if it is not NULL. If
   * 'userSuppliedCapacity' is not zero, buffer is user supplied and must be kept.
   */
  public static <T> T[] reallocateBuffer(Class<T> klass, T[] buffer, int userSuppliedCapacity,
      int oldCapacity, int newCapacity, boolean deferred) {
    assert (newCapacity > oldCapacity);
    assert (userSuppliedCapacity == 0 || newCapacity <= userSuppliedCapacity);
    if ((!deferred || buffer != null) && userSuppliedCapacity == 0) {
      buffer = reallocateBuffer(klass, buffer, oldCapacity, newCapacity);
    }
    return buffer;
  }

  /**
   * Reallocate an int buffer. A 'deferred' buffer is reallocated only if it is not NULL. If
   * 'userSuppliedCapacity' is not zero, buffer is user supplied and must be kept.
   */
  public static int[] reallocateBuffer(int[] buffer, int userSuppliedCapacity, int oldCapacity,
      int newCapacity, boolean deferred) {
    assert (newCapacity > oldCapacity);
    assert (userSuppliedCapacity == 0 || newCapacity <= userSuppliedCapacity);
    if ((!deferred || buffer != null) && userSuppliedCapacity == 0) {
      buffer = reallocateBuffer(buffer, oldCapacity, newCapacity);
    }
    return buffer;
  }

  /**
   * Reallocate a float buffer. A 'deferred' buffer is reallocated only if it is not NULL. If
   * 'userSuppliedCapacity' is not zero, buffer is user supplied and must be kept.
   */
  public static float[] reallocateBuffer(float[] buffer, int userSuppliedCapacity, int oldCapacity,
      int newCapacity, boolean deferred) {
    assert (newCapacity > oldCapacity);
    assert (userSuppliedCapacity == 0 || newCapacity <= userSuppliedCapacity);
    if ((!deferred || buffer != null) && userSuppliedCapacity == 0) {
      buffer = reallocateBuffer(buffer, oldCapacity, newCapacity);
    }
    return buffer;
  }

  /** Rotate an array, see std::rotate */
  public static <T> void rotate(T[] ray, int first, int new_first, int last) {
    int next = new_first;
    while (next != first) {
      T temp = ray[first];
      ray[first] = ray[next];
      ray[next] = temp;
      first++;
      next++;
      if (next == last) {
        next = new_first;
      } else if (first == new_first) {
        new_first = next;
      }
    }
  }

  /** Rotate an array, see std::rotate */
  public static void rotate(int[] ray, int first, int new_first, int last) {
    int next = new_first;
    while (next != first) {
      int temp = ray[first];
      ray[first] = ray[next];
      ray[next] = temp;
      first++;
      next++;
      if (next == last) {
        next = new_first;
      } else if (first == new_first) {
        new_first = next;
      }
    }
  }

  /** Rotate an array, see std::rotate */
  public static void rotate(float[] ray, int first, int new_first, int last) {
    int next = new_first;
    while (next != first) {
      float temp = ray[first];
      ray[first] = ray[next];
      ray[next] = temp;
      first++;
      next++;
      if (next == last) {
        next = new_first;
      } else if (first == new_first) {
        new_first = next;
      }
    }
  }
}
