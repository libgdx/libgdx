public class Switch {
  private static int table(int k) {
    switch (k) {
    case 0:
      return 0;
    case 1:
      return 1;
    case 2:
      return 2;
    case 9:
      return 9;
    case 10:
      return 10;
    case 11:
      return 11;
    case 12:
      return 8;
    case -5:
      return 5;
    default:
      return 7;
    }
  }

  private static int lookup(int k) {
    switch (k) {
    case 0:
      return 0;
    case 45:
      return 45;
    case 46:
      return 46;
    case 47:
      return -47;
    case 200:
      return 200;
    case 244:
      return 244;
    case 245:
      return 245;
    default:
      return 91;
    }
  }

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  public static void main(String[] args) {
    expect(table(0) == 0);
    expect(table(9) == 9);
    expect(table(10) == 10);
    expect(table(11) == 11);
    expect(table(12) == 8);
    expect(table(-5) == 5);
    expect(table(-13) == 7);

    expect(lookup(0) == 0);
    expect(lookup(45) == 45);
    expect(lookup(46) == 46);
    expect(lookup(47) == -47);
    expect(lookup(245) == 245);
    expect(lookup(246) == 91);
  }
}
