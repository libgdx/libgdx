package com.badlogic.gdx.models.md2;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LittleEndianDataInputStream extends FilterInputStream implements DataInput {
  private DataInputStream din;
  
  public LittleEndianDataInputStream(InputStream in) {
    super(in);
    din=new DataInputStream(in);
  }
  
  public void readFully(byte[] b) throws IOException {
    din.readFully(b);
  }
  
  public void readFully(byte[] b, int off, int len) throws IOException {
    din.readFully(b, off, len);
  }
  
  public int skipBytes(int n) throws IOException {
    return din.skipBytes(n);
  }
  
  public boolean readBoolean() throws IOException {
    return din.readBoolean();
  }

  public byte readByte() throws IOException {
    return din.readByte();
  }

  public int readUnsignedByte() throws IOException {
    return din.readUnsignedByte();
  }
  
  public short readShort() throws IOException {
    int low=din.read();
    int high=din.read();
    return (short)((high << 8) | (low & 0xff));
  }
  
  public int readUnsignedShort() throws IOException {
    int low=din.read();
    int high=din.read();
    return ((high & 0xff) << 8) | (low & 0xff);
  }
  
  public char readChar() throws IOException {
    return din.readChar();
  }
  
  public int readInt() throws IOException {
    int[] res=new int[4];
    for(int i=3;i>=0;i--)
      res[i]=din.read();
    
    return ((res[0] & 0xff) << 24) |
           ((res[1] & 0xff) << 16) |
           ((res[2] & 0xff) << 8) |
           (res[3] & 0xff);
  }

  public long readLong() throws IOException {
    int[] res=new int[8];
    for(int i=7;i>=0;i--)
      res[i]=din.read();
    
    return (((long)(res[0] & 0xff) << 56) |
            ((long)(res[1] & 0xff) << 48) |
            ((long)(res[2] & 0xff) << 40) |
            ((long)(res[3] & 0xff) << 32) |
            ((long)(res[4] & 0xff) << 24) |
            ((long)(res[5] & 0xff) << 16) |
            ((long)(res[6] & 0xff) <<  8) |
            ((long)(res[7] & 0xff)));
  }
  
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }
  
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public final String readLine() throws IOException {
    return din.readLine();
  }

  public String readUTF() throws IOException {
    return din.readUTF();
  }
}