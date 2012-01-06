/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import java.util.IdentityHashMap;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ObjectOutputStream extends OutputStream {
  private final PrintStream out;

  public ObjectOutputStream(OutputStream out) {
    this.out = new PrintStream(out);
  }
  
  public void write(int c) throws IOException {
    out.write(c);
  }

  public void write(byte[] b, int offset, int length) throws IOException {
    out.write(b, offset, length);
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    out.close();
  }

  public void writeObject(Object o) throws IOException {
    writeObject(o, new IdentityHashMap(), new int[] {0});
  }

  public void writeBoolean(boolean v) {
    out.print("z");
    out.print((v ? 1 : 0));
  }

  public void writeByte(byte v) {
    out.print("b");
    out.print((int) v);
  }

  public void writeChar(char v) {
    out.print("c");
    out.print((int) v);
  }

  public void writeShort(short v) {
    out.print("s");
    out.print((int) v);
  }

  public void writeInt(int v) {
    out.print("i");
    out.print(v);
  }

  public void writeLong(long v) {
    out.print("j");
    out.print(v);
  }

  public void writeFloat(float v) {
    out.print("f");
    out.print(v);
  }

  public void writeDouble(double v) {
    out.print("d");
    out.print(v);
  }

  public void defaultWriteObject() throws IOException {
    throw new UnsupportedOperationException();
  }
  
  private void writeObject(Object o, IdentityHashMap<Object, Integer> map,
                           int[] nextId)
    throws IOException
  {
    if (o == null) {
      out.print("n");
    } else {
      Integer id = map.get(o);
      if (id == null) {
        map.put(o, nextId[0]);

        Class c = o.getClass();
        if (c.isArray()) {
          serializeArray(o, map, nextId);
        } else if (Serializable.class.isAssignableFrom(c)) {
          serializeObject(o, map, nextId);
        } else {
          throw new NotSerializableException(c.getName());
        }
      } else {
        out.print("r");
        out.print(id.intValue());
      }
    }
  }

  private void serializeArray(Object o, IdentityHashMap<Object, Integer> map,
                              int[] nextId)
    throws IOException
  {
    Class c = o.getClass();
    Class t = c.getComponentType();
    int length = Array.getLength(o);

    out.print("a(");
    out.print(nextId[0]++);
    out.print(" ");
    out.print(c.getName());
    out.print(" ");
    out.print(length);

    for (int i = 0; i < length; ++i) {
      out.print(" ");
      Object v = Array.get(o, i);
      if (t.equals(boolean.class)) {
        writeBoolean((Boolean) v);
      } else if (t.equals(byte.class)) {
        writeByte((Byte) v);
      } else if (t.equals(char.class)) {
        writeChar((Character) v);
      } else if (t.equals(short.class)) {
        writeShort((Short) v);
      } else if (t.equals(int.class)) {
        writeInt((Integer) v);
      } else if (t.equals(long.class)) {
        writeLong((Long) v);
      } else if (t.equals(float.class)) {
        writeFloat((Float) v);
      } else if (t.equals(double.class)) {
        writeDouble((Double) v);
      } else {
        writeObject(v, map, nextId);
      }
    }

    out.print(")");
  }
  
  private void serializeObject(Object o, IdentityHashMap<Object, Integer> map,
                               int[] nextId)
    throws IOException
  {
    Class c = o.getClass();

    out.print("l(");
    out.print(nextId[0]++);
    out.print(" ");
    out.print(c.getName());

    for (Field f: c.getAllFields()) {
      int modifiers = f.getModifiers();
      if ((modifiers & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
        out.print(" ");
        Object v;

        try {
          v = f.get(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        Class t = f.getType();
        if (t.equals(boolean.class)) {
          writeBoolean((Boolean) v);
        } else if (t.equals(byte.class)) {
          writeByte((Byte) v);
        } else if (t.equals(char.class)) {
          writeChar((Character) v);
        } else if (t.equals(short.class)) {
          writeShort((Short) v);
        } else if (t.equals(int.class)) {
          writeInt((Integer) v);
        } else if (t.equals(long.class)) {
          writeLong((Long) v);
        } else if (t.equals(float.class)) {
          writeFloat((Float) v);
        } else if (t.equals(double.class)) {
          writeDouble((Double) v);
        } else {
          writeObject(v, map, nextId);
        }
      }
    }

    out.print(")");
  }
  
}
