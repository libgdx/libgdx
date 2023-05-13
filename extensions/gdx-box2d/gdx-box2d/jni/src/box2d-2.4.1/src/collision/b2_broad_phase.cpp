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

#include "box2d/b2_broad_phase.h"
#include <string.h>

b2BroadPhase::b2BroadPhase()
{
	m_proxyCount = 0;

	m_pairCapacity = 16;
	m_pairCount = 0;
	m_pairBuffer = (b2Pair*)b2Alloc(m_pairCapacity * sizeof(b2Pair));

	m_moveCapacity = 16;
	m_moveCount = 0;
	m_moveBuffer = (int32*)b2Alloc(m_moveCapacity * sizeof(int32));
}

b2BroadPhase::~b2BroadPhase()
{
	b2Free(m_moveBuffer);
	b2Free(m_pairBuffer);
}

int32 b2BroadPhase::CreateProxy(const b2AABB& aabb, void* userData)
{
	int32 proxyId = m_tree.CreateProxy(aabb, userData);
	++m_proxyCount;
	BufferMove(proxyId);
	return proxyId;
}

void b2BroadPhase::DestroyProxy(int32 proxyId)
{
	UnBufferMove(proxyId);
	--m_proxyCount;
	m_tree.DestroyProxy(proxyId);
}

void b2BroadPhase::MoveProxy(int32 proxyId, const b2AABB& aabb, const b2Vec2& displacement)
{
	bool buffer = m_tree.MoveProxy(proxyId, aabb, displacement);
	if (buffer)
	{
		BufferMove(proxyId);
	}
}

void b2BroadPhase::TouchProxy(int32 proxyId)
{
	BufferMove(proxyId);
}

void b2BroadPhase::BufferMove(int32 proxyId)
{
	if (m_moveCount == m_moveCapacity)
	{
		int32* oldBuffer = m_moveBuffer;
		m_moveCapacity *= 2;
		m_moveBuffer = (int32*)b2Alloc(m_moveCapacity * sizeof(int32));
		memcpy(m_moveBuffer, oldBuffer, m_moveCount * sizeof(int32));
		b2Free(oldBuffer);
	}

	m_moveBuffer[m_moveCount] = proxyId;
	++m_moveCount;
}

void b2BroadPhase::UnBufferMove(int32 proxyId)
{
	for (int32 i = 0; i < m_moveCount; ++i)
	{
		if (m_moveBuffer[i] == proxyId)
		{
			m_moveBuffer[i] = e_nullProxy;
		}
	}
}

// This is called from b2DynamicTree::Query when we are gathering pairs.
bool b2BroadPhase::QueryCallback(int32 proxyId)
{
	// A proxy cannot form a pair with itself.
	if (proxyId == m_queryProxyId)
	{
		return true;
	}

	const bool moved = m_tree.WasMoved(proxyId);
	if (moved && proxyId > m_queryProxyId)
	{
		// Both proxies are moving. Avoid duplicate pairs.
		return true;
	}

	// Grow the pair buffer as needed.
	if (m_pairCount == m_pairCapacity)
	{
		b2Pair* oldBuffer = m_pairBuffer;
		m_pairCapacity = m_pairCapacity + (m_pairCapacity >> 1);
		m_pairBuffer = (b2Pair*)b2Alloc(m_pairCapacity * sizeof(b2Pair));
		memcpy(m_pairBuffer, oldBuffer, m_pairCount * sizeof(b2Pair));
		b2Free(oldBuffer);
	}

	m_pairBuffer[m_pairCount].proxyIdA = b2Min(proxyId, m_queryProxyId);
	m_pairBuffer[m_pairCount].proxyIdB = b2Max(proxyId, m_queryProxyId);
	++m_pairCount;

	return true;
}
