public class Integers {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static int gcd(int m, int n) {
    int temp;
    m = Math.abs(m);
    n = Math.abs(n);
    if (m < n) {
      temp = m;
      m = n;
      n = temp;
    }
    while (n != 0) {
      temp = m;
      m = n;
      n = temp % n;
    }
    return m;
  }

  public static void main(String[] args) {
    { int foo = 1028;
      foo -= 1023;
      expect(foo == 5);
    }

    expect(gcd(12, 4) == 4);

    { int a = 2;
      int b = 2;
      int c = a + b;
    }

    { int a = 2;
      int c = a + a;
    }

    { int a = -5;
      int b = 2;
      expect(a >> b == -5 >> 2);
      expect(a >>> b == -5 >>> 2);
      expect(a << b == -5 << 2);
      expect(a + b == -5 + 2);
      expect(a - b == -5 - 2);
      expect(a * b == -5 * 2);
      expect(a / b == -5 / 2);
      expect(a % b == -5 % 2);
      expect((a & b) == (-5 & 2));
      expect((a | b) == (-5 | 2));
      expect((a ^ b) == (-5 ^ 2));
      expect(-a == 5);
      expect(~a == ~-5);

      a = 5;
      b = 2;
      expect(a >> b == 5 >> 2);
      expect(a >>> b == 5 >>> 2);
      expect(a << b == 5 << 2);
      expect(a + b == 5 + 2);
      expect(a - b == 5 - 2);
      expect(a * b == 5 * 2);
      expect(a / b == 5 / 2);
      expect(a % b == 5 % 2);
      expect((a & b) == (5 & 2));
      expect((a | b) == (5 | 2));
      expect((a ^ b) == (5 ^ 2));
      expect(-a == -5);
      expect(~a == ~5);
    }

    { int a = -5;
      expect(a >> 2 == -5 >> 2);
      expect(a >>> 2 == -5 >>> 2);
      expect(a << 2 == -5 << 2);
      expect(a + 2 == -5 + 2);
      expect(a - 2 == -5 - 2);
      expect(a * 2 == -5 * 2);
      expect(a / 2 == -5 / 2);
      expect(a % 2 == -5 % 2);
      expect((a & 2) == (-5 & 2));
      expect((a | 2) == (-5 | 2));
      expect((a ^ 2) == (-5 ^ 2));

      a = 5;
      expect(a >> 2 == 5 >> 2);
      expect(a >>> 2 == 5 >>> 2);
      expect(a << 2 == 5 << 2);
      expect(a + 2 == 5 + 2);
      expect(a - 2 == 5 - 2);
      expect(a * 2 == 5 * 2);
      expect(a / 2 == 5 / 2);
      expect(a % 2 == 5 % 2);
      expect((a & 2) == (5 & 2));
      expect((a | 2) == (5 | 2));
      expect((a ^ 2) == (5 ^ 2));
    }

    { int a = -5;
      int b = 1234567;
      expect(a + b == -5 + 1234567);
      expect(a - b == -5 - 1234567);
      expect(a * b == -5 * 1234567);
      expect(a / b == -5 / 1234567);
      expect(a % b == -5 % 1234567);
      expect((a & b) == (-5 & 1234567));
      expect((a | b) == (-5 | 1234567));
      expect((a ^ b) == (-5 ^ 1234567));

      a = 5;
      b = 1234567;
      expect(a + b == 5 + 1234567);
      expect(a - b == 5 - 1234567);
      expect(a * b == 5 * 1234567);
      expect(a / b == 5 / 1234567);
      expect(a % b == 5 % 1234567);
      expect((a & b) == (5 & 1234567));
      expect((a | b) == (5 | 1234567));
      expect((a ^ b) == (5 ^ 1234567));
    }

    { int a = -5;
      expect(a + 1234567 == -5 + 1234567);
      expect(a - 1234567 == -5 - 1234567);
      expect(a * 1234567 == -5 * 1234567);
      expect(a / 1234567 == -5 / 1234567);
      expect(a % 1234567 == -5 % 1234567);
      expect((a & 1234567) == (-5 & 1234567));
      expect((a | 1234567) == (-5 | 1234567));
      expect((a ^ 1234567) == (-5 ^ 1234567));

      a = 5;
      expect(a + 1234567 == 5 + 1234567);
      expect(a - 1234567 == 5 - 1234567);
      expect(a * 1234567 == 5 * 1234567);
      expect(a / 1234567 == 5 / 1234567);
      expect(a % 1234567 == 5 % 1234567);
      expect((a & 1234567) == (5 & 1234567));
      expect((a | 1234567) == (5 | 1234567));
      expect((a ^ 1234567) == (5 ^ 1234567));
    }

    { int a = -1234567;
      int b = 2;
      expect(a >> b == -1234567 >> 2);
      expect(a >>> b == -1234567 >>> 2);
      expect(a << b == -1234567 << 2);
      expect(a + b == -1234567 + 2);
      expect(a - b == -1234567 - 2);
      expect(a * b == -1234567 * 2);
      expect(a / b == -1234567 / 2);
      expect(a % b == -1234567 % 2);
      expect((a & b) == (-1234567 & 2));
      expect((a | b) == (-1234567 | 2));
      expect((a ^ b) == (-1234567 ^ 2));
      expect(-a == 1234567);
      expect(~a == ~-1234567);

      a = 1234567;
      b = 2;
      expect(a >> b == 1234567 >> 2);
      expect(a >>> b == 1234567 >>> 2);
      expect(a << b == 1234567 << 2);
      expect(a + b == 1234567 + 2);
      expect(a - b == 1234567 - 2);
      expect(a * b == 1234567 * 2);
      expect(a / b == 1234567 / 2);
      expect(a % b == 1234567 % 2);
      expect((a & b) == (1234567 & 2));
      expect((a | b) == (1234567 | 2));
      expect((a ^ b) == (1234567 ^ 2));
      expect(-a == -1234567);
      expect(~a == ~1234567);
    }

    { int a = -1234567;
      expect(a >> 2 == -1234567 >> 2);
      expect(a >>> 2 == -1234567 >>> 2);
      expect(a << 2 == -1234567 << 2);
      expect(a + 2 == -1234567 + 2);
      expect(a - 2 == -1234567 - 2);
      expect(a * 2 == -1234567 * 2);
      expect(a / 2 == -1234567 / 2);
      expect(a % 2 == -1234567 % 2);
      expect((a & 2) == (-1234567 & 2));
      expect((a | 2) == (-1234567 | 2));
      expect((a ^ 2) == (-1234567 ^ 2));

      a = 1234567;
      expect(a >> 2 == 1234567 >> 2);
      expect(a >>> 2 == 1234567 >>> 2);
      expect(a << 2 == 1234567 << 2);
      expect(a + 2 == 1234567 + 2);
      expect(a - 2 == 1234567 - 2);
      expect(a * 2 == 1234567 * 2);
      expect(a / 2 == 1234567 / 2);
      expect(a % 2 == 1234567 % 2);
      expect((a & 2) == (1234567 & 2));
      expect((a | 2) == (1234567 | 2));
      expect((a ^ 2) == (1234567 ^ 2));
    }

    { int a = -1234567;
      int b = 1234567;
      expect(a + b == -1234567 + 1234567);
      expect(a - b == -1234567 - 1234567);
      expect(a * b == -1234567 * 1234567);
      expect(a / b == -1234567 / 1234567);
      expect(a % b == -1234567 % 1234567);
      expect((a & b) == (-1234567 & 1234567));
      expect((a | b) == (-1234567 | 1234567));
      expect((a ^ b) == (-1234567 ^ 1234567));

      a = 1234567;
      b = 1234567;
      expect(a + b == 1234567 + 1234567);
      expect(a - b == 1234567 - 1234567);
      expect(a * b == 1234567 * 1234567);
      expect(a / b == 1234567 / 1234567);
      expect(a % b == 1234567 % 1234567);
      expect((a & b) == (1234567 & 1234567));
      expect((a | b) == (1234567 | 1234567));
      expect((a ^ b) == (1234567 ^ 1234567));
    }

    { int a = -1234567;
      expect(a + 1234567 == -1234567 + 1234567);
      expect(a - 1234567 == -1234567 - 1234567);
      expect(a * 1234567 == -1234567 * 1234567);
      expect(a / 1234567 == -1234567 / 1234567);
      expect(a % 1234567 == -1234567 % 1234567);
      expect((a & 1234567) == (-1234567 & 1234567));
      expect((a | 1234567) == (-1234567 | 1234567));
      expect((a ^ 1234567) == (-1234567 ^ 1234567));

      a = 1234567;
      expect(a + 1234567 == 1234567 + 1234567);
      expect(a - 1234567 == 1234567 - 1234567);
      expect(a * 1234567 == 1234567 * 1234567);
      expect(a / 1234567 == 1234567 / 1234567);
      expect(a % 1234567 == 1234567 % 1234567);
      expect((a & 1234567) == (1234567 & 1234567));
      expect((a | 1234567) == (1234567 | 1234567));
      expect((a ^ 1234567) == (1234567 ^ 1234567));
    }

    { int get_buffer = 2144642881;
      int bits_left = 30;
      int l = 9;
      int code = (((get_buffer >> (bits_left -= (l)))) & ((1<<(l))-1));
      expect(code == 510);
    }

    { int width = 8;
      int height = 8;
      int depth = 24;
      int scanlinePad = 4;

      int bytesPerLine = (((width * depth + 7) / 8) + (scanlinePad - 1))
        / scanlinePad * scanlinePad;
      expect(bytesPerLine == 24);
    }

    int y = -11760768;
    expect((y + 0x8000) == (-11760768 + 0x8000));

    expect(Math.min(796, 1069) == 796);
  }
}
