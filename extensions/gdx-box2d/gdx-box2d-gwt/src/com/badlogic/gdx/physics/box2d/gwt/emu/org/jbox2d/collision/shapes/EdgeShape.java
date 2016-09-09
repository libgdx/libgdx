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
package org.jbox2d.collision.shapes;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

/**
 * A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The
 * connectivity information is used to ensure correct contact normals.
 * 
 * @author Daniel
 */
public class EdgeShape extends Shape {

  /**
   * edge vertex 1
   */
  public final Vec2 m_vertex1 = new Vec2();
  /**
   * edge vertex 2
   */
  public final Vec2 m_vertex2 = new Vec2();

  /**
   * optional adjacent vertex 1. Used for smooth collision
   */
  public final Vec2 m_vertex0 = new Vec2();
  /**
   * optional adjacent vertex 2. Used for smooth collision
   */
  public final Vec2 m_vertex3 = new Vec2();
  public boolean m_hasVertex0 = false, m_hasVertex3 = false;


  public EdgeShape() {
    super(ShapeType.EDGE);
    m_radius = Settings.polygonRadius;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  public void set(Vec2 v1, Vec2 v2) {
    m_vertex1.set(v1);
    m_vertex2.set(v2);
    m_hasVertex0 = m_hasVertex3 = false;
  }

  @Override
  public boolean testPoint(Transform xf, Vec2 p) {
    return false;
  }

  // for pooling
  private final Vec2 normal = new Vec2();

  @Override
  public float computeDistanceToOut(Transform xf, Vec2 p, int childIndex, Vec2 normalOut) {
    float xfqc = xf.q.c;
    float xfqs = xf.q.s;
    float xfpx = xf.p.x;
    float xfpy = xf.p.y;
    float v1x = (xfqc * m_vertex1.x - xfqs * m_vertex1.y) + xfpx;
    float v1y = (xfqs * m_vertex1.x + xfqc * m_vertex1.y) + xfpy;
    float v2x = (xfqc * m_vertex2.x - xfqs * m_vertex2.y) + xfpx;
    float v2y = (xfqs * m_vertex2.x + xfqc * m_vertex2.y) + xfpy;

    float dx = p.x - v1x;
    float dy = p.y - v1y;
    float sx = v2x - v1x;
    float sy = v2y - v1y;
    float ds = dx * sx + dy * sy;
    if (ds > 0) {
      float s2 = sx * sx + sy * sy;
      if (ds > s2) {
        dx = p.x - v2x;
        dy = p.y - v2y;
      } else {
        dx -= ds / s2 * sx;
        dy -= ds / s2 * sy;
      }
    }

    float d1 = MathUtils.sqrt(dx * dx + dy * dy);
    if (d1 > 0) {
      normalOut.x = 1 / d1 * dx;
      normalOut.y = 1 / d1 * dy;
    } else {
      normalOut.x = 0;
      normalOut.y = 0;
    }
    return d1;
  }

  // p = p1 + t * d
  // v = v1 + s * e
  // p1 + t * d = v1 + s * e
  // s * e - t * d = p1 - v1
  @Override
  public boolean raycast(RayCastOutput output, RayCastInput input, Transform xf, int childIndex) {

    float tempx, tempy;
    final Vec2 v1 = m_vertex1;
    final Vec2 v2 = m_vertex2;
    final Rot xfq = xf.q;
    final Vec2 xfp = xf.p;

    // Put the ray into the edge's frame of reference.
    // b2Vec2 p1 = b2MulT(xf.q, input.p1 - xf.p);
    // b2Vec2 p2 = b2MulT(xf.q, input.p2 - xf.p);
    tempx = input.p1.x - xfp.x;
    tempy = input.p1.y - xfp.y;
    final float p1x = xfq.c * tempx + xfq.s * tempy;
    final float p1y = -xfq.s * tempx + xfq.c * tempy;

    tempx = input.p2.x - xfp.x;
    tempy = input.p2.y - xfp.y;
    final float p2x = xfq.c * tempx + xfq.s * tempy;
    final float p2y = -xfq.s * tempx + xfq.c * tempy;

    final float dx = p2x - p1x;
    final float dy = p2y - p1y;

    // final Vec2 normal = pool2.set(v2).subLocal(v1);
    // normal.set(normal.y, -normal.x);
    normal.x = v2.y - v1.y;
    normal.y = v1.x - v2.x;
    normal.normalize();
    final float normalx = normal.x;
    final float normaly = normal.y;

    // q = p1 + t * d
    // dot(normal, q - v1) = 0
    // dot(normal, p1 - v1) + t * dot(normal, d) = 0
    tempx = v1.x - p1x;
    tempy = v1.y - p1y;
    float numerator = normalx * tempx + normaly * tempy;
    float denominator = normalx * dx + normaly * dy;

    if (denominator == 0.0f) {
      return false;
    }

    float t = numerator / denominator;
    if (t < 0.0f || 1.0f < t) {
      return false;
    }

    // Vec2 q = p1 + t * d;
    final float qx = p1x + t * dx;
    final float qy = p1y + t * dy;

    // q = v1 + s * r
    // s = dot(q - v1, r) / dot(r, r)
    // Vec2 r = v2 - v1;
    final float rx = v2.x - v1.x;
    final float ry = v2.y - v1.y;
    final float rr = rx * rx + ry * ry;
    if (rr == 0.0f) {
      return false;
    }
    tempx = qx - v1.x;
    tempy = qy - v1.y;
    // float s = Vec2.dot(pool5, r) / rr;
    float s = (tempx * rx + tempy * ry) / rr;
    if (s < 0.0f || 1.0f < s) {
      return false;
    }

    output.fraction = t;
    if (numerator > 0.0f) {
      // output.normal = -b2Mul(xf.q, normal);
      output.normal.x = -xfq.c * normal.x + xfq.s * normal.y;
      output.normal.y = -xfq.s * normal.x - xfq.c * normal.y;
    } else {
      // output->normal = b2Mul(xf.q, normal);
      output.normal.x = xfq.c * normal.x - xfq.s * normal.y;
      output.normal.y = xfq.s * normal.x + xfq.c * normal.y;
    }
    return true;
  }

  @Override
  public void computeAABB(AABB aabb, Transform xf, int childIndex) {
    final Vec2 lowerBound = aabb.lowerBound;
    final Vec2 upperBound = aabb.upperBound;
    final Rot xfq = xf.q;

    final float v1x = (xfq.c * m_vertex1.x - xfq.s * m_vertex1.y) + xf.p.x;
    final float v1y = (xfq.s * m_vertex1.x + xfq.c * m_vertex1.y) + xf.p.y;
    final float v2x = (xfq.c * m_vertex2.x - xfq.s * m_vertex2.y) + xf.p.x;
    final float v2y = (xfq.s * m_vertex2.x + xfq.c * m_vertex2.y) + xf.p.y;

    lowerBound.x = v1x < v2x ? v1x : v2x;
    lowerBound.y = v1y < v2y ? v1y : v2y;
    upperBound.x = v1x > v2x ? v1x : v2x;
    upperBound.y = v1y > v2y ? v1y : v2y;

    lowerBound.x -= m_radius;
    lowerBound.y -= m_radius;
    upperBound.x += m_radius;
    upperBound.y += m_radius;
  }

  @Override
  public void computeMass(MassData massData, float density) {
    massData.mass = 0.0f;
    massData.center.set(m_vertex1).addLocal(m_vertex2).mulLocal(0.5f);
    massData.I = 0.0f;
  }

  @Override
  public Shape clone() {
    EdgeShape edge = new EdgeShape();
    edge.m_radius = this.m_radius;
    edge.m_hasVertex0 = this.m_hasVertex0;
    edge.m_hasVertex3 = this.m_hasVertex3;
    edge.m_vertex0.set(this.m_vertex0);
    edge.m_vertex1.set(this.m_vertex1);
    edge.m_vertex2.set(this.m_vertex2);
    edge.m_vertex3.set(this.m_vertex3);
    return edge;
  }
}
