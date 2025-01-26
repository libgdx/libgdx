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

#include "box2d/b2_chain_shape.h"
#include "box2d/b2_edge_shape.h"

#include "box2d/b2_block_allocator.h"

#include <new>
#include <string.h>

b2ChainShape::~b2ChainShape()
{
	Clear();
}

void b2ChainShape::Clear()
{
	b2Free(m_vertices);
	m_vertices = nullptr;
	m_count = 0;
}

void b2ChainShape::CreateLoop(const b2Vec2* vertices, int32 count)
{
	b2Assert(m_vertices == nullptr && m_count == 0);
	b2Assert(count >= 3);
	if (count < 3)
	{
		return;
	}

	for (int32 i = 1; i < count; ++i)
	{
		b2Vec2 v1 = vertices[i-1];
		b2Vec2 v2 = vertices[i];
		// If the code crashes here, it means your vertices are too close together.
		b2Assert(b2DistanceSquared(v1, v2) > b2_linearSlop * b2_linearSlop);
	}

	m_count = count + 1;
	m_vertices = (b2Vec2*)b2Alloc(m_count * sizeof(b2Vec2));
	memcpy(m_vertices, vertices, count * sizeof(b2Vec2));
	m_vertices[count] = m_vertices[0];
	m_prevVertex = m_vertices[m_count - 2];
	m_nextVertex = m_vertices[1];
}

void b2ChainShape::CreateChain(const b2Vec2* vertices, int32 count,	const b2Vec2& prevVertex, const b2Vec2& nextVertex)
{
	b2Assert(m_vertices == nullptr && m_count == 0);
	b2Assert(count >= 2);
	for (int32 i = 1; i < count; ++i)
	{
		// If the code crashes here, it means your vertices are too close together.
		b2Assert(b2DistanceSquared(vertices[i-1], vertices[i]) > b2_linearSlop * b2_linearSlop);
	}

	m_count = count;
	m_vertices = (b2Vec2*)b2Alloc(count * sizeof(b2Vec2));
	memcpy(m_vertices, vertices, m_count * sizeof(b2Vec2));

	m_prevVertex = prevVertex;
	m_nextVertex = nextVertex;
}

b2Shape* b2ChainShape::Clone(b2BlockAllocator* allocator) const
{
	void* mem = allocator->Allocate(sizeof(b2ChainShape));
	b2ChainShape* clone = new (mem) b2ChainShape;
	clone->CreateChain(m_vertices, m_count, m_prevVertex, m_nextVertex);
	return clone;
}

int32 b2ChainShape::GetChildCount() const
{
	// edge count = vertex count - 1
	return m_count - 1;
}

void b2ChainShape::GetChildEdge(b2EdgeShape* edge, int32 index) const
{
	b2Assert(0 <= index && index < m_count - 1);
	edge->m_type = b2Shape::e_edge;
	edge->m_radius = m_radius;

	edge->m_vertex1 = m_vertices[index + 0];
	edge->m_vertex2 = m_vertices[index + 1];
	edge->m_oneSided = true;

	if (index > 0)
	{
		edge->m_vertex0 = m_vertices[index - 1];
	}
	else
	{
		edge->m_vertex0 = m_prevVertex;
	}

	if (index < m_count - 2)
	{
		edge->m_vertex3 = m_vertices[index + 2];
	}
	else
	{
		edge->m_vertex3 = m_nextVertex;
	}
}

bool b2ChainShape::TestPoint(const b2Transform& xf, const b2Vec2& p) const
{
	B2_NOT_USED(xf);
	B2_NOT_USED(p);
	return false;
}

bool b2ChainShape::RayCast(b2RayCastOutput* output, const b2RayCastInput& input,
							const b2Transform& xf, int32 childIndex) const
{
	b2Assert(childIndex < m_count);

	b2EdgeShape edgeShape;

	int32 i1 = childIndex;
	int32 i2 = childIndex + 1;
	if (i2 == m_count)
	{
		i2 = 0;
	}

	edgeShape.m_vertex1 = m_vertices[i1];
	edgeShape.m_vertex2 = m_vertices[i2];

	return edgeShape.RayCast(output, input, xf, 0);
}

void b2ChainShape::ComputeAABB(b2AABB* aabb, const b2Transform& xf, int32 childIndex) const
{
	b2Assert(childIndex < m_count);

	int32 i1 = childIndex;
	int32 i2 = childIndex + 1;
	if (i2 == m_count)
	{
		i2 = 0;
	}

	b2Vec2 v1 = b2Mul(xf, m_vertices[i1]);
	b2Vec2 v2 = b2Mul(xf, m_vertices[i2]);

	b2Vec2 lower = b2Min(v1, v2);
	b2Vec2 upper = b2Max(v1, v2);

	b2Vec2 r(m_radius, m_radius);
	aabb->lowerBound = lower - r;
	aabb->upperBound = upper + r;
}

void b2ChainShape::ComputeMass(b2MassData* massData, float density) const
{
	B2_NOT_USED(density);

	massData->mass = 0.0f;
	massData->center.SetZero();
	massData->I = 0.0f;
}
