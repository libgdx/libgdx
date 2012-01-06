/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio.channels;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketChannel extends SelectableChannel
  implements ReadableByteChannel, GatheringByteChannel
{
  public static final int InvalidSocket = -1;

  int socket = InvalidSocket;
  boolean connected = false;
  boolean readyToConnect = false;
  boolean blocking = true;

  public static SocketChannel open() throws IOException {
    Socket.init();

    return new SocketChannel();
  }

  public SelectableChannel configureBlocking(boolean v) throws IOException {
    blocking = v;
    if (socket != InvalidSocket) {
      configureBlocking(socket, v);
    }
    return this;
  }

  public boolean isBlocking() {
    return blocking;
  }

  public boolean isConnected() {
    return connected;
  }

  public Socket socket() {
    return new Handle();
  }

  public boolean connect(SocketAddress address) throws IOException {
    InetSocketAddress a;
    try {
      a = (InetSocketAddress) address;
    } catch (ClassCastException e) {
      throw new UnsupportedAddressTypeException();
    }
    socket = doConnect(a.getHostName(), a.getPort());
    configureBlocking(blocking);
    return connected;
  }

  public boolean finishConnect() throws IOException {
    if (! connected) {
      while (! readyToConnect) {
        Selector selector = Selector.open();
        SelectionKey key = register(selector, SelectionKey.OP_CONNECT, null);

        if (blocking) {
          selector.select();
        } else {
          selector.selectNow();
          break;
        }
      }

      natFinishConnect(socket);

      connected = readyToConnect;
    }

    return connected;
  }

  public void close() throws IOException {
    if (isOpen()) {
      super.close();
      closeSocket();
    }
  }

  private int doConnect(String host, int port) throws IOException {
    if (host == null) throw new NullPointerException();

    boolean b[] = new boolean[1];
    int s = natDoConnect(host, port, blocking, b);
    connected = b[0];
    return s;
  }

  public int read(ByteBuffer b) throws IOException {
    if (! isOpen()) return -1;
    if (b.remaining() == 0) return 0;

    byte[] array = b.array();
    if (array == null) throw new NullPointerException();

    int r = natRead(socket, array, b.arrayOffset() + b.position(), b.remaining(), blocking);
    if (r > 0) {
      b.position(b.position() + r);
    }
    return r;
  }

  public int write(ByteBuffer b) throws IOException {
    if (! connected) {
      natThrowWriteError(socket);
    }
    if (b.remaining() == 0) return 0;

    byte[] array = b.array();
    if (array == null) throw new NullPointerException();

    int w = natWrite(socket, array, b.arrayOffset() + b.position(), b.remaining(), blocking);
    if (w > 0) {
      b.position(b.position() + w);
    }
    return w;
  }

  public long write(ByteBuffer[] srcs) throws IOException {
    return write(srcs, 0, srcs.length);
  }

  public long write(ByteBuffer[] srcs, int offset, int length)
    throws IOException
  {
    long total = 0;
    for (int i = offset; i < offset + length; ++i) {
      total += write(srcs[i]);
      if (srcs[i].hasRemaining()) {
        return total;
      }
    }
    return total;
  }

  private void closeSocket() {
    natCloseSocket(socket);
  }

  int socketFD() {
    return socket;
  }

  void handleReadyOps(int ops) {
    if ((ops & SelectionKey.OP_CONNECT) != 0) {
      readyToConnect = true;
    }
  }

  public class Handle extends Socket {
    public void setTcpNoDelay(boolean on) throws SocketException {
      natSetTcpNoDelay(socket, on);
    }
  }

  private static native void configureBlocking(int socket, boolean blocking)
    throws IOException;

  private static native void natSetTcpNoDelay(int socket, boolean on)
    throws SocketException;

  private static native int natDoConnect(String host, int port, boolean blocking, boolean[] connected)
    throws IOException;
  private static native void natFinishConnect(int socket)
    throws IOException;
  private static native int natRead(int socket, byte[] buffer, int offset, int length, boolean blocking)
    throws IOException;
  private static native int natWrite(int socket, byte[] buffer, int offset, int length, boolean blocking)
    throws IOException;
  private static native void natThrowWriteError(int socket) throws IOException;
  private static native void natCloseSocket(int socket);
}
