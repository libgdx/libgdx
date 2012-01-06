/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.zip;

public class Deflater {
  private static final int DEFAULT_LEVEL = 6; // default compression level (6 is default for gzip)
  private static final int Z_OK = 0;
  private static final int Z_STREAM_END = 1;
  private static final int Z_NEED_DICT = 2;

//   static {
//     System.loadLibrary("natives");
//   }

  private long peer;
  private byte[] input;
  private int offset;
  private int length;
  private boolean needDictionary;
  private boolean finished;
  private final boolean nowrap;
  private boolean finish;

  public Deflater(int level, boolean nowrap) {
    this.nowrap = nowrap;
    peer = make(nowrap, level);
  }

  public Deflater(int level) {
    this(level, false);
  }

  public Deflater() {
    this(DEFAULT_LEVEL);
  }

  private void check() {
    if (peer == 0) {
      throw new IllegalStateException();      
    }
  }

  private static native long make(boolean nowrap, int level);

  public boolean finished() {
    return finished;
  }

  public boolean needsDictionary() {
    return needDictionary;
  }

  public boolean needsInput() {
    return getRemaining() == 0;
  }

  public int getRemaining() {
    return length;
  }
  
  public void setLevel(int level) throws IllegalArgumentException {
    if (level < 0 || level > 9) {
      throw new IllegalArgumentException("Valid compression levels are 0-9");
    }

    dispose(peer);
    peer = make(nowrap, level);
  }
  
  public void setInput(byte[] input) {
    setInput(input, 0, input.length);
  }

  public void setInput(byte[] input, int offset, int length) {
    this.input = input;
    this.offset = offset;
    this.length = length;
  }

  public void reset() {
    dispose();
    peer = make(nowrap, DEFAULT_LEVEL);
    input = null;
    offset = length = 0;
    finish = false;
    needDictionary = finished = false;
  }

  public int deflate(byte[] output) {
    return deflate(output, 0, output.length);
  }

  public int deflate(byte[] output, int offset, int length) {
    final int zlibResult = 0;
    final int inputCount = 1;
    final int outputCount = 2;

    if (peer == 0) {
      throw new IllegalStateException();      
    }

    if (input == null || output == null) {
      throw new NullPointerException();
    }

    int[] results = new int[3];
    deflate(peer, 
            input, this.offset, this.length,
            output, offset, length, finish, results);

    if (results[zlibResult] < 0) {
      throw new AssertionError();
    }

    switch (results[zlibResult]) {
    case Z_NEED_DICT:
      needDictionary = true;
      break;

    case Z_STREAM_END:
      finished = true;
      break;
    }

    this.offset += results[inputCount];
    this.length -= results[inputCount];
    
    return results[outputCount];
  }

  public void finish() {
    finish = true;
  }

  private static native void deflate
    (long peer,
     byte[] input, int inputOffset, int inputLength,
     byte[] output, int outputOffset, int outputLength,
     boolean finish,
     int[] results);

  public void end() {
    dispose();
  }

  public void dispose() {
    if (peer != 0) {
      dispose(peer);
      peer = 0;
    }
  }

  private static native void dispose(long peer);
}
