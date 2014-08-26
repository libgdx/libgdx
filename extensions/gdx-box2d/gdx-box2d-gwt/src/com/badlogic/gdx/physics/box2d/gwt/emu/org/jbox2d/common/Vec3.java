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
 * @author Daniel Murphy
 */
public class Vec3 implements Serializable {
  private static final long serialVersionUID = 1L;

  public float x, y, z;

  public Vec3() {
    x = y = z = 0f;
  }

  public Vec3(float argX, float argY, float argZ) {
    x = argX;
    y = argY;
    z = argZ;
  }

  public Vec3(Vec3 copy) {
    x = copy.x;
    y = copy.y;
    z = copy.z;
  }

  public Vec3 set(Vec3 vec) {
    x = vec.x;
    y = vec.y;
    z = vec.z;
    return this;
  }

  public Vec3 set(float argX, float argY, float argZ) {
    x = argX;
    y = argY;
    z = argZ;
    return this;
  }

  public Vec3 addLocal(Vec3 argVec) {
    x += argVec.x;
    y += argVec.y;
    z += argVec.z;
    return this;
  }

  public Vec3 add(Vec3 argVec) {
    return new Vec3(x + argVec.x, y + argVec.y, z + argVec.z);
  }

  public Vec3 subLocal(Vec3 argVec) {
    x -= argVec.x;
    y -= argVec.y;
    z -= argVec.z;
    return this;
  }

  public Vec3 sub(Vec3 argVec) {
    return new Vec3(x - argVec.x, y - argVec.y, z - argVec.z);
  }

  public Vec3 mulLocal(float argScalar) {
    x *= argScalar;
    y *= argScalar;
    z *= argScalar;
    return this;
  }

  public Vec3 mul(float argScalar) {
    return new Vec3(x * argScalar, y * argScalar, z * argScalar);
  }

  public Vec3 negate() {
    return new Vec3(-x, -y, -z);
  }

  public Vec3 negateLocal() {
    x = -x;
    y = -y;
    z = -z;
    return this;
  }

  public void setZero() {
    x = 0;
    y = 0;
    z = 0;
  }

  public Vec3 clone() {
    return new Vec3(this);
  }

  public String toString() {
    return "(" + x + "," + y + "," + z + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(x);
    result = prime * result + Float.floatToIntBits(y);
    result = prime * result + Float.floatToIntBits(z);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Vec3 other = (Vec3) obj;
    if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) return false;
    if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) return false;
    if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z)) return false;
    return true;
  }

  public final static float dot(Vec3 a, Vec3 b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
  }

  public final static Vec3 cross(Vec3 a, Vec3 b) {
    return new Vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
  }

  public final static void crossToOut(Vec3 a, Vec3 b, Vec3 out) {
    final float tempy = a.z * b.x - a.x * b.z;
    final float tempz = a.x * b.y - a.y * b.x;
    out.x = a.y * b.z - a.z * b.y;
    out.y = tempy;
    out.z = tempz;
  }
  
  public final static void crossToOutUnsafe(Vec3 a, Vec3 b, Vec3 out) {
    assert(out != b);
    assert(out != a);
    out.x = a.y * b.z - a.z * b.y;
    out.y = a.z * b.x - a.x * b.z;
    out.z = a.x * b.y - a.y * b.x;
  }
}
