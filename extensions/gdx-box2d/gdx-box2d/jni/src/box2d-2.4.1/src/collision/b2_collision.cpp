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
#include "box2d/b2_distance.h"

void b2WorldManifold::Initialize(const b2Manifold* manifold,
						  const b2Transform& xfA, float radiusA,
						  const b2Transform& xfB, float radiusB)
{
	if (manifold->pointCount == 0)
	{
		return;
	}

	switch (manifold->type)
	{
	case b2Manifold::e_circles:
		{
			normal.Set(1.0f, 0.0f);
			b2Vec2 pointA = b2Mul(xfA, manifold->localPoint);
			b2Vec2 pointB = b2Mul(xfB, manifold->points[0].localPoint);
			if (b2DistanceSquared(pointA, pointB) > b2_epsilon * b2_epsilon)
			{
				normal = pointB - pointA;
				normal.Normalize();
			}

			b2Vec2 cA = pointA + radiusA * normal;
			b2Vec2 cB = pointB - radiusB * normal;
			points[0] = 0.5f * (cA + cB);
			separations[0] = b2Dot(cB - cA, normal);
		}
		break;

	case b2Manifold::e_faceA:
		{
			normal = b2Mul(xfA.q, manifold->localNormal);
			b2Vec2 planePoint = b2Mul(xfA, manifold->localPoint);
			
			for (int32 i = 0; i < manifold->pointCount; ++i)
			{
				b2Vec2 clipPoint = b2Mul(xfB, manifold->points[i].localPoint);
				b2Vec2 cA = clipPoint + (radiusA - b2Dot(clipPoint - planePoint, normal)) * normal;
				b2Vec2 cB = clipPoint - radiusB * normal;
				points[i] = 0.5f * (cA + cB);
				separations[i] = b2Dot(cB - cA, normal);
			}
		}
		break;

	case b2Manifold::e_faceB:
		{
			normal = b2Mul(xfB.q, manifold->localNormal);
			b2Vec2 planePoint = b2Mul(xfB, manifold->localPoint);

			for (int32 i = 0; i < manifold->pointCount; ++i)
			{
				b2Vec2 clipPoint = b2Mul(xfA, manifold->points[i].localPoint);
				b2Vec2 cB = clipPoint + (radiusB - b2Dot(clipPoint - planePoint, normal)) * normal;
				b2Vec2 cA = clipPoint - radiusA * normal;
				points[i] = 0.5f * (cA + cB);
				separations[i] = b2Dot(cA - cB, normal);
			}

			// Ensure normal points from A to B.
			normal = -normal;
		}
		break;
	}
}

void b2GetPointStates(b2PointState state1[b2_maxManifoldPoints], b2PointState state2[b2_maxManifoldPoints],
					  const b2Manifold* manifold1, const b2Manifold* manifold2)
{
	for (int32 i = 0; i < b2_maxManifoldPoints; ++i)
	{
		state1[i] = b2_nullState;
		state2[i] = b2_nullState;
	}

	// Detect persists and removes.
	for (int32 i = 0; i < manifold1->pointCount; ++i)
	{
		b2ContactID id = manifold1->points[i].id;

		state1[i] = b2_removeState;

		for (int32 j = 0; j < manifold2->pointCount; ++j)
		{
			if (manifold2->points[j].id.key == id.key)
			{
				state1[i] = b2_persistState;
				break;
			}
		}
	}

	// Detect persists and adds.
	for (int32 i = 0; i < manifold2->pointCount; ++i)
	{
		b2ContactID id = manifold2->points[i].id;

		state2[i] = b2_addState;

		for (int32 j = 0; j < manifold1->pointCount; ++j)
		{
			if (manifold1->points[j].id.key == id.key)
			{
				state2[i] = b2_persistState;
				break;
			}
		}
	}
}

// From Real-time Collision Detection, p179.
bool b2AABB::RayCast(b2RayCastOutput* output, const b2RayCastInput& input) const
{
	float tmin = -b2_maxFloat;
	float tmax = b2_maxFloat;

	b2Vec2 p = input.p1;
	b2Vec2 d = input.p2 - input.p1;
	b2Vec2 absD = b2Abs(d);

	b2Vec2 normal;

	for (int32 i = 0; i < 2; ++i)
	{
		if (absD(i) < b2_epsilon)
		{
			// Parallel.
			if (p(i) < lowerBound(i) || upperBound(i) < p(i))
			{
				return false;
			}
		}
		else
		{
			float inv_d = 1.0f / d(i);
			float t1 = (lowerBound(i) - p(i)) * inv_d;
			float t2 = (upperBound(i) - p(i)) * inv_d;

			// Sign of the normal vector.
			float s = -1.0f;

			if (t1 > t2)
			{
				b2Swap(t1, t2);
				s = 1.0f;
			}

			// Push the min up
			if (t1 > tmin)
			{
				normal.SetZero();
				normal(i) = s;
				tmin = t1;
			}

			// Pull the max down
			tmax = b2Min(tmax, t2);

			if (tmin > tmax)
			{
				return false;
			}
		}
	}

	// Does the ray start inside the box?
	// Does the ray intersect beyond the max fraction?
	if (tmin < 0.0f || input.maxFraction < tmin)
	{
		return false;
	}

	// Intersection.
	output->fraction = tmin;
	output->normal = normal;
	return true;
}

// Sutherland-Hodgman clipping.
int32 b2ClipSegmentToLine(b2ClipVertex vOut[2], const b2ClipVertex vIn[2],
						const b2Vec2& normal, float offset, int32 vertexIndexA)
{
	// Start with no output points
	int32 count = 0;

	// Calculate the distance of end points to the line
	float distance0 = b2Dot(normal, vIn[0].v) - offset;
	float distance1 = b2Dot(normal, vIn[1].v) - offset;

	// If the points are behind the plane
	if (distance0 <= 0.0f) vOut[count++] = vIn[0];
	if (distance1 <= 0.0f) vOut[count++] = vIn[1];

	// If the points are on different sides of the plane
	if (distance0 * distance1 < 0.0f)
	{
		// Find intersection point of edge and plane
		float interp = distance0 / (distance0 - distance1);
		vOut[count].v = vIn[0].v + interp * (vIn[1].v - vIn[0].v);

		// VertexA is hitting edgeB.
		vOut[count].id.cf.indexA = static_cast<uint8>(vertexIndexA);
		vOut[count].id.cf.indexB = vIn[0].id.cf.indexB;
		vOut[count].id.cf.typeA = b2ContactFeature::e_vertex;
		vOut[count].id.cf.typeB = b2ContactFeature::e_face;
		++count;

		b2Assert(count == 2);
	}

	return count;
}

bool b2TestOverlap(	const b2Shape* shapeA, int32 indexA,
					const b2Shape* shapeB, int32 indexB,
					const b2Transform& xfA, const b2Transform& xfB)
{
	b2DistanceInput input;
	input.proxyA.Set(shapeA, indexA);
	input.proxyB.Set(shapeB, indexB);
	input.transformA = xfA;
	input.transformB = xfB;
	input.useRadii = true;

	b2SimplexCache cache;
	cache.count = 0;

	b2DistanceOutput output;

	b2Distance(&output, &cache, &input);

	return output.distance < 10.0f * b2_epsilon;
}

// quickhull recursion
static b2Hull b2RecurseHull(b2Vec2 p1, b2Vec2 p2, b2Vec2* ps, int32 count)
{
	b2Hull hull;
	hull.count = 0;

	if (count == 0)
	{
		return hull;
	}

	// create an edge vector pointing from p1 to p2
	b2Vec2 e = p2 - p1;
	e.Normalize();

	// discard points left of e and find point furthest to the right of e
	b2Vec2 rightPoints[b2_maxPolygonVertices]{};
	int32 rightCount = 0;

	int32 bestIndex = 0;
	float bestDistance = b2Cross(ps[bestIndex] - p1, e);
	if (bestDistance > 0.0f)
	{
		rightPoints[rightCount++] = ps[bestIndex];
	}

	for (int32 i = 1; i < count; ++i)
	{
		float distance = b2Cross(ps[i] - p1, e);
		if (distance > bestDistance)
		{
			bestIndex = i;
			bestDistance = distance;
		}

		if (distance > 0.0f)
		{
			rightPoints[rightCount++] = ps[i];
		}
	}

	if (bestDistance < 2.0f * b2_linearSlop)
	{
		return hull;
	}

	b2Vec2 bestPoint = ps[bestIndex];

	// compute hull to the right of p1-bestPoint
	b2Hull hull1 = b2RecurseHull(p1, bestPoint, rightPoints, rightCount);

	// compute hull to the right of bestPoint-p2
	b2Hull hull2 = b2RecurseHull(bestPoint, p2, rightPoints, rightCount);

	// stich together hulls
	for (int32 i = 0; i < hull1.count; ++i)
	{
		hull.points[hull.count++] = hull1.points[i];
	}

	hull.points[hull.count++] = bestPoint;

	for (int32 i = 0; i < hull2.count; ++i)
	{
		hull.points[hull.count++] = hull2.points[i];
	}

	b2Assert(hull.count < b2_maxPolygonVertices);

	return hull;
}

// quickhull algorithm
// - merges vertices based on b2_linearSlop
// - removes collinear points using b2_linearSlop
// - returns an empty hull if it fails
b2Hull b2ComputeHull(const b2Vec2* points, int32 count)
{
	b2Hull hull;
	hull.count = 0;

	if (count < 3 || count > b2_maxPolygonVertices)
	{
		// check your data
		return hull;
	}

	count = b2Min(count, b2_maxPolygonVertices);

	b2AABB aabb = { {b2_maxFloat, b2_maxFloat}, {-b2_maxFloat, -b2_maxFloat} };

	// Perform aggressive point welding. First point always remains.
	// Also compute the bounding box for later.
	b2Vec2 ps[b2_maxPolygonVertices];
	int32 n = 0;
	const float tolSqr = 16.0f * b2_linearSlop * b2_linearSlop;
	for (int32 i = 0; i < count; ++i)
	{
		aabb.lowerBound = b2Min(aabb.lowerBound, points[i]);
		aabb.upperBound = b2Max(aabb.upperBound, points[i]);

		b2Vec2 vi = points[i];

		bool unique = true;
		for (int32 j = 0; j < i; ++j)
		{
			b2Vec2 vj = points[j];

			float distSqr = b2DistanceSquared(vi, vj);
			if (distSqr < tolSqr)
			{
				unique = false;
				break;
			}
		}

		if (unique)
		{
			ps[n++] = vi;
		}
	}

	if (n < 3)
	{
		// all points very close together, check your data and check your scale
		return hull;
	}

	// Find an extreme point as the first point on the hull
	b2Vec2 c = aabb.GetCenter();
	int32 i1 = 0;
	float dsq1 = b2DistanceSquared(c, ps[i1]);
	for (int32 i = 1; i < n; ++i)
	{
		float dsq = b2DistanceSquared(c, ps[i]);
		if (dsq > dsq1)
		{
			i1 = i;
			dsq1 = dsq;
		}
	}

	// remove p1 from working set
	b2Vec2 p1 = ps[i1];
	ps[i1] = ps[n - 1];
	n = n - 1;

	int32 i2 = 0;
	float dsq2 = b2DistanceSquared(p1, ps[i2]);
	for (int32 i = 1; i < n; ++i)
	{
		float dsq = b2DistanceSquared(p1, ps[i]);
		if (dsq > dsq2)
		{
			i2 = i;
			dsq2 = dsq;
		}
	}

	// remove p2 from working set
	b2Vec2 p2 = ps[i2];
	ps[i2] = ps[n - 1];
	n = n - 1;

	// split the points into points that are left and right of the line p1-p2.
	b2Vec2 rightPoints[b2_maxPolygonVertices - 2];
	int32 rightCount = 0;

	b2Vec2 leftPoints[b2_maxPolygonVertices - 2];
	int32 leftCount = 0;

	b2Vec2 e = p2 - p1;
	e.Normalize();

	for (int32 i = 0; i < n; ++i)
	{
		float d = b2Cross(ps[i] - p1, e);

		// slop used here to skip points that are very close to the line p1-p2
		if (d >= 2.0f * b2_linearSlop)
		{
			rightPoints[rightCount++] = ps[i];
		}
		else if (d <= -2.0f * b2_linearSlop)
		{
			leftPoints[leftCount++] = ps[i];
		}
	}

	// compute hulls on right and left
	b2Hull hull1 = b2RecurseHull(p1, p2, rightPoints, rightCount);
	b2Hull hull2 = b2RecurseHull(p2, p1, leftPoints, leftCount);

	if (hull1.count == 0 && hull2.count == 0)
	{
		// all points collinear
		return hull;
	}

	// stitch hulls together, preserving CCW winding order
	hull.points[hull.count++] = p1;

	for (int32 i = 0; i < hull1.count; ++i)
	{
		hull.points[hull.count++] = hull1.points[i];
	}

	hull.points[hull.count++] = p2;

	for (int32 i = 0; i < hull2.count; ++i)
	{
		hull.points[hull.count++] = hull2.points[i];
	}

	b2Assert(hull.count <= b2_maxPolygonVertices);

	// merge collinear
	bool searching = true;
	while (searching && hull.count > 2)
	{
		searching = false;

		for (int32 i = 0; i < hull.count; ++i)
		{
			int32 i1 = i;
			int32 i2 = (i + 1) % hull.count;
			int32 i3 = (i + 2) % hull.count;

			b2Vec2 p1 = hull.points[i1];
			b2Vec2 p2 = hull.points[i2];
			b2Vec2 p3 = hull.points[i3];

			b2Vec2 e = p3 - p1;
			e.Normalize();

			b2Vec2 v = p2 - p1;
			float distance = b2Cross(p2 - p1, e);
			if (distance <= 2.0f * b2_linearSlop)
			{
				// remove midpoint from hull
				for (int32 j = i2; j < hull.count - 1; ++j)
				{
					hull.points[j] = hull.points[j + 1];
				}
				hull.count -= 1;

				// continue searching for collinear points
				searching = true;

				break;
			}
		}
	}

	if (hull.count < 3)
	{
		// all points collinear, shouldn't be reached since this was validated above
		hull.count = 0;
	}

	return hull;
}

bool b2ValidateHull(const b2Hull& hull)
{
	if (hull.count < 3 || b2_maxPolygonVertices < hull.count)
	{
		return false;
	}

	// test that every point is behind every edge
	for (int32 i = 0; i < hull.count; ++i)
	{
		// create an edge vector
		int32 i1 = i;
		int32 i2 = i < hull.count - 1 ? i1 + 1 : 0;
		b2Vec2 p = hull.points[i1];
		b2Vec2 e = hull.points[i2] - p;
		e.Normalize();

		for (int32 j = 0; j < hull.count; ++j)
		{
			// skip points that subtend the current edge
			if (j == i1 || j == i2)
			{
				continue;
			}

			float distance = b2Cross(hull.points[j] - p, e);
			if (distance >= 0.0f)
			{
				return false;
			}
		}
	}

	// test for collinear points
	for (int32 i = 0; i < hull.count; ++i)
	{
		int32 i1 = i;
		int32 i2 = (i + 1) % hull.count;
		int32 i3 = (i + 2) % hull.count;

		b2Vec2 p1 = hull.points[i1];
		b2Vec2 p2 = hull.points[i2];
		b2Vec2 p3 = hull.points[i3];

		b2Vec2 e = p3 - p1;
		e.Normalize();

		b2Vec2 v = p2 - p1;
		float distance = b2Cross(p2 - p1, e);
		if (distance <= b2_linearSlop)
		{
			// p1-p2-p3 are collinear
			return false;
		}
	}

	return true;
}
