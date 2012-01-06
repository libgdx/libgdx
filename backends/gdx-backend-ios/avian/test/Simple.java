public class Simple {
  public static int size(long v, int radix) {
    int size = 0;
    for (long n = v; n != 0; n /= radix) ++size;
    return size;
  }
  
  public static void main(String[] args) {
    size(42, 10);
  }
}
