public class Longs {
  private static volatile long volatileLong = getConstant();
  
  private static long getConstant() {
    return 0x123456789ABCDEFL;
  }

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static long readLongFrom(java.io.InputStream in)
    throws java.io.IOException
  {
    long b1 = in.read();
    long b2 = in.read();
    long b3 = in.read();
    long b4 = in.read();
    long b5 = in.read();
    long b6 = in.read();
    long b7 = in.read();
    long b8 = in.read();
    if (b8 == -1) throw new java.io.EOFException();
    return (long) ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) |
                   (b5 << 24) | (b6 << 16) | (b7 << 8) | (b8));
  }

  public static void putInt(int val, byte[] dst, int offset) {
    System.out.println("put " + val);
    dst[offset]   = (byte)((val >> 24) & 0xff);
    dst[offset+1] = (byte)((val >> 16) & 0xff);
    dst[offset+2] = (byte)((val >>  8) & 0xff);
    dst[offset+3] = (byte)((val      ) & 0xff);
  }

  public static void putLong(long val, byte[] dst, int offset) {
    putInt((int)(val >> 32), dst, offset);
    putInt((int)val, dst, offset + 4);
  }

  public static int getInt(byte[] src, int offset) {
    int r = ((src[offset] & 0xFF) << 24)
      | ((src[offset + 1] & 0xFF) << 16)
      | ((src[offset + 2] & 0xFF) <<  8)
      | ((src[offset + 3] & 0xFF));
    System.out.println("get " + r);
    return r;
  }

  public static long getLong(byte[] src, int offset) {
    return ((long) getInt(src, offset) << 32)
      | ((long) getInt(src, offset + 4) & 0xffffffffL);
  }

  private static long roundUp(long a, long b) {
    a += b - 1L;
    return a - (a % b);
  }

  private static int negativeOne() {
    return -1;
  }

  private static long unsignedShiftRight32(long x) {
    return x >>> 32;
  }

  public static class Rectangle {
    public long x;
    public long y;
    public long width;
    public long height;

    public void setX(long x) {
      this.x = x;
    }
  }

  public static void main(String[] args) throws Exception {
    expect(volatileLong == getConstant());

    { Rectangle r = new Rectangle();
      Rectangle.class.getMethod("setX", long.class).invoke(r, 42L);
      expect(r.x == 42L);
    }

    { long a = 0x1FFFFFFFFL;
      long b = -1;
      expect(a != b);
    }

    expect(Math.abs(-123L) == 123L);

    expect(readLongFrom(new java.io.InputStream() {
        int step;

        public int read() {
          return ++step;
        }
      }) == 0x0102030405060708L);

    expect(((long) negativeOne()) == -1);

    { long foo = 25214903884L;
      int radix = 10;
      expect(foo > 0);
      foo /= radix;
      expect(foo > 0);
    }

    expect(roundUp(156, 2) == 156);
    expect(((int) roundUp(156, 2)) == 156);

    expect(Long.parseLong("25214903884") == 25214903884L);

    expect(Long.parseLong("-9223372036854775808") == -9223372036854775808L);

    expect(String.valueOf(25214903884L).equals("25214903884"));

    expect(String.valueOf(-9223372036854775808L).equals
           ("-9223372036854775808"));

    { long a = -5;
      long b = 2;
      expect(a >> b == -5L >> 2);
      expect(a >>> b == -5L >>> 2);
      expect(a << b == -5L << 2);
      expect(a + b == -5L + 2L);
      expect(a - b == -5L - 2L);
      expect(a * b == -5L * 2L);
      expect(a / b == -5L / 2L);
      expect(a % b == -5L % 2L);
      expect((a & b) == (-5L & 2L));
      expect((a | b) == (-5L | 2L));
      expect((a ^ b) == (-5L ^ 2L));
      expect(-a == 5L);
      expect(~a == ~-5L);

      a = 5;
      b = 2;
      expect(a >> b == 5L >> 2);
      expect(a >>> b == 5L >>> 2);
      expect(a << b == 5L << 2);
      expect(a + b == 5L + 2L);
      expect(a - b == 5L - 2L);
      expect(a * b == 5L * 2L);
      expect(a / b == 5L / 2L);
      expect(a % b == 5L % 2L);
      expect((a & b) == (5L & 2L));
      expect((a | b) == (5L | 2L));
      expect((a ^ b) == (5L ^ 2L));
      expect(-a == -5L);
      expect(~a == ~5L);
    }

    { long a = -25214903884L;
      long b = 2;
      expect(a >> b == -25214903884L >> 2);
      expect(a >>> b == -25214903884L >>> 2);
      expect(a << b == -25214903884L << 2);
      expect(a + b == -25214903884L + 2L);
      expect(a - b == -25214903884L - 2L);
      expect(a * b == -25214903884L * 2L);
      expect(a / b == -25214903884L / 2L);
      expect(a % b == -25214903884L % 2L);
      expect((a & b) == (-25214903884L & 2L));
      expect((a | b) == (-25214903884L | 2L));
      expect((a ^ b) == (-25214903884L ^ 2L));
      expect(-a == 25214903884L);
      expect(~a == ~-25214903884L);

      a = 25214903884L;
      b = 2;
      expect(a >> b == 25214903884L >> 2);
      expect(a >>> b == 25214903884L >>> 2);
      expect(a << b == 25214903884L << 2);
      expect(a + b == 25214903884L + 2L);
      expect(a - b == 25214903884L - 2L);
      expect(a * b == 25214903884L * 2L);
      expect(a / b == 25214903884L / 2L);
      expect(a % b == 25214903884L % 2L);
      expect((a & b) == (25214903884L & 2L));
      expect((a | b) == (25214903884L | 2L));
      expect((a ^ b) == (25214903884L ^ 2L));
      expect(-a == -25214903884L);
      expect(~a == ~25214903884L);
    }

    { long b = 2;
      expect((-25214903884L) >> b == -25214903884L >> 2);
      expect((-25214903884L) >>> b == -25214903884L >>> 2);
      expect((-25214903884L) << b == -25214903884L << 2);
      expect((-25214903884L) + b == -25214903884L + 2L);
      expect((-25214903884L) - b == -25214903884L - 2L);
      expect((-25214903884L) * b == -25214903884L * 2L);
      expect((-25214903884L) / b == -25214903884L / 2L);
      expect((-25214903884L) % b == -25214903884L % 2L);
      expect(((-25214903884L) & b) == (-25214903884L & 2L));
      expect(((-25214903884L) | b) == (-25214903884L | 2L));
      expect(((-25214903884L) ^ b) == (-25214903884L ^ 2L));

      b = 2;
      expect(25214903884L >> b == 25214903884L >> 2);
      expect(25214903884L >>> b == 25214903884L >>> 2);
      expect(25214903884L << b == 25214903884L << 2);
      expect(25214903884L + b == 25214903884L + 2L);
      expect(25214903884L - b == 25214903884L - 2L);
      expect(25214903884L * b == 25214903884L * 2L);
      expect(25214903884L / b == 25214903884L / 2L);
      expect(25214903884L % b == 25214903884L % 2L);
      expect((25214903884L & b) == (25214903884L & 2L));
      expect((25214903884L | b) == (25214903884L | 2L));
      expect((25214903884L ^ b) == (25214903884L ^ 2L));
    }

    { long a = 2L;
      expect(a + (-25214903884L) == 2L + (-25214903884L));
      expect(a - (-25214903884L) == 2L - (-25214903884L));
      expect(a * (-25214903884L) == 2L * (-25214903884L));
      expect(a / (-25214903884L) == 2L / (-25214903884L));
      expect(a % (-25214903884L) == 2L % (-25214903884L));
      expect((a & (-25214903884L)) == (2L & (-25214903884L)));
      expect((a | (-25214903884L)) == (2L | (-25214903884L)));
      expect((a ^ (-25214903884L)) == (2L ^ (-25214903884L)));

      a = 2L;
      expect(a + 25214903884L == 2L + 25214903884L);
      expect(a - 25214903884L == 2L - 25214903884L);
      expect(a * 25214903884L == 2L * 25214903884L);
      expect(a / 25214903884L == 2L / 25214903884L);
      expect(a % 25214903884L == 2L % 25214903884L);
      expect((a & 25214903884L) == (2L & 25214903884L));
      expect((a | 25214903884L) == (2L | 25214903884L));
      expect((a ^ 25214903884L) == (2L ^ 25214903884L));
    }

    { long x = 231;
      expect((x >> 32) == 0);
      expect((x >>> 32) == 0);
      expect((x << 32) == 992137445376L);

      int shift = 32;
      expect((x >> shift) == 0);
      expect((x >>> shift) == 0);
      expect((x << shift) == 992137445376L);

      long y = -231;
      expect((y >> 32) == 0xffffffffffffffffL);
      expect((y >>> 32) == 0xffffffffL);
    }

    expect(Long.valueOf(231L) == 231L);

    { byte[] array = new byte[8];
      putLong(231, array, 0);

      expect((array[0] & 0xff) == 0);
      expect((array[1] & 0xff) == 0);
      expect((array[2] & 0xff) == 0);
      expect((array[3] & 0xff) == 0);
      expect((array[4] & 0xff) == 0);
      expect((array[5] & 0xff) == 0);
      expect((array[6] & 0xff) == 0);
      expect((array[7] & 0xff) == 231);

      expect(getLong(array, 0) == 231);
    }

    java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(8);
    buffer.putLong(231);
    buffer.flip();
    expect(buffer.getLong() == 231);

    expect(unsignedShiftRight32(231) == 0);

    { int[] x = new int[] { 1701899151 };
      int[] z = new int[x.length * 2];
      final long LONG_MASK = 0xffffffffL;

      int lastProductLowWord = 0;
      for (int j=0, i=0; j<x.length; j++) {
        long piece = (x[j] & LONG_MASK);
        long product = piece * piece;
        z[i++] = (lastProductLowWord << 31) | (int) (product >>> 33);
        z[i++] = (int) (product >>> 1);
        lastProductLowWord = (int) product;
      }

      expect(z[0] == 337192406);
      expect(z[1] == -437261072);
    }
  }

}
