/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class StringBuilder implements CharSequence, Appendable {
  private static final int BufferSize = 32;

  private Cell chain;
  private int length;
  private char[] buffer;
  private int position;

  public StringBuilder(String s) {
    append(s);
  }

  public StringBuilder(int capacity) { }

  public StringBuilder() {
    this(0);
  }

  private void flush() {
    if (position > 0) {
      chain = new Cell(new String(buffer, 0, position, false), chain);
      buffer = null;
      position = 0;
    }
  }

  public StringBuilder append(String s) {
    if (s == null) {
      return append("null");
    } else {
      if (s.length() > 0) {
        if (buffer != null && s.length() <= buffer.length - position) {
          s.getChars(0, s.length(), buffer, position);
          position += s.length();
        } else {
          flush();
          chain = new Cell(s, chain);
        }
        length += s.length();
      }
      return this;
    }
  }

  public StringBuilder append(StringBuffer sb) {
    return append(sb.toString());
  }

  public StringBuilder append(CharSequence sequence) {
    return append(sequence.toString());
  }

  public Appendable append(CharSequence sequence, int start, int end) {
    return append(sequence.subSequence(start, end));
  }

  public StringBuilder append(char[] b, int offset, int length) {
    return append(new String(b, offset, length));
  }

  public StringBuilder append(Object o) {
    return append(o == null ? "null" : o.toString());
  }

  public StringBuilder append(char v) {
    if (buffer == null) {
      buffer = new char[BufferSize];
    } else if (position >= buffer.length) {
      flush();
      buffer = new char[BufferSize];
    }

    buffer[position++] = v;
    ++ length;

    return this;
  }

  public StringBuilder append(boolean v) {
    return append(String.valueOf(v));
  }

  public StringBuilder append(int v) {
    return append(String.valueOf(v));
  }

  public StringBuilder append(long v) {
    return append(String.valueOf(v));
  }

  public StringBuilder append(float v) {
    return append(String.valueOf(v));
  }

  public StringBuilder append(double v) {
    return append(String.valueOf(v));
  }

  public char charAt(int i) {
    if (i < 0 || i >= length) {
      throw new IndexOutOfBoundsException();
    }

    flush();

    int index = length;
    for (Cell c = chain; c != null; c = c.next) {
      int start = index - c.value.length();
      index = start;
      
      if (i >= start) {
        return c.value.charAt(i - start);
      }
    }

    throw new RuntimeException();
  }

  public StringBuilder insert(int i, String s) {
    if (i < 0 || i > length) {
      throw new IndexOutOfBoundsException();
    }

    if (i == length) {
      append(s);
    } else {
      flush();

      int index = length;
      for (Cell c = chain; c != null; c = c.next) {
        int start = index - c.value.length();
        index = start;
      
        if (i >= start) {
          if (i == start) {
            c.next = new Cell(s, c.next);
          } else {
            String v = c.value;
            c.value = v.substring(i - start, v.length());
            c.next = new Cell(s, new Cell(v.substring(0, i - start), c.next));
          }
          break;
        }
      }

      length += s.length();
    }

    return this;
  }

  public StringBuilder insert(int i, CharSequence s) {
    return insert(i, s.toString());
  }

  public StringBuilder insert(int i, char c) {
    return insert(i, new String(new char[] { c }, 0, 1, false));
  }

  public StringBuilder insert(int i, int v) {
    return insert(i, String.valueOf(v));
  }

  public StringBuilder delete(int start, int end) {
    if (start >= end) {
      return this;
    }

    if (start < 0 || end > length) {
      throw new IndexOutOfBoundsException();
    }

    flush();

    int index = length;
    Cell p = null;
    for (Cell c = chain; c != null; c = c.next) {
      int e = index;
      int s = index - c.value.length();
      index = s;
      
      if (end >= e) {
        if (start <= s) {
          if (p == null) {
            chain = c.next;
          } else {
            p.next = c.next;
          }          
        } else {
          c.value = c.value.substring(0, start - s);
          break;
        }
      } else {
        if (start <= s) {
          c.value = c.value.substring(end - s, e - s);
        } else {
          String v = c.value;
          c.value = v.substring(end - s, e - s);
          c.next = new Cell(v.substring(0, start - s), c.next);
          break;
        }        
      }
    }

    length -= (end - start);

    return this;
  }

  public StringBuilder deleteCharAt(int i) {
    return delete(i, i + 1);
  }

  public StringBuilder replace(int start, int end, String str) {
    delete(start, end);
    insert(start, str);
    return this;
  }
  
  public int indexOf(String s) {
    return indexOf(s, 0);
  }
  
  public int indexOf(String s, int start) {
    int slength = s.length();
    if (slength == 0) return start;

    for (int i = start; i < length - slength + 1; ++i) {
      int j = 0;
      for (; j < slength; ++j) {
        if (charAt(i + j) != s.charAt(j)) {
          break;
        }
      }
      if (j == slength) {
        return i;
      }
    }

    return -1;
  }
  
  public int lastIndexOf(String s) {
    return lastIndexOf(s, length - s.length());
  }

  public int lastIndexOf(String s, int lastIndex) {
    int slength = s.length();
    if (slength == 0) return lastIndex;

    for (int i = Math.min(length - slength, lastIndex); i >= 0; --i) {
      int j = 0;
      for (; j < slength && i + j < length; ++j) {
        if (charAt(i + j) != s.charAt(j)) {
          break;
        }
      }
      if (j == slength) {
        return i;
      }
    }

    return -1;
  }

  public int length() {
    return length;
  }

  public void setLength(int v) {
    if (v < 0) {
      throw new IndexOutOfBoundsException();
    }

    if (v == 0) {
      length = 0;
      chain = null;
      return;
    }

    flush();

    int index = length;
    length = v;
    for (Cell c = chain; c != null; c = c.next) {
      int start = index - c.value.length();

      if (v > start) {
        if (v < index) {
          c.value = c.value.substring(0, v - start);
        }
        break;
      }

      chain = c.next;
      index = start;
    }
  }

  public void getChars(int srcStart, int srcEnd, char[] dst, int dstStart) {
    if (srcStart < 0 || srcEnd > length) {
      throw new IndexOutOfBoundsException();
    }

    flush();

    int index = length;
    for (Cell c = chain; c != null; c = c.next) {
      int start = index - c.value.length();
      int end = index;
      index = start;

      if (start < srcStart) {
        start = srcStart;
      }

      if (end > srcEnd) {
        end = srcEnd;
      }

      if (start < end) {
        c.value.getChars(start - index, end - index,
                         dst, dstStart + (start - srcStart));
      }
    }
  }

  public String toString() {
    char[] array = new char[length];
    getChars(0, length, array, 0);
    return new String(array, 0, length, false);
  }

  private static class Cell {
    public String value;
    public Cell next;

    public Cell(String value, Cell next) {
      this.value = value;
      this.next = next;
    }
  }

  public String substring(int start) {
    return substring(start, length);
  }

  public String substring(int start, int end) {
    int len = end-start;
    char[] buf = new char[len]; 
    getChars(start, end, buf, 0);
    return new String(buf, 0, len, false);
  }
        
  public CharSequence subSequence(int start, int end) {
    return substring(start, end);
  }

  public void setCharAt(int index, char ch) {
    if(index < 0 || index >= length) throw new IndexOutOfBoundsException();
    deleteCharAt(index);
    insert(index, ch);
  }

  public void ensureCapacity(int capacity) {
    // ignore
  }
}
