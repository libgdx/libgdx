public class AllFloats {
  private static float multiplyByFive(float a) {return 5f * a;}
  private static double multiplyByFive(double a) {return 5d * a;}
  private static float multiply(float a, float b) {return a * b;}
  private static double multiply(double a, double b) {return a * b;}
  private static double multiply(float a, double b) {return a * b;}
  private static float divide(float a, float b) {return a / b;}
  private static double divide(double a, double b) {return a / b;}
  private static double divide(float a, double b) {return a / b;}
  private static float remainder(float a, float b) {return a % b;}
  private static double remainder(double a, double b) {return a % b;}
  private static double remainder(float a, double b) {return a % b;}
  private static float add(float a, float b) {return a + b;}
  private static double add(double a, double b) {return a + b;}
  private static double add(float a, double b) {return a + b;}
  private static float subtract(float a, float b) {return a - b;}
  private static double subtract(double a, double b) {return a - b;}
  private static double subtract(float a, double b) {return a - b;}
  private static float complex(float a, float b) {return (a - b) / (a * b) + (float)Math.sqrt(a);}
  private static double complex(double a, double b) {return (a - b) / (a * b) + Math.sqrt(a);}
  private static double complex(float a, double b) {return (a - b) / (a * b) + Math.sqrt(a);}
  private static double sqrt(double a) {return Math.sqrt(a);}
  private static float complexNoIntrinsic(float a, float b) {return (a - b) / (a * b) + (float)sqrt(a);}
  private static int f2i(float a) {return (int)a;}
  private static long f2l(float a) {return (long)a;}
  private static float i2f(int a) {return (float)a;}
  private static double i2d(int a) {return (double)a;}
  private static int d2i(double a) {return (int)a;}
  private static long d2l(double a) {return (long)a;}
  private static float l2f(long a) {return (float)a;}
  private static double l2d(long a) {return (double)a;}
  private static float negate(float a) {return -a;}
  private static double negate(double a) {return -a;}
  private static int abs(int a) {return Math.abs(a);}
  private static float abs(float a) {return Math.abs(a);}
  
  private static void expect(boolean v) {
    if(!v)throw new RuntimeException();
  }
  
  private static int last(){return 0;}
  
  public static void main(String[] args) {
    expect(multiplyByFive(36f) == 5f * 36f);
    expect(multiplyByFive(36d) == 5d * 36d);
    expect(multiply(5f, 4f) == 5f*4f);
    expect(multiply(5d, 4d) == 5d*4d);
    expect(multiply(5f, 4d) == 5f*4d);
    expect(divide(5f, 2f) == 5f/2f);
    expect(divide(5d, 2d) == 5d/2d);
    expect(divide(5f, 2d) == 5f/2d);
    expect(remainder(5f, 2f) == 5f%2f);
    expect(remainder(5d, 2d) == 5d%2d);
    expect(remainder(5f, 2d) == 5f%2d);
    expect(add(5f, 4f) == 5f+4f);
    expect(add(5d, 4d) == 5f+4d);
    expect(add(5f, 4d) == 5f+4d);
    expect(subtract(5f, 4f) == 5f-4f);
    expect(subtract(5d, 4d) == 5f-4d);
    expect(subtract(5f, 4d) == 5f-4d);
    expect(complex(4f, 3f) == (4f-3f)/(4f*3f) + 2f);
    expect(complex(4d, 3d) == (4d-3d)/(4d*3d) + 2d);
    expect(complex(4f, 3d) == (4f-3d)/(4f*3d) + 2f);
    expect(complexNoIntrinsic(4f, 3f) == (4f-3f)/(4f*3f) + 2f);
    
    expect(f2i(4f) == 4);
    expect(f2l(4f) == 4);
    expect(i2f(4) == 4f);
    expect(i2d(4) == 4d);
    
    expect(d2i(4d) == 4);
    expect(d2l(4d) == 4);
    expect(l2f(4) == 4f);
    expect(l2d(4) == 4d);
    
    expect(negate(4f) == -4f);
    expect(negate(4d) == -4d);
    
    expect(abs(-4) == 4);
    expect(abs(12) == 12);
    expect(abs(-4f) == 4f);
    expect(abs(12f) == 12f);
    
    int unused = last();
  }
}
