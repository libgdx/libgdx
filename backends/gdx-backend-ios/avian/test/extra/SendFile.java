package extra;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.io.OutputStream;
import java.io.FileInputStream;

public class SendFile {
  private static class SocketOutputStream extends OutputStream {
    private final SocketChannel channel;
    private final Selector selector;
    public SocketOutputStream(String host, int port) throws Exception {
      channel = SocketChannel.open();
      channel.connect(new InetSocketAddress(host, port));
      channel.configureBlocking(false);
      selector = Selector.open();
      channel.register(selector, SelectionKey.OP_WRITE, null);
    }

    public void close() throws IOException {
      channel.close();
    }

    public void write(int c) {
      throw new RuntimeException("Do not use!");
    }
    public void write(byte[] buffer, int offset, int length)
      throws IOException {
      ByteBuffer buf = ByteBuffer.wrap(buffer);
      buf.position(offset);
      buf.limit(offset+length);
      while (buf.hasRemaining()) {
        selector.select(10000);
        for (SelectionKey key : selector.selectedKeys()) {
          if (key.isWritable() && (key.channel() == channel)) {
            channel.write(buf);
          }
        }
      }
    }
  }

  public static void sendFile(String file, String host, int port)
    throws Exception {
    System.out.println("Sending " + file);
    OutputStream os = new SocketOutputStream(host, port);
    FileInputStream is = new FileInputStream(file);
    byte[] buf = new byte[16384];
    int count=-1;
    while ((count = is.read(buf)) >= 0) {
      os.write(buf, 0, count);
    }
    is.close();
    os.close();
  }

  public static void main(String args[]) {
    if (args.length != 2) {
      System.out.println("Usage:  SendFile file host");
    } else {
      try {
        sendFile(args[0], args[1], 8988);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
   
