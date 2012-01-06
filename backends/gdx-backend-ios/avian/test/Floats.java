public class Floats {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static double multiply(double a, double b) {
    return a * b;
  }

  private static float multiply(float a, float b) {
    return a * b;
  }

  private static double divide(double a, double b) {
    return a / b;
  }

  private static double subtract(double a, double b) {
    return a - b;
  }

  private double field = 100d;

  private static int doubleToInt(Floats f) {
    return (int) f.field;
  }

  private static void multiplyAndStore(double a, double b, Floats f) {
    f.field = a * b;
  }

  private static double loadAndMultiply(double a, Floats f) {
    return f.field * a;
  }

  private static void subdivide(double src[], int srcoff,
                                double left[], int leftoff,
                                double right[], int rightoff)
  {
    double x1 = src[srcoff + 0];
    double y1 = src[srcoff + 1];
    double ctrlx1 = src[srcoff + 2];
    double ctrly1 = src[srcoff + 3];
    double ctrlx2 = src[srcoff + 4];
    double ctrly2 = src[srcoff + 5];
    double x2 = src[srcoff + 6];
    double y2 = src[srcoff + 7];
    if (left != null) {
      left[leftoff + 0] = x1;
      left[leftoff + 1] = y1;
    }
    if (right != null) {
      right[rightoff + 6] = x2;
      right[rightoff + 7] = y2;
    }
    x1 = (x1 + ctrlx1) / 2.0;
    y1 = (y1 + ctrly1) / 2.0;
    x2 = (x2 + ctrlx2) / 2.0;
    y2 = (y2 + ctrly2) / 2.0;
    double centerx = (ctrlx1 + ctrlx2) / 2.0;
    double centery = (ctrly1 + ctrly2) / 2.0;
    ctrlx1 = (x1 + centerx) / 2.0;
    ctrly1 = (y1 + centery) / 2.0;
    ctrlx2 = (x2 + centerx) / 2.0;
    ctrly2 = (y2 + centery) / 2.0;
    centerx = (ctrlx1 + ctrlx2) / 2.0;
    centery = (ctrly1 + ctrly2) / 2.0;
    if (left != null) {
      left[leftoff + 2] = x1;
      left[leftoff + 3] = y1;
      left[leftoff + 4] = ctrlx1;
      left[leftoff + 5] = ctrly1;
      left[leftoff + 6] = centerx;
      left[leftoff + 7] = centery;
    }
    if (right != null) {
      right[rightoff + 0] = centerx;
      right[rightoff + 1] = centery;
      right[rightoff + 2] = ctrlx2;
      right[rightoff + 3] = ctrly2;
      right[rightoff + 4] = x2;
      right[rightoff + 5] = y2;
    }
  }

  public static class Rectangle {
    public double x;
    public double y;
    public double width;
    public double height;

    public void setX(double x) {
      this.x = x;
    }
  }

  public static void main(String[] args) throws Exception {
    expect(new Double(42.0) == 42.0);

    { Rectangle r = new Rectangle();
      Rectangle.class.getMethod("setX", double.class).invoke(r, 42.0);
      expect(r.x == 42.0);
    }

    { double input[] = new double[8];
      double left[] = new double[8];
      double right[] = new double[8];

      input[0] = 732.0;
      input[1] = 952.0;
      input[2] = 761.0;
      input[3] = 942.0;
      input[4] = 786.0;
      input[5] = 944.0;
      input[6] = 813.0;
      input[7] = 939.0;

      subdivide(input, 0, left, 0, right, 0);

      expect(left[0] == 732.0);
      expect(left[1] == 952.0);
      expect(left[2] == 746.5);
      expect(left[3] == 947.0);
      expect(left[4] == 760.0);
      expect(left[5] == 945.0);
      expect(left[6] == 773.25);
      expect(left[7] == 943.625);

      expect(right[0] == 773.25);
      expect(right[1] == 943.625);
      expect(right[2] == 786.5);
      expect(right[3] == 942.25);
      expect(right[4] == 799.5);
      expect(right[5] == 941.5);
      expect(right[6] == 813.0);
      expect(right[7] == 939.0);
    }

    expect(multiply(0.5d, 0.5d) == 0.25d);
    expect(multiply(0.5f, 0.5f) == 0.25f);

    expect(multiply(0.5d, 0.1d) == 0.05d);
    expect(multiply(0.5f, 0.1f) == 0.05f);

    expect(multiply(0.5d, 0.5d) < 0.5d);
    expect(multiply(0.5f, 0.5f) < 0.5f);

    expect(multiply(0.5d, 0.1d) < 0.5d);
    expect(multiply(0.5f, 0.1f) < 0.5f);

    expect(multiply(0.5d, 0.5d) > 0.1d);
    expect(multiply(0.5f, 0.5f) > 0.1f);

    expect(multiply(0.5d, 0.1d) > 0.01d);
    expect(multiply(0.5f, 0.1f) > 0.01f);

    expect(divide(0.5d, 0.5d) == 1.0d);

    expect(divide(0.5d, 0.1d) == 5.0d);

    expect(subtract(0.5d, 0.5d) == 0.0d);

    expect(subtract(0.5d, 0.1d) == 0.4d);

    { double d = 1d;
      expect(((int) d) == 1);
    }

    { double d = 12345d;
      expect(((int) d) == 12345);
    }

    expect(doubleToInt(new Floats()) == 100);

    { Floats f = new Floats();
      f.field = 32.0d;
      expect(loadAndMultiply(2.0d, f) == 64.0d);
    }

    { Floats f = new Floats();
      f.field = 32.0d;
      expect(multiply(2.0d, f.field) == 64.0d);
    }

    { Floats f = new Floats();
      multiplyAndStore(32.0d, 0.5d, f);
      expect(f.field == 16.0d);
    }

    { float f = 1f;
      expect(((int) f) == 1);
    }

    { float f = 1f;
      expect(((long) f) == 1);
    }

    expect(Math.round(0.4f) == 0);
    expect(Math.round(0.5f) == 1);
    expect(Math.round(1.0f) == 1);
    expect(Math.round(1.9f) == 2);

    expect(Math.round(0.4d) == 0);
    expect(Math.round(0.5d) == 1);
    expect(Math.round(1.0d) == 1);
    expect(Math.round(1.9d) == 2);

    { float b = 1.0f;
      int blue = (int)(b * 255 + 0.5);
      expect(blue == 255);
    }

    { long z = 6553311036568663L;
      double d = (double) z;
      expect(d == 6553311036568663.0);
    }

    { long z = 12345L;
      float f = (float) z;
      expect(f == 12345.0);
    }

    { int z = 12345;
      float f = (float) z;
      expect(f == 12345.0);
    }

    { int z = 12345;
      double d = (double) z;
      expect(d == 12345.0);
    }

    // Test floatToIntBits 
    { 
      int orig = 0x7f800001;
      float NaN = Float.intBitsToFloat(orig);
      int result = Float.floatToIntBits(NaN);
      int expected = 0x7fc00000;
      expect(result == expected);
    }
    
    { 
      int orig = 0x7f801001;
      float NaN = Float.intBitsToFloat(orig);
      int result = Float.floatToIntBits(NaN);
      int expected = 0x7fc00000;
      expect(result == expected);
    }
    
    {
      int orig = 0x00800001;
      float number = Float.intBitsToFloat(orig);
      int result = Float.floatToIntBits(number);
      expect(result == orig);
    }
    
    {
      int orig = 0x80800003;
      float number = Float.intBitsToFloat(orig);
      int result = Float.floatToIntBits(number);
      expect(result == orig);
    }
  }
}
