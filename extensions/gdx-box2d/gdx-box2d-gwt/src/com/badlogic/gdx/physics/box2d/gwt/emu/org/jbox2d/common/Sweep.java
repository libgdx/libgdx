/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.common;

import java.io.Serializable;

/**
 * This describes the motion of a body/shape for TOI computation. Shapes are defined with respect to
 * the body origin, which may no coincide with the center of mass. However, to support dynamics we
 * must interpolate the center of mass position.
 */
public class Sweep implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Local center of mass position */
  public final Vec2 localCenter;
  /** Center world positions */
  public final Vec2 c0, c;
  /** World angles */
  public float a0, a;

  /** Fraction of the current time step in the range [0,1] c0 and a0 are the positions at alpha0. */
  public float alpha0;

  public String toString() {
    String s = "Sweep:\nlocalCenter: " + localCenter + "\n";
    s += "c0: " + c0 + ", c: " + c + "\n";
    s += "a0: " + a0 + ", a: " + a + "\n";
    s += "alpha0: " + alpha0;
    return s;
  }

  public Sweep() {
    localCenter = new Vec2();
    c0 = new Vec2();
    c = new Vec2();
  }

  public final void normalize() {
    float d = MathUtils.TWOPI * MathUtils.floor(a0 / MathUtils.TWOPI);
    a0 -= d;
    a -= d;
  }

  public final Sweep set(Sweep other) {
    localCenter.set(other.localCenter);
    c0.set(other.c0);
    c.set(other.c);
    a0 = other.a0;
    a = other.a;
    alpha0 = other.alpha0;
    return this;
  }

  /**
   * Get the interpolated transform at a specific time.
   * 
   * @param xf the result is placed here - must not be null
   * @param t the normalized time in [0,1].
   */
  public final void getTransform(final Transform xf, final float beta) {
    assert (xf != null);
    // xf->p = (1.0f - beta) * c0 + beta * c;
    // float32 angle = (1.0f - beta) * a0 + beta * a;
    // xf->q.Set(angle);
    xf.p.x = (1.0f - beta) * c0.x + beta * c.x;
    xf.p.y = (1.0f - beta) * c0.y + beta * c.y;
    float angle = (1.0f - beta) * a0 + beta * a;
    xf.q.set(angle);

    // Shift to origin
    // xf->p -= b2Mul(xf->q, localCenter);
    final Rot q = xf.q;
    xf.p.x -= q.c * localCenter.x - q.s * localCenter.y;
    xf.p.y -= q.s * localCenter.x + q.c * localCenter.y;
  }

  /**
   * Advance the sweep forward, yielding a new initial state.
   * 
   * @param alpha the new initial time.
   */
  public final void advance(final float alpha) {
    assert(alpha0 < 1.0f);
    // float32 beta = (alpha - alpha0) / (1.0f - alpha0);
    // c0 += beta * (c - c0);
    // a0 += beta * (a - a0);
    // alpha0 = alpha;
    float beta = (alpha - alpha0) / (1.0f - alpha0);
    c0.x += beta * (c.x - c0.x);
    c0.y += beta * (c.y - c0.y);
    a0 += beta * (a - a0);
    alpha0 = alpha;
  }
}
