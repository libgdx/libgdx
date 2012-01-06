/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import avian.VMClass;

import java.util.HashMap;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ObjectInputStream extends InputStream {
  private final InputStream in;
  private final PushbackReader r;

  public ObjectInputStream(InputStream in) {
    this.in = in;
    this.r = new PushbackReader(new InputStreamReader(in));
  }

  public int read() throws IOException {
    return in.read();
  }

  public int read(byte[] b, int offset, int length) throws IOException {
    return in.read(b, offset, length);
  }

  public void close() throws IOException {
    in.close();
  }

  public Object readObject() throws IOException, ClassNotFoundException {
    return readObject(new HashMap());
  }

  public boolean readBoolean() throws IOException {
    read('z');
    return readLongToken() != 0;
  }

  public byte readByte() throws IOException {
    read('b');
    return (byte) readLongToken();
  }

  public char readChar() throws IOException {
    read('c');
    return (char) readLongToken();
  }

  public short readShort() throws IOException {
    read('s');
    return (short) readLongToken();
  }

  public int readInt() throws IOException {
    read('i');
    return (int) readLongToken();
  }

  public long readLong() throws IOException {
    read('j');
    return readLongToken();
  }

  public float readFloat() throws IOException {
    read('f');
    return (float) readDoubleToken();
  }

  public double readDouble() throws IOException {
    read('d');
    return readDoubleToken();
  }
  
  public void defaultReadObject() throws IOException {
    throw new UnsupportedOperationException();
  }

  private void skipSpace() throws IOException {
    int c;
    while ((c = r.read()) != -1 && Character.isWhitespace((char) c));
    if (c != -1) {
      r.unread(c);
    }
  }

  private void read(char v) throws IOException {
    skipSpace();

    int c = r.read();
    if (c != v) {
      if (c == -1) {
        throw new EOFException();
      } else {
        throw new StreamCorruptedException();
      }
    }
  }

  private String readStringToken() throws IOException {
    skipSpace();

    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = r.read()) != -1 && ! Character.isWhitespace((char) c) && c != ')') {
      sb.append((char) c);
    }
    if (c != -1) {
      r.unread(c);
    }
    return sb.toString();
  }

  private long readLongToken() throws IOException {
    return Long.parseLong(readStringToken());
  }

  private double readDoubleToken() throws IOException {
    return Double.parseDouble(readStringToken());
  }

  private Object readObject(HashMap<Integer, Object> map)
    throws IOException, ClassNotFoundException
  {
    skipSpace();
    switch (r.read()) {
    case 'a':
      return deserializeArray(map);
    case 'l':
      return deserializeObject(map);
    case 'n':
      return null;
    case -1:
      throw new EOFException();
    default:
      throw new StreamCorruptedException();
    }
  }

  private Object deserialize(HashMap<Integer, Object> map)
    throws IOException, ClassNotFoundException
  {
    skipSpace(); 
    switch (r.read()) {
    case 'a':
      return deserializeArray(map);
    case 'l':
      return deserializeObject(map);
    case 'r':
      return map.get((int) readLongToken());
    case 'n':
      return null;
    case 'z':
      return (readLongToken() != 0);
    case 'b':
      return (byte) readLongToken();
    case 'c':
      return (char) readLongToken();
    case 's':
      return (short) readLongToken();
    case 'i':
      return (int) readLongToken();
    case 'j':
      return readLongToken();
    case 'f':
      return (float) readDoubleToken();
    case 'd':
      return readDoubleToken();
    case -1:
      throw new EOFException();
    default:
      throw new StreamCorruptedException();
    }
  }

  private Object deserializeArray(HashMap<Integer, Object> map)
    throws IOException, ClassNotFoundException
  {
    read('(');
    int id = (int) readLongToken();
    Class c = Class.forName(readStringToken());
    int length = (int) readLongToken();
    Class t = c.getComponentType();
    Object o = Array.newInstance(t, length);

    map.put(id, o);
  
    for (int i = 0; i < length; ++i) {
      Array.set(o, i, deserialize(map));
    }

    read(')');

    return o;
  }

  private static native Object makeInstance(VMClass c);

  private Object deserializeObject(HashMap<Integer, Object> map)
    throws IOException, ClassNotFoundException
  {
    read('(');
    int id = (int) readLongToken();
    Class c = Class.forName(readStringToken());
    Object o = makeInstance(c.vmClass);

    map.put(id, o);

    for (Field f: c.getAllFields()) {
      int modifiers = f.getModifiers();
      if ((modifiers & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
        try {
          f.set(o, deserialize(map));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    read(')');

    return o;
  }
}
