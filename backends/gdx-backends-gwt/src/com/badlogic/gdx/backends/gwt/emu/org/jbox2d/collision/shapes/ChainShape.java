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
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

/**
 * A chain shape is a free form sequence of line segments. The chain has two-sided collision, so you
 * can use inside and outside collision. Therefore, you may use any winding order. Since there may
 * be many vertices, they are allocated using Alloc. Connectivity information is used to create
 * smooth collisions. WARNING The chain will not collide properly if there are self-intersections.
 * 
 * @author Daniel
 */
public class ChainShape extends Shape {

  public Vec2[] m_vertices;
  public int m_count;
  public final Vec2 m_prevVertex = new Vec2(), m_nextVertex = new Vec2();
  public boolean m_hasPrevVertex = false, m_hasNextVertex = false;

  private final EdgeShape pool0 = new EdgeShape();
  private final Vec2 pool1 = new Vec2();
  private final Vec2 pool2 = new Vec2();

  public ChainShape() {
    super(ShapeType.CHAIN);
    m_vertices = null;
    m_radius = Settings.polygonRadius;
    m_count = 0;
  }

  @Override
  public int getChildCount() {
    return m_count - 1;
  }

  /**
   * Get a child edge.
   */
  public void getChildEdge(EdgeShape edge, int index) {
    assert (0 <= index && index < m_count - 1);
    edge.m_radius = m_radius;

    edge.m_vertex1.set(m_vertices[index + 0]);
    edge.m_vertex2.set(m_vertices[index + 1]);

    if (index > 0) {
      edge.m_vertex0.set(m_vertices[index - 1]);
      edge.m_hasVertex0 = true;
    } else {
      edge.m_vertex0.set(m_prevVertex);
      edge.m_hasVertex0 = m_hasPrevVertex;
    }

    if (index < m_count - 2) {
      edge.m_vertex3.set(m_vertices[index + 2]);
      edge.m_hasVertex3 = true;
    } else {
      edge.m_vertex3.set(m_nextVertex);
      edge.m_hasVertex3 = m_hasNextVertex;
    }
  }

  @Override
  public boolean testPoint(Transform xf, Vec2 p) {
    return false;
  }

  @Override
  public boolean raycast(RayCastOutput output, RayCastInput input, Transform xf, int childIndex) {
    assert (childIndex < m_count);

    final EdgeShape edgeShape = pool0;

    int i1 = childIndex;
    int i2 = childIndex + 1;
    if (i2 == m_count) {
      i2 = 0;
    }

    edgeShape.m_vertex1.set(m_vertices[i1]);
    edgeShape.m_vertex2.set(m_vertices[i2]);

    return edgeShape.raycast(output, input, xf, 0);
  }

  @Override
  public void computeAABB(AABB aabb, Transform xf, int childIndex) {
    assert (childIndex < m_count);

    int i1 = childIndex;
    int i2 = childIndex + 1;
    if (i2 == m_count) {
      i2 = 0;
    }

    final Vec2 v1 = pool1;
    final Vec2 v2 = pool2;
    Transform.mulToOutUnsafe(xf, m_vertices[i1], v1);
    Transform.mulToOutUnsafe(xf, m_vertices[i2], v2);

    Vec2.minToOut(v1, v2, aabb.lowerBound);
    Vec2.maxToOut(v1, v2, aabb.upperBound);
  }

  @Override
  public void computeMass(MassData massData, float density) {
    massData.mass = 0.0f;
    massData.center.setZero();
    massData.I = 0.0f;
  }

  @Override
  public Shape clone() {
    ChainShape clone = new ChainShape();
    clone.createChain(m_vertices, m_count);
    clone.m_prevVertex.set(m_prevVertex);
    clone.m_nextVertex.set(m_nextVertex);
    clone.m_hasPrevVertex = m_hasPrevVertex;
    clone.m_hasNextVertex = m_hasNextVertex;
    return clone;
  }

  /**
   * Create a loop. This automatically adjusts connectivity.
   * 
   * @param vertices an array of vertices, these are copied
   * @param count the vertex count
   */
  public void createLoop(final Vec2[] vertices, int count) {
    assert (m_vertices == null && m_count == 0);
    assert (count >= 3);
    m_count = count + 1;
    m_vertices = new Vec2[m_count];
    for (int i = 0; i < count; i++) {
      m_vertices[i] = new Vec2(vertices[i]);
    }
    m_vertices[count] = m_vertices[0];
    m_prevVertex.set(m_vertices[m_count - 2]);
    m_nextVertex.set(m_vertices[1]);
    m_hasPrevVertex = true;
    m_hasNextVertex = true;
  }

  /**
   * Create a chain with isolated end vertices.
   * 
   * @param vertices an array of vertices, these are copied
   * @param count the vertex count
   */
  public void createChain(final Vec2 vertices[], int count) {
    assert (m_vertices == null && m_count == 0);
    assert (count >= 2);
    m_count = count;
    m_vertices = new Vec2[m_count];
    for (int i = 0; i < m_count; i++) {
      m_vertices[i] = new Vec2(vertices[i]);
    }
    m_hasPrevVertex = false;
    m_hasNextVertex = false;
  }

  /**
   * Establish connectivity to a vertex that precedes the first vertex. Don't call this for loops.
   * 
   * @param prevVertex
   */
  public void setPrevVertex(final Vec2 prevVertex) {
    m_prevVertex.set(prevVertex);
    m_hasPrevVertex = true;
  }

  /**
   * Establish connectivity to a vertex that follows the last vertex. Don't call this for loops.
   * 
   * @param nextVertex
   */
  public void setNextVertex(final Vec2 nextVertex) {
    m_nextVertex.set(nextVertex);
    m_hasNextVertex = true;
  }
}
