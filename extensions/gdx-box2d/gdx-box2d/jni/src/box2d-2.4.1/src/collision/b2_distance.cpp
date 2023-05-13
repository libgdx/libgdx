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

#include "box2d/b2_circle_shape.h"
#include "box2d/b2_distance.h"
#include "box2d/b2_edge_shape.h"
#include "box2d/b2_chain_shape.h"
#include "box2d/b2_polygon_shape.h"

// GJK using Voronoi regions (Christer Ericson) and Barycentric coordinates.
B2_API int32 b2_gjkCalls, b2_gjkIters, b2_gjkMaxIters;

void b2DistanceProxy::Set(const b2Shape* shape, int32 index)
{
	switch (shape->GetType())
	{
	case b2Shape::e_circle:
		{
			const b2CircleShape* circle = static_cast<const b2CircleShape*>(shape);
			m_vertices = &circle->m_p;
			m_count = 1;
			m_radius = circle->m_radius;
		}
		break;

	case b2Shape::e_polygon:
		{
			const b2PolygonShape* polygon = static_cast<const b2PolygonShape*>(shape);
			m_vertices = polygon->m_vertices;
			m_count = polygon->m_count;
			m_radius = polygon->m_radius;
		}
		break;

	case b2Shape::e_chain:
		{
			const b2ChainShape* chain = static_cast<const b2ChainShape*>(shape);
			b2Assert(0 <= index && index < chain->m_count);

			m_buffer[0] = chain->m_vertices[index];
			if (index + 1 < chain->m_count)
			{
				m_buffer[1] = chain->m_vertices[index + 1];
			}
			else
			{
				m_buffer[1] = chain->m_vertices[0];
			}

			m_vertices = m_buffer;
			m_count = 2;
			m_radius = chain->m_radius;
		}
		break;

	case b2Shape::e_edge:
		{
			const b2EdgeShape* edge = static_cast<const b2EdgeShape*>(shape);
			m_vertices = &edge->m_vertex1;
			m_count = 2;
			m_radius = edge->m_radius;
		}
		break;

	default:
		b2Assert(false);
	}
}

void b2DistanceProxy::Set(const b2Vec2* vertices, int32 count, float radius)
{
    m_vertices = vertices;
    m_count = count;
    m_radius = radius;
}

struct b2SimplexVertex
{
	b2Vec2 wA;		// support point in proxyA
	b2Vec2 wB;		// support point in proxyB
	b2Vec2 w;		// wB - wA
	float a;		// barycentric coordinate for closest point
	int32 indexA;	// wA index
	int32 indexB;	// wB index
};

struct b2Simplex
{
	void ReadCache(	const b2SimplexCache* cache,
					const b2DistanceProxy* proxyA, const b2Transform& transformA,
					const b2DistanceProxy* proxyB, const b2Transform& transformB)
	{
		b2Assert(cache->count <= 3);
		
		// Copy data from cache.
		m_count = cache->count;
		b2SimplexVertex* vertices = &m_v1;
		for (int32 i = 0; i < m_count; ++i)
		{
			b2SimplexVertex* v = vertices + i;
			v->indexA = cache->indexA[i];
			v->indexB = cache->indexB[i];
			b2Vec2 wALocal = proxyA->GetVertex(v->indexA);
			b2Vec2 wBLocal = proxyB->GetVertex(v->indexB);
			v->wA = b2Mul(transformA, wALocal);
			v->wB = b2Mul(transformB, wBLocal);
			v->w = v->wB - v->wA;
			v->a = 0.0f;
		}

		// Compute the new simplex metric, if it is substantially different than
		// old metric then flush the simplex.
		if (m_count > 1)
		{
			float metric1 = cache->metric;
			float metric2 = GetMetric();
			if (metric2 < 0.5f * metric1 || 2.0f * metric1 < metric2 || metric2 < b2_epsilon)
			{
				// Reset the simplex.
				m_count = 0;
			}
		}

		// If the cache is empty or invalid ...
		if (m_count == 0)
		{
			b2SimplexVertex* v = vertices + 0;
			v->indexA = 0;
			v->indexB = 0;
			b2Vec2 wALocal = proxyA->GetVertex(0);
			b2Vec2 wBLocal = proxyB->GetVertex(0);
			v->wA = b2Mul(transformA, wALocal);
			v->wB = b2Mul(transformB, wBLocal);
			v->w = v->wB - v->wA;
			v->a = 1.0f;
			m_count = 1;
		}
	}

	void WriteCache(b2SimplexCache* cache) const
	{
		cache->metric = GetMetric();
		cache->count = uint16(m_count);
		const b2SimplexVertex* vertices = &m_v1;
		for (int32 i = 0; i < m_count; ++i)
		{
			cache->indexA[i] = uint8(vertices[i].indexA);
			cache->indexB[i] = uint8(vertices[i].indexB);
		}
	}

	b2Vec2 GetSearchDirection() const
	{
		switch (m_count)
		{
		case 1:
			return -m_v1.w;

		case 2:
			{
				b2Vec2 e12 = m_v2.w - m_v1.w;
				float sgn = b2Cross(e12, -m_v1.w);
				if (sgn > 0.0f)
				{
					// Origin is left of e12.
					return b2Cross(1.0f, e12);
				}
				else
				{
					// Origin is right of e12.
					return b2Cross(e12, 1.0f);
				}
			}

		default:
			b2Assert(false);
			return b2Vec2_zero;
		}
	}

	b2Vec2 GetClosestPoint() const
	{
		switch (m_count)
		{
		case 0:
			b2Assert(false);
			return b2Vec2_zero;

		case 1:
			return m_v1.w;

		case 2:
			return m_v1.a * m_v1.w + m_v2.a * m_v2.w;

		case 3:
			return b2Vec2_zero;

		default:
			b2Assert(false);
			return b2Vec2_zero;
		}
	}

	void GetWitnessPoints(b2Vec2* pA, b2Vec2* pB) const
	{
		switch (m_count)
		{
		case 0:
			b2Assert(false);
			break;

		case 1:
			*pA = m_v1.wA;
			*pB = m_v1.wB;
			break;

		case 2:
			*pA = m_v1.a * m_v1.wA + m_v2.a * m_v2.wA;
			*pB = m_v1.a * m_v1.wB + m_v2.a * m_v2.wB;
			break;

		case 3:
			*pA = m_v1.a * m_v1.wA + m_v2.a * m_v2.wA + m_v3.a * m_v3.wA;
			*pB = *pA;
			break;

		default:
			b2Assert(false);
			break;
		}
	}

	float GetMetric() const
	{
		switch (m_count)
		{
		case 0:
			b2Assert(false);
			return 0.0f;

		case 1:
			return 0.0f;

		case 2:
			return b2Distance(m_v1.w, m_v2.w);

		case 3:
			return b2Cross(m_v2.w - m_v1.w, m_v3.w - m_v1.w);

		default:
			b2Assert(false);
			return 0.0f;
		}
	}

	void Solve2();
	void Solve3();

	b2SimplexVertex m_v1, m_v2, m_v3;
	int32 m_count;
};


// Solve a line segment using barycentric coordinates.
//
// p = a1 * w1 + a2 * w2
// a1 + a2 = 1
//
// The vector from the origin to the closest point on the line is
// perpendicular to the line.
// e12 = w2 - w1
// dot(p, e) = 0
// a1 * dot(w1, e) + a2 * dot(w2, e) = 0
//
// 2-by-2 linear system
// [1      1     ][a1] = [1]
// [w1.e12 w2.e12][a2] = [0]
//
// Define
// d12_1 =  dot(w2, e12)
// d12_2 = -dot(w1, e12)
// d12 = d12_1 + d12_2
//
// Solution
// a1 = d12_1 / d12
// a2 = d12_2 / d12
void b2Simplex::Solve2()
{
	b2Vec2 w1 = m_v1.w;
	b2Vec2 w2 = m_v2.w;
	b2Vec2 e12 = w2 - w1;

	// w1 region
	float d12_2 = -b2Dot(w1, e12);
	if (d12_2 <= 0.0f)
	{
		// a2 <= 0, so we clamp it to 0
		m_v1.a = 1.0f;
		m_count = 1;
		return;
	}

	// w2 region
	float d12_1 = b2Dot(w2, e12);
	if (d12_1 <= 0.0f)
	{
		// a1 <= 0, so we clamp it to 0
		m_v2.a = 1.0f;
		m_count = 1;
		m_v1 = m_v2;
		return;
	}

	// Must be in e12 region.
	float inv_d12 = 1.0f / (d12_1 + d12_2);
	m_v1.a = d12_1 * inv_d12;
	m_v2.a = d12_2 * inv_d12;
	m_count = 2;
}

// Possible regions:
// - points[2]
// - edge points[0]-points[2]
// - edge points[1]-points[2]
// - inside the triangle
void b2Simplex::Solve3()
{
	b2Vec2 w1 = m_v1.w;
	b2Vec2 w2 = m_v2.w;
	b2Vec2 w3 = m_v3.w;

	// Edge12
	// [1      1     ][a1] = [1]
	// [w1.e12 w2.e12][a2] = [0]
	// a3 = 0
	b2Vec2 e12 = w2 - w1;
	float w1e12 = b2Dot(w1, e12);
	float w2e12 = b2Dot(w2, e12);
	float d12_1 = w2e12;
	float d12_2 = -w1e12;

	// Edge13
	// [1      1     ][a1] = [1]
	// [w1.e13 w3.e13][a3] = [0]
	// a2 = 0
	b2Vec2 e13 = w3 - w1;
	float w1e13 = b2Dot(w1, e13);
	float w3e13 = b2Dot(w3, e13);
	float d13_1 = w3e13;
	float d13_2 = -w1e13;

	// Edge23
	// [1      1     ][a2] = [1]
	// [w2.e23 w3.e23][a3] = [0]
	// a1 = 0
	b2Vec2 e23 = w3 - w2;
	float w2e23 = b2Dot(w2, e23);
	float w3e23 = b2Dot(w3, e23);
	float d23_1 = w3e23;
	float d23_2 = -w2e23;
	
	// Triangle123
	float n123 = b2Cross(e12, e13);

	float d123_1 = n123 * b2Cross(w2, w3);
	float d123_2 = n123 * b2Cross(w3, w1);
	float d123_3 = n123 * b2Cross(w1, w2);

	// w1 region
	if (d12_2 <= 0.0f && d13_2 <= 0.0f)
	{
		m_v1.a = 1.0f;
		m_count = 1;
		return;
	}

	// e12
	if (d12_1 > 0.0f && d12_2 > 0.0f && d123_3 <= 0.0f)
	{
		float inv_d12 = 1.0f / (d12_1 + d12_2);
		m_v1.a = d12_1 * inv_d12;
		m_v2.a = d12_2 * inv_d12;
		m_count = 2;
		return;
	}

	// e13
	if (d13_1 > 0.0f && d13_2 > 0.0f && d123_2 <= 0.0f)
	{
		float inv_d13 = 1.0f / (d13_1 + d13_2);
		m_v1.a = d13_1 * inv_d13;
		m_v3.a = d13_2 * inv_d13;
		m_count = 2;
		m_v2 = m_v3;
		return;
	}

	// w2 region
	if (d12_1 <= 0.0f && d23_2 <= 0.0f)
	{
		m_v2.a = 1.0f;
		m_count = 1;
		m_v1 = m_v2;
		return;
	}

	// w3 region
	if (d13_1 <= 0.0f && d23_1 <= 0.0f)
	{
		m_v3.a = 1.0f;
		m_count = 1;
		m_v1 = m_v3;
		return;
	}

	// e23
	if (d23_1 > 0.0f && d23_2 > 0.0f && d123_1 <= 0.0f)
	{
		float inv_d23 = 1.0f / (d23_1 + d23_2);
		m_v2.a = d23_1 * inv_d23;
		m_v3.a = d23_2 * inv_d23;
		m_count = 2;
		m_v1 = m_v3;
		return;
	}

	// Must be in triangle123
	float inv_d123 = 1.0f / (d123_1 + d123_2 + d123_3);
	m_v1.a = d123_1 * inv_d123;
	m_v2.a = d123_2 * inv_d123;
	m_v3.a = d123_3 * inv_d123;
	m_count = 3;
}

void b2Distance(b2DistanceOutput* output,
				b2SimplexCache* cache,
				const b2DistanceInput* input)
{
	++b2_gjkCalls;

	const b2DistanceProxy* proxyA = &input->proxyA;
	const b2DistanceProxy* proxyB = &input->proxyB;

	b2Transform transformA = input->transformA;
	b2Transform transformB = input->transformB;

	// Initialize the simplex.
	b2Simplex simplex;
	simplex.ReadCache(cache, proxyA, transformA, proxyB, transformB);

	// Get simplex vertices as an array.
	b2SimplexVertex* vertices = &simplex.m_v1;
	const int32 k_maxIters = 20;

	// These store the vertices of the last simplex so that we
	// can check for duplicates and prevent cycling.
	int32 saveA[3], saveB[3];
	int32 saveCount = 0;

	// Main iteration loop.
	int32 iter = 0;
	while (iter < k_maxIters)
	{
		// Copy simplex so we can identify duplicates.
		saveCount = simplex.m_count;
		for (int32 i = 0; i < saveCount; ++i)
		{
			saveA[i] = vertices[i].indexA;
			saveB[i] = vertices[i].indexB;
		}

		switch (simplex.m_count)
		{
		case 1:
			break;

		case 2:
			simplex.Solve2();
			break;

		case 3:
			simplex.Solve3();
			break;

		default:
			b2Assert(false);
		}

		// If we have 3 points, then the origin is in the corresponding triangle.
		if (simplex.m_count == 3)
		{
			break;
		}

		// Get search direction.
		b2Vec2 d = simplex.GetSearchDirection();

		// Ensure the search direction is numerically fit.
		if (d.LengthSquared() < b2_epsilon * b2_epsilon)
		{
			// The origin is probably contained by a line segment
			// or triangle. Thus the shapes are overlapped.

			// We can't return zero here even though there may be overlap.
			// In case the simplex is a point, segment, or triangle it is difficult
			// to determine if the origin is contained in the CSO or very close to it.
			break;
		}

		// Compute a tentative new simplex vertex using support points.
		b2SimplexVertex* vertex = vertices + simplex.m_count;
		vertex->indexA = proxyA->GetSupport(b2MulT(transformA.q, -d));
		vertex->wA = b2Mul(transformA, proxyA->GetVertex(vertex->indexA));
		vertex->indexB = proxyB->GetSupport(b2MulT(transformB.q, d));
		vertex->wB = b2Mul(transformB, proxyB->GetVertex(vertex->indexB));
		vertex->w = vertex->wB - vertex->wA;

		// Iteration count is equated to the number of support point calls.
		++iter;
		++b2_gjkIters;

		// Check for duplicate support points. This is the main termination criteria.
		bool duplicate = false;
		for (int32 i = 0; i < saveCount; ++i)
		{
			if (vertex->indexA == saveA[i] && vertex->indexB == saveB[i])
			{
				duplicate = true;
				break;
			}
		}

		// If we found a duplicate support point we must exit to avoid cycling.
		if (duplicate)
		{
			break;
		}

		// New vertex is ok and needed.
		++simplex.m_count;
	}

	b2_gjkMaxIters = b2Max(b2_gjkMaxIters, iter);

	// Prepare output.
	simplex.GetWitnessPoints(&output->pointA, &output->pointB);
	output->distance = b2Distance(output->pointA, output->pointB);
	output->iterations = iter;

	// Cache the simplex.
	simplex.WriteCache(cache);

	// Apply radii if requested
	if (input->useRadii)
	{
		if (output->distance < b2_epsilon)
		{
			// Shapes are too close to safely compute normal
			b2Vec2 p = 0.5f * (output->pointA + output->pointB);
			output->pointA = p;
			output->pointB = p;
			output->distance = 0.0f;
		}
		else
		{
			// Keep closest points on perimeter even if overlapped, this way
			// the points move smoothly.
			float rA = proxyA->m_radius;
			float rB = proxyB->m_radius;
			b2Vec2 normal = output->pointB - output->pointA;
			normal.Normalize();
			output->distance = b2Max(0.0f, output->distance - rA - rB);
			output->pointA += rA * normal;
			output->pointB -= rB * normal;
		}
	}
}

// GJK-raycast
// Algorithm by Gino van den Bergen.
// "Smooth Mesh Contacts with GJK" in Game Physics Pearls. 2010
bool b2ShapeCast(b2ShapeCastOutput * output, const b2ShapeCastInput * input)
{
    output->iterations = 0;
    output->lambda = 1.0f;
    output->normal.SetZero();
    output->point.SetZero();

	const b2DistanceProxy* proxyA = &input->proxyA;
	const b2DistanceProxy* proxyB = &input->proxyB;

    float radiusA = b2Max(proxyA->m_radius, b2_polygonRadius);
    float radiusB = b2Max(proxyB->m_radius, b2_polygonRadius);
    float radius = radiusA + radiusB;

	b2Transform xfA = input->transformA;
	b2Transform xfB = input->transformB;

	b2Vec2 r = input->translationB;
	b2Vec2 n(0.0f, 0.0f);
	float lambda = 0.0f;

	// Initial simplex
	b2Simplex simplex;
	simplex.m_count = 0;

	// Get simplex vertices as an array.
	b2SimplexVertex* vertices = &simplex.m_v1;

	// Get support point in -r direction
	int32 indexA = proxyA->GetSupport(b2MulT(xfA.q, -r));
	b2Vec2 wA = b2Mul(xfA, proxyA->GetVertex(indexA));
	int32 indexB = proxyB->GetSupport(b2MulT(xfB.q, r));
	b2Vec2 wB = b2Mul(xfB, proxyB->GetVertex(indexB));
    b2Vec2 v = wA - wB;

    // Sigma is the target distance between polygons
    float sigma = b2Max(b2_polygonRadius, radius - b2_polygonRadius);
	const float tolerance = 0.5f * b2_linearSlop;

	// Main iteration loop.
	const int32 k_maxIters = 20;
	int32 iter = 0;
	while (iter < k_maxIters && v.Length() - sigma > tolerance)
	{
		b2Assert(simplex.m_count < 3);

        output->iterations += 1;

		// Support in direction -v (A - B)
		indexA = proxyA->GetSupport(b2MulT(xfA.q, -v));
		wA = b2Mul(xfA, proxyA->GetVertex(indexA));
		indexB = proxyB->GetSupport(b2MulT(xfB.q, v));
		wB = b2Mul(xfB, proxyB->GetVertex(indexB));
        b2Vec2 p = wA - wB;

        // -v is a normal at p
        v.Normalize();

        // Intersect ray with plane
		float vp = b2Dot(v, p);
        float vr = b2Dot(v, r);
		if (vp - sigma > lambda * vr)
		{
			if (vr <= 0.0f)
			{
				return false;
			}

			lambda = (vp - sigma) / vr;
			if (lambda > 1.0f)
			{
				return false;
			}

            n = -v;
            simplex.m_count = 0;
		}

        // Reverse simplex since it works with B - A.
        // Shift by lambda * r because we want the closest point to the current clip point.
        // Note that the support point p is not shifted because we want the plane equation
        // to be formed in unshifted space.
		b2SimplexVertex* vertex = vertices + simplex.m_count;
		vertex->indexA = indexB;
		vertex->wA = wB + lambda * r;
		vertex->indexB = indexA;
		vertex->wB = wA;
		vertex->w = vertex->wB - vertex->wA;
		vertex->a = 1.0f;
		simplex.m_count += 1;

		switch (simplex.m_count)
		{
		case 1:
			break;

		case 2:
			simplex.Solve2();
			break;

		case 3:
			simplex.Solve3();
			break;

		default:
			b2Assert(false);
		}
		
		// If we have 3 points, then the origin is in the corresponding triangle.
		if (simplex.m_count == 3)
		{
			// Overlap
			return false;
		}

		// Get search direction.
		v = simplex.GetClosestPoint();

		// Iteration count is equated to the number of support point calls.
		++iter;
	}

	if (iter == 0)
	{
		// Initial overlap
		return false;
	}

	// Prepare output.
	b2Vec2 pointA, pointB;
	simplex.GetWitnessPoints(&pointB, &pointA);

	if (v.LengthSquared() > 0.0f)
	{
        n = -v;
		n.Normalize();
	}

    output->point = pointA + radiusA * n;
	output->normal = n;
	output->lambda = lambda;
	output->iterations = iter;
	return true;
}
