import java.io.File;

public class Files {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }
  
  private static void isAbsoluteTest(boolean absolutePath) {
    File file = new File("test.txt");
    if (absolutePath) {
      file = file.getAbsoluteFile();
    }
    
    boolean isAbsolute = file.isAbsolute();
    
    if (absolutePath) {
      expect(isAbsolute);
    } else {
      expect(!isAbsolute);
    }
    
  }
  
  public static void main(String[] args) {
    isAbsoluteTest(true);
    isAbsoluteTest(false);
  }

}
