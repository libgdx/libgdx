package org.jbox2d.particle;

import org.jbox2d.common.Color3f;

/**
 * Small color object for each particle
 * 
 * @author dmurph
 */
public class ParticleColor {
  public byte r, g, b, a;

  public ParticleColor() {
    r = (byte) 127;
    g = (byte) 127;
    b = (byte) 127;
    a = (byte) 50;
  }

  public ParticleColor(byte r, byte g, byte b, byte a) {
    set(r, g, b, a);
  }

  public ParticleColor(Color3f color) {
    set(color);
  }

  public void set(Color3f color) {
    r = (byte) (255 * color.x);
    g = (byte) (255 * color.y);
    b = (byte) (255 * color.z);
    a = (byte) 255;
  }
  
  public void set(ParticleColor color) {
    r = color.r;
    g = color.g;
    b = color.b;
    a = color.a;
  }
  
  public boolean isZero() {
    return r == 0 && g == 0 && b == 0 && a == 0;
  }

  public void set(byte r, byte g, byte b, byte a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }
}
