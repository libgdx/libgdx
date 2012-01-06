/* Copyright (c) 2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import static avian.Stream.write1;
import static avian.Stream.write2;
import static avian.Stream.write4;

import java.util.List;
import java.io.OutputStream;
import java.io.IOException;

public class ConstantPool {
  private static final int CONSTANT_Integer = 3;
  private static final int CONSTANT_Utf8 = 1;
  private static final int CONSTANT_String = 8;
  private static final int CONSTANT_Class = 7;
  private static final int CONSTANT_NameAndType = 12;
  private static final int CONSTANT_Fieldref = 9;
  private static final int CONSTANT_Methodref = 10;
  
  public static int add(List<PoolEntry> pool, PoolEntry e) {
    int i = 0;
    for (PoolEntry existing: pool) {
      if (existing.equals(e)) {
        return i;
      } else {
        ++i;
      }
    }
    pool.add(e);
    return pool.size() - 1;
  }

  public static int addInteger(List<PoolEntry> pool, int value) {
    return add(pool, new IntegerPoolEntry(value));
  }

  public static int addUtf8(List<PoolEntry> pool, String value) {
    return add(pool, new Utf8PoolEntry(value));
  }

  public static int addString(List<PoolEntry> pool, String value) {
    return add(pool, new StringPoolEntry(addUtf8(pool, value)));
  }

  public static int addClass(List<PoolEntry> pool, String name) {
    return add(pool, new ClassPoolEntry(addUtf8(pool, name)));
  }

  public static int addNameAndType(List<PoolEntry> pool,
                                   String name,
                                   String type)
  {
    return add(pool, new NameAndTypePoolEntry
               (addUtf8(pool, name),
                addUtf8(pool, type)));
  }

  public static int addFieldRef(List<PoolEntry> pool,
                                String className,
                                String name,
                                String spec)
  {
    return add(pool, new FieldRefPoolEntry
               (addClass(pool, className),
                addNameAndType(pool, name, spec)));
  }

  public static int addMethodRef(List<PoolEntry> pool,
                                 String className,
                                 String name,
                                 String spec)
  {
    return add(pool, new MethodRefPoolEntry
               (addClass(pool, className),
                addNameAndType(pool, name, spec)));
  }

  public interface PoolEntry {
    public void writeTo(OutputStream out) throws IOException;
  }

  private static class IntegerPoolEntry implements PoolEntry {
    private final int value;

    public IntegerPoolEntry(int value) {
      this.value = value;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_Integer);
      write4(out, value);
    }

    public boolean equals(Object o) {
      return o instanceof IntegerPoolEntry 
        && ((IntegerPoolEntry) o).value == value;
    }
  }

  private static class Utf8PoolEntry implements PoolEntry {
    private final String data;

    public Utf8PoolEntry(String data) {
      this.data = data;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_Utf8);
      byte[] bytes = data.getBytes();
      write2(out, bytes.length);
      out.write(bytes);
    }

    public boolean equals(Object o) {
      return o instanceof Utf8PoolEntry
        && ((Utf8PoolEntry) o).data.equals(data);
    }
  }

  private static class StringPoolEntry implements PoolEntry {
    private final int valueIndex;

    public StringPoolEntry(int valueIndex) {
      this.valueIndex = valueIndex;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_String);
      write2(out, valueIndex + 1);
    }

    public boolean equals(Object o) {
      return o instanceof StringPoolEntry 
        && ((StringPoolEntry) o).valueIndex == valueIndex;
    }
  }

  private static class ClassPoolEntry implements PoolEntry {
    private final int nameIndex;

    public ClassPoolEntry(int nameIndex) {
      this.nameIndex = nameIndex;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_Class);
      write2(out, nameIndex + 1);
    }

    public boolean equals(Object o) {
      return o instanceof ClassPoolEntry 
        && ((ClassPoolEntry) o).nameIndex == nameIndex;
    }
  }

  private static class NameAndTypePoolEntry implements PoolEntry {
    private final int nameIndex;
    private final int typeIndex;

    public NameAndTypePoolEntry(int nameIndex, int typeIndex) {
      this.nameIndex = nameIndex;
      this.typeIndex = typeIndex;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_NameAndType);
      write2(out, nameIndex + 1);
      write2(out, typeIndex + 1);
    }

    public boolean equals(Object o) {
      if (o instanceof NameAndTypePoolEntry) {
        NameAndTypePoolEntry other = (NameAndTypePoolEntry) o;
        return other.nameIndex == nameIndex && other.typeIndex == typeIndex;
      } else {
        return false;
      }
    }
  }

  private static class FieldRefPoolEntry implements PoolEntry {
    private final int classIndex;
    private final int nameAndTypeIndex;

    public FieldRefPoolEntry(int classIndex, int nameAndTypeIndex) {
      this.classIndex = classIndex;
      this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_Fieldref);
      write2(out, classIndex + 1);
      write2(out, nameAndTypeIndex + 1);
    }

    public boolean equals(Object o) {
      if (o instanceof FieldRefPoolEntry) {
        FieldRefPoolEntry other = (FieldRefPoolEntry) o;
        return other.classIndex == classIndex
          && other.nameAndTypeIndex == nameAndTypeIndex;
      } else {
        return false;
      }
    }
  }

  private static class MethodRefPoolEntry implements PoolEntry {
    private final int classIndex;
    private final int nameAndTypeIndex;

    public MethodRefPoolEntry(int classIndex, int nameAndTypeIndex) {
      this.classIndex = classIndex;
      this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public void writeTo(OutputStream out) throws IOException {
      write1(out, CONSTANT_Methodref);
      write2(out, classIndex + 1);
      write2(out, nameAndTypeIndex + 1);
    }

    public boolean equals(Object o) {
      if (o instanceof MethodRefPoolEntry) {
        MethodRefPoolEntry other = (MethodRefPoolEntry) o;
        return other.classIndex == classIndex
          && other.nameAndTypeIndex == nameAndTypeIndex;
      } else {
        return false;
      }
    }
  }
}
