public class Exceptions {

  private static void evenMoreDangerous() {
    throw new RuntimeException("chaos! panic! overwhelming anxiety!");
  }

  private static void moreDangerous() {
    evenMoreDangerous();
  }

  private static void dangerous() {
    moreDangerous();
  }

  public static void main(String[] args) {
    try {
      dangerous();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
