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
package org.jbox2d.collision;

import org.jbox2d.collision.Distance.SimplexCache;
import org.jbox2d.collision.Manifold.ManifoldType;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
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
    final ClipVertex vIn0 = vIn[0];
    final ClipVertex vIn1 = vIn[1];
    final Vec2 vIn0v = vIn0.v;
    final Vec2 vIn1v = vIn1.v;

    // Calculate the distance of end points to the line
    float distance0 = Vec2.dot(normal, vIn0v) - offset;
    float distance1 = Vec2.dot(normal, vIn1v) - offset;

    // If the points are behind the plane
    if (distance0 <= 0.0f) {
      vOut[numOut++].set(vIn0);
    }
    if (distance1 <= 0.0f) {
      vOut[numOut++].set(vIn1);
    }

    // If the points are on different sides of the plane
    if (distance0 * distance1 < 0.0f) {
      // Find intersection point of edge and plane
      float interp = distance0 / (distance0 - distance1);

      ClipVertex vOutNO = vOut[numOut];
      // vOut[numOut].v = vIn[0].v + interp * (vIn[1].v - vIn[0].v);
      vOutNO.v.x = vIn0v.x + interp * (vIn1v.x - vIn0v.x);
      vOutNO.v.y = vIn0v.y + interp * (vIn1v.y - vIn0v.y);

      // VertexA is hitting edgeB.
      vOutNO.id.indexA = (byte) vertexIndexA;
      vOutNO.id.indexB = vIn0.id.indexB;
      vOutNO.id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
      vOutNO.id.typeB = (byte) ContactID.Type.FACE.ordinal();
      ++numOut;
    }

    return numOut;
  }

  // #### COLLISION STUFF (not from collision.h or collision.cpp) ####

  // djm pooling
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
    // Transform.mulToOut(xfA, circle1.m_p, pA);
    // Transform.mulToOut(xfB, circle2.m_p, pB);
    // d.set(pB).subLocal(pA);
    // float distSqr = d.x * d.x + d.y * d.y;

    // after inline:
    Vec2 circle1p = circle1.m_p;
    Vec2 circle2p = circle2.m_p;
    float pAx = (xfA.q.c * circle1p.x - xfA.q.s * circle1p.y) + xfA.p.x;
    float pAy = (xfA.q.s * circle1p.x + xfA.q.c * circle1p.y) + xfA.p.y;
    float pBx = (xfB.q.c * circle2p.x - xfB.q.s * circle2p.y) + xfB.p.x;
    float pBy = (xfB.q.s * circle2p.x + xfB.q.c * circle2p.y) + xfB.p.y;
    float dx = pBx - pAx;
    float dy = pBy - pAy;
    float distSqr = dx * dx + dy * dy;
    // end inline

    final float radius = circle1.m_radius + circle2.m_radius;
    if (distSqr > radius * radius) {
      return;
    }

    manifold.type = ManifoldType.CIRCLES;
    manifold.localPoint.set(circle1p);
    manifold.localNormal.setZero();
    manifold.pointCount = 1;

    manifold.points[0].localPoint.set(circle2p);
    manifold.points[0].id.zero();
  }

  // djm pooling, and from above

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
    // Vec2 v = circle.m_p;

    // Compute circle position in the frame of the polygon.
    // before inline:
    // Transform.mulToOutUnsafe(xfB, circle.m_p, c);
    // Transform.mulTransToOut(xfA, c, cLocal);
    // final float cLocalx = cLocal.x;
    // final float cLocaly = cLocal.y;
    // after inline:
    final Vec2 circlep = circle.m_p;
    final Rot xfBq = xfB.q;
    final Rot xfAq = xfA.q;
    final float cx = (xfBq.c * circlep.x - xfBq.s * circlep.y) + xfB.p.x;
    final float cy = (xfBq.s * circlep.x + xfBq.c * circlep.y) + xfB.p.y;
    final float px = cx - xfA.p.x;
    final float py = cy - xfA.p.y;
    final float cLocalx = (xfAq.c * px + xfAq.s * py);
    final float cLocaly = (-xfAq.s * px + xfAq.c * py);
    // end inline

    // Find the min separating edge.
    int normalIndex = 0;
    float separation = -Float.MAX_VALUE;
    final float radius = polygon.m_radius + circle.m_radius;
    final int vertexCount = polygon.m_count;
    float s;
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
      s = normals[i].x * tempx + normals[i].y * tempy;


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
      mpoint.localPoint.x = circlep.x;
      mpoint.localPoint.y = circlep.y;
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
      manifold.points[0].localPoint.set(circlep);
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
      manifold.points[0].localPoint.set(circlep);
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
      manifold.points[0].localPoint.set(circlep);
      manifold.points[0].id.zero();
    }
  }

  // djm pooling, and from above
  private final Vec2 temp = new Vec2();
  private final Transform xf = new Transform();
  private final Vec2 n = new Vec2();
  private final Vec2 v1 = new Vec2();

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
    int count2 = poly2.m_count;
    Vec2[] n1s = poly1.m_normals;
    Vec2[] v1s = poly1.m_vertices;
    Vec2[] v2s = poly2.m_vertices;
    
    Transform.mulTransToOutUnsafe(xf2, xf1, xf);
    final Rot xfq = xf.q;

    int bestIndex = 0;
    float maxSeparation = -Float.MAX_VALUE;
    for (int i = 0; i < count1; i++) {
      // Get poly1 normal in frame2.
      Rot.mulToOutUnsafe(xfq, n1s[i], n);
      Transform.mulToOutUnsafe(xf, v1s[i], v1);

      // Find deepest point for normal i.
      float si = Float.MAX_VALUE;
      for (int j = 0; j < count2; ++j) {
        Vec2 v2sj = v2s[j];
        float sij = n.x * (v2sj.x - v1.x) + n.y * (v2sj.y - v1.y);
        if (sij < si) {
          si = sij;
        }
      }
      
      if (si > maxSeparation) {
        maxSeparation = si;
        bestIndex = i;
      }
    }

    results.edgeIndex = bestIndex;
    results.separation = maxSeparation;
  }

  public final void findIncidentEdge(final ClipVertex[] c, final PolygonShape poly1,
      final Transform xf1, int edge1, final PolygonShape poly2, final Transform xf2) {
    int count1 = poly1.m_count;
    final Vec2[] normals1 = poly1.m_normals;

    int count2 = poly2.m_count;
    final Vec2[] vertices2 = poly2.m_vertices;
    final Vec2[] normals2 = poly2.m_normals;

    assert (0 <= edge1 && edge1 < count1);

    final ClipVertex c0 = c[0];
    final ClipVertex c1 = c[1];
    final Rot xf1q = xf1.q;
    final Rot xf2q = xf2.q;

    // Get the normal of the reference edge in poly2's frame.
    // Vec2 normal1 = MulT(xf2.R, Mul(xf1.R, normals1[edge1]));
    // before inline:
    // Rot.mulToOutUnsafe(xf1.q, normals1[edge1], normal1); // temporary
    // Rot.mulTrans(xf2.q, normal1, normal1);
    // after inline:
    final Vec2 v = normals1[edge1];
    final float tempx = xf1q.c * v.x - xf1q.s * v.y;
    final float tempy = xf1q.s * v.x + xf1q.c * v.y;
    final float normal1x = xf2q.c * tempx + xf2q.s * tempy;
    final float normal1y = -xf2q.s * tempx + xf2q.c * tempy;

    // end inline

    // Find the incident edge on poly2.
    int index = 0;
    float minDot = Float.MAX_VALUE;
    for (int i = 0; i < count2; ++i) {
      Vec2 b = normals2[i];
      float dot = normal1x * b.x + normal1y * b.y;
      if (dot < minDot) {
        minDot = dot;
        index = i;
      }
    }

    // Build the clip vertices for the incident edge.
    int i1 = index;
    int i2 = i1 + 1 < count2 ? i1 + 1 : 0;

    // c0.v = Mul(xf2, vertices2[i1]);
    Vec2 v1 = vertices2[i1];
    Vec2 out = c0.v;
    out.x = (xf2q.c * v1.x - xf2q.s * v1.y) + xf2.p.x;
    out.y = (xf2q.s * v1.x + xf2q.c * v1.y) + xf2.p.y;
    c0.id.indexA = (byte) edge1;
    c0.id.indexB = (byte) i1;
    c0.id.typeA = (byte) ContactID.Type.FACE.ordinal();
    c0.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

    // c1.v = Mul(xf2, vertices2[i2]);
    Vec2 v2 = vertices2[i2];
    Vec2 out1 = c1.v;
    out1.x = (xf2q.c * v2.x - xf2q.s * v2.y) + xf2.p.x;
    out1.y = (xf2q.s * v2.x + xf2q.c * v2.y) + xf2.p.y;
    c1.id.indexA = (byte) edge1;
    c1.id.indexB = (byte) i2;
    c1.id.typeA = (byte) ContactID.Type.FACE.ordinal();
    c1.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();
  }

  private final EdgeResults results1 = new EdgeResults();
  private final EdgeResults results2 = new EdgeResults();
  private final ClipVertex[] incidentEdge = new ClipVertex[2];
  private final Vec2 localTangent = new Vec2();
  private final Vec2 localNormal = new Vec2();
  private final Vec2 planePoint = new Vec2();
  private final Vec2 tangent = new Vec2();
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

    final PolygonShape poly1;  // reference polygon
    final PolygonShape poly2;  // incident polygon
    Transform xf1, xf2;
    int edge1;                 // reference edge
    boolean flip;
    final float k_tol = 0.1f * Settings.linearSlop;

    if (results2.separation > results1.separation + k_tol) {
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
    final Rot xf1q = xf1.q;

    findIncidentEdge(incidentEdge, poly1, xf1, edge1, poly2, xf2);

    int count1 = poly1.m_count;
    final Vec2[] vertices1 = poly1.m_vertices;

    final int iv1 = edge1;
    final int iv2 = edge1 + 1 < count1 ? edge1 + 1 : 0;
    v11.set(vertices1[iv1]);
    v12.set(vertices1[iv2]);
    localTangent.x = v12.x - v11.x;
    localTangent.y = v12.y - v11.y;
    localTangent.normalize();

    // Vec2 localNormal = Vec2.cross(dv, 1.0f);
    localNormal.x = 1f * localTangent.y;
    localNormal.y = -1f * localTangent.x;

    // Vec2 planePoint = 0.5f * (v11+ v12);
    planePoint.x = (v11.x + v12.x) * .5f;
    planePoint.y = (v11.y + v12.y) * .5f;

    // Rot.mulToOutUnsafe(xf1.q, localTangent, tangent);
    tangent.x = xf1q.c * localTangent.x - xf1q.s * localTangent.y;
    tangent.y = xf1q.s * localTangent.x + xf1q.c * localTangent.y;

    // Vec2.crossToOutUnsafe(tangent, 1f, normal);
    final float normalx = 1f * tangent.y;
    final float normaly = -1f * tangent.x;


    Transform.mulToOut(xf1, v11, v11);
    Transform.mulToOut(xf1, v12, v12);
    // v11 = Mul(xf1, v11);
    // v12 = Mul(xf1, v12);

    // Face offset
    // float frontOffset = Vec2.dot(normal, v11);
    float frontOffset = normalx * v11.x + normaly * v11.y;

    // Side offsets, extended by polytope skin thickness.
    // float sideOffset1 = -Vec2.dot(tangent, v11) + totalRadius;
    // float sideOffset2 = Vec2.dot(tangent, v12) + totalRadius;
    float sideOffset1 = -(tangent.x * v11.x + tangent.y * v11.y) + totalRadius;
    float sideOffset2 = tangent.x * v12.x + tangent.y * v12.y + totalRadius;

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
      // float separation = Vec2.dot(normal, clipPoints2[i].v) - frontOffset;
      float separation = normalx * clipPoints2[i].v.x + normaly * clipPoints2[i].v.y - frontOffset;

      if (separation <= totalRadius) {
        ManifoldPoint cp = manifold.points[pointCount];
        // cp.m_localPoint = MulT(xf2, clipPoints2[i].v);
        Vec2 out = cp.localPoint;
        final float px = clipPoints2[i].v.x - xf2.p.x;
        final float py = clipPoints2[i].v.y - xf2.p.y;
        out.x = (xf2.q.c * px + xf2.q.s * py);
        out.y = (-xf2.q.s * px + xf2.q.c * py);
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
      Vec2 v1 = cv.v;
      v.x = v1.x;
      v.y = v1.y;
      ContactID c = cv.id;
      id.indexA = c.indexA;
      id.indexB = c.indexB;
      id.typeA = c.typeA;
      id.typeB = c.typeB;
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
      for (int i = 0; i < vertices.length; i++) {
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
      for (int i = 0; i < 2; i++) {
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
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = m_normal0.x;
            m_lowerLimit.y = m_normal0.y;
            m_upperLimit.x = m_normal2.x;
            m_upperLimit.y = m_normal2.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = -m_normal1.x;
            m_lowerLimit.y = -m_normal1.y;
            m_upperLimit.x = -m_normal1.x;
            m_upperLimit.y = -m_normal1.y;
          }
        } else if (convex1) {
          m_front = offset0 >= 0.0f || (offset1 >= 0.0f && offset2 >= 0.0f);
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = m_normal0.x;
            m_lowerLimit.y = m_normal0.y;
            m_upperLimit.x = m_normal1.x;
            m_upperLimit.y = m_normal1.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = -m_normal2.x;
            m_lowerLimit.y = -m_normal2.y;
            m_upperLimit.x = -m_normal1.x;
            m_upperLimit.y = -m_normal1.y;
          }
        } else if (convex2) {
          m_front = offset2 >= 0.0f || (offset0 >= 0.0f && offset1 >= 0.0f);
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = m_normal1.x;
            m_lowerLimit.y = m_normal1.y;
            m_upperLimit.x = m_normal2.x;
            m_upperLimit.y = m_normal2.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = -m_normal1.x;
            m_lowerLimit.y = -m_normal1.y;
            m_upperLimit.x = -m_normal0.x;
            m_upperLimit.y = -m_normal0.y;
          }
        } else {
          m_front = offset0 >= 0.0f && offset1 >= 0.0f && offset2 >= 0.0f;
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = m_normal1.x;
            m_lowerLimit.y = m_normal1.y;
            m_upperLimit.x = m_normal1.x;
            m_upperLimit.y = m_normal1.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = -m_normal2.x;
            m_lowerLimit.y = -m_normal2.y;
            m_upperLimit.x = -m_normal0.x;
            m_upperLimit.y = -m_normal0.y;
          }
        }
      } else if (hasVertex0) {
        if (convex1) {
          m_front = offset0 >= 0.0f || offset1 >= 0.0f;
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = m_normal0.x;
            m_lowerLimit.y = m_normal0.y;
            m_upperLimit.x = -m_normal1.x;
            m_upperLimit.y = -m_normal1.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = m_normal1.x;
            m_lowerLimit.y = m_normal1.y;
            m_upperLimit.x = -m_normal1.x;
            m_upperLimit.y = -m_normal1.y;
          }
        } else {
          m_front = offset0 >= 0.0f && offset1 >= 0.0f;
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = m_normal1.x;
            m_lowerLimit.y = m_normal1.y;
            m_upperLimit.x = -m_normal1.x;
            m_upperLimit.y = -m_normal1.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = m_normal1.x;
            m_lowerLimit.y = m_normal1.y;
            m_upperLimit.x = -m_normal0.x;
            m_upperLimit.y = -m_normal0.y;
          }
        }
      } else if (hasVertex3) {
        if (convex2) {
          m_front = offset1 >= 0.0f || offset2 >= 0.0f;
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = -m_normal1.x;
            m_lowerLimit.y = -m_normal1.y;
            m_upperLimit.x = m_normal2.x;
            m_upperLimit.y = m_normal2.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = -m_normal1.x;
            m_lowerLimit.y = -m_normal1.y;
            m_upperLimit.x = m_normal1.x;
            m_upperLimit.y = m_normal1.y;
          }
        } else {
          m_front = offset1 >= 0.0f && offset2 >= 0.0f;
          if (m_front) {
            m_normal.x = m_normal1.x;
            m_normal.y = m_normal1.y;
            m_lowerLimit.x = -m_normal1.x;
            m_lowerLimit.y = -m_normal1.y;
            m_upperLimit.x = m_normal1.x;
            m_upperLimit.y = m_normal1.y;
          } else {
            m_normal.x = -m_normal1.x;
            m_normal.y = -m_normal1.y;
            m_lowerLimit.x = -m_normal2.x;
            m_lowerLimit.y = -m_normal2.y;
            m_upperLimit.x = m_normal1.x;
            m_upperLimit.y = m_normal1.y;
          }
        }
      } else {
        m_front = offset1 >= 0.0f;
        if (m_front) {
          m_normal.x = m_normal1.x;
          m_normal.y = m_normal1.y;
          m_lowerLimit.x = -m_normal1.x;
          m_lowerLimit.y = -m_normal1.y;
          m_upperLimit.x = -m_normal1.x;
          m_upperLimit.y = -m_normal1.y;
        } else {
          m_normal.x = -m_normal1.x;
          m_normal.y = -m_normal1.y;
          m_lowerLimit.x = m_normal1.x;
          m_lowerLimit.y = m_normal1.y;
          m_upperLimit.x = m_normal1.x;
          m_upperLimit.y = m_normal1.y;
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

      final ClipVertex ie0 = ie[0];
      final ClipVertex ie1 = ie[1];

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

        ie0.v.set(m_polygonB.vertices[i1]);
        ie0.id.indexA = 0;
        ie0.id.indexB = (byte) i1;
        ie0.id.typeA = (byte) ContactID.Type.FACE.ordinal();
        ie0.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

        ie1.v.set(m_polygonB.vertices[i2]);
        ie1.id.indexA = 0;
        ie1.id.indexB = (byte) i2;
        ie1.id.typeA = (byte) ContactID.Type.FACE.ordinal();
        ie1.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

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

        ie0.v.set(m_v1);
        ie0.id.indexA = 0;
        ie0.id.indexB = (byte) primaryAxis.index;
        ie0.id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
        ie0.id.typeB = (byte) ContactID.Type.FACE.ordinal();

        ie1.v.set(m_v2);
        ie1.id.indexA = 0;
        ie1.id.indexB = (byte) primaryAxis.index;
        ie1.id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
        ie1.id.typeB = (byte) ContactID.Type.FACE.ordinal();

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
      float nx = m_normal.x;
      float ny = m_normal.y;

      for (int i = 0; i < m_polygonB.count; ++i) {
        Vec2 v = m_polygonB.vertices[i];
        float tempx = v.x - m_v1.x;
        float tempy = v.y - m_v1.y;
        float s = nx * tempx + ny * tempy;
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
      axis.separation = -Float.MAX_VALUE;

      perp.x = -m_normal.y;
      perp.y = m_normal.x;

      for (int i = 0; i < m_polygonB.count; ++i) {
        Vec2 normalB = m_polygonB.normals[i];
        Vec2 vB = m_polygonB.vertices[i];
        n.x = -normalB.x;
        n.y = -normalB.y;

        // float s1 = Vec2.dot(n, temp.set(vB).subLocal(m_v1));
        // float s2 = Vec2.dot(n, temp.set(vB).subLocal(m_v2));
        float tempx = vB.x - m_v1.x;
        float tempy = vB.y - m_v1.y;
        float s1 = n.x * tempx + n.y * tempy;
        tempx = vB.x - m_v2.x;
        tempy = vB.y - m_v2.y;
        float s2 = n.x * tempx + n.y * tempy;
        float s = MathUtils.min(s1, s2);

        if (s > m_radius) {
          // No collision
          axis.type = EPAxis.Type.EDGE_B;
          axis.index = i;
          axis.separation = s;
          return;
        }

        // Adjacency
        if (n.x * perp.x + n.y * perp.y >= 0.0f) {
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
