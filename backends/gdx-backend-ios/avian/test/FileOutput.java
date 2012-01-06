import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

public class FileOutput {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static void test(boolean appendFirst) throws IOException {
    try {
      FileOutputStream f = new FileOutputStream("test.txt", appendFirst);
      f.write("Hello world!\n".getBytes());
      f.close();
      
      FileOutputStream f2 = new FileOutputStream("test.txt", true);
      f2.write("Hello world again!".getBytes());
      f2.close();
      
      FileInputStream in = new FileInputStream("test.txt");
      byte[] buffer = new byte[256];
      int c;
      int offset = 0;
      while ((c = in.read(buffer, offset, buffer.length - offset)) != -1) {
        offset += c;
      }
      in.close();

      if (! "Hello world!\nHello world again!".equals
          (new String(buffer, 0, offset)))
      {
        throw new RuntimeException();
      }
    } finally {
      expect(new File("test.txt").delete());
    }
  }

  public static void main(String[] args) throws IOException {
    expect(new File("nonexistent-file").length() == 0);

    test(false);
    test(true);
  }

}
