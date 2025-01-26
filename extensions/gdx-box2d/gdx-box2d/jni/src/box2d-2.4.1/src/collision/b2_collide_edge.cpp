// MIT License

// Copyright (c) 2019 Erin Catto

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#include "box2d/b2_collision.h"
#include "box2d/b2_circle_shape.h"
#include "box2d/b2_edge_shape.h"
#include "box2d/b2_polygon_shape.h"


// Compute contact points for edge versus circle.
// This accounts for edge connectivity.
void b2CollideEdgeAndCircle(b2Manifold* manifold,
							const b2EdgeShape* edgeA, const b2Transform& xfA,
							const b2CircleShape* circleB, const b2Transform& xfB)
{
	manifold->pointCount = 0;
	
	// Compute circle in frame of edge
	b2Vec2 Q = b2MulT(xfA, b2Mul(xfB, circleB->m_p));
	
	b2Vec2 A = edgeA->m_vertex1, B = edgeA->m_vertex2;
	b2Vec2 e = B - A;
	
	// Normal points to the right for a CCW winding
	b2Vec2 n(e.y, -e.x);
	float offset = b2Dot(n, Q - A);

	bool oneSided = edgeA->m_oneSided;
	if (oneSided && offset < 0.0f)
	{
		return;
	}

	// Barycentric coordinates
	float u = b2Dot(e, B - Q);
	float v = b2Dot(e, Q - A);
	
	float radius = edgeA->m_radius + circleB->m_radius;
	
	b2ContactFeature cf;
	cf.indexB = 0;
	cf.typeB = b2ContactFeature::e_vertex;
	
	// Region A
	if (v <= 0.0f)
	{
		b2Vec2 P = A;
		b2Vec2 d = Q - P;
		float dd = b2Dot(d, d);
		if (dd > radius * radius)
		{
			return;
		}
		
		// Is there an edge connected to A?
		if (edgeA->m_oneSided)
		{
			b2Vec2 A1 = edgeA->m_vertex0;
			b2Vec2 B1 = A;
			b2Vec2 e1 = B1 - A1;
			float u1 = b2Dot(e1, B1 - Q);
			
			// Is the circle in Region AB of the previous edge?
			if (u1 > 0.0f)
			{
				return;
			}
		}
		
		cf.indexA = 0;
		cf.typeA = b2ContactFeature::e_vertex;
		manifold->pointCount = 1;
		manifold->type = b2Manifold::e_circles;
		manifold->localNormal.SetZero();
		manifold->localPoint = P;
		manifold->points[0].id.key = 0;
		manifold->points[0].id.cf = cf;
		manifold->points[0].localPoint = circleB->m_p;
		return;
	}
	
	// Region B
	if (u <= 0.0f)
	{
		b2Vec2 P = B;
		b2Vec2 d = Q - P;
		float dd = b2Dot(d, d);
		if (dd > radius * radius)
		{
			return;
		}
		
		// Is there an edge connected to B?
		if (edgeA->m_oneSided)
		{
			b2Vec2 B2 = edgeA->m_vertex3;
			b2Vec2 A2 = B;
			b2Vec2 e2 = B2 - A2;
			float v2 = b2Dot(e2, Q - A2);
			
			// Is the circle in Region AB of the next edge?
			if (v2 > 0.0f)
			{
				return;
			}
		}
		
		cf.indexA = 1;
		cf.typeA = b2ContactFeature::e_vertex;
		manifold->pointCount = 1;
		manifold->type = b2Manifold::e_circles;
		manifold->localNormal.SetZero();
		manifold->localPoint = P;
		manifold->points[0].id.key = 0;
		manifold->points[0].id.cf = cf;
		manifold->points[0].localPoint = circleB->m_p;
		return;
	}
	
	// Region AB
	float den = b2Dot(e, e);
	b2Assert(den > 0.0f);
	b2Vec2 P = (1.0f / den) * (u * A + v * B);
	b2Vec2 d = Q - P;
	float dd = b2Dot(d, d);
	if (dd > radius * radius)
	{
		return;
	}
	
	if (offset < 0.0f)
	{
		n.Set(-n.x, -n.y);
	}
	n.Normalize();
	
	cf.indexA = 0;
	cf.typeA = b2ContactFeature::e_face;
	manifold->pointCount = 1;
	manifold->type = b2Manifold::e_faceA;
	manifold->localNormal = n;
	manifold->localPoint = A;
	manifold->points[0].id.key = 0;
	manifold->points[0].id.cf = cf;
	manifold->points[0].localPoint = circleB->m_p;
}

// This structure is used to keep track of the best separating axis.
struct b2EPAxis
{
	enum Type
	{
		e_unknown,
		e_edgeA,
		e_edgeB
	};
	
	b2Vec2 normal;
	Type type;
	int32 index;
	float separation;
};

// This holds polygon B expressed in frame A.
struct b2TempPolygon
{
	b2Vec2 vertices[b2_maxPolygonVertices];
	b2Vec2 normals[b2_maxPolygonVertices];
	int32 count;
};

// Reference face used for clipping
struct b2ReferenceFace
{
	int32 i1, i2;
	b2Vec2 v1, v2;
	b2Vec2 normal;
	
	b2Vec2 sideNormal1;
	float sideOffset1;
	
	b2Vec2 sideNormal2;
	float sideOffset2;
};

static b2EPAxis b2ComputeEdgeSeparation(const b2TempPolygon& polygonB, const b2Vec2& v1, const b2Vec2& normal1)
{
	b2EPAxis axis;
	axis.type = b2EPAxis::e_edgeA;
	axis.index = -1;
	axis.separation = -FLT_MAX;
	axis.normal.SetZero();

	b2Vec2 axes[2] = { normal1, -normal1 };

	// Find axis with least overlap (min-max problem)
	for (int32 j = 0; j < 2; ++j)
	{
		float sj = FLT_MAX;

		// Find deepest polygon vertex along axis j
		for (int32 i = 0; i < polygonB.count; ++i)
		{
			float si = b2Dot(axes[j], polygonB.vertices[i] - v1);
			if (si < sj)
			{
				sj = si;
			}
		}

		if (sj > axis.separation)
		{
			axis.index = j;
			axis.separation = sj;
			axis.normal = axes[j];
		}
	}

	return axis;
}

static b2EPAxis b2ComputePolygonSeparation(const b2TempPolygon& polygonB, const b2Vec2& v1, const b2Vec2& v2)
{
	b2EPAxis axis;
	axis.type = b2EPAxis::e_unknown;
	axis.index = -1;
	axis.separation = -FLT_MAX;
	axis.normal.SetZero();

	for (int32 i = 0; i < polygonB.count; ++i)
	{
		b2Vec2 n = -polygonB.normals[i];

		float s1 = b2Dot(n, polygonB.vertices[i] - v1);
		float s2 = b2Dot(n, polygonB.vertices[i] - v2);
		float s = b2Min(s1, s2);

		if (s > axis.separation)
		{
			axis.type = b2EPAxis::e_edgeB;
			axis.index = i;
			axis.separation = s;
			axis.normal = n;
		}
	}

	return axis;
}

void b2CollideEdgeAndPolygon(b2Manifold* manifold,
							const b2EdgeShape* edgeA, const b2Transform& xfA,
							const b2PolygonShape* polygonB, const b2Transform& xfB)
{
	manifold->pointCount = 0;

	b2Transform xf = b2MulT(xfA, xfB);

	b2Vec2 centroidB = b2Mul(xf, polygonB->m_centroid);

	b2Vec2 v1 = edgeA->m_vertex1;
	b2Vec2 v2 = edgeA->m_vertex2;

	b2Vec2 edge1 = v2 - v1;
	edge1.Normalize();

	// Normal points to the right for a CCW winding
	b2Vec2 normal1(edge1.y, -edge1.x);
	float offset1 = b2Dot(normal1, centroidB - v1);

	bool oneSided = edgeA->m_oneSided;
	if (oneSided && offset1 < 0.0f)
	{
		return;
	}

	// Get polygonB in frameA
	b2TempPolygon tempPolygonB;
	tempPolygonB.count = polygonB->m_count;
	for (int32 i = 0; i < polygonB->m_count; ++i)
	{
		tempPolygonB.vertices[i] = b2Mul(xf, polygonB->m_vertices[i]);
		tempPolygonB.normals[i] = b2Mul(xf.q, polygonB->m_normals[i]);
	}

	float radius = polygonB->m_radius + edgeA->m_radius;

	b2EPAxis edgeAxis = b2ComputeEdgeSeparation(tempPolygonB, v1, normal1);
	if (edgeAxis.separation > radius)
	{
		return;
	}

	b2EPAxis polygonAxis = b2ComputePolygonSeparation(tempPolygonB, v1, v2);
	if (polygonAxis.separation > radius)
	{
		return;
	}

	// Use hysteresis for jitter reduction.
	const float k_relativeTol = 0.98f;
	const float k_absoluteTol = 0.001f;

	b2EPAxis primaryAxis;
	if (polygonAxis.separation - radius > k_relativeTol * (edgeAxis.separation - radius) + k_absoluteTol)
	{
		primaryAxis = polygonAxis;
	}
	else
	{
		primaryAxis = edgeAxis;
	}

	if (oneSided)
	{
		// Smooth collision
		// See https://box2d.org/posts/2020/06/ghost-collisions/

		b2Vec2 edge0 = v1 - edgeA->m_vertex0;
		edge0.Normalize();
		b2Vec2 normal0(edge0.y, -edge0.x);
		bool convex1 = b2Cross(edge0, edge1) >= 0.0f;

		b2Vec2 edge2 = edgeA->m_vertex3 - v2;
		edge2.Normalize();
		b2Vec2 normal2(edge2.y, -edge2.x);
		bool convex2 = b2Cross(edge1, edge2) >= 0.0f;

		const float sinTol = 0.1f;
		bool side1 = b2Dot(primaryAxis.normal, edge1) <= 0.0f;

		// Check Gauss Map
		if (side1)
		{
			if (convex1)
			{
				if (b2Cross(primaryAxis.normal, normal0) > sinTol)
				{
					// Skip region
					return;
				}

				// Admit region
			}
			else
			{
				// Snap region
				primaryAxis = edgeAxis;
			}
		}
		else
		{
			if (convex2)
			{
				if (b2Cross(normal2, primaryAxis.normal) > sinTol)
				{
					// Skip region
					return;
				}

				// Admit region
			}
			else
			{
				// Snap region
				primaryAxis = edgeAxis;
			}
		}
	}

	b2ClipVertex clipPoints[2];
	b2ReferenceFace ref;
	if (primaryAxis.type == b2EPAxis::e_edgeA)
	{
		manifold->type = b2Manifold::e_faceA;

		// Search for the polygon normal that is most anti-parallel to the edge normal.
		int32 bestIndex = 0;
		float bestValue = b2Dot(primaryAxis.normal, tempPolygonB.normals[0]);
		for (int32 i = 1; i < tempPolygonB.count; ++i)
		{
			float value = b2Dot(primaryAxis.normal, tempPolygonB.normals[i]);
			if (value < bestValue)
			{
				bestValue = value;
				bestIndex = i;
			}
		}

		int32 i1 = bestIndex;
		int32 i2 = i1 + 1 < tempPolygonB.count ? i1 + 1 : 0;

		clipPoints[0].v = tempPolygonB.vertices[i1];
		clipPoints[0].id.cf.indexA = 0;
		clipPoints[0].id.cf.indexB = static_cast<uint8>(i1);
		clipPoints[0].id.cf.typeA = b2ContactFeature::e_face;
		clipPoints[0].id.cf.typeB = b2ContactFeature::e_vertex;

		clipPoints[1].v = tempPolygonB.vertices[i2];
		clipPoints[1].id.cf.indexA = 0;
		clipPoints[1].id.cf.indexB = static_cast<uint8>(i2);
		clipPoints[1].id.cf.typeA = b2ContactFeature::e_face;
		clipPoints[1].id.cf.typeB = b2ContactFeature::e_vertex;

		ref.i1 = 0;
		ref.i2 = 1;
		ref.v1 = v1;
		ref.v2 = v2;
		ref.normal = primaryAxis.normal;
		ref.sideNormal1 = -edge1;
		ref.sideNormal2 = edge1;
	}
	else
	{
		manifold->type = b2Manifold::e_faceB;

		clipPoints[0].v = v2;
		clipPoints[0].id.cf.indexA = 1;
		clipPoints[0].id.cf.indexB = static_cast<uint8>(primaryAxis.index);
		clipPoints[0].id.cf.typeA = b2ContactFeature::e_vertex;
		clipPoints[0].id.cf.typeB = b2ContactFeature::e_face;

		clipPoints[1].v = v1;
		clipPoints[1].id.cf.indexA = 0;
		clipPoints[1].id.cf.indexB = static_cast<uint8>(primaryAxis.index);		
		clipPoints[1].id.cf.typeA = b2ContactFeature::e_vertex;
		clipPoints[1].id.cf.typeB = b2ContactFeature::e_face;

		ref.i1 = primaryAxis.index;
		ref.i2 = ref.i1 + 1 < tempPolygonB.count ? ref.i1 + 1 : 0;
		ref.v1 = tempPolygonB.vertices[ref.i1];
		ref.v2 = tempPolygonB.vertices[ref.i2];
		ref.normal = tempPolygonB.normals[ref.i1];

		// CCW winding
		ref.sideNormal1.Set(ref.normal.y, -ref.normal.x);
		ref.sideNormal2 = -ref.sideNormal1;
	}

	ref.sideOffset1 = b2Dot(ref.sideNormal1, ref.v1);
	ref.sideOffset2 = b2Dot(ref.sideNormal2, ref.v2);

	// Clip incident edge against reference face side planes
	b2ClipVertex clipPoints1[2];
	b2ClipVertex clipPoints2[2];
	int32 np;

	// Clip to side 1
	np = b2ClipSegmentToLine(clipPoints1, clipPoints, ref.sideNormal1, ref.sideOffset1, ref.i1);

	if (np < b2_maxManifoldPoints)
	{
		return;
	}

	// Clip to side 2
	np = b2ClipSegmentToLine(clipPoints2, clipPoints1, ref.sideNormal2, ref.sideOffset2, ref.i2);

	if (np < b2_maxManifoldPoints)
	{
		return;
	}

	// Now clipPoints2 contains the clipped points.
	if (primaryAxis.type == b2EPAxis::e_edgeA)
	{
		manifold->localNormal = ref.normal;
		manifold->localPoint = ref.v1;
	}
	else
	{
		manifold->localNormal = polygonB->m_normals[ref.i1];
		manifold->localPoint = polygonB->m_vertices[ref.i1];
	}

	int32 pointCount = 0;
	for (int32 i = 0; i < b2_maxManifoldPoints; ++i)
	{
		float separation;

		separation = b2Dot(ref.normal, clipPoints2[i].v - ref.v1);

		if (separation <= radius)
		{
			b2ManifoldPoint* cp = manifold->points + pointCount;

			if (primaryAxis.type == b2EPAxis::e_edgeA)
			{
				cp->localPoint = b2MulT(xf, clipPoints2[i].v);
				cp->id = clipPoints2[i].id;
			}
			else
			{
				cp->localPoint = clipPoints2[i].v;
				cp->id.cf.typeA = clipPoints2[i].id.cf.typeB;
				cp->id.cf.typeB = clipPoints2[i].id.cf.typeA;
				cp->id.cf.indexA = clipPoints2[i].id.cf.indexB;
				cp->id.cf.indexB = clipPoints2[i].id.cf.indexA;
			}

			++pointCount;
		}
	}

	manifold->pointCount = pointCount;
}
