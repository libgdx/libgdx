/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio.channels;

import java.io.IOException;
import java.util.Iterator;
import java.net.Socket;

class SocketSelector extends Selector {
  protected long state;
  protected final Object lock = new Object();
  protected boolean woken = false;

  public SocketSelector() throws IOException {
    Socket.init();

    state = natInit();
  }

  public boolean isOpen() {
    return state != 0;
  }

  public Selector wakeup() {
    synchronized (lock) {
      if (! woken) {
        woken = true;

        natWakeup(state);
      }
    }
    return this;
  }

  private boolean clearWoken() {
    synchronized (lock) {
      if (woken) {
        woken = false;
        return true;
      } else {
        return false;
      }
    }
  }

  public synchronized int selectNow() throws IOException {
    return doSelect(-1);
  }

  public synchronized int select() throws IOException {
    return doSelect(0);
  }

  public synchronized int select(long interval) throws IOException {
    if (interval < 0) throw new IllegalArgumentException();

    return doSelect(interval);
  }

  public int doSelect(long interval) throws IOException {
    selectedKeys.clear();

    if (clearWoken()) interval = -1;

    int max=0;
    for (Iterator<SelectionKey> it = keys.iterator();
         it.hasNext();)
    {
      SelectionKey key = it.next();
      SelectableChannel c = key.channel();
      int socket = c.socketFD();
      if (c.isOpen()) {
        key.readyOps(0);
        max = natSelectUpdateInterestSet
          (socket, key.interestOps(), state, max);
      } else {
        natSelectClearAll(socket, state);
        it.remove();
      }
    }

    int r = natDoSocketSelect(state, max, interval);

    if (r > 0) {
      for (SelectionKey key : keys) {
        SelectableChannel c = key.channel();
        int socket = c.socketFD();
        int ready = natUpdateReadySet(socket, key.interestOps(), state);
        key.readyOps(ready);
        if (ready != 0) {
          c.handleReadyOps(ready);
          selectedKeys.add(key);
        }
      }
    }
    clearWoken();

    return selectedKeys.size();
  }

  public void close() {
    natClose(state);
  }

  private static native long natInit();
  private static native void natWakeup(long state);
  private static native void natClose(long state);
  private static native void natSelectClearAll(int socket, long state);
  private static native int natSelectUpdateInterestSet(int socket,
                                                       int interest,
                                                       long state,
                                                       int max);
  private static native int natDoSocketSelect(long state, int max, long interval)
    throws IOException;
  private static native int natUpdateReadySet(int socket, int interest, long state);
}
