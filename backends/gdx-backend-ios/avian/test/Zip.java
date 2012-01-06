import java.io.InputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class Zip {

  private static String findJar(File directory) {
    for (File file: directory.listFiles()) {
      if (file.isFile()) {
        if (file.getName().endsWith(".jar")) {
          System.out.println
            ("found " + file.getAbsolutePath() + " length " + file.length());

          return file.getAbsolutePath();
        }
      } else if (file.isDirectory()) {
        String result = findJar(file);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }
  
  public static void main(String[] args) throws Exception {
    ZipFile file = new ZipFile
      (findJar(new File(System.getProperty("user.dir"))));

    try {
      byte[] buffer = new byte[4096];
      for (Enumeration<? extends ZipEntry> e = file.entries();
           e.hasMoreElements();)
      {
        ZipEntry entry = e.nextElement();
        InputStream in = file.getInputStream(entry);
        try {
          int size = 0;
          int c; while ((c = in.read(buffer)) != -1) size += c;
          System.out.println
            (entry.getName() + " " + entry.getCompressedSize() + " " + size);
        } finally {
          in.close();
        }
      }
    } finally {
      file.close();
    }
  }

}
