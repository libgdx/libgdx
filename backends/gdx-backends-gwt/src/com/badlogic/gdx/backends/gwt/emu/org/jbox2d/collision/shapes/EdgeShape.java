/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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

  private final Vec2 pool0 = new Vec2();
  private final Vec2 pool1 = new Vec2();
  private final Vec2 pool2 = new Vec2();
  private final Vec2 pool3 = new Vec2();
  private final Vec2 pool4 = new Vec2();
  private final Vec2 pool5 = new Vec2();

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

  @Override
  public boolean raycast(RayCastOutput output, RayCastInput input, Transform xf, int childIndex) {

    // Put the ray into the edge's frame of reference.
    final Vec2 p1 = pool0.set(input.p1).subLocal(xf.p);
    Rot.mulTrans(xf.q, p1, p1);
    final Vec2 p2 = pool1.set(input.p2).subLocal(xf.p);
    Rot.mulTrans(xf.q, p1, p1);
    final Vec2 d = p2.subLocal(p1); // we don't use p2 later

    final Vec2 v1 = m_vertex1;
    final Vec2 v2 = m_vertex2;
    final Vec2 normal = pool2.set(v2).subLocal(v1);
    normal.set(normal.y, -normal.x);
    normal.normalize();

    // q = p1 + t * d
    // dot(normal, q - v1) = 0
    // dot(normal, p1 - v1) + t * dot(normal, d) = 0
    pool3.set(v1).subLocal(p1);
    float numerator = Vec2.dot(normal, pool3);
    float denominator = Vec2.dot(normal, d);

    if (denominator == 0.0f) {
      return false;
    }

    float t = numerator / denominator;
    if (t < 0.0f || 1.0f < t) {
      return false;
    }

    final Vec2 q = pool3;
    final Vec2 r = pool4;

    // Vec2 q = p1 + t * d;
    q.set(d).mulLocal(t).addLocal(p1);

    // q = v1 + s * r
    // s = dot(q - v1, r) / dot(r, r)
    // Vec2 r = v2 - v1;
    r.set(v2).subLocal(v1);
    float rr = Vec2.dot(r, r);
    if (rr == 0.0f) {
      return false;
    }

    pool5.set(q).subLocal(v1);
    float s = Vec2.dot(pool5, r) / rr;
    if (s < 0.0f || 1.0f < s) {
      return false;
    }

    output.fraction = t;
    if (numerator > 0.0f) {
      // argOutput.normal = -normal;
      output.normal.set(normal).negateLocal();
    } else {
      // output.normal = normal;
      output.normal.set(normal);
    }
    return true;
  }

  @Override
  public void computeAABB(AABB aabb, Transform xf, int childIndex) {
    final Vec2 v1 = pool1;
    final Vec2 v2 = pool2;

    Transform.mulToOutUnsafe(xf, m_vertex1, v1);
    Transform.mulToOutUnsafe(xf, m_vertex2, v2);

    Vec2.minToOut(v1, v2, aabb.lowerBound);
    Vec2.maxToOut(v1, v2, aabb.upperBound);

    aabb.lowerBound.x -= m_radius;
    aabb.lowerBound.y -= m_radius;
    aabb.upperBound.x += m_radius;
    aabb.upperBound.y += m_radius;
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
