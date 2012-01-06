public class Arrays {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static void main(String[] args) {
    { int[] array = new int[0];
      Exception exception = null;
      try {
        int x = array[0];
      } catch (ArrayIndexOutOfBoundsException e) {
        exception = e;
      }

      expect(exception != null);
    }

    { int[] array = new int[0];
      Exception exception = null;
      try {
        int x = array[-1];
      } catch (ArrayIndexOutOfBoundsException e) {
        exception = e;
      }

      expect(exception != null);
    }

    { int[] array = new int[3];
      int i = 0;
      array[i++] = 1;
      array[i++] = 2;
      array[i++] = 3;

      expect(array[--i] == 3);
      expect(array[--i] == 2);
      expect(array[--i] == 1);
    }

    { Object[][] array = new Object[1][1];
      expect(array.length == 1);
      expect(array[0].length == 1);
    }

    { Object[][] array = new Object[2][3];
      expect(array.length == 2);
      expect(array[0].length == 3);
    }

    { int j = 0;
      byte[] decodeTable = new byte[256];
      for (int i = 'A'; i <= 'Z'; ++i) decodeTable[i] = (byte) j++;
      for (int i = 'a'; i <= 'z'; ++i) decodeTable[i] = (byte) j++;
      for (int i = '0'; i <= '9'; ++i) decodeTable[i] = (byte) j++;
      decodeTable['+'] = (byte) j++;
      decodeTable['/'] = (byte) j++;
      decodeTable['='] = 0;

      expect(decodeTable['a'] != 0);
    }

    { boolean p = true;
      int[] array = new int[] { 1, 2 };
      expect(array[0] == array[p ? 0 : 1]);
      p = false;
      expect(array[1] == array[p ? 0 : 1]);
    }

    { int[] array = new int[1024];
      array[1023] = -1;
      expect(array[1023] == -1);
      expect(array[1022] == 0);
    }

    { Integer[] array = (Integer[])
        java.lang.reflect.Array.newInstance(Integer.class, 1);
      array[0] = Integer.valueOf(42);
      expect(array[0].intValue() == 42);
    }
  }
}
