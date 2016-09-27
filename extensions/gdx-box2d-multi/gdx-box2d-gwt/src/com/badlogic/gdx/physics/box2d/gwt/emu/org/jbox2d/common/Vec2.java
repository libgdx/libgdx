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
 * A 2D column vector
 */
public class Vec2 implements Serializable {
  private static final long serialVersionUID = 1L;

  public float x, y;

  public Vec2() {
    this(0, 0);
  }

  public Vec2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public Vec2(Vec2 toCopy) {
    this(toCopy.x, toCopy.y);
  }

  /** Zero out this vector. */
  public final void setZero() {
    x = 0.0f;
    y = 0.0f;
  }

  /** Set the vector component-wise. */
  public final Vec2 set(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /** Set this vector to another vector. */
  public final Vec2 set(Vec2 v) {
    this.x = v.x;
    this.y = v.y;
    return this;
  }

  /** Return the sum of this vector and another; does not alter either one. */
  public final Vec2 add(Vec2 v) {
    return new Vec2(x + v.x, y + v.y);
  }



  /** Return the difference of this vector and another; does not alter either one. */
  public final Vec2 sub(Vec2 v) {
    return new Vec2(x - v.x, y - v.y);
  }

  /** Return this vector multiplied by a scalar; does not alter this vector. */
  public final Vec2 mul(float a) {
    return new Vec2(x * a, y * a);
  }

  /** Return the negation of this vector; does not alter this vector. */
  public final Vec2 negate() {
    return new Vec2(-x, -y);
  }

  /** Flip the vector and return it - alters this vector. */
  public final Vec2 negateLocal() {
    x = -x;
    y = -y;
    return this;
  }

  /** Add another vector to this one and returns result - alters this vector. */
  public final Vec2 addLocal(Vec2 v) {
    x += v.x;
    y += v.y;
    return this;
  }

  /** Adds values to this vector and returns result - alters this vector. */
  public final Vec2 addLocal(float x, float y) {
    this.x += x;
    this.y += y;
    return this;
  }

  /** Subtract another vector from this one and return result - alters this vector. */
  public final Vec2 subLocal(Vec2 v) {
    x -= v.x;
    y -= v.y;
    return this;
  }

  /** Multiply this vector by a number and return result - alters this vector. */
  public final Vec2 mulLocal(float a) {
    x *= a;
    y *= a;
    return this;
  }

  /** Get the skew vector such that dot(skew_vec, other) == cross(vec, other) */
  public final Vec2 skew() {
    return new Vec2(-y, x);
  }

  /** Get the skew vector such that dot(skew_vec, other) == cross(vec, other) */
  public final void skew(Vec2 out) {
    out.x = -y;
    out.y = x;
  }

  /** Return the length of this vector. */
  public final float length() {
    return MathUtils.sqrt(x * x + y * y);
  }

  /** Return the squared length of this vector. */
  public final float lengthSquared() {
    return (x * x + y * y);
  }

  /** Normalize this vector and return the length before normalization. Alters this vector. */
  public final float normalize() {
    float length = length();
    if (length < Settings.EPSILON) {
      return 0f;
    }

    float invLength = 1.0f / length;
    x *= invLength;
    y *= invLength;
    return length;
  }

  /** True if the vector represents a pair of valid, non-infinite floating point numbers. */
  public final boolean isValid() {
    return !Float.isNaN(x) && !Float.isInfinite(x) && !Float.isNaN(y) && !Float.isInfinite(y);
  }

  /** Return a new vector that has positive components. */
  public final Vec2 abs() {
    return new Vec2(MathUtils.abs(x), MathUtils.abs(y));
  }

  public final void absLocal() {
    x = MathUtils.abs(x);
    y = MathUtils.abs(y);
  }

  // @Override // annotation omitted for GWT-compatibility
  /** Return a copy of this vector. */
  public final Vec2 clone() {
    return new Vec2(x, y);
  }

  @Override
  public final String toString() {
    return "(" + x + "," + y + ")";
  }

  /*
   * Static
   */

  public final static Vec2 abs(Vec2 a) {
    return new Vec2(MathUtils.abs(a.x), MathUtils.abs(a.y));
  }

  public final static void absToOut(Vec2 a, Vec2 out) {
    out.x = MathUtils.abs(a.x);
    out.y = MathUtils.abs(a.y);
  }

  public final static float dot(final Vec2 a, final Vec2 b) {
    return a.x * b.x + a.y * b.y;
  }

  public final static float cross(final Vec2 a, final Vec2 b) {
    return a.x * b.y - a.y * b.x;
  }

  public final static Vec2 cross(Vec2 a, float s) {
    return new Vec2(s * a.y, -s * a.x);
  }

  public final static void crossToOut(Vec2 a, float s, Vec2 out) {
    final float tempy = -s * a.x;
    out.x = s * a.y;
    out.y = tempy;
  }

  public final static void crossToOutUnsafe(Vec2 a, float s, Vec2 out) {
    assert (out != a);
    out.x = s * a.y;
    out.y = -s * a.x;
  }

  public final static Vec2 cross(float s, Vec2 a) {
    return new Vec2(-s * a.y, s * a.x);
  }

  public final static void crossToOut(float s, Vec2 a, Vec2 out) {
    final float tempY = s * a.x;
    out.x = -s * a.y;
    out.y = tempY;
  }

  public final static void crossToOutUnsafe(float s, Vec2 a, Vec2 out) {
    assert (out != a);
    out.x = -s * a.y;
    out.y = s * a.x;
  }

  public final static void negateToOut(Vec2 a, Vec2 out) {
    out.x = -a.x;
    out.y = -a.y;
  }

  public final static Vec2 min(Vec2 a, Vec2 b) {
    return new Vec2(a.x < b.x ? a.x : b.x, a.y < b.y ? a.y : b.y);
  }

  public final static Vec2 max(Vec2 a, Vec2 b) {
    return new Vec2(a.x > b.x ? a.x : b.x, a.y > b.y ? a.y : b.y);
  }

  public final static void minToOut(Vec2 a, Vec2 b, Vec2 out) {
    out.x = a.x < b.x ? a.x : b.x;
    out.y = a.y < b.y ? a.y : b.y;
  }

  public final static void maxToOut(Vec2 a, Vec2 b, Vec2 out) {
    out.x = a.x > b.x ? a.x : b.x;
    out.y = a.y > b.y ? a.y : b.y;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() { // automatically generated by Eclipse
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) { // automatically generated by Eclipse
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Vec2 other = (Vec2) obj;
    if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) return false;
    if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) return false;
    return true;
  }
}
