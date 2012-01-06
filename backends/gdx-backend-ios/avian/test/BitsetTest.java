import java.util.BitSet;

public class BitsetTest {

  public static void main(String[] args) {
    BitSet bits = new BitSet(16);
    bits.set(5);
    bits.set(1);
    
    BitSet other = new BitSet(16);
    other.set(5);
    
    assertTrue("bit 1 is set", bits.get(1));
    assertTrue("bit 5 is set", bits.get(5));
    assertTrue("bit 0 is not set", !bits.get(0));
    assertTrue("bit 16 is not set", !bits.get(16));
    
    bits.and(other);
    
    assertTrue("bit 5 is set", bits.get(5));
    assertTrue("bit 1 is not set", !bits.get(1));
    
    bits.set(100);
    
    assertTrue("bit 100 is set", bits.get(100));
    assertTrue("bit 101 is not set", !bits.get(101));
    
    other.set(101);
    
    bits.or(other);
    
    assertTrue("bit 101 is set", bits.get(101));
    
    assertEquals("first bit is 5", 5, bits.nextSetBit(0));
    assertEquals("first bit is 5 from 3", 5, bits.nextSetBit(4));
    assertEquals("first bit is 5 from 5", 5, bits.nextSetBit(5));
    assertEquals("second bit is 100", 100, bits.nextSetBit(6));
    assertEquals("second bit is 100 from 100", 100, bits.nextSetBit(100));
    assertEquals("third bit is 101", 101, bits.nextSetBit(101));
    assertEquals("there is no 4th bit", -1, bits.nextSetBit(102));
    
    assertEquals("first empty bit is 0", 0, bits.nextClearBit(0));
    assertEquals("after 5, 6 is empty", 6, bits.nextClearBit(5));
    assertEquals("after 100, 102 is empty", 102, bits.nextClearBit(100));
    
  }
  
  static void assertTrue(String msg, boolean flag) {
    if (flag) {
      System.out.println(msg + " : OK.");
    } else {
      throw new RuntimeException("Error:"+msg);
    }
  }

  static void assertEquals(String msg, int expected, int actual) {
    if (expected==actual) {
      System.out.println(msg + " : OK. ["+actual+']');
    } else {
      throw new RuntimeException("Error:"+msg+" expected:"+expected+", actual:"+actual);
    }
  }
  
}
