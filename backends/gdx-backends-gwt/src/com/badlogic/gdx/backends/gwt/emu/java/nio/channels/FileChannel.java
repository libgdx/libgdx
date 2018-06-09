package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public abstract class FileChannel {

   public static class MapMode {
       public static final MapMode READ_ONLY = new MapMode();
       public static final MapMode READ_WRITE = new MapMode();
       public static final MapMode PRIVATE = new MapMode();
   }

   public abstract ByteBuffer map(MapMode mode, long position, long size);
}
