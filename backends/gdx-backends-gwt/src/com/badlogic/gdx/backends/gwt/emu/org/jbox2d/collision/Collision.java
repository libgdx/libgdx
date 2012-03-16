/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
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
package org.jbox2d.collision;

import org.jbox2d.collision.Distance.SimplexCache;
import org.jbox2d.collision.Manifold.ManifoldType;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.pooling.IWorldPool;

/**
 * Functions used for computing contact points, distance queries, and TOI queries. Collision methods
 * are non-static for pooling speed, retrieve a collision object from the {@link SingletonPool}.
 * Should not be finalructed.
 * 
 * @author Daniel Murphy
 */
public class Collision {
  public static final int NULL_FEATURE = Integer.MAX_VALUE;

  private final IWorldPool pool;

  public Collision(IWorldPool argPool) {
    incidentEdge[0] = new ClipVertex();
    incidentEdge[1] = new ClipVertex();
    clipPoints1[0] = new ClipVertex();
    clipPoints1[1] = new ClipVertex();
    clipPoints2[0] = new ClipVertex();
    clipPoints2[1] = new ClipVertex();
    pool = argPool;
  }

  private final DistanceInput input = new DistanceInput();
  private final SimplexCache cache = new SimplexCache();
  private final DistanceOutput output = new DistanceOutput();

  /**
   * Determine if two generic shapes overlap.
   * 
   * @param shapeA
   * @param shapeB
   * @param xfA
   * @param xfB
   * @return
   */
  public final boolean testOverlap(Shape shapeA, int indexA, Shape shapeB, int indexB,
      Transform xfA, Transform xfB) {
    input.proxyA.set(shapeA, indexA);
    input.proxyB.set(shapeB, indexB);
    input.transformA.set(xfA);
    input.transformB.set(xfB);
    input.useRadii = true;

    cache.count = 0;

    pool.getDistance().distance(output, cache, input);
    // djm note: anything significant about 10.0f?
    return output.distance < 10.0f * Settings.EPSILON;
  }

  /**
   * Compute the point states given two manifolds. The states pertain to the transition from
   * manifold1 to manifold2. So state1 is either persist or remove while state2 is either add or
   * persist.
   * 
   * @param state1
   * @param state2
   * @param manifold1
   * @param manifold2
   */
  public static final void getPointStates(final PointState[] state1, final PointState[] state2,
      final Manifold manifold1, final Manifold manifold2) {

    for (int i = 0; i < Settings.maxManifoldPoints; i++) {
      state1[i] = PointState.NULL_STATE;
      state2[i] = PointState.NULL_STATE;
    }

    // Detect persists and removes.
    for (int i = 0; i < manifold1.pointCount; i++) {
      ContactID id = manifold1.points[i].id;

      state1[i] = PointState.REMOVE_STATE;

      for (int j = 0; j < manifold2.pointCount; j++) {
        if (manifold2.points[j].id.isEqual(id)) {
          state1[i] = PointState.PERSIST_STATE;
          break;
        }
      }
    }

    // Detect persists and adds
    for (int i = 0; i < manifold2.pointCount; i++) {
      ContactID id = manifold2.points[i].id;

      state2[i] = PointState.ADD_STATE;

      for (int j = 0; j < manifold1.pointCount; j++) {
        if (manifold1.points[j].id.isEqual(id)) {
          state2[i] = PointState.PERSIST_STATE;
          break;
        }
      }
    }
  }

  /**
   * Clipping for contact manifolds. Sutherland-Hodgman clipping.
   * 
   * @param vOut
   * @param vIn
   * @param normal
   * @param offset
   * @return
   */
  public static final int clipSegmentToLine(final ClipVertex[] vOut, final ClipVertex[] vIn,
      final Vec2 normal, float offset, int vertexIndexA) {

    // Start with no output points
    int numOut = 0;

    // Calculate the distance of end points to the line
    float distance0 = Vec2.dot(normal, vIn[0].v) - offset;
    float distance1 = Vec2.dot(normal, vIn[1].v) - offset;

    // If the points are behind the plane
    if (distance0 <= 0.0f) {
      vOut[numOut++].set(vIn[0]);
    }
    if (distance1 <= 0.0f) {
      vOut[numOut++].set(vIn[1]);
    }

    // If the points are on different sides of the plane
    if (distance0 * distance1 < 0.0f) {
      // Find intersection point of edge and plane
      float interp = distance0 / (distance0 - distance1);
      // vOut[numOut].v = vIn[0].v + interp * (vIn[1].v - vIn[0].v);
      vOut[numOut].v.set(vIn[1].v).subLocal(vIn[0].v).mulLocal(interp).addLocal(vIn[0].v);

      // VertexA is hitting edgeB.
      vOut[numOut].id.indexA = (byte) vertexIndexA;
      vOut[numOut].id.indexB = vIn[0].id.indexB;
      vOut[numOut].id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
      vOut[numOut].id.typeB = (byte) ContactID.Type.FACE.ordinal();
      ++numOut;
    }

    return numOut;
  }

  // #### COLLISION STUFF (not from collision.h or collision.cpp) ####

  // djm pooling
  private static Vec2 pA = new Vec2();
  private static Vec2 pB = new Vec2();
  private static Vec2 d = new Vec2();

  /**
   * Compute the collision manifold between two circles.
   * 
   * @param manifold
   * @param circle1
   * @param xfA
   * @param circle2
   * @param xfB
   */
  public final void collideCircles(Manifold manifold, final CircleShape circle1,
      final Transform xfA, final CircleShape circle2, final Transform xfB) {
    manifold.pointCount = 0;

    // before inline:
    Transform.mulToOut(xfA, circle1.m_p, pA);
    Transform.mulToOut(xfB, circle2.m_p, pB);
    d.set(pB).subLocal(pA);
    float distSqr = d.x * d.x + d.y * d.y;

    // after inline:
    // final Vec2 v = circle1.m_p;
    // final float pAy = xfA.p.y + xfA.q.ex.y * v.x + xfA.q.ey.y * v.y;
    // final float pAx = xfA.p.x + xfA.q.ex.x * v.x + xfA.q.ey.x * v.y;
    //
    // final Vec2 v1 = circle2.m_p;
    // final float pBy = xfB.p.y + xfB.q.ex.y * v1.x + xfB.q.ey.y * v1.y;
    // final float pBx = xfB.p.x + xfB.q.ex.x * v1.x + xfB.q.ey.x * v1.y;
    //
    // final float dx = pBx - pAx;
    // final float dy = pBy - pAy;
    //
    // final float distSqr = dx * dx + dy * dy;
    // end inline

    final float radius = circle1.m_radius + circle2.m_radius;
    if (distSqr > radius * radius) {
      return;
    }

    manifold.type = ManifoldType.CIRCLES;
    manifold.localPoint.set(circle1.m_p);
    manifold.localNormal.setZero();
    manifold.pointCount = 1;

    manifold.points[0].localPoint.set(circle2.m_p);
    manifold.points[0].id.zero();
  }

  // djm pooling, and from above
  private static final Vec2 c = new Vec2();
  private static final Vec2 cLocal = new Vec2();

  /**
   * Compute the collision manifold between a polygon and a circle.
   * 
   * @param manifold
   * @param polygon
   * @param xfA
   * @param circle
   * @param xfB
   */
  public final void collidePolygonAndCircle(Manifold manifold, final PolygonShape polygon,
      final Transform xfA, final CircleShape circle, final Transform xfB) {
    manifold.pointCount = 0;
//    Vec2 v = circle.m_p;

    // Compute circle position in the frame of the polygon.
    // before inline:
    Transform.mulToOut(xfB, circle.m_p, c);
    Transform.mulTransToOut(xfA, c, cLocal);
    final float cLocalx = cLocal.x;
    final float cLocaly = cLocal.y;
    // after inline:
    // final float cy = xfB.p.y + xfB.q.ex.y * v.x + xfB.q.ey.y * v.y;
    // final float cx = xfB.p.x + xfB.q.ex.x * v.x + xfB.q.ey.x * v.y;
    // final float v1x = cx - xfA.p.x;
    // final float v1y = cy - xfA.p.y;
    // final Vec2 b = xfA.q.ex;
    // final Vec2 b1 = xfA.q.ey;
    // final float cLocaly = v1x * b1.x + v1y * b1.y;
    // final float cLocalx = v1x * b.x + v1y * b.y;
    // end inline

    // Find the min separating edge.
    int normalIndex = 0;
    float separation = Float.MIN_VALUE;
    final float radius = polygon.m_radius + circle.m_radius;
    final int vertexCount = polygon.m_count;

    final Vec2[] vertices = polygon.m_vertices;
    final Vec2[] normals = polygon.m_normals;

    for (int i = 0; i < vertexCount; i++) {
      // before inline
      // temp.set(cLocal).subLocal(vertices[i]);
      // float s = Vec2.dot(normals[i], temp);
      // after inline
      final Vec2 vertex = vertices[i];
      final float tempx = cLocalx - vertex.x;
      final float tempy = cLocaly - vertex.y;
      final Vec2 normal = normals[i];
      final float s = normal.x * tempx + normal.y * tempy;


      if (s > radius) {
        // early out
        return;
      }

      if (s > separation) {
        separation = s;
        normalIndex = i;
      }
    }

    // Vertices that subtend the incident face.
    final int vertIndex1 = normalIndex;
    final int vertIndex2 = vertIndex1 + 1 < vertexCount ? vertIndex1 + 1 : 0;
    final Vec2 v1 = vertices[vertIndex1];
    final Vec2 v2 = vertices[vertIndex2];

    // If the center is inside the polygon ...
    if (separation < Settings.EPSILON) {
      manifold.pointCount = 1;
      manifold.type = ManifoldType.FACE_A;

      // before inline:
      // manifold.localNormal.set(normals[normalIndex]);
      // manifold.localPoint.set(v1).addLocal(v2).mulLocal(.5f);
      // manifold.points[0].localPoint.set(circle.m_p);
      // after inline:
      final Vec2 normal = normals[normalIndex];
      manifold.localNormal.x = normal.x;
      manifold.localNormal.y = normal.y;
      manifold.localPoint.x = (v1.x + v2.x) * .5f;
      manifold.localPoint.y = (v1.y + v2.y) * .5f;
      final ManifoldPoint mpoint = manifold.points[0];
      mpoint.localPoint.x = circle.m_p.x;
      mpoint.localPoint.y = circle.m_p.y;
      mpoint.id.zero();
      // end inline
      return;
    }

    // Compute barycentric coordinates
    // before inline:
    // temp.set(cLocal).subLocal(v1);
    // temp2.set(v2).subLocal(v1);
    // float u1 = Vec2.dot(temp, temp2);
    // temp.set(cLocal).subLocal(v2);
    // temp2.set(v1).subLocal(v2);
    // float u2 = Vec2.dot(temp, temp2);
    // after inline:
    final float tempX = cLocalx - v1.x;
    final float tempY = cLocaly - v1.y;
    final float temp2X = v2.x - v1.x;
    final float temp2Y = v2.y - v1.y;
    final float u1 = tempX * temp2X + tempY * temp2Y;

    final float temp3X = cLocalx - v2.x;
    final float temp3Y = cLocaly - v2.y;
    final float temp4X = v1.x - v2.x;
    final float temp4Y = v1.y - v2.y;
    final float u2 = temp3X * temp4X + temp3Y * temp4Y;
    // end inline

    if (u1 <= 0f) {
      // inlined
      final float dx = cLocalx - v1.x;
      final float dy = cLocaly - v1.y;
      if (dx * dx + dy * dy > radius * radius) {
        return;
      }

      manifold.pointCount = 1;
      manifold.type = ManifoldType.FACE_A;
      // before inline:
      // manifold.localNormal.set(cLocal).subLocal(v1);
      // after inline:
      manifold.localNormal.x = cLocalx - v1.x;
      manifold.localNormal.y = cLocaly - v1.y;
      // end inline
      manifold.localNormal.normalize();
      manifold.localPoint.set(v1);
      manifold.points[0].localPoint.set(circle.m_p);
      manifold.points[0].id.zero();
    } else if (u2 <= 0.0f) {
      // inlined
      final float dx = cLocalx - v2.x;
      final float dy = cLocaly - v2.y;
      if (dx * dx + dy * dy > radius * radius) {
        return;
      }

      manifold.pointCount = 1;
      manifold.type = ManifoldType.FACE_A;
      // before inline:
      // manifold.localNormal.set(cLocal).subLocal(v2);
      // after inline:
      manifold.localNormal.x = cLocalx - v2.x;
      manifold.localNormal.y = cLocaly - v2.y;
      // end inline
      manifold.localNormal.normalize();
      manifold.localPoint.set(v2);
      manifold.points[0].localPoint.set(circle.m_p);
      manifold.points[0].id.zero();
    } else {
      // Vec2 faceCenter = 0.5f * (v1 + v2);
      // (temp is faceCenter)
      // before inline:
      // temp.set(v1).addLocal(v2).mulLocal(.5f);
      //
      // temp2.set(cLocal).subLocal(temp);
      // separation = Vec2.dot(temp2, normals[vertIndex1]);
      // if (separation > radius) {
      // return;
      // }
      // after inline:
      final float fcx = (v1.x + v2.x) * .5f;
      final float fcy = (v1.y + v2.y) * .5f;

      final float tx = cLocalx - fcx;
      final float ty = cLocaly - fcy;
      final Vec2 normal = normals[vertIndex1];
      separation = tx * normal.x + ty * normal.y;
      if (separation > radius) {
        return;
      }
      // end inline

      manifold.pointCount = 1;
      manifold.type = ManifoldType.FACE_A;
      manifold.localNormal.set(normals[vertIndex1]);
      manifold.localPoint.x = fcx; // (faceCenter)
      manifold.localPoint.y = fcy;
      manifold.points[0].localPoint.set(circle.m_p);
      manifold.points[0].id.zero();
    }
  }

  // djm pooling
  private final Vec2 normal1 = new Vec2();
  private final Vec2 normal1World = new Vec2();
  private final Vec2 v1 = new Vec2();
  private final Vec2 v2 = new Vec2();

  /**
   * Find the separation between poly1 and poly2 for a given edge normal on poly1.
   * 
   * @param poly1
   * @param xf1
   * @param edge1
   * @param poly2
   * @param xf2
   */
  public final float edgeSeparation(final PolygonShape poly1, final Transform xf1, final int edge1,
      final PolygonShape poly2, final Transform xf2) {

    final int count1 = poly1.m_count;
    final Vec2[] vertices1 = poly1.m_vertices;
    final Vec2[] normals1 = poly1.m_normals;

    final int count2 = poly2.m_count;
    final Vec2[] vertices2 = poly2.m_vertices;

    assert (0 <= edge1 && edge1 < count1);
    // Convert normal from poly1's frame into poly2's frame.
    // before inline:
    // Vec2 normal1World = Mul(xf1.R, normals1[edge1]);
    Rot.mulToOutUnsafe(xf1.q, normals1[edge1], normal1World);
    // Vec2 normal1 = MulT(xf2.R, normal1World);
    Rot.mulTransUnsafe(xf2.q, normal1World, normal1);
    final float normal1x = normal1.x;
    final float normal1y = normal1.y;
    final float normal1Worldx = normal1World.x;
    final float normal1Worldy = normal1World.y;
    // after inline:
    // R.mulToOut(v,out);
    // final Mat22 R = xf1.q;
    // final Vec2 v = normals1[edge1];
    // final float normal1Worldy = R.ex.y * v.x + R.ey.y * v.y;
    // final float normal1Worldx = R.ex.x * v.x + R.ey.x * v.y;
    // final Mat22 R1 = xf2.q;
    // final float normal1x = normal1Worldx * R1.ex.x + normal1Worldy * R1.ex.y;
    // final float normal1y = normal1Worldx * R1.ey.x + normal1Worldy * R1.ey.y;
    // end inline

    // Find support vertex on poly2 for -normal.
    int index = 0;
    float minDot = Float.MAX_VALUE;

    for (int i = 0; i < count2; ++i) {
      final Vec2 a = vertices2[i];
      final float dot = a.x * normal1x + a.y * normal1y;
      if (dot < minDot) {
        minDot = dot;
        index = i;
      }
    }

    // Vec2 v1 = Mul(xf1, vertices1[edge1]);
    // Vec2 v2 = Mul(xf2, vertices2[index]);
    // before inline:
    Transform.mulToOut(xf1, vertices1[edge1], v1);
    Transform.mulToOut(xf2, vertices2[index], v2);

    float separation = Vec2.dot(v2.subLocal(v1), normal1World);
    return separation;

    // after inline:
    // final Vec2 v3 = vertices1[edge1];
    // final float v1y = xf1.p.y + R.ex.y * v3.x + R.ey.y * v3.y;
    // final float v1x = xf1.p.x + R.ex.x * v3.x + R.ey.x * v3.y;
    // final Vec2 v4 = vertices2[index];
    // final float v2y = xf2.p.y + R1.ex.y * v4.x + R1.ey.y * v4.y - v1y;
    // final float v2x = xf2.p.x + R1.ex.x * v4.x + R1.ey.x * v4.y - v1x;
    //
    // return v2x * normal1Worldx + v2y * normal1Worldy;
    // end inline
  }

  // djm pooling, and from above
  private final Vec2 temp = new Vec2();
  private final Vec2 dLocal1 = new Vec2();

  /**
   * Find the max separation between poly1 and poly2 using edge normals from poly1.
   * 
   * @param edgeIndex
   * @param poly1
   * @param xf1
   * @param poly2
   * @param xf2
   * @return
   */
  public final void findMaxSeparation(EdgeResults results, final PolygonShape poly1,
      final Transform xf1, final PolygonShape poly2, final Transform xf2) {
    int count1 = poly1.m_count;
    final Vec2[] normals1 = poly1.m_normals;
//    Vec2 v = poly2.m_centroid;

    // Vector pointing from the centroid of poly1 to the centroid of poly2.
    // before inline:
    Transform.mulToOutUnsafe(xf2, poly2.m_centroid, d);
    Transform.mulToOutUnsafe(xf1, poly1.m_centroid, temp);
    d.subLocal(temp);

    Rot.mulTransUnsafe(xf1.q, d, dLocal1);
    final float dLocal1x = dLocal1.x;
    final float dLocal1y = dLocal1.y;
    // after inline:
    // final float predy = xf2.p.y + xf2.q.ex.y * v.x + xf2.q.ey.y * v.y;
    // final float predx = xf2.p.x + xf2.q.ex.x * v.x + xf2.q.ey.x * v.y;
    // final Vec2 v1 = poly1.m_centroid;
    // final float tempy = xf1.p.y + xf1.q.ex.y * v1.x + xf1.q.ey.y * v1.y;
    // final float tempx = xf1.p.x + xf1.q.ex.x * v1.x + xf1.q.ey.x * v1.y;
    // final float dx = predx - tempx;
    // final float dy = predy - tempy;
    //
    // final Mat22 R = xf1.q;
    // final float dLocal1x = dx * R.ex.x + dy * R.ex.y;
    // final float dLocal1y = dx * R.ey.x + dy * R.ey.y;
    // end inline

    // Find edge normal on poly1 that has the largest projection onto d.
    int edge = 0;
    float dot;
    float maxDot = Float.MIN_VALUE;
    for (int i = 0; i < count1; i++) {
      final Vec2 normal = normals1[i];
      dot = normal.x * dLocal1x + normal.y * dLocal1y;
      if (dot > maxDot) {
        maxDot = dot;
        edge = i;
      }
    }

    // Get the separation for the edge normal.
    float s = edgeSeparation(poly1, xf1, edge, poly2, xf2);

    // Check the separation for the previous edge normal.
    int prevEdge = edge - 1 >= 0 ? edge - 1 : count1 - 1;
    float sPrev = edgeSeparation(poly1, xf1, prevEdge, poly2, xf2);

    // Check the separation for the next edge normal.
    int nextEdge = edge + 1 < count1 ? edge + 1 : 0;
    float sNext = edgeSeparation(poly1, xf1, nextEdge, poly2, xf2);

    // Find the best edge and the search direction.
    int bestEdge;
    float bestSeparation;
    int increment;
    if (sPrev > s && sPrev > sNext) {
      increment = -1;
      bestEdge = prevEdge;
      bestSeparation = sPrev;
    } else if (sNext > s) {
      increment = 1;
      bestEdge = nextEdge;
      bestSeparation = sNext;
    } else {
      results.edgeIndex = edge;
      results.separation = s;
      return;
    }

    // Perform a local search for the best edge normal.
    for (;;) {
      if (increment == -1) {
        edge = bestEdge - 1 >= 0 ? bestEdge - 1 : count1 - 1;
      } else {
        edge = bestEdge + 1 < count1 ? bestEdge + 1 : 0;
      }

      s = edgeSeparation(poly1, xf1, edge, poly2, xf2);

      if (s > bestSeparation) {
        bestEdge = edge;
        bestSeparation = s;
      } else {
        break;
      }
    }

    results.edgeIndex = bestEdge;
    results.separation = bestSeparation;
  }

  // djm pooling from above
  public final void findIncidentEdge(final ClipVertex[] c, final PolygonShape poly1,
      final Transform xf1, int edge1, final PolygonShape poly2, final Transform xf2) {
    int count1 = poly1.m_count;
    final Vec2[] normals1 = poly1.m_normals;

    int count2 = poly2.m_count;
    final Vec2[] vertices2 = poly2.m_vertices;
    final Vec2[] normals2 = poly2.m_normals;

    assert (0 <= edge1 && edge1 < count1);

    // Get the normal of the reference edge in poly2's frame.
    Rot.mulToOutUnsafe(xf1.q, normals1[edge1], normal1); // temporary
    // Vec2 normal1 = MulT(xf2.R, Mul(xf1.R, normals1[edge1]));
    Rot.mulTrans(xf2.q, normal1, normal1);

    // Find the incident edge on poly2.
    int index = 0;
    float minDot = Float.MAX_VALUE;
    for (int i = 0; i < count2; ++i) {
      float dot = Vec2.dot(normal1, normals2[i]);
      if (dot < minDot) {
        minDot = dot;
        index = i;
      }
    }

    // Build the clip vertices for the incident edge.
    int i1 = index;
    int i2 = i1 + 1 < count2 ? i1 + 1 : 0;

    Transform.mulToOutUnsafe(xf2, vertices2[i1], c[0].v); // = Mul(xf2, vertices2[i1]);
    c[0].id.indexA = (byte) edge1;
    c[0].id.indexB = (byte) i1;
    c[0].id.typeA = (byte) ContactID.Type.FACE.ordinal();
    c[0].id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

    Transform.mulToOutUnsafe(xf2, vertices2[i2], c[1].v); // = Mul(xf2, vertices2[i2]);
    c[1].id.indexA = (byte) edge1;
    c[1].id.indexB = (byte) i2;
    c[1].id.typeA = (byte) ContactID.Type.FACE.ordinal();
    c[1].id.typeB = (byte) ContactID.Type.VERTEX.ordinal();
  }

  private final EdgeResults results1 = new EdgeResults();
  private final EdgeResults results2 = new EdgeResults();
  private final ClipVertex[] incidentEdge = new ClipVertex[2];
  private final Vec2 localTangent = new Vec2();
  private final Vec2 localNormal = new Vec2();
  private final Vec2 planePoint = new Vec2();
  private final Vec2 tangent = new Vec2();
  private final Vec2 normal = new Vec2();
  private final Vec2 v11 = new Vec2();
  private final Vec2 v12 = new Vec2();
  private final ClipVertex[] clipPoints1 = new ClipVertex[2];
  private final ClipVertex[] clipPoints2 = new ClipVertex[2];

  /**
   * Compute the collision manifold between two polygons.
   * 
   * @param manifold
   * @param polygon1
   * @param xf1
   * @param polygon2
   * @param xf2
   */
  public final void collidePolygons(Manifold manifold, final PolygonShape polyA,
      final Transform xfA, final PolygonShape polyB, final Transform xfB) {
    // Find edge normal of max separation on A - return if separating axis is found
    // Find edge normal of max separation on B - return if separation axis is found
    // Choose reference edge as min(minA, minB)
    // Find incident edge
    // Clip

    // The normal points from 1 to 2

    manifold.pointCount = 0;
    float totalRadius = polyA.m_radius + polyB.m_radius;

    findMaxSeparation(results1, polyA, xfA, polyB, xfB);
    if (results1.separation > totalRadius) {
      return;
    }

    findMaxSeparation(results2, polyB, xfB, polyA, xfA);
    if (results2.separation > totalRadius) {
      return;
    }

    final PolygonShape poly1; // reference polygon
    final PolygonShape poly2; // incident polygon
    Transform xf1, xf2;
    int edge1; // reference edge
    boolean flip;
    final float k_relativeTol = 0.98f;
    final float k_absoluteTol = 0.001f;

    if (results2.separation > k_relativeTol * results1.separation + k_absoluteTol) {
      poly1 = polyB;
      poly2 = polyA;
      xf1 = xfB;
      xf2 = xfA;
      edge1 = results2.edgeIndex;
      manifold.type = ManifoldType.FACE_B;
      flip = true;
    } else {
      poly1 = polyA;
      poly2 = polyB;
      xf1 = xfA;
      xf2 = xfB;
      edge1 = results1.edgeIndex;
      manifold.type = ManifoldType.FACE_A;
      flip = false;
    }

    findIncidentEdge(incidentEdge, poly1, xf1, edge1, poly2, xf2);

    int count1 = poly1.m_count;
    final Vec2[] vertices1 = poly1.m_vertices;

    final int iv1 = edge1;
    final int iv2 = edge1 + 1 < count1 ? edge1 + 1 : 0;
    v11.set(vertices1[iv1]);
    v12.set(vertices1[iv2]);
    localTangent.set(v12).subLocal(v11);
    localTangent.normalize();

    Vec2.crossToOutUnsafe(localTangent, 1f, localNormal); // Vec2 localNormal = Vec2.cross(dv,
    // 1.0f);

    planePoint.set(v11).addLocal(v12).mulLocal(.5f); // Vec2 planePoint = 0.5f * (v11
    // + v12);

    Rot.mulToOutUnsafe(xf1.q, localTangent, tangent); // Vec2 sideNormal = Mul(xf1.R, v12
    // - v11);
    Vec2.crossToOutUnsafe(tangent, 1f, normal); // Vec2 frontNormal = Vec2.cross(sideNormal,
    // 1.0f);

    Transform.mulToOut(xf1, v11, v11);
    Transform.mulToOut(xf1, v12, v12);
    // v11 = Mul(xf1, v11);
    // v12 = Mul(xf1, v12);

    // Face offset
    float frontOffset = Vec2.dot(normal, v11);

    // Side offsets, extended by polytope skin thickness.
    float sideOffset1 = -Vec2.dot(tangent, v11) + totalRadius;
    float sideOffset2 = Vec2.dot(tangent, v12) + totalRadius;

    // Clip incident edge against extruded edge1 side edges.
    // ClipVertex clipPoints1[2];
    // ClipVertex clipPoints2[2];
    int np;

    // Clip to box side 1
    // np = ClipSegmentToLine(clipPoints1, incidentEdge, -sideNormal, sideOffset1);
    tangent.negateLocal();
    np = clipSegmentToLine(clipPoints1, incidentEdge, tangent, sideOffset1, iv1);
    tangent.negateLocal();

    if (np < 2) {
      return;
    }

    // Clip to negative box side 1
    np = clipSegmentToLine(clipPoints2, clipPoints1, tangent, sideOffset2, iv2);

    if (np < 2) {
      return;
    }

    // Now clipPoints2 contains the clipped points.
    manifold.localNormal.set(localNormal);
    manifold.localPoint.set(planePoint);

    int pointCount = 0;
    for (int i = 0; i < Settings.maxManifoldPoints; ++i) {
      float separation = Vec2.dot(normal, clipPoints2[i].v) - frontOffset;

      if (separation <= totalRadius) {
        ManifoldPoint cp = manifold.points[pointCount];
        Transform.mulTransToOut(xf2, clipPoints2[i].v, cp.localPoint);
        // cp.m_localPoint = MulT(xf2, clipPoints2[i].v);
        cp.id.set(clipPoints2[i].id);
        if (flip) {
          // Swap features
          cp.id.flip();
        }
        ++pointCount;
      }
    }

    manifold.pointCount = pointCount;
  }

  private final Vec2 Q = new Vec2();
  private final Vec2 e = new Vec2();
  private final ContactID cf = new ContactID();
  private final Vec2 e1 = new Vec2();
  private final Vec2 P = new Vec2();
  private final Vec2 n = new Vec2();

  // Compute contact points for edge versus circle.
  // This accounts for edge connectivity.
  public void collideEdgeAndCircle(Manifold manifold, final EdgeShape edgeA, final Transform xfA,
      final CircleShape circleB, final Transform xfB) {
    manifold.pointCount = 0;


    // Compute circle in frame of edge
    // Vec2 Q = MulT(xfA, Mul(xfB, circleB.m_p));
    Transform.mulToOutUnsafe(xfB, circleB.m_p, temp);
    Transform.mulTransToOutUnsafe(xfA, temp, Q);

    final Vec2 A = edgeA.m_vertex1;
    final Vec2 B = edgeA.m_vertex2;
    e.set(B).subLocal(A);

    // Barycentric coordinates
    float u = Vec2.dot(e, temp.set(B).subLocal(Q));
    float v = Vec2.dot(e, temp.set(Q).subLocal(A));

    float radius = edgeA.m_radius + circleB.m_radius;

    // ContactFeature cf;
    cf.indexB = 0;
    cf.typeB = (byte) ContactID.Type.VERTEX.ordinal();

    // Region A
    if (v <= 0.0f) {
      final Vec2 P = A;
      d.set(Q).subLocal(P);
      float dd = Vec2.dot(d, d);
      if (dd > radius * radius) {
        return;
      }

      // Is there an edge connected to A?
      if (edgeA.m_hasVertex0) {
        final Vec2 A1 = edgeA.m_vertex0;
        final Vec2 B1 = A;
        e1.set(B1).subLocal(A1);
        float u1 = Vec2.dot(e1, temp.set(B1).subLocal(Q));

        // Is the circle in Region AB of the previous edge?
        if (u1 > 0.0f) {
          return;
        }
      }

      cf.indexA = 0;
      cf.typeA = (byte) ContactID.Type.VERTEX.ordinal();
      manifold.pointCount = 1;
      manifold.type = Manifold.ManifoldType.CIRCLES;
      manifold.localNormal.setZero();
      manifold.localPoint.set(P);
      // manifold.points[0].id.key = 0;
      manifold.points[0].id.set(cf);
      manifold.points[0].localPoint.set(circleB.m_p);
      return;
    }

    // Region B
    if (u <= 0.0f) {
      Vec2 P = B;
      d.set(Q).subLocal(P);
      float dd = Vec2.dot(d, d);
      if (dd > radius * radius) {
        return;
      }

      // Is there an edge connected to B?
      if (edgeA.m_hasVertex3) {
        final Vec2 B2 = edgeA.m_vertex3;
        final Vec2 A2 = B;
        final Vec2 e2 = e1;
        e2.set(B2).subLocal(A2);
        float v2 = Vec2.dot(e2, temp.set(Q).subLocal(A2));

        // Is the circle in Region AB of the next edge?
        if (v2 > 0.0f) {
          return;
        }
      }

      cf.indexA = 1;
      cf.typeA = (byte) ContactID.Type.VERTEX.ordinal();
      manifold.pointCount = 1;
      manifold.type = Manifold.ManifoldType.CIRCLES;
      manifold.localNormal.setZero();
      manifold.localPoint.set(P);
      // manifold.points[0].id.key = 0;
      manifold.points[0].id.set(cf);
      manifold.points[0].localPoint.set(circleB.m_p);
      return;
    }

    // Region AB
    float den = Vec2.dot(e, e);
    assert (den > 0.0f);

    // Vec2 P = (1.0f / den) * (u * A + v * B);
    P.set(A).mulLocal(u).addLocal(temp.set(B).mulLocal(v));
    P.mulLocal(1.0f / den);
    d.set(Q).subLocal(P);
    float dd = Vec2.dot(d, d);
    if (dd > radius * radius) {
      return;
    }

    n.x = -e.y;
    n.y = e.x;
    if (Vec2.dot(n, temp.set(Q).subLocal(A)) < 0.0f) {
      n.set(-n.x, -n.y);
    }
    n.normalize();

    cf.indexA = 0;
    cf.typeA = (byte) ContactID.Type.FACE.ordinal();
    manifold.pointCount = 1;
    manifold.type = Manifold.ManifoldType.FACE_A;
    manifold.localNormal.set(n);
    manifold.localPoint.set(A);
    // manifold.points[0].id.key = 0;
    manifold.points[0].id.set(cf);
    manifold.points[0].localPoint.set(circleB.m_p);
  }

  private final EPCollider collider = new EPCollider();

  public void collideEdgeAndPolygon(Manifold manifold, final EdgeShape edgeA, final Transform xfA,
      final PolygonShape polygonB, final Transform xfB) {
    collider.collide(manifold, edgeA, xfA, polygonB, xfB);
  }



  /**
   * Java-specific class for returning edge results
   */
  private static class EdgeResults {
    public float separation;
    public int edgeIndex;
  }

  /**
   * Used for computing contact manifolds.
   */
  public static class ClipVertex {
    public final Vec2 v;
    public final ContactID id;

    public ClipVertex() {
      v = new Vec2();
      id = new ContactID();
    }

    public void set(final ClipVertex cv) {
      v.set(cv.v);
      id.set(cv.id);
    }
  }

  /**
   * This is used for determining the state of contact points.
   * 
   * @author Daniel Murphy
   */
  public static enum PointState {
    /**
     * point does not exist
     */
    NULL_STATE,
    /**
     * point was added in the update
     */
    ADD_STATE,
    /**
     * point persisted across the update
     */
    PERSIST_STATE,
    /**
     * point was removed in the update
     */
    REMOVE_STATE
  }

  /**
   * This structure is used to keep track of the best separating axis.
   */
  static class EPAxis {
    enum Type {
      UNKNOWN, EDGE_A, EDGE_B
    }

    Type type;
    int index;
    float separation;
  }

  /**
   * This holds polygon B expressed in frame A.
   */
  static class TempPolygon {
    final Vec2[] vertices = new Vec2[Settings.maxPolygonVertices];
    final Vec2[] normals = new Vec2[Settings.maxPolygonVertices];
    int count;
    
    public TempPolygon() {
      for(int i=0; i<vertices.length; i++) {
        vertices[i] = new Vec2();
        normals[i] = new Vec2();
      }
    }
  }

  /**
   * Reference face used for clipping
   */
  static class ReferenceFace {
    int i1, i2;
    final Vec2 v1 = new Vec2();
    final Vec2 v2 = new Vec2();
    final Vec2 normal = new Vec2();

    final Vec2 sideNormal1 = new Vec2();
    float sideOffset1;

    final Vec2 sideNormal2 = new Vec2();
    float sideOffset2;
  }

  /**
   * This class collides and edge and a polygon, taking into account edge adjacency.
   */
  static class EPCollider {
    enum VertexType {
      ISOLATED, CONCAVE, CONVEX
    }

    final TempPolygon m_polygonB = new TempPolygon();

    final Transform m_xf = new Transform();
    final Vec2 m_centroidB = new Vec2();
    Vec2 m_v0 = new Vec2();
    Vec2 m_v1 = new Vec2();
    Vec2 m_v2 = new Vec2();
    Vec2 m_v3 = new Vec2();
    final Vec2 m_normal0 = new Vec2();
    final Vec2 m_normal1 = new Vec2();
    final Vec2 m_normal2 = new Vec2();
    final Vec2 m_normal = new Vec2();

    VertexType m_type1, m_type2;

    final Vec2 m_lowerLimit = new Vec2();
    final Vec2 m_upperLimit = new Vec2();
    float m_radius;
    boolean m_front;

    public EPCollider() {
      for(int i=0; i<2; i++) {
        ie[i] = new ClipVertex();
        clipPoints1[i] = new ClipVertex();
        clipPoints2[i] = new ClipVertex();
      }
    }

    private final Vec2 edge1 = new Vec2();
    private final Vec2 temp = new Vec2();
    private final Vec2 edge0 = new Vec2();
    private final Vec2 edge2 = new Vec2();
    private final ClipVertex[] ie = new ClipVertex[2];
    private final ClipVertex[] clipPoints1 = new ClipVertex[2];
    private final ClipVertex[] clipPoints2 = new ClipVertex[2];
    private final ReferenceFace rf = new ReferenceFace();
    private final EPAxis edgeAxis = new EPAxis();
    private final EPAxis polygonAxis = new EPAxis();

    public void collide(Manifold manifold, final EdgeShape edgeA, final Transform xfA,
        final PolygonShape polygonB, final Transform xfB) {

      Transform.mulTransToOutUnsafe(xfA, xfB, m_xf);
      Transform.mulToOutUnsafe(m_xf, polygonB.m_centroid, m_centroidB);

      m_v0 = edgeA.m_vertex0;
      m_v1 = edgeA.m_vertex1;
      m_v2 = edgeA.m_vertex2;
      m_v3 = edgeA.m_vertex3;

      boolean hasVertex0 = edgeA.m_hasVertex0;
      boolean hasVertex3 = edgeA.m_hasVertex3;

      edge1.set(m_v2).subLocal(m_v1);
      edge1.normalize();
      m_normal1.set(edge1.y, -edge1.x);
      float offset1 = Vec2.dot(m_normal1, temp.set(m_centroidB).subLocal(m_v1));
      float offset0 = 0.0f, offset2 = 0.0f;
      boolean convex1 = false, convex2 = false;

      // Is there a preceding edge?
      if (hasVertex0) {
        edge0.set(m_v1).subLocal(m_v0);
        edge0.normalize();
        m_normal0.set(edge0.y, -edge0.x);
        convex1 = Vec2.cross(edge0, edge1) >= 0.0f;
        offset0 = Vec2.dot(m_normal0, temp.set(m_centroidB).subLocal(m_v0));
      }

      // Is there a following edge?
      if (hasVertex3) {
        edge2.set(m_v3).subLocal(m_v2);
        edge2.normalize();
        m_normal2.set(edge2.y, -edge2.x);
        convex2 = Vec2.cross(edge1, edge2) > 0.0f;
        offset2 = Vec2.dot(m_normal2, temp.set(m_centroidB).subLocal(m_v2));
      }

      // Determine front or back collision. Determine collision normal limits.
      if (hasVertex0 && hasVertex3) {
        if (convex1 && convex2) {
          m_front = offset0 >= 0.0f || offset1 >= 0.0f || offset2 >= 0.0f;
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal0);
            m_upperLimit.set(m_normal2);
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal1).negateLocal();
            m_upperLimit.set(m_normal1).negateLocal();
          }
        } else if (convex1) {
          m_front = offset0 >= 0.0f || (offset1 >= 0.0f && offset2 >= 0.0f);
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal0);
            m_upperLimit.set(m_normal1);
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal2).negateLocal();
            m_upperLimit.set(m_normal1).negateLocal();
          }
        } else if (convex2) {
          m_front = offset2 >= 0.0f || (offset0 >= 0.0f && offset1 >= 0.0f);
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal1);
            m_upperLimit.set(m_normal2);
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal1).negateLocal();
            m_upperLimit.set(m_normal0).negateLocal();
          }
        } else {
          m_front = offset0 >= 0.0f && offset1 >= 0.0f && offset2 >= 0.0f;
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal1);
            m_upperLimit.set(m_normal1);
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal2).negateLocal();
            m_upperLimit.set(m_normal0).negateLocal();
          }
        }
      } else if (hasVertex0) {
        if (convex1) {
          m_front = offset0 >= 0.0f || offset1 >= 0.0f;
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal0);
            m_upperLimit.set(m_normal1).negateLocal();
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal1);
            m_upperLimit.set(m_normal1).negateLocal();
          }
        } else {
          m_front = offset0 >= 0.0f && offset1 >= 0.0f;
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal1);
            m_upperLimit.set(m_normal1).negateLocal();
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal1);
            m_upperLimit.set(m_normal0).negateLocal();
          }
        }
      } else if (hasVertex3) {
        if (convex2) {
          m_front = offset1 >= 0.0f || offset2 >= 0.0f;
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal1).negateLocal();
            m_upperLimit.set(m_normal2);
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal1).negateLocal();
            m_upperLimit.set(m_normal1);
          }
        } else {
          m_front = offset1 >= 0.0f && offset2 >= 0.0f;
          if (m_front) {
            m_normal.set(m_normal1);
            m_lowerLimit.set(m_normal1).negateLocal();
            m_upperLimit.set(m_normal1);
          } else {
            m_normal.set(m_normal1).negateLocal();
            m_lowerLimit.set(m_normal2).negateLocal();
            m_upperLimit.set(m_normal1);
          }
        }
      } else {
        m_front = offset1 >= 0.0f;
        if (m_front) {
          m_normal.set(m_normal1);
          m_lowerLimit.set(m_normal1).negateLocal();
          m_upperLimit.set(m_normal1).negateLocal();
        } else {
          m_normal.set(m_normal1).negateLocal();
          m_lowerLimit.set(m_normal1);
          m_upperLimit.set(m_normal1);
        }
      }

      // Get polygonB in frameA
      m_polygonB.count = polygonB.m_count;
      for (int i = 0; i < polygonB.m_count; ++i) {
        Transform.mulToOutUnsafe(m_xf, polygonB.m_vertices[i], m_polygonB.vertices[i]);
        Rot.mulToOutUnsafe(m_xf.q, polygonB.m_normals[i], m_polygonB.normals[i]);
      }

      m_radius = 2.0f * Settings.polygonRadius;

      manifold.pointCount = 0;

      computeEdgeSeparation(edgeAxis);

      // If no valid normal can be found than this edge should not collide.
      if (edgeAxis.type == EPAxis.Type.UNKNOWN) {
        return;
      }

      if (edgeAxis.separation > m_radius) {
        return;
      }

      computePolygonSeparation(polygonAxis);
      if (polygonAxis.type != EPAxis.Type.UNKNOWN && polygonAxis.separation > m_radius) {
        return;
      }

      // Use hysteresis for jitter reduction.
      final float k_relativeTol = 0.98f;
      final float k_absoluteTol = 0.001f;

      EPAxis primaryAxis;
      if (polygonAxis.type == EPAxis.Type.UNKNOWN) {
        primaryAxis = edgeAxis;
      } else if (polygonAxis.separation > k_relativeTol * edgeAxis.separation + k_absoluteTol) {
        primaryAxis = polygonAxis;
      } else {
        primaryAxis = edgeAxis;
      }

      // ClipVertex[] ie = new ClipVertex[2];
      if (primaryAxis.type == EPAxis.Type.EDGE_A) {
        manifold.type = Manifold.ManifoldType.FACE_A;

        // Search for the polygon normal that is most anti-parallel to the edge normal.
        int bestIndex = 0;
        float bestValue = Vec2.dot(m_normal, m_polygonB.normals[0]);
        for (int i = 1; i < m_polygonB.count; ++i) {
          float value = Vec2.dot(m_normal, m_polygonB.normals[i]);
          if (value < bestValue) {
            bestValue = value;
            bestIndex = i;
          }
        }

        int i1 = bestIndex;
        int i2 = i1 + 1 < m_polygonB.count ? i1 + 1 : 0;

        ie[0].v.set(m_polygonB.vertices[i1]);
        ie[0].id.indexA = 0;
        ie[0].id.indexB = (byte) i1;
        ie[0].id.typeA = (byte) ContactID.Type.FACE.ordinal();
        ie[0].id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

        ie[1].v.set(m_polygonB.vertices[i2]);
        ie[1].id.indexA = 0;
        ie[1].id.indexB = (byte) i2;
        ie[1].id.typeA = (byte) ContactID.Type.FACE.ordinal();
        ie[1].id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

        if (m_front) {
          rf.i1 = 0;
          rf.i2 = 1;
          rf.v1.set(m_v1);
          rf.v2.set(m_v2);
          rf.normal.set(m_normal1);
        } else {
          rf.i1 = 1;
          rf.i2 = 0;
          rf.v1.set(m_v2);
          rf.v2.set(m_v1);
          rf.normal.set(m_normal1).negateLocal();
        }
      } else {
        manifold.type = Manifold.ManifoldType.FACE_B;

        ie[0].v.set(m_v1);
        ie[0].id.indexA = 0;
        ie[0].id.indexB = (byte) primaryAxis.index;
        ie[0].id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
        ie[0].id.typeB = (byte) ContactID.Type.FACE.ordinal();

        ie[1].v.set(m_v2);
        ie[1].id.indexA = 0;
        ie[1].id.indexB = (byte) primaryAxis.index;
        ie[1].id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
        ie[1].id.typeB = (byte) ContactID.Type.FACE.ordinal();

        rf.i1 = primaryAxis.index;
        rf.i2 = rf.i1 + 1 < m_polygonB.count ? rf.i1 + 1 : 0;
        rf.v1.set(m_polygonB.vertices[rf.i1]);
        rf.v2.set(m_polygonB.vertices[rf.i2]);
        rf.normal.set(m_polygonB.normals[rf.i1]);
      }

      rf.sideNormal1.set(rf.normal.y, -rf.normal.x);
      rf.sideNormal2.set(rf.sideNormal1).negateLocal();
      rf.sideOffset1 = Vec2.dot(rf.sideNormal1, rf.v1);
      rf.sideOffset2 = Vec2.dot(rf.sideNormal2, rf.v2);

      // Clip incident edge against extruded edge1 side edges.
      int np;

      // Clip to box side 1
      np = clipSegmentToLine(clipPoints1, ie, rf.sideNormal1, rf.sideOffset1, rf.i1);

      if (np < Settings.maxManifoldPoints) {
        return;
      }

      // Clip to negative box side 1
      np = clipSegmentToLine(clipPoints2, clipPoints1, rf.sideNormal2, rf.sideOffset2, rf.i2);

      if (np < Settings.maxManifoldPoints) {
        return;
      }

      // Now clipPoints2 contains the clipped points.
      if (primaryAxis.type == EPAxis.Type.EDGE_A) {
        manifold.localNormal.set(rf.normal);
        manifold.localPoint.set(rf.v1);
      } else {
        manifold.localNormal.set(polygonB.m_normals[rf.i1]);
        manifold.localPoint.set(polygonB.m_vertices[rf.i1]);
      }

      int pointCount = 0;
      for (int i = 0; i < Settings.maxManifoldPoints; ++i) {
        float separation;

        separation = Vec2.dot(rf.normal, temp.set(clipPoints2[i].v).subLocal(rf.v1));

        if (separation <= m_radius) {
          ManifoldPoint cp = manifold.points[pointCount];

          if (primaryAxis.type == EPAxis.Type.EDGE_A) {
            // cp.localPoint = MulT(m_xf, clipPoints2[i].v);
            Transform.mulTransToOutUnsafe(m_xf, clipPoints2[i].v, cp.localPoint);
            cp.id.set(clipPoints2[i].id);
          } else {
            cp.localPoint.set(clipPoints2[i].v);
            cp.id.typeA = clipPoints2[i].id.typeB;
            cp.id.typeB = clipPoints2[i].id.typeA;
            cp.id.indexA = clipPoints2[i].id.indexB;
            cp.id.indexB = clipPoints2[i].id.indexA;
          }

          ++pointCount;
        }
      }

      manifold.pointCount = pointCount;
    }


    public void computeEdgeSeparation(EPAxis axis) {
      axis.type = EPAxis.Type.EDGE_A;
      axis.index = m_front ? 0 : 1;
      axis.separation = Float.MAX_VALUE;

      for (int i = 0; i < m_polygonB.count; ++i) {
        float s = Vec2.dot(m_normal, temp.set(m_polygonB.vertices[i]).subLocal(m_v1));
        if (s < axis.separation) {
          axis.separation = s;
        }
      }
    }

    private final Vec2 perp = new Vec2();
    private final Vec2 n = new Vec2();

    public void computePolygonSeparation(EPAxis axis) {
      axis.type = EPAxis.Type.UNKNOWN;
      axis.index = -1;
      axis.separation = Float.MIN_VALUE;

      perp.set(-m_normal.y, m_normal.x);

      for (int i = 0; i < m_polygonB.count; ++i) {
        n.set(m_polygonB.normals[i]).negateLocal();

        float s1 = Vec2.dot(n, temp.set(m_polygonB.vertices[i]).subLocal(m_v1));
        float s2 = Vec2.dot(n, temp.set(m_polygonB.vertices[i]).subLocal(m_v2));
        float s = MathUtils.min(s1, s2);

        if (s > m_radius) {
          // No collision
          axis.type = EPAxis.Type.EDGE_B;
          axis.index = i;
          axis.separation = s;
          return;
        }

        // Adjacency
        if (Vec2.dot(n, perp) >= 0.0f) {
          if (Vec2.dot(temp.set(n).subLocal(m_upperLimit), m_normal) < -Settings.angularSlop) {
            continue;
          }
        } else {
          if (Vec2.dot(temp.set(n).subLocal(m_lowerLimit), m_normal) < -Settings.angularSlop) {
            continue;
          }
        }

        if (s > axis.separation) {
          axis.type = EPAxis.Type.EDGE_B;
          axis.index = i;
          axis.separation = s;
        }
      }
    }
  }
}
