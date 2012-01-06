package com.badlogic.gdx.backends.ios;

public class Hello {
  private long peer;

  public Hello(long peer) {
    this.peer = peer;
  }

  public void draw(int x, int y, int width, int height) {
    drawText(peer, "Hello, World!", 10, 20, 24.0);
    System.out.println("Hurray, Hurray the dog is dead...");
  }

  private static native void drawText(long peer, String text, int x, int y,
                                      double size);

  public void dispose() {
    peer = 0;
  }
}
