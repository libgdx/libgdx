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
#include "box2d/b2_block_allocator.h"

#include <new>

b2Shape* b2CircleShape::Clone(b2BlockAllocator* allocator) const
{
	void* mem = allocator->Allocate(sizeof(b2CircleShape));
	b2CircleShape* clone = new (mem) b2CircleShape;
	*clone = *this;
	return clone;
}

int32 b2CircleShape::GetChildCount() const
{
	return 1;
}

bool b2CircleShape::TestPoint(const b2Transform& transform, const b2Vec2& p) const
{
	b2Vec2 center = transform.p + b2Mul(transform.q, m_p);
	b2Vec2 d = p - center;
	return b2Dot(d, d) <= m_radius * m_radius;
}

// Collision Detection in Interactive 3D Environments by Gino van den Bergen
// From Section 3.1.2
// x = s + a * r
// norm(x) = radius
bool b2CircleShape::RayCast(b2RayCastOutput* output, const b2RayCastInput& input,
							const b2Transform& transform, int32 childIndex) const
{
	B2_NOT_USED(childIndex);

	b2Vec2 position = transform.p + b2Mul(transform.q, m_p);
	b2Vec2 s = input.p1 - position;
	float b = b2Dot(s, s) - m_radius * m_radius;

	// Solve quadratic equation.
	b2Vec2 r = input.p2 - input.p1;
	float c =  b2Dot(s, r);
	float rr = b2Dot(r, r);
	float sigma = c * c - rr * b;

	// Check for negative discriminant and short segment.
	if (sigma < 0.0f || rr < b2_epsilon)
	{
		return false;
	}

	// Find the point of intersection of the line with the circle.
	float a = -(c + b2Sqrt(sigma));

	// Is the intersection point on the segment?
	if (0.0f <= a && a <= input.maxFraction * rr)
	{
		a /= rr;
		output->fraction = a;
		output->normal = s + a * r;
		output->normal.Normalize();
		return true;
	}

	return false;
}

void b2CircleShape::ComputeAABB(b2AABB* aabb, const b2Transform& transform, int32 childIndex) const
{
	B2_NOT_USED(childIndex);

	b2Vec2 p = transform.p + b2Mul(transform.q, m_p);
	aabb->lowerBound.Set(p.x - m_radius, p.y - m_radius);
	aabb->upperBound.Set(p.x + m_radius, p.y + m_radius);
}

void b2CircleShape::ComputeMass(b2MassData* massData, float density) const
{
	massData->mass = density * b2_pi * m_radius * m_radius;
	massData->center = m_p;

	// inertia about the local origin
	massData->I = massData->mass * (0.5f * m_radius * m_radius + b2Dot(m_p, m_p));
}
