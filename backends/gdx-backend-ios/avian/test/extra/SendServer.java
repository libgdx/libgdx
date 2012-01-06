package extra;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SendServer {
  private static char cIndex = 'A';
  private static ByteBuffer inBuf = ByteBuffer.allocate(8192);

  private static void dumpByteBuffer(char note, ByteBuffer buf) {
    System.out.println(note + ": Buffer position: " + buf.position() + " limit: " +
                       buf.limit() + " capacity: " + buf.capacity() + " remaining: " +
                       buf.remaining());
  }

  private static class Connection {
    private final char myIndex;
    private final java.io.FileOutputStream fos;

    public Connection() throws Exception {
      myIndex = cIndex++;
      fos = new java.io.FileOutputStream("dump." + myIndex);
    }

    public void handleRead(SocketChannel channel) throws Exception {
      int count = -1;
      while ((count = channel.read(inBuf)) > 0) {
        System.out.println(myIndex + ": read " + count);
      }
      inBuf.flip();
      fos.write(inBuf.array(), inBuf.arrayOffset()+inBuf.position(), inBuf.remaining());
      inBuf.position(inBuf.limit());
      if (count < 0) {
        System.out.println(myIndex + ": Closing channel");
        fos.close();
        channel.close();
      }
//      dumpByteBuffer(myIndex, inBuf);
      inBuf.compact();
    }
  }
    
  public void runMainLoop() throws Exception {
    boolean keepRunning = true;
    int port = 8988;
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    try {
      serverChannel.configureBlocking(false);
      serverChannel.socket().bind(new InetSocketAddress("0.0.0.0", port));
      Selector selector = Selector.open();
      serverChannel.register(selector, SelectionKey.OP_ACCEPT, null);
      while (keepRunning) {
        System.out.println("Running main loop");
        selector.select(10000);
        for (SelectionKey key : selector.selectedKeys()) {
          if (key.isAcceptable()) {
            System.out.println("Accepting new connection");
            SocketChannel c = ((ServerSocketChannel) key.channel()).accept();
            if (c != null) {
              c.configureBlocking(false);
              c.register(selector, SelectionKey.OP_READ, new Connection());
            }
          } else {
            SocketChannel c = (SocketChannel) key.channel();
            if (c.isOpen() && key.isReadable()) {
              Connection connection = (Connection)key.attachment();
              connection.handleRead(c);
            }
          }
        }
        selector.selectedKeys().clear();
      }
    } finally {
      serverChannel.close();
    }
  }

  public static void main(String args[]) {
    try {
      System.out.println("Starting server");
      if (args.length > 0) {
        new SendServer().runMainLoop();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
