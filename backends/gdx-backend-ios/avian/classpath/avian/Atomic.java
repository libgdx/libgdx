package avian;

import java.lang.reflect.Field;

public class Atomic {
  public static native long getOffset(Field field);

  public static native boolean compareAndSwapObject
    (Object o, long offset, Object old, Object new_);
}
