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

// updated to rev 100

/**
 * A transform contains translation and rotation. It is used to represent the position and
 * orientation of rigid frames.
 */
public class Transform implements Serializable {
  private static final long serialVersionUID = 1L;

  /** The translation caused by the transform */
  public final Vec2 p;

  /** A matrix representing a rotation */
  public final Rot q;

  /** The default constructor. */
  public Transform() {
    p = new Vec2();
    q = new Rot();
  }

  /** Initialize as a copy of another transform. */
  public Transform(final Transform xf) {
    p = xf.p.clone();
    q = xf.q.clone();
  }

  /** Initialize using a position vector and a rotation matrix. */
  public Transform(final Vec2 _position, final Rot _R) {
    p = _position.clone();
    q = _R.clone();
  }

  /** Set this to equal another transform. */
  public final Transform set(final Transform xf) {
    p.set(xf.p);
    q.set(xf.q);
    return this;
  }

  /**
   * Set this based on the position and angle.
   * 
   * @param p
   * @param angle
   */
  public final void set(Vec2 p, float angle) {
    this.p.set(p);
    q.set(angle);
  }

  /** Set this to the identity transform. */
  public final void setIdentity() {
    p.setZero();
    q.setIdentity();
  }

  public final static Vec2 mul(final Transform T, final Vec2 v) {
    return new Vec2((T.q.c * v.x - T.q.s * v.y) + T.p.x, (T.q.s * v.x + T.q.c * v.y) + T.p.y);
  }

  public final static void mulToOut(final Transform T, final Vec2 v, final Vec2 out) {
    final float tempy = (T.q.s * v.x + T.q.c * v.y) + T.p.y;
    out.x = (T.q.c * v.x - T.q.s * v.y) + T.p.x;
    out.y = tempy;
  }

  public final static void mulToOutUnsafe(final Transform T, final Vec2 v, final Vec2 out) {
    assert (v != out);
    out.x = (T.q.c * v.x - T.q.s * v.y) + T.p.x;
    out.y = (T.q.s * v.x + T.q.c * v.y) + T.p.y;
  }

  public final static Vec2 mulTrans(final Transform T, final Vec2 v) {
    final float px = v.x - T.p.x;
    final float py = v.y - T.p.y;
    return new Vec2((T.q.c * px + T.q.s * py), (-T.q.s * px + T.q.c * py));
  }

  public final static void mulTransToOut(final Transform T, final Vec2 v, final Vec2 out) {
    final float px = v.x - T.p.x;
    final float py = v.y - T.p.y;
    final float tempy = (-T.q.s * px + T.q.c * py);
    out.x = (T.q.c * px + T.q.s * py);
    out.y = tempy;
  }
  
  public final static void mulTransToOutUnsafe(final Transform T, final Vec2 v, final Vec2 out) {
    assert(v != out);
    final float px = v.x - T.p.x;
    final float py = v.y - T.p.y;
    out.x = (T.q.c * px + T.q.s * py);
    out.y = (-T.q.s * px + T.q.c * py);
  }

  public final static Transform mul(final Transform A, final Transform B) {
    Transform C = new Transform();
    Rot.mulUnsafe(A.q, B.q, C.q);
    Rot.mulToOutUnsafe(A.q, B.p, C.p);
    C.p.addLocal(A.p);
    return C;
  }

  public final static void mulToOut(final Transform A, final Transform B, final Transform out) {
    assert (out != A);
    Rot.mul(A.q, B.q, out.q);
    Rot.mulToOut(A.q, B.p, out.p);
    out.p.addLocal(A.p);
  }

  public final static void mulToOutUnsafe(final Transform A, final Transform B, final Transform out) {
    assert (out != B);
    assert (out != A);
    Rot.mulUnsafe(A.q, B.q, out.q);
    Rot.mulToOutUnsafe(A.q, B.p, out.p);
    out.p.addLocal(A.p);
  }

  private static Vec2 pool = new Vec2();

  public final static Transform mulTrans(final Transform A, final Transform B) {
    Transform C = new Transform();
    Rot.mulTransUnsafe(A.q, B.q, C.q);
    pool.set(B.p).subLocal(A.p);
    Rot.mulTransUnsafe(A.q, pool, C.p);
    return C;
  }

  public final static void mulTransToOut(final Transform A, final Transform B, final Transform out) {
    assert (out != A);
    Rot.mulTrans(A.q, B.q, out.q);
    pool.set(B.p).subLocal(A.p);
    Rot.mulTrans(A.q, pool, out.p);
  }

  public final static void mulTransToOutUnsafe(final Transform A, final Transform B,
      final Transform out) {
    assert (out != A);
    assert (out != B);
    Rot.mulTransUnsafe(A.q, B.q, out.q);
    pool.set(B.p).subLocal(A.p);
    Rot.mulTransUnsafe(A.q, pool, out.p);
  }

  @Override
  public final String toString() {
    String s = "XForm:\n";
    s += "Position: " + p + "\n";
    s += "R: \n" + q + "\n";
    return s;
  }
}
