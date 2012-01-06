package java.util;

public class UUID {
  private final byte[] data;

  private UUID(byte[] data) {
    this.data = data;
  }

  public static UUID randomUUID() {
    byte[] array = new byte[16];

    new Random().nextBytes(array);

    array[6] &= 0x0f;
    array[6] |= 0x40;
    array[8] &= 0x3f;
    array[8] |= 0x80;

    return new UUID(array);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    toHex(sb, data, 0, 4); sb.append('-');
    toHex(sb, data, 4, 2); sb.append('-');
    toHex(sb, data, 6, 2); sb.append('-');
    toHex(sb, data, 8, 2); sb.append('-');
    toHex(sb, data, 10, 6);
    return sb.toString();
  }

  private static char toHex(int i) {
    return (char) (i < 10 ? i + '0' : (i - 10) + 'A');
  }

  private static void toHex(StringBuilder sb, byte[] array, int offset,
                            int length)
  {
    for (int i = offset; i < offset + length; ++i) {
      sb.append(toHex((array[i] >> 4) & 0xf));
      sb.append(toHex((array[i]     ) & 0xf));
    }
  }
}
