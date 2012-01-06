public class Strings {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  private static boolean arraysEqual(Object[] a, Object[] b) {
    if (a.length != b.length) {
      return false;
    }

    for (int i = 0; i < a.length; ++i) {
      if (! equal(a[i], b[i])) {
        return false;
      }
    }

    return true;
  }

  public static void main(String[] args) {
    expect(new String(new byte[] { 99, 111, 109, 46, 101, 99, 111, 118, 97,
                                   116, 101, 46, 110, 97, 116, 46, 98, 117,
                                   115, 46, 83, 121, 109, 98, 111, 108 })
      .equals("com.ecovate.nat.bus.Symbol"));
    
    final String months = "Jan\u00aeFeb\u00aeMar\u00ae";
    expect(months.split("\u00ae").length == 3);
    expect(months.replaceAll("\u00ae", ".").equals("Jan.Feb.Mar."));

    expect(arraysEqual
           ("xyz".split("",  0), new String[] { "", "x", "y", "z" }));
    expect(arraysEqual
           ("xyz".split("",  1), new String[] { "xyz" }));
    expect(arraysEqual
           ("xyz".split("",  2), new String[] { "", "xyz" }));
    expect(arraysEqual
           ("xyz".split("",  3), new String[] { "", "x", "yz" }));
    expect(arraysEqual
           ("xyz".split("",  4), new String[] { "", "x", "y", "z" }));
    expect(arraysEqual
           ("xyz".split("",  5), new String[] { "", "x", "y", "z", "" }));
    expect(arraysEqual
           ("xyz".split("",  6), new String[] { "", "x", "y", "z", "" }));
    expect(arraysEqual
           ("xyz".split("", -1), new String[] { "", "x", "y", "z", "" }));

    expect(arraysEqual("".split("xyz",  0), new String[] { "" }));
    expect(arraysEqual("".split("xyz",  1), new String[] { "" }));
    expect(arraysEqual("".split("xyz", -1), new String[] { "" }));

    expect(arraysEqual("".split("",  0), new String[] { "" }));
    expect(arraysEqual("".split("",  1), new String[] { "" }));
    expect(arraysEqual("".split("", -1), new String[] { "" }));

    expect("foo_foofoo__foo".replaceAll("_", "__")
           .equals("foo__foofoo____foo"));

    expect("foo_foofoo__foo".replaceFirst("_", "__")
           .equals("foo__foofoo__foo"));

    expect("stereomime".matches("stereomime"));
    expect(! "stereomime".matches("stereomim"));
    expect(! "stereomime".matches("tereomime"));
    expect(! "stereomime".matches("sterEomime"));

    StringBuilder sb = new StringBuilder();
    sb.append('$');
    sb.append('2');
    expect(sb.substring(1).equals("2"));

    expect(Character.forDigit(Character.digit('0', 10), 10) == '0');
    expect(Character.forDigit(Character.digit('9', 10), 10) == '9');
    expect(Character.forDigit(Character.digit('b', 16), 16) == 'b');
    expect(Character.forDigit(Character.digit('f', 16), 16) == 'f');
    expect(Character.forDigit(Character.digit('z', 36), 36) == 'z');
  }
}
