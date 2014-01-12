/*
* Copyright (c) 2013 Google, Inc.
*
* This software is provided 'as-is', without any express or implied
* warranty.  In no event will the authors be held liable for any damages
* arising from the use of this software.
* Permission is granted to anyone to use this software for any purpose,
* including commercial applications, and to alter it and redistribute it
* freely, subject to the following restrictions:
* 1. The origin of this software must not be misrepresented; you must not
* claim that you wrote the original software. If you use this software
* in a product, an acknowledgment in the product documentation would be
* appreciated but is not required.
* 2. Altered source versions must be plainly marked as such, and must not be
* misrepresented as being the original software.
* 3. This notice may not be removed or altered from any source distribution.
*/
#include <Box2D/Particle/b2ParticleSystem.h>
#include <Box2D/Particle/b2ParticleGroup.h>
#include <Box2D/Particle/b2VoronoiDiagram.h>
#include <Box2D/Common/b2BlockAllocator.h>
#include <Box2D/Dynamics/b2World.h>
#include <Box2D/Dynamics/b2WorldCallbacks.h>
#include <Box2D/Dynamics/b2Body.h>
#include <Box2D/Dynamics/b2Fixture.h>
#include <Box2D/Collision/Shapes/b2Shape.h>
#include <algorithm>

static const uint32 xTruncBits = 12;
static const uint32 yTruncBits = 12;
static const uint32 tagBits = 8 * sizeof(uint32);
static const uint32 yOffset = 1 << (yTruncBits - 1);
static const uint32 yShift = tagBits - yTruncBits;
static const uint32 xShift = tagBits - yTruncBits - xTruncBits;
static const uint32 xScale = 1 << xShift;
static const uint32 xOffset = xScale * (1 << (xTruncBits - 1));
static const uint32 xMask = (1 << xTruncBits) - 1;
static const uint32 yMask = (1 << yTruncBits) - 1;

static inline uint32 computeTag(float32 x, float32 y)
{
	return ((uint32)(y + yOffset) << yShift) + (uint32)(xScale * x + xOffset);
}

static inline uint32 computeRelativeTag(uint32 tag, int32 x, int32 y)
{
	return tag + (y << yShift) + (x << xShift);
}

b2ParticleSystem::b2ParticleSystem()
{

	m_timestamp = 0;
	m_allParticleFlags = 0;
	m_allGroupFlags = 0;
	m_density = 1;
	m_inverseDensity = 1;
	m_gravityScale = 1;
	m_particleDiameter = 1;
	m_inverseDiameter = 1;
	m_squaredDiameter = 1;

	m_count = 0;
	m_internalAllocatedCapacity = 0;
	m_maxCount = 0;
	m_accumulationBuffer = NULL;
	m_accumulation2Buffer = NULL;
	m_depthBuffer = NULL;
	m_groupBuffer = NULL;

	m_proxyCount = 0;
	m_proxyCapacity = 0;
	m_proxyBuffer = NULL;

	m_contactCount = 0;
	m_contactCapacity = 0;
	m_contactBuffer = NULL;

	m_bodyContactCount = 0;
	m_bodyContactCapacity = 0;
	m_bodyContactBuffer = NULL;

	m_pairCount = 0;
	m_pairCapacity = 0;
	m_pairBuffer = NULL;

	m_triadCount = 0;
	m_triadCapacity = 0;
	m_triadBuffer = NULL;

	m_groupCount = 0;
	m_groupList = NULL;

	m_pressureStrength = 0.05f;
	m_dampingStrength = 1.0f;
	m_elasticStrength = 0.25f;
	m_springStrength = 0.25f;
	m_viscousStrength = 0.25f;
	m_surfaceTensionStrengthA = 0.1f;
	m_surfaceTensionStrengthB = 0.2f;
	m_powderStrength = 0.5f;
	m_ejectionStrength = 0.5f;
	m_colorMixingStrength = 0.5f;

	m_world = NULL;

}

b2ParticleSystem::~b2ParticleSystem()
{
}

// Reallocate a buffer
template <typename T> T* b2ParticleSystem::ReallocateBuffer(T* oldBuffer, int32 oldCapacity, int32 newCapacity)
{
	b2Assert(newCapacity > oldCapacity);
	T* newBuffer = (T*) m_world->m_blockAllocator.Allocate(sizeof(T) * newCapacity);
	memcpy(newBuffer, oldBuffer, sizeof(T) * oldCapacity);
	m_world->m_blockAllocator.Free(oldBuffer, sizeof(T) * oldCapacity);
	return newBuffer;
}

// Reallocate a buffer
template <typename T> T* b2ParticleSystem::ReallocateBuffer(T* buffer, int32 userSuppliedCapacity, int32 oldCapacity, int32 newCapacity, bool deferred)
{
	b2Assert(newCapacity > oldCapacity);
	// A 'deferred' buffer is reallocated only if it is not NULL.
	// If 'userSuppliedCapacity' is not zero, buffer is user supplied and must be kept.
	b2Assert(!userSuppliedCapacity || newCapacity <= userSuppliedCapacity);
	if ((!deferred || buffer) && !userSuppliedCapacity)
	{
		buffer = ReallocateBuffer(buffer, oldCapacity, newCapacity);
	}
	return buffer;
}

// Reallocate a buffer
template <typename T> T* b2ParticleSystem::ReallocateBuffer(ParticleBuffer<T>* buffer, int32 oldCapacity, int32 newCapacity, bool deferred)
{
	b2Assert(newCapacity > oldCapacity);
	return ReallocateBuffer(buffer->data, buffer->userSuppliedCapacity, oldCapacity, newCapacity, deferred);
}

template <typename T> T* b2ParticleSystem::RequestParticleBuffer(T* buffer)
{
	if (!buffer)
	{
		buffer = (T*) (m_world->m_blockAllocator.Allocate(sizeof(T) * m_internalAllocatedCapacity));
		memset(buffer, 0, sizeof(T) * m_internalAllocatedCapacity);
	}
	return buffer;
}

static int32 LimitCapacity(int32 capacity, int32 maxCount)
{
	return maxCount && capacity > maxCount ? maxCount : capacity;
}

int32 b2ParticleSystem::CreateParticle(const b2ParticleDef& def)
{
	if (m_count >= m_internalAllocatedCapacity)
	{
		int32 capacity = m_count ? 2 * m_count : b2_minParticleBufferCapacity;
		capacity = LimitCapacity(capacity, m_maxCount);
		capacity = LimitCapacity(capacity, m_flagsBuffer.userSuppliedCapacity);
		capacity = LimitCapacity(capacity, m_positionBuffer.userSuppliedCapacity);
		capacity = LimitCapacity(capacity, m_velocityBuffer.userSuppliedCapacity);
		capacity = LimitCapacity(capacity, m_colorBuffer.userSuppliedCapacity);
		capacity = LimitCapacity(capacity, m_userDataBuffer.userSuppliedCapacity);
		if (m_internalAllocatedCapacity < capacity)
		{
			m_flagsBuffer.data = ReallocateBuffer(&m_flagsBuffer, m_internalAllocatedCapacity, capacity, false);
			m_positionBuffer.data = ReallocateBuffer(&m_positionBuffer, m_internalAllocatedCapacity, capacity, false);
			m_velocityBuffer.data = ReallocateBuffer(&m_velocityBuffer, m_internalAllocatedCapacity, capacity, false);
			m_accumulationBuffer = ReallocateBuffer(m_accumulationBuffer, 0, m_internalAllocatedCapacity, capacity, false);
			m_accumulation2Buffer = ReallocateBuffer(m_accumulation2Buffer, 0, m_internalAllocatedCapacity, capacity, true);
			m_depthBuffer = ReallocateBuffer(m_depthBuffer, 0, m_internalAllocatedCapacity, capacity, true);
			m_colorBuffer.data = ReallocateBuffer(&m_colorBuffer, m_internalAllocatedCapacity, capacity, true);
			m_groupBuffer = ReallocateBuffer(m_groupBuffer, 0, m_internalAllocatedCapacity, capacity, false);
			m_userDataBuffer.data = ReallocateBuffer(&m_userDataBuffer, m_internalAllocatedCapacity, capacity, true);
			m_internalAllocatedCapacity = capacity;
		}
	}
	if (m_count >= m_internalAllocatedCapacity)
	{
		return b2_invalidParticleIndex;
	}
	int32 index = m_count++;
	m_flagsBuffer.data[index] = def.flags;
	m_positionBuffer.data[index] = def.position;
	m_velocityBuffer.data[index] = def.velocity;
	m_groupBuffer[index] = NULL;
	if (m_depthBuffer)
	{
		m_depthBuffer[index] = 0;
	}
	if (m_colorBuffer.data || !def.color.IsZero())
	{
		m_colorBuffer.data = RequestParticleBuffer(m_colorBuffer.data);
		m_colorBuffer.data[index] = def.color;
	}
	if (m_userDataBuffer.data || def.userData)
	{
		m_userDataBuffer.data= RequestParticleBuffer(m_userDataBuffer.data);
		m_userDataBuffer.data[index] = def.userData;
	}
	if (m_proxyCount >= m_proxyCapacity)
	{
		int32 oldCapacity = m_proxyCapacity;
		int32 newCapacity = m_proxyCount ? 2 * m_proxyCount : b2_minParticleBufferCapacity;
		m_proxyBuffer = ReallocateBuffer(m_proxyBuffer, oldCapacity, newCapacity);
		m_proxyCapacity = newCapacity;
	}
	m_proxyBuffer[m_proxyCount++].index = index;
	return index;
}

void b2ParticleSystem::DestroyParticle(
	int32 index, bool callDestructionListener)
{
	uint32 flags = b2_zombieParticle;
	if (callDestructionListener)
	{
		flags |= b2_destructionListener;
	}
	m_flagsBuffer.data[index] |= flags;
}

int32 b2ParticleSystem::DestroyParticlesInShape(
	const b2Shape& shape, const b2Transform& xf,
	bool callDestructionListener)
{
	class DestroyParticlesInShapeCallback : public b2QueryCallback
	{
	public:
		DestroyParticlesInShapeCallback(
			b2ParticleSystem* system, const b2Shape& shape,
			const b2Transform& xf, bool callDestructionListener)
		{
			m_system = system;
			m_shape = &shape;
			m_xf = xf;
			m_callDestructionListener = callDestructionListener;
			m_destroyed = 0;
		}

		bool ReportFixture(b2Fixture* fixture)
		{
			return false;
		}

		bool ReportParticle(int32 index)
		{
			b2Assert(index >=0 && index < m_system->m_count);
			if (m_shape->TestPoint(m_xf, m_system->m_positionBuffer.data[index]))
			{
				m_system->DestroyParticle(index, m_callDestructionListener);
				m_destroyed++;
			}
			return true;
		}

		int32 Destroyed() { return m_destroyed; }

	private:
		b2ParticleSystem* m_system;
		const b2Shape* m_shape;
		b2Transform m_xf;
		bool m_callDestructionListener;
		int32 m_destroyed;
	} callback(this, shape, xf, callDestructionListener);
	b2AABB aabb;
	shape.ComputeAABB(&aabb, xf, 0);
	m_world->QueryAABB(&callback, aabb);
	return callback.Destroyed();
}

void b2ParticleSystem::DestroyParticlesInGroup(
	b2ParticleGroup* group, bool callDestructionListener)
{
	for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++) {
		DestroyParticle(i, callDestructionListener);
	}
}

b2ParticleGroup* b2ParticleSystem::CreateParticleGroup(const b2ParticleGroupDef& groupDef)
{
	float32 stride = GetParticleStride();
	b2Transform identity;
	identity.SetIdentity();
	b2Transform transform = identity;
	int32 firstIndex = m_count;
	if (groupDef.shape)
	{
		b2ParticleDef particleDef;
		particleDef.flags = groupDef.flags;
		particleDef.color = groupDef.color;
		particleDef.userData = groupDef.userData;
		const b2Shape *shape = groupDef.shape;
		transform.Set(groupDef.position, groupDef.angle);
		b2AABB aabb;
		int32 childCount = shape->GetChildCount();
		for (int32 childIndex = 0; childIndex < childCount; childIndex++)
		{
			if (childIndex == 0)
			{
				shape->ComputeAABB(&aabb, identity, childIndex);
			}
			else
			{
				b2AABB childAABB;
				shape->ComputeAABB(&childAABB, identity, childIndex);
				aabb.Combine(childAABB);
			}
		}
		for (float32 y = floorf(aabb.lowerBound.y / stride) * stride; y < aabb.upperBound.y; y += stride)
		{
			for (float32 x = floorf(aabb.lowerBound.x / stride) * stride; x < aabb.upperBound.x; x += stride)
			{
				b2Vec2 p(x, y);
				if (shape->TestPoint(identity, p))
				{
					p = b2Mul(transform, p);
					particleDef.position = p;
					particleDef.velocity =
						groupDef.linearVelocity +
						b2Cross(groupDef.angularVelocity, p - groupDef.position);
					CreateParticle(particleDef);
				}
			}
		}
	}
	int32 lastIndex = m_count;

	void* mem = m_world->m_blockAllocator.Allocate(sizeof(b2ParticleGroup));
	b2ParticleGroup* group = new (mem) b2ParticleGroup();
	group->m_system = this;
	group->m_firstIndex = firstIndex;
	group->m_lastIndex = lastIndex;
	group->m_groupFlags = groupDef.groupFlags;
	group->m_strength = groupDef.strength;
	group->m_userData = groupDef.userData;
	group->m_transform = transform;
	group->m_destroyAutomatically = groupDef.destroyAutomatically;
	group->m_prev = NULL;
	group->m_next = m_groupList;
	if (m_groupList)
	{
		m_groupList->m_prev = group;
	}
	m_groupList = group;
	++m_groupCount;
	for (int32 i = firstIndex; i < lastIndex; i++)
	{
		m_groupBuffer[i] = group;
	}

	UpdateContacts(true);
	if (groupDef.flags & k_pairFlags)
	{
		for (int32 k = 0; k < m_contactCount; k++)
		{
			const b2ParticleContact& contact = m_contactBuffer[k];
			int32 a = contact.indexA;
			int32 b = contact.indexB;
			if (a > b) b2Swap(a, b);
			if (firstIndex <= a && b < lastIndex)
			{
				if (m_pairCount >= m_pairCapacity)
				{
					int32 oldCapacity = m_pairCapacity;
					int32 newCapacity = m_pairCount ? 2 * m_pairCount : b2_minParticleBufferCapacity;
					m_pairBuffer = ReallocateBuffer(m_pairBuffer, oldCapacity, newCapacity);
					m_pairCapacity = newCapacity;
				}
				Pair& pair = m_pairBuffer[m_pairCount];
				pair.indexA = a;
				pair.indexB = b;
				pair.flags = contact.flags;
				pair.strength = groupDef.strength;
				pair.distance = b2Distance(
					m_positionBuffer.data[a],
					m_positionBuffer.data[b]);
				m_pairCount++;
			}
		}
	}
	if (groupDef.flags & k_triadFlags)
	{
		b2VoronoiDiagram diagram(
			&m_world->m_stackAllocator, lastIndex - firstIndex);
		for (int32 i = firstIndex; i < lastIndex; i++)
		{
			diagram.AddGenerator(m_positionBuffer.data[i], i);
		}
		diagram.Generate(stride / 2);
		CreateParticleGroupCallback callback;
		callback.system = this;
		callback.def = &groupDef;
		callback.firstIndex = firstIndex;
		diagram.GetNodes(callback);
	}
	if (groupDef.groupFlags & b2_solidParticleGroup)
	{
		ComputeDepthForGroup(group);
	}

	return group;
}

void b2ParticleSystem::CreateParticleGroupCallback::operator()(int32 a, int32 b, int32 c) const
{
	const b2Vec2& pa = system->m_positionBuffer.data[a];
	const b2Vec2& pb = system->m_positionBuffer.data[b];
	const b2Vec2& pc = system->m_positionBuffer.data[c];
	b2Vec2 dab = pa - pb;
	b2Vec2 dbc = pb - pc;
	b2Vec2 dca = pc - pa;
	float32 maxDistanceSquared = b2_maxTriadDistanceSquared * system->m_squaredDiameter;
	if (b2Dot(dab, dab) < maxDistanceSquared &&
		b2Dot(dbc, dbc) < maxDistanceSquared &&
		b2Dot(dca, dca) < maxDistanceSquared)
	{
		if (system->m_triadCount >= system->m_triadCapacity)
		{
			int32 oldCapacity = system->m_triadCapacity;
			int32 newCapacity = system->m_triadCount ? 2 * system->m_triadCount : b2_minParticleBufferCapacity;
			system->m_triadBuffer = system->ReallocateBuffer(system->m_triadBuffer, oldCapacity, newCapacity);
			system->m_triadCapacity = newCapacity;
		}
		Triad& triad = system->m_triadBuffer[system->m_triadCount];
		triad.indexA = a;
		triad.indexB = b;
		triad.indexC = c;
		triad.flags =
			system->m_flagsBuffer.data[a] |
			system->m_flagsBuffer.data[b] |
			system->m_flagsBuffer.data[c];
		triad.strength = def->strength;
		b2Vec2 midPoint = (float32) 1 / 3 * (pa + pb + pc);
		triad.pa = pa - midPoint;
		triad.pb = pb - midPoint;
		triad.pc = pc - midPoint;
		triad.ka = -b2Dot(dca, dab);
		triad.kb = -b2Dot(dab, dbc);
		triad.kc = -b2Dot(dbc, dca);
		triad.s = b2Cross(pa, pb) + b2Cross(pb, pc) + b2Cross(pc, pa);
		system->m_triadCount++;
	}
};

void b2ParticleSystem::JoinParticleGroups(b2ParticleGroup* groupA, b2ParticleGroup* groupB)
{
	b2Assert(groupA != groupB);
	RotateBuffer(groupB->m_firstIndex, groupB->m_lastIndex, m_count);
	b2Assert(groupB->m_lastIndex == m_count);
	RotateBuffer(groupA->m_firstIndex, groupA->m_lastIndex, groupB->m_firstIndex);
	b2Assert(groupA->m_lastIndex == groupB->m_firstIndex);

	uint32 particleFlags = 0;
	for (int32 i = groupA->m_firstIndex; i < groupB->m_lastIndex; i++)
	{
		particleFlags |= m_flagsBuffer.data[i];
	}

	UpdateContacts(true);
	if (particleFlags & k_pairFlags)
	{
		for (int32 k = 0; k < m_contactCount; k++)
		{
			const b2ParticleContact& contact = m_contactBuffer[k];
			int32 a = contact.indexA;
			int32 b = contact.indexB;
			if (a > b) b2Swap(a, b);
			if (groupA->m_firstIndex <= a && a < groupA->m_lastIndex &&
				groupB->m_firstIndex <= b && b < groupB->m_lastIndex)
			{
				if (m_pairCount >= m_pairCapacity)
				{
					int32 oldCapacity = m_pairCapacity;
					int32 newCapacity = m_pairCount ? 2 * m_pairCount : b2_minParticleBufferCapacity;
					m_pairBuffer = ReallocateBuffer(m_pairBuffer, oldCapacity, newCapacity);
					m_pairCapacity = newCapacity;
				}
				Pair& pair = m_pairBuffer[m_pairCount];
				pair.indexA = a;
				pair.indexB = b;
				pair.flags = contact.flags;
				pair.strength = b2Min(groupA->m_strength, groupB->m_strength);
				pair.distance = b2Distance(m_positionBuffer.data[a], m_positionBuffer.data[b]);
				m_pairCount++;
			}
		}
	}
	if (particleFlags & k_triadFlags)
	{
		b2VoronoiDiagram diagram(
			&m_world->m_stackAllocator,
			groupB->m_lastIndex - groupA->m_firstIndex);
		for (int32 i = groupA->m_firstIndex; i < groupB->m_lastIndex; i++)
		{
			if (!(m_flagsBuffer.data[i] & b2_zombieParticle))
			{
				diagram.AddGenerator(m_positionBuffer.data[i], i);
			}
		}
		diagram.Generate(GetParticleStride() / 2);
		JoinParticleGroupsCallback callback;
		callback.system = this;
		callback.groupA = groupA;
		callback.groupB = groupB;
		diagram.GetNodes(callback);
	}

	for (int32 i = groupB->m_firstIndex; i < groupB->m_lastIndex; i++)
	{
		m_groupBuffer[i] = groupA;
	}
	uint32 groupFlags = groupA->m_groupFlags | groupB->m_groupFlags;
	groupA->m_groupFlags = groupFlags;
	groupA->m_lastIndex = groupB->m_lastIndex;
	groupB->m_firstIndex = groupB->m_lastIndex;
	DestroyParticleGroup(groupB);

	if (groupFlags & b2_solidParticleGroup)
	{
		ComputeDepthForGroup(groupA);
	}
}

void b2ParticleSystem::JoinParticleGroupsCallback::operator()(int32 a, int32 b, int32 c) const
{
	// Create a triad if it will contain particles from both groups.
	int32 countA =
		(a < groupB->m_firstIndex) +
		(b < groupB->m_firstIndex) +
		(c < groupB->m_firstIndex);
	if (countA > 0 && countA < 3)
	{
		uint32 af = system->m_flagsBuffer.data[a];
		uint32 bf = system->m_flagsBuffer.data[b];
		uint32 cf = system->m_flagsBuffer.data[c];
		if (af & bf & cf & k_triadFlags)
		{
			const b2Vec2& pa = system->m_positionBuffer.data[a];
			const b2Vec2& pb = system->m_positionBuffer.data[b];
			const b2Vec2& pc = system->m_positionBuffer.data[c];
			b2Vec2 dab = pa - pb;
			b2Vec2 dbc = pb - pc;
			b2Vec2 dca = pc - pa;
			float32 maxDistanceSquared = b2_maxTriadDistanceSquared * system->m_squaredDiameter;
			if (b2Dot(dab, dab) < maxDistanceSquared &&
				b2Dot(dbc, dbc) < maxDistanceSquared &&
				b2Dot(dca, dca) < maxDistanceSquared)
			{
				if (system->m_triadCount >= system->m_triadCapacity)
				{
					int32 oldCapacity = system->m_triadCapacity;
					int32 newCapacity = system->m_triadCount ? 2 * system->m_triadCount : b2_minParticleBufferCapacity;
					system->m_triadBuffer = system->ReallocateBuffer(system->m_triadBuffer, oldCapacity, newCapacity);
					system->m_triadCapacity = newCapacity;
				}
				Triad& triad = system->m_triadBuffer[system->m_triadCount];
				triad.indexA = a;
				triad.indexB = b;
				triad.indexC = c;
				triad.flags = af | bf | cf;
				triad.strength = b2Min(groupA->m_strength, groupB->m_strength);
				b2Vec2 midPoint = (float32) 1 / 3 * (pa + pb + pc);
				triad.pa = pa - midPoint;
				triad.pb = pb - midPoint;
				triad.pc = pc - midPoint;
				triad.ka = -b2Dot(dca, dab);
				triad.kb = -b2Dot(dab, dbc);
				triad.kc = -b2Dot(dbc, dca);
				triad.s = b2Cross(pa, pb) + b2Cross(pb, pc) + b2Cross(pc, pa);
				system->m_triadCount++;
			}
		}
	}
};

// Only called from SolveZombie() or JoinParticleGroups().
void b2ParticleSystem::DestroyParticleGroup(b2ParticleGroup* group)
{
	b2Assert(m_groupCount > 0);
	b2Assert(group);

	if (m_world->m_destructionListener)
	{
		m_world->m_destructionListener->SayGoodbye(group);
	}

	for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++)
	{
		m_groupBuffer[i] = NULL;
	}

	if (group->m_prev)
	{
		group->m_prev->m_next = group->m_next;
	}
	if (group->m_next)
	{
		group->m_next->m_prev = group->m_prev;
	}
	if (group == m_groupList)
	{
		m_groupList = group->m_next;
	}

	--m_groupCount;
	group->~b2ParticleGroup();
	m_world->m_blockAllocator.Free(group, sizeof(b2ParticleGroup));
}

void b2ParticleSystem::ComputeDepthForGroup(b2ParticleGroup* group)
{
	for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++)
	{
		m_accumulationBuffer[i] = 0;
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		if (a >= group->m_firstIndex && a < group->m_lastIndex &&
			b >= group->m_firstIndex && b < group->m_lastIndex)
		{
			float32 w = contact.weight;
			m_accumulationBuffer[a] += w;
			m_accumulationBuffer[b] += w;
		}
	}
	m_depthBuffer = RequestParticleBuffer(m_depthBuffer);
	for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++)
	{
		float32 w = m_accumulationBuffer[i];
		m_depthBuffer[i] = w < 0.8f ? 0 : b2_maxFloat;
	}
	int32 interationCount = group->GetParticleCount();
	for (int32 t = 0; t < interationCount; t++)
	{
		bool updated = false;
		for (int32 k = 0; k < m_contactCount; k++)
		{
			const b2ParticleContact& contact = m_contactBuffer[k];
			int32 a = contact.indexA;
			int32 b = contact.indexB;
			if (a >= group->m_firstIndex && a < group->m_lastIndex &&
				b >= group->m_firstIndex && b < group->m_lastIndex)
			{
				float32 r = 1 - contact.weight;
				float32& ap0 = m_depthBuffer[a];
				float32& bp0 = m_depthBuffer[b];
				float32 ap1 = bp0 + r;
				float32 bp1 = ap0 + r;
				if (ap0 > ap1)
				{
					ap0 = ap1;
					updated = true;
				}
				if (bp0 > bp1)
				{
					bp0 = bp1;
					updated = true;
				}
			}
		}
		if (!updated)
		{
			break;
		}
	}
	for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++)
	{
		float32& p = m_depthBuffer[i];
		if (p < b2_maxFloat)
		{
			p *= m_particleDiameter;
		}
		else
		{
			p = 0;
		}
	}

}

inline void b2ParticleSystem::AddContact(int32 a, int32 b)
{
	b2Vec2 d = m_positionBuffer.data[b] - m_positionBuffer.data[a];
	float32 d2 = b2Dot(d, d);
	if (d2 < m_squaredDiameter)
	{
		if (m_contactCount >= m_contactCapacity)
		{
			int32 oldCapacity = m_contactCapacity;
			int32 newCapacity = m_contactCount ? 2 * m_contactCount : b2_minParticleBufferCapacity;
			m_contactBuffer = ReallocateBuffer(m_contactBuffer, oldCapacity, newCapacity);
			m_contactCapacity = newCapacity;
		}
		float32 invD = b2InvSqrt(d2);
		b2ParticleContact& contact = m_contactBuffer[m_contactCount];
		contact.indexA = a;
		contact.indexB = b;
		contact.flags = m_flagsBuffer.data[a] | m_flagsBuffer.data[b];
		contact.weight = 1 - d2 * invD * m_inverseDiameter;
		contact.normal = invD * d;
		m_contactCount++;
	}
}

static bool b2ParticleContactIsZombie(const b2ParticleContact& contact)
{
	return (contact.flags & b2_zombieParticle) == b2_zombieParticle;
}

void b2ParticleSystem::UpdateContacts(bool exceptZombie)
{
	Proxy* beginProxy = m_proxyBuffer;
	Proxy* endProxy = beginProxy + m_proxyCount;
	for (Proxy* proxy = beginProxy; proxy < endProxy; ++proxy)
	{
		int32 i = proxy->index;
		b2Vec2 p = m_positionBuffer.data[i];
		proxy->tag = computeTag(m_inverseDiameter * p.x, m_inverseDiameter * p.y);
	}
	std::sort(beginProxy, endProxy);
	m_contactCount = 0;
	for (Proxy *a = beginProxy, *c = beginProxy; a < endProxy; a++)
	{
		uint32 rightTag = computeRelativeTag(a->tag, 1, 0);
		for (Proxy* b = a + 1; b < endProxy; b++)
		{
			if (rightTag < b->tag) break;
			AddContact(a->index, b->index);
		}
		uint32 bottomLeftTag = computeRelativeTag(a->tag, -1, 1);
		for (; c < endProxy; c++)
		{
			if (bottomLeftTag <= c->tag) break;
		}
		uint32 bottomRightTag = computeRelativeTag(a->tag, 1, 1);
		for (Proxy* b = c; b < endProxy; b++)
		{
			if (bottomRightTag < b->tag) break;
			AddContact(a->index, b->index);
		}
	}
	if (exceptZombie)
	{
		b2ParticleContact* lastContact = std::remove_if(
			m_contactBuffer, m_contactBuffer + m_contactCount,
			b2ParticleContactIsZombie);
		m_contactCount = (int32) (lastContact - m_contactBuffer);
	}
}

void b2ParticleSystem::UpdateBodyContacts()
{
	b2AABB aabb;
	aabb.lowerBound.x = +b2_maxFloat;
	aabb.lowerBound.y = +b2_maxFloat;
	aabb.upperBound.x = -b2_maxFloat;
	aabb.upperBound.y = -b2_maxFloat;
	for (int32 i = 0; i < m_count; i++)
	{
		b2Vec2 p = m_positionBuffer.data[i];
		aabb.lowerBound = b2Min(aabb.lowerBound, p);
		aabb.upperBound = b2Max(aabb.upperBound, p);
	}
	aabb.lowerBound.x -= m_particleDiameter;
	aabb.lowerBound.y -= m_particleDiameter;
	aabb.upperBound.x += m_particleDiameter;
	aabb.upperBound.y += m_particleDiameter;
	m_bodyContactCount = 0;
	class UpdateBodyContactsCallback : public b2QueryCallback
	{
		bool ReportFixture(b2Fixture* fixture)
		{
			if (fixture->IsSensor())
			{
				return true;
			}
			const b2Shape* shape = fixture->GetShape();
			b2Body* b = fixture->GetBody();
			b2Vec2 bp = b->GetWorldCenter();
			float32 bm = b->GetMass();
			float32 bI = b->GetInertia() - bm * b->GetLocalCenter().LengthSquared();
			float32 invBm = bm > 0 ? 1 / bm : 0;
			float32 invBI = bI > 0 ? 1 / bI : 0;
			int32 childCount = shape->GetChildCount();
			for (int32 childIndex = 0; childIndex < childCount; childIndex++)
			{
				b2AABB aabb = fixture->GetAABB(childIndex);
				aabb.lowerBound.x -= m_system->m_particleDiameter;
				aabb.lowerBound.y -= m_system->m_particleDiameter;
				aabb.upperBound.x += m_system->m_particleDiameter;
				aabb.upperBound.y += m_system->m_particleDiameter;
				Proxy* beginProxy = m_system->m_proxyBuffer;
				Proxy* endProxy = beginProxy + m_system->m_proxyCount;
				Proxy* firstProxy = std::lower_bound(
					beginProxy, endProxy,
					computeTag(
						m_system->m_inverseDiameter * aabb.lowerBound.x,
						m_system->m_inverseDiameter * aabb.lowerBound.y));
				Proxy* lastProxy = std::upper_bound(
					firstProxy, endProxy,
					computeTag(
						m_system->m_inverseDiameter * aabb.upperBound.x,
						m_system->m_inverseDiameter * aabb.upperBound.y));
				for (Proxy* proxy = firstProxy; proxy != lastProxy; ++proxy)
				{
					int32 a = proxy->index;
					b2Vec2 ap = m_system->m_positionBuffer.data[a];
					if (aabb.lowerBound.x <= ap.x && ap.x <= aabb.upperBound.x &&
						aabb.lowerBound.y <= ap.y && ap.y <= aabb.upperBound.y)
					{
						float32 d;
						b2Vec2 n;
						fixture->ComputeDistance(ap, &d, &n, childIndex);
						if (d < m_system->m_particleDiameter)
						{
							float32 invAm =
								m_system->m_flagsBuffer.data[a] & b2_wallParticle ?
								0 : m_system->GetParticleInvMass();
							b2Vec2 rp = ap - bp;
							float32 rpn = b2Cross(rp, n);
							if (m_system->m_bodyContactCount >= m_system->m_bodyContactCapacity)
							{
								int32 oldCapacity = m_system->m_bodyContactCapacity;
								int32 newCapacity = m_system->m_bodyContactCount ? 2 * m_system->m_bodyContactCount : b2_minParticleBufferCapacity;
								m_system->m_bodyContactBuffer = m_system->ReallocateBuffer(m_system->m_bodyContactBuffer, oldCapacity, newCapacity);
								m_system->m_bodyContactCapacity = newCapacity;
							}
							b2ParticleBodyContact& contact = m_system->m_bodyContactBuffer[m_system->m_bodyContactCount];
							contact.index = a;
							contact.body = b;
							contact.weight = 1 - d * m_system->m_inverseDiameter;
							contact.normal = -n;
							contact.mass = 1 / (invAm + invBm + invBI * rpn * rpn);
							m_system->m_bodyContactCount++;
						}
					}
				}
			}
			return true;
		}

		b2ParticleSystem* m_system;

	public:
		UpdateBodyContactsCallback(b2ParticleSystem* system)
		{
			m_system = system;
		}
	} callback(this);
	m_world->QueryAABB(&callback, aabb);
}

void b2ParticleSystem::SolveCollision(const b2TimeStep& step)
{
	b2AABB aabb;
	aabb.lowerBound.x = +b2_maxFloat;
	aabb.lowerBound.y = +b2_maxFloat;
	aabb.upperBound.x = -b2_maxFloat;
	aabb.upperBound.y = -b2_maxFloat;
	for (int32 i = 0; i < m_count; i++)
	{
		b2Vec2 v = m_velocityBuffer.data[i];
		b2Vec2 p1 = m_positionBuffer.data[i];
		b2Vec2 p2 = p1 + step.dt * v;
		aabb.lowerBound = b2Min(aabb.lowerBound, b2Min(p1, p2));
		aabb.upperBound = b2Max(aabb.upperBound, b2Max(p1, p2));
	}
	class SolveCollisionCallback : public b2QueryCallback
	{
		bool ReportFixture(b2Fixture* fixture)
		{
			if (fixture->IsSensor())
			{
				return true;
			}
			const b2Shape* shape = fixture->GetShape();
			b2Body* body = fixture->GetBody();
			Proxy* beginProxy = m_system->m_proxyBuffer;
			Proxy* endProxy = beginProxy + m_system->m_proxyCount;
			int32 childCount = shape->GetChildCount();
			for (int32 childIndex = 0; childIndex < childCount; childIndex++)
			{
				b2AABB aabb = fixture->GetAABB(childIndex);
				aabb.lowerBound.x -= m_system->m_particleDiameter;
				aabb.lowerBound.y -= m_system->m_particleDiameter;
				aabb.upperBound.x += m_system->m_particleDiameter;
				aabb.upperBound.y += m_system->m_particleDiameter;
				Proxy* firstProxy = std::lower_bound(
					beginProxy, endProxy,
					computeTag(
						m_system->m_inverseDiameter * aabb.lowerBound.x,
						m_system->m_inverseDiameter * aabb.lowerBound.y));
				Proxy* lastProxy = std::upper_bound(
					firstProxy, endProxy,
					computeTag(
						m_system->m_inverseDiameter * aabb.upperBound.x,
						m_system->m_inverseDiameter * aabb.upperBound.y));
				for (Proxy* proxy = firstProxy; proxy != lastProxy; ++proxy)
				{
					int32 a = proxy->index;
					b2Vec2 ap = m_system->m_positionBuffer.data[a];
					if (aabb.lowerBound.x <= ap.x && ap.x <= aabb.upperBound.x &&
						aabb.lowerBound.y <= ap.y && ap.y <= aabb.upperBound.y)
					{
						b2Vec2 av = m_system->m_velocityBuffer.data[a];
						b2RayCastOutput output;
						b2RayCastInput input;
						input.p1 = b2Mul(body->m_xf, b2MulT(body->m_xf0, ap));
						input.p2 = ap + m_step.dt * av;
						input.maxFraction = 1;
						if (fixture->RayCast(&output, input, childIndex))
						{
							b2Vec2 p =
								(1 - output.fraction) * input.p1 +
								output.fraction * input.p2 +
								b2_linearSlop * output.normal;
							b2Vec2 v = m_step.inv_dt * (p - ap);
							m_system->m_velocityBuffer.data[a] = v;
							b2Vec2 f = m_system->GetParticleMass() * (av - v);
							f = b2Dot(f, output.normal) * output.normal;
							body->ApplyLinearImpulse(f, p, true);
						}
					}
				}
			}
			return true;
		}

		b2ParticleSystem* m_system;
		b2TimeStep m_step;

	public:
		SolveCollisionCallback(b2ParticleSystem* system, const b2TimeStep& step)
		{
			m_system = system;
			m_step = step;
		}
	} callback(this, step);
	m_world->QueryAABB(&callback, aabb);
}

void b2ParticleSystem::Solve(const b2TimeStep& step)
{
	++m_timestamp;
	if (m_count == 0)
	{
		return;
	}
	m_allParticleFlags = 0;
	for (int32 i = 0; i < m_count; i++)
	{
		m_allParticleFlags |= m_flagsBuffer.data[i];
	}
	if (m_allParticleFlags & b2_zombieParticle)
	{
		SolveZombie();
	}
	m_allGroupFlags = 0;
	for (const b2ParticleGroup* group = m_groupList; group; group = group->GetNext())
	{
		m_allGroupFlags |= group->m_groupFlags;
	}
	b2Vec2 gravity = step.dt * m_gravityScale * m_world->GetGravity();
	float32 criticalVelocytySquared = GetCriticalVelocitySquared(step);
	for (int32 i = 0; i < m_count; i++)
	{
		b2Vec2& v = m_velocityBuffer.data[i];
		v += gravity;
		float32 v2 = b2Dot(v, v);
		if (v2 > criticalVelocytySquared)
		{
			v *= b2Sqrt(criticalVelocytySquared / v2);
		}
	}
	SolveCollision(step);
	if (m_allGroupFlags & b2_rigidParticleGroup)
	{
		SolveRigid(step);
	}
	if (m_allParticleFlags & b2_wallParticle)
	{
		SolveWall(step);
	}
	for (int32 i = 0; i < m_count; i++)
	{
		m_positionBuffer.data[i] += step.dt * m_velocityBuffer.data[i];
	}
	UpdateBodyContacts();
	UpdateContacts(false);
	if (m_allParticleFlags & b2_viscousParticle)
	{
		SolveViscous(step);
	}
	if (m_allParticleFlags & b2_powderParticle)
	{
		SolvePowder(step);
	}
	if (m_allParticleFlags & b2_tensileParticle)
	{
		SolveTensile(step);
	}
	if (m_allParticleFlags & b2_elasticParticle)
	{
		SolveElastic(step);
	}
	if (m_allParticleFlags & b2_springParticle)
	{
		SolveSpring(step);
	}
	if (m_allGroupFlags & b2_solidParticleGroup)
	{
		SolveSolid(step);
	}
	if (m_allParticleFlags & b2_colorMixingParticle)
	{
		SolveColorMixing(step);
	}
	SolvePressure(step);
	SolveDamping(step);
}

void b2ParticleSystem::SolvePressure(const b2TimeStep& step)
{
	// calculates the sum of contact-weights for each particle
	// that means dimensionless density
	for (int32 i = 0; i < m_count; i++)
	{
		m_accumulationBuffer[i] = 0;
	}
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		const b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		int32 a = contact.index;
		float32 w = contact.weight;
		m_accumulationBuffer[a] += w;
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		float32 w = contact.weight;
		m_accumulationBuffer[a] += w;
		m_accumulationBuffer[b] += w;
	}
	// ignores powder particles
	if (m_allParticleFlags & k_noPressureFlags)
	{
		for (int32 i = 0; i < m_count; i++)
		{
			if (m_flagsBuffer.data[i] & k_noPressureFlags)
			{
				m_accumulationBuffer[i] = 0;
			}
		}
	}
	// calculates pressure as a linear function of density
	float32 pressurePerWeight = m_pressureStrength * GetCriticalPressure(step);
	for (int32 i = 0; i < m_count; i++)
	{
		float32 w = m_accumulationBuffer[i];
		float32 h = pressurePerWeight * b2Max(0.0f, b2Min(w, b2_maxParticleWeight) - b2_minParticleWeight);
		m_accumulationBuffer[i] = h;
	}
	// applies pressure between each particles in contact
	float32 velocityPerPressure = step.dt / (m_density * m_particleDiameter);
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		const b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		int32 a = contact.index;
		b2Body* b = contact.body;
		float32 w = contact.weight;
		float32 m = contact.mass;
		b2Vec2 n = contact.normal;
		b2Vec2 p = m_positionBuffer.data[a];
		float32 h = m_accumulationBuffer[a] + pressurePerWeight * w;
		b2Vec2 f = velocityPerPressure * w * m * h * n;
		m_velocityBuffer.data[a] -= GetParticleInvMass() * f;
		b->ApplyLinearImpulse(f, p, true);
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		float32 w = contact.weight;
		b2Vec2 n = contact.normal;
		float32 h = m_accumulationBuffer[a] + m_accumulationBuffer[b];
		b2Vec2 f = velocityPerPressure * w * h * n;
		m_velocityBuffer.data[a] -= f;
		m_velocityBuffer.data[b] += f;
	}
}

void b2ParticleSystem::SolveDamping(const b2TimeStep& step)
{
	// reduces normal velocity of each contact
	float32 damping = m_dampingStrength;
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		const b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		int32 a = contact.index;
		b2Body* b = contact.body;
		float32 w = contact.weight;
		float32 m = contact.mass;
		b2Vec2 n = contact.normal;
		b2Vec2 p = m_positionBuffer.data[a];
		b2Vec2 v = b->GetLinearVelocityFromWorldPoint(p) - m_velocityBuffer.data[a];
		float32 vn = b2Dot(v, n);
		if (vn < 0)
		{
			b2Vec2 f = damping * w * m * vn * n;
			m_velocityBuffer.data[a] += GetParticleInvMass() * f;
			b->ApplyLinearImpulse(-f, p, true);
		}
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		float32 w = contact.weight;
		b2Vec2 n = contact.normal;
		b2Vec2 v = m_velocityBuffer.data[b] - m_velocityBuffer.data[a];
		float32 vn = b2Dot(v, n);
		if (vn < 0)
		{
			b2Vec2 f = damping * w * vn * n;
			m_velocityBuffer.data[a] += f;
			m_velocityBuffer.data[b] -= f;
		}
	}
}

void b2ParticleSystem::SolveWall(const b2TimeStep& step)
{
	for (int32 i = 0; i < m_count; i++)
	{
		if (m_flagsBuffer.data[i] & b2_wallParticle)
		{
			m_velocityBuffer.data[i].SetZero();
		}
	}
}

void b2ParticleSystem::SolveRigid(const b2TimeStep& step)
{
	for (b2ParticleGroup* group = m_groupList; group; group = group->GetNext())
	{
		if (group->m_groupFlags & b2_rigidParticleGroup)
		{
			group->UpdateStatistics();
			b2Rot rotation(step.dt * group->m_angularVelocity);
			b2Transform transform(
				group->m_center + step.dt * group->m_linearVelocity -
				b2Mul(rotation, group->m_center), rotation);
			group->m_transform = b2Mul(transform, group->m_transform);
			b2Transform velocityTransform;
			velocityTransform.p.x = step.inv_dt * transform.p.x;
			velocityTransform.p.y = step.inv_dt * transform.p.y;
			velocityTransform.q.s = step.inv_dt * transform.q.s;
			velocityTransform.q.c = step.inv_dt * (transform.q.c - 1);
			for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++)
			{
				m_velocityBuffer.data[i] = b2Mul(velocityTransform, m_positionBuffer.data[i]);
			}
		}
	}
}

void b2ParticleSystem::SolveElastic(const b2TimeStep& step)
{
	float32 elasticStrength = step.inv_dt * m_elasticStrength;
	for (int32 k = 0; k < m_triadCount; k++)
	{
		const Triad& triad = m_triadBuffer[k];
		if (triad.flags & b2_elasticParticle)
		{
			int32 a = triad.indexA;
			int32 b = triad.indexB;
			int32 c = triad.indexC;
			const b2Vec2& oa = triad.pa;
			const b2Vec2& ob = triad.pb;
			const b2Vec2& oc = triad.pc;
			const b2Vec2& pa = m_positionBuffer.data[a];
			const b2Vec2& pb = m_positionBuffer.data[b];
			const b2Vec2& pc = m_positionBuffer.data[c];
			b2Vec2 p = (float32) 1 / 3 * (pa + pb + pc);
			b2Rot r;
			r.s = b2Cross(oa, pa) + b2Cross(ob, pb) + b2Cross(oc, pc);
			r.c = b2Dot(oa, pa) + b2Dot(ob, pb) + b2Dot(oc, pc);
			float32 r2 = r.s * r.s + r.c * r.c;
			float32 invR = b2InvSqrt(r2);
			r.s *= invR;
			r.c *= invR;
			float32 strength = elasticStrength * triad.strength;
			m_velocityBuffer.data[a] += strength * (b2Mul(r, oa) - (pa - p));
			m_velocityBuffer.data[b] += strength * (b2Mul(r, ob) - (pb - p));
			m_velocityBuffer.data[c] += strength * (b2Mul(r, oc) - (pc - p));
		}
	}
}

void b2ParticleSystem::SolveSpring(const b2TimeStep& step)
{
	float32 springStrength = step.inv_dt * m_springStrength;
	for (int32 k = 0; k < m_pairCount; k++)
	{
		const Pair& pair = m_pairBuffer[k];
		if (pair.flags & b2_springParticle)
		{
			int32 a = pair.indexA;
			int32 b = pair.indexB;
			b2Vec2 d = m_positionBuffer.data[b] - m_positionBuffer.data[a];
			float32 r0 = pair.distance;
			float32 r1 = d.Length();
			float32 strength = springStrength * pair.strength;
			b2Vec2 f = strength * (r0 - r1) / r1 * d;
			m_velocityBuffer.data[a] -= f;
			m_velocityBuffer.data[b] += f;
		}
	}
}

void b2ParticleSystem::SolveTensile(const b2TimeStep& step)
{
	m_accumulation2Buffer = RequestParticleBuffer(m_accumulation2Buffer);
	for (int32 i = 0; i < m_count; i++)
	{
		m_accumulationBuffer[i] = 0;
		m_accumulation2Buffer[i] = b2Vec2_zero;
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		if (contact.flags & b2_tensileParticle)
		{
			int32 a = contact.indexA;
			int32 b = contact.indexB;
			float32 w = contact.weight;
			b2Vec2 n = contact.normal;
			m_accumulationBuffer[a] += w;
			m_accumulationBuffer[b] += w;
			m_accumulation2Buffer[a] -= (1 - w) * w * n;
			m_accumulation2Buffer[b] += (1 - w) * w * n;
		}
	}
	float32 strengthA = m_surfaceTensionStrengthA * GetCriticalVelocity(step);
	float32 strengthB = m_surfaceTensionStrengthB * GetCriticalVelocity(step);
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		if (contact.flags & b2_tensileParticle)
		{
			int32 a = contact.indexA;
			int32 b = contact.indexB;
			float32 w = contact.weight;
			b2Vec2 n = contact.normal;
			float32 h = m_accumulationBuffer[a] + m_accumulationBuffer[b];
			b2Vec2 s = m_accumulation2Buffer[b] - m_accumulation2Buffer[a];
			float32 fn = (strengthA * (h - 2) + strengthB * b2Dot(s, n)) * w;
			b2Vec2 f = fn * n;
			m_velocityBuffer.data[a] -= f;
			m_velocityBuffer.data[b] += f;
		}
	}
}

void b2ParticleSystem::SolveViscous(const b2TimeStep& step)
{
	float32 viscousStrength = m_viscousStrength;
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		const b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		int32 a = contact.index;
		if (m_flagsBuffer.data[a] & b2_viscousParticle)
		{
			b2Body* b = contact.body;
			float32 w = contact.weight;
			float32 m = contact.mass;
			b2Vec2 p = m_positionBuffer.data[a];
			b2Vec2 v = b->GetLinearVelocityFromWorldPoint(p) - m_velocityBuffer.data[a];
			b2Vec2 f = viscousStrength * m * w * v;
			m_velocityBuffer.data[a] += GetParticleInvMass() * f;
			b->ApplyLinearImpulse(-f, p, true);
		}
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		if (contact.flags & b2_viscousParticle)
		{
			int32 a = contact.indexA;
			int32 b = contact.indexB;
			float32 w = contact.weight;
			b2Vec2 v = m_velocityBuffer.data[b] - m_velocityBuffer.data[a];
			b2Vec2 f = viscousStrength * w * v;
			m_velocityBuffer.data[a] += f;
			m_velocityBuffer.data[b] -= f;
		}
	}
}

void b2ParticleSystem::SolvePowder(const b2TimeStep& step)
{
	float32 powderStrength = m_powderStrength * GetCriticalVelocity(step);
	float32 minWeight = 1.0f - b2_particleStride;
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		const b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		int32 a = contact.index;
		if (m_flagsBuffer.data[a] & b2_powderParticle)
		{
			float32 w = contact.weight;
			if (w > minWeight)
			{
				b2Body* b = contact.body;
				float32 m = contact.mass;
				b2Vec2 p = m_positionBuffer.data[a];
				b2Vec2 n = contact.normal;
				b2Vec2 f = powderStrength * m * (w - minWeight) * n;
				m_velocityBuffer.data[a] -= GetParticleInvMass() * f;
				b->ApplyLinearImpulse(f, p, true);
			}
		}
	}
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		if (contact.flags & b2_powderParticle)
		{
			float32 w = contact.weight;
			if (w > minWeight)
			{
				int32 a = contact.indexA;
				int32 b = contact.indexB;
				b2Vec2 n = contact.normal;
				b2Vec2 f = powderStrength * (w - minWeight) * n;
				m_velocityBuffer.data[a] -= f;
				m_velocityBuffer.data[b] += f;
			}
		}
	}
}

void b2ParticleSystem::SolveSolid(const b2TimeStep& step)
{
	// applies extra repulsive force from solid particle groups
	m_depthBuffer = RequestParticleBuffer(m_depthBuffer);
	float32 ejectionStrength = step.inv_dt * m_ejectionStrength;
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		if (m_groupBuffer[a] != m_groupBuffer[b])
		{
			float32 w = contact.weight;
			b2Vec2 n = contact.normal;
			float32 h = m_depthBuffer[a] + m_depthBuffer[b];
			b2Vec2 f = ejectionStrength * h * w * n;
			m_velocityBuffer.data[a] -= f;
			m_velocityBuffer.data[b] += f;
		}
	}
}

void b2ParticleSystem::SolveColorMixing(const b2TimeStep& step)
{
	// mixes color between contacting particles
	m_colorBuffer.data = RequestParticleBuffer(m_colorBuffer.data);
	int32 colorMixing256 = (int32) (256 * m_colorMixingStrength);
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		if (m_flagsBuffer.data[a] & m_flagsBuffer.data[b] & b2_colorMixingParticle)
		{
			b2ParticleColor& colorA = m_colorBuffer.data[a];
			b2ParticleColor& colorB = m_colorBuffer.data[b];
			int32 dr = (colorMixing256 * (colorB.r - colorA.r)) >> 8;
			int32 dg = (colorMixing256 * (colorB.g - colorA.g)) >> 8;
			int32 db = (colorMixing256 * (colorB.b - colorA.b)) >> 8;
			int32 da = (colorMixing256 * (colorB.a - colorA.a)) >> 8;
			colorA.r += dr;
			colorA.g += dg;
			colorA.b += db;
			colorA.a += da;
			colorB.r -= dr;
			colorB.g -= dg;
			colorB.b -= db;
			colorB.a -= da;
		}
	}
}

void b2ParticleSystem::SolveZombie()
{
	// removes particles with zombie flag
	int32 newCount = 0;
	int32* newIndices = (int32*) m_world->m_stackAllocator.Allocate(sizeof(int32) * m_count);
	for (int32 i = 0; i < m_count; i++)
	{
		int32 flags = m_flagsBuffer.data[i];
		if (flags & b2_zombieParticle)
		{
			b2DestructionListener * const destructionListener =
				m_world->m_destructionListener;
			if ((flags & b2_destructionListener) &&
				destructionListener)
			{
				destructionListener->SayGoodbye(i);
			}
			newIndices[i] = b2_invalidParticleIndex;
		}
		else
		{
			newIndices[i] = newCount;
			if (i != newCount)
			{
				m_flagsBuffer.data[newCount] = m_flagsBuffer.data[i];
				m_positionBuffer.data[newCount] = m_positionBuffer.data[i];
				m_velocityBuffer.data[newCount] = m_velocityBuffer.data[i];
				m_groupBuffer[newCount] = m_groupBuffer[i];
				if (m_depthBuffer)
				{
					m_depthBuffer[newCount] = m_depthBuffer[i];
				}
				if (m_colorBuffer.data)
				{
					m_colorBuffer.data[newCount] = m_colorBuffer.data[i];
				}
				if (m_userDataBuffer.data)
				{
					m_userDataBuffer.data[newCount] = m_userDataBuffer.data[i];
				}
			}
			newCount++;
		}
	}

	// predicate functions
	struct Test
	{
		static bool IsProxyInvalid(const Proxy& proxy)
		{
			return proxy.index < 0;
		}
		static bool IsContactInvalid(const b2ParticleContact& contact)
		{
			return contact.indexA < 0 || contact.indexB < 0;
		}
		static bool IsBodyContactInvalid(const b2ParticleBodyContact& contact)
		{
			return contact.index < 0;
		}
		static bool IsPairInvalid(const Pair& pair)
		{
			return pair.indexA < 0 || pair.indexB < 0;
		}
		static bool IsTriadInvalid(const Triad& triad)
		{
			return triad.indexA < 0 || triad.indexB < 0 || triad.indexC < 0;
		}
	};

	// update proxies
	for (int32 k = 0; k < m_proxyCount; k++)
	{
		Proxy& proxy = m_proxyBuffer[k];
		proxy.index = newIndices[proxy.index];
	}
	Proxy* lastProxy = std::remove_if(
		m_proxyBuffer, m_proxyBuffer + m_proxyCount,
		Test::IsProxyInvalid);
	m_proxyCount = (int32) (lastProxy - m_proxyBuffer);

	// update contacts
	for (int32 k = 0; k < m_contactCount; k++)
	{
		b2ParticleContact& contact = m_contactBuffer[k];
		contact.indexA = newIndices[contact.indexA];
		contact.indexB = newIndices[contact.indexB];
	}
	b2ParticleContact* lastContact = std::remove_if(
		m_contactBuffer, m_contactBuffer + m_contactCount,
		Test::IsContactInvalid);
	m_contactCount = (int32) (lastContact - m_contactBuffer);

	// update particle-body contacts
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		contact.index = newIndices[contact.index];
	}
	b2ParticleBodyContact* lastBodyContact = std::remove_if(
		m_bodyContactBuffer, m_bodyContactBuffer + m_bodyContactCount,
		Test::IsBodyContactInvalid);
	m_bodyContactCount = (int32) (lastBodyContact - m_bodyContactBuffer);

	// update pairs
	for (int32 k = 0; k < m_pairCount; k++)
	{
		Pair& pair = m_pairBuffer[k];
		pair.indexA = newIndices[pair.indexA];
		pair.indexB = newIndices[pair.indexB];
	}
	Pair* lastPair = std::remove_if(
		m_pairBuffer, m_pairBuffer + m_pairCount, Test::IsPairInvalid);
	m_pairCount = (int32) (lastPair - m_pairBuffer);

	// update triads
	for (int32 k = 0; k < m_triadCount; k++)
	{
		Triad& triad = m_triadBuffer[k];
		triad.indexA = newIndices[triad.indexA];
		triad.indexB = newIndices[triad.indexB];
		triad.indexC = newIndices[triad.indexC];
	}
	Triad* lastTriad = std::remove_if(
		m_triadBuffer, m_triadBuffer + m_triadCount,
		Test::IsTriadInvalid);
	m_triadCount = (int32) (lastTriad - m_triadBuffer);

	// update groups
	for (b2ParticleGroup* group = m_groupList; group; group = group->GetNext())
	{
		int32 firstIndex = newCount;
		int32 lastIndex = 0;
		bool modified = false;
		for (int32 i = group->m_firstIndex; i < group->m_lastIndex; i++)
		{
			int32 j = newIndices[i];
			if (j >= 0) {
				firstIndex = b2Min(firstIndex, j);
				lastIndex = b2Max(lastIndex, j + 1);
			} else {
				modified = true;
			}
		}
		if (firstIndex < lastIndex)
		{
			group->m_firstIndex = firstIndex;
			group->m_lastIndex = lastIndex;
			if (modified)
			{
				if (group->m_groupFlags & b2_rigidParticleGroup)
				{
					group->m_toBeSplit = true;
				}
			}
		}
		else
		{
			group->m_firstIndex = 0;
			group->m_lastIndex = 0;
			if (group->m_destroyAutomatically)
			{
				group->m_toBeDestroyed = true;
			}
		}
	}

	// update particle count
	m_count = newCount;
	m_world->m_stackAllocator.Free(newIndices);

	// destroy bodies with no particles
	for (b2ParticleGroup* group = m_groupList; group;)
	{
		b2ParticleGroup* next = group->GetNext();
		if (group->m_toBeDestroyed)
		{
			DestroyParticleGroup(group);
		}
		else if (group->m_toBeSplit)
		{
			// TODO: split the group
		}
		group = next;
	}
}

void b2ParticleSystem::RotateBuffer(int32 start, int32 mid, int32 end)
{
	// move the particles assigned to the given group toward the end of array
	if (start == mid || mid == end)
	{
		return;
	}
	struct NewIndices
	{
		int32 operator[](int32 i) const
		{
			if (i < start)
			{
				return i;
			}
			else if (i < mid)
			{
				return i + end - mid;
			}
			else if (i < end)
			{
				return i + start - mid;
			}
			else
			{
				return i;
			}
		}
		int32 start, mid, end;
	} newIndices;
	newIndices.start = start;
	newIndices.mid = mid;
	newIndices.end = end;

	std::rotate(m_flagsBuffer.data + start, m_flagsBuffer.data + mid, m_flagsBuffer.data + end);
	std::rotate(m_positionBuffer.data + start, m_positionBuffer.data + mid, m_positionBuffer.data + end);
	std::rotate(m_velocityBuffer.data + start, m_velocityBuffer.data + mid, m_velocityBuffer.data + end);
	std::rotate(m_groupBuffer + start, m_groupBuffer + mid, m_groupBuffer + end);
	if (m_depthBuffer)
	{
		std::rotate(m_depthBuffer + start, m_depthBuffer + mid, m_depthBuffer + end);
	}
	if (m_colorBuffer.data)
	{
		std::rotate(m_colorBuffer.data + start, m_colorBuffer.data + mid, m_colorBuffer.data + end);
	}
	if (m_userDataBuffer.data)
	{
		std::rotate(m_userDataBuffer.data + start, m_userDataBuffer.data + mid, m_userDataBuffer.data + end);
	}

	// update proxies
	for (int32 k = 0; k < m_proxyCount; k++)
	{
		Proxy& proxy = m_proxyBuffer[k];
		proxy.index = newIndices[proxy.index];
	}

	// update contacts
	for (int32 k = 0; k < m_contactCount; k++)
	{
		b2ParticleContact& contact = m_contactBuffer[k];
		contact.indexA = newIndices[contact.indexA];
		contact.indexB = newIndices[contact.indexB];
	}

	// update particle-body contacts
	for (int32 k = 0; k < m_bodyContactCount; k++)
	{
		b2ParticleBodyContact& contact = m_bodyContactBuffer[k];
		contact.index = newIndices[contact.index];
	}

	// update pairs
	for (int32 k = 0; k < m_pairCount; k++)
	{
		Pair& pair = m_pairBuffer[k];
		pair.indexA = newIndices[pair.indexA];
		pair.indexB = newIndices[pair.indexB];
	}

	// update triads
	for (int32 k = 0; k < m_triadCount; k++)
	{
		Triad& triad = m_triadBuffer[k];
		triad.indexA = newIndices[triad.indexA];
		triad.indexB = newIndices[triad.indexB];
		triad.indexC = newIndices[triad.indexC];
	}

	// update groups
	for (b2ParticleGroup* group = m_groupList; group; group = group->GetNext())
	{
		group->m_firstIndex = newIndices[group->m_firstIndex];
		group->m_lastIndex = newIndices[group->m_lastIndex - 1] + 1;
	}
}

void b2ParticleSystem::SetParticleRadius(float32 radius)
{
	m_particleDiameter = 2 * radius;
	m_squaredDiameter = m_particleDiameter * m_particleDiameter;
	m_inverseDiameter = 1 / m_particleDiameter;
}

void b2ParticleSystem::SetParticleDensity(float32 density)
{
	m_density = density;
	m_inverseDensity =  1 / m_density;
}

float32 b2ParticleSystem::GetParticleDensity() const
{
	return m_density;
}

void b2ParticleSystem::SetParticleGravityScale(float32 gravityScale)
{
	m_gravityScale = gravityScale;
}

float32 b2ParticleSystem::GetParticleGravityScale() const
{
	return m_gravityScale;
}

void b2ParticleSystem::SetParticleDamping(float32 damping)
{
	m_dampingStrength = damping;
}

float32 b2ParticleSystem::GetParticleDamping() const
{
	return m_dampingStrength;
}

float32 b2ParticleSystem::GetParticleRadius() const
{
	return m_particleDiameter / 2;
}

float32 b2ParticleSystem::GetCriticalVelocity(const b2TimeStep& step) const
{
	return m_particleDiameter * step.inv_dt;
}

float32 b2ParticleSystem::GetCriticalVelocitySquared(const b2TimeStep& step) const
{
	float32 velocity = GetCriticalVelocity(step);
	return velocity * velocity;
}

float32 b2ParticleSystem::GetCriticalPressure(const b2TimeStep& step) const
{
	return m_density * GetCriticalVelocitySquared(step);
}

float32 b2ParticleSystem::GetParticleStride() const
{
	return b2_particleStride * m_particleDiameter;
}

float32 b2ParticleSystem::GetParticleMass() const
{
	float32 stride = GetParticleStride();
	return m_density * stride * stride;
}

float32 b2ParticleSystem::GetParticleInvMass() const
{
	return 1.777777f * m_inverseDensity * m_inverseDiameter * m_inverseDiameter;
}

uint32* b2ParticleSystem::GetParticleFlagsBuffer()
{
	return m_flagsBuffer.data;
}

b2Vec2* b2ParticleSystem::GetParticlePositionBuffer()
{
	return m_positionBuffer.data;
}

b2Vec2* b2ParticleSystem::GetParticleVelocityBuffer()
{
	return m_velocityBuffer.data;
}

b2ParticleColor* b2ParticleSystem::GetParticleColorBuffer()
{
	m_colorBuffer.data = RequestParticleBuffer(m_colorBuffer.data);
	return m_colorBuffer.data;
}

void** b2ParticleSystem::GetParticleUserDataBuffer()
{
	m_userDataBuffer.data = RequestParticleBuffer(m_userDataBuffer.data);
	return m_userDataBuffer.data;
}

int32 b2ParticleSystem::GetParticleMaxCount() const
{
	return m_maxCount;
}

void b2ParticleSystem::SetParticleMaxCount(int32 count)
{
	b2Assert(m_count <= count);
	m_maxCount = count;
}

const uint32* b2ParticleSystem::GetParticleFlagsBuffer() const
{
	return m_flagsBuffer.data;
}

const b2Vec2* b2ParticleSystem::GetParticlePositionBuffer() const
{
	return m_positionBuffer.data;
}

const b2Vec2* b2ParticleSystem::GetParticleVelocityBuffer() const
{
	return m_velocityBuffer.data;
}

const b2ParticleColor* b2ParticleSystem::GetParticleColorBuffer() const
{
	return ((b2ParticleSystem*) this)->GetParticleColorBuffer();
}

const b2ParticleGroup* const* b2ParticleSystem::GetParticleGroupBuffer() const
{
	return m_groupBuffer;
}

void* const* b2ParticleSystem::GetParticleUserDataBuffer() const
{
	return ((b2ParticleSystem*) this)->GetParticleUserDataBuffer();
}

template <typename T> void b2ParticleSystem::SetParticleBuffer(ParticleBuffer<T>* buffer, T* newData, int32 newCapacity)
{
	b2Assert((newData && newCapacity) || (!newData && !newCapacity));
	if (!buffer->userSuppliedCapacity)
	{
		m_world->m_blockAllocator.Free(buffer->data, sizeof(T) * m_internalAllocatedCapacity);
	}
	buffer->data = newData;
	buffer->userSuppliedCapacity = newCapacity;}

void b2ParticleSystem::SetParticleFlagsBuffer(uint32* buffer, int32 capacity)
{
	SetParticleBuffer(&m_flagsBuffer, buffer, capacity);
}

void b2ParticleSystem::SetParticlePositionBuffer(b2Vec2* buffer, int32 capacity)
{
	SetParticleBuffer(&m_positionBuffer, buffer, capacity);
}

void b2ParticleSystem::SetParticleVelocityBuffer(b2Vec2* buffer, int32 capacity)
{
	SetParticleBuffer(&m_velocityBuffer, buffer, capacity);
}

void b2ParticleSystem::SetParticleColorBuffer(b2ParticleColor* buffer, int32 capacity)
{
	SetParticleBuffer(&m_colorBuffer, buffer, capacity);
}

b2ParticleGroup* const* b2ParticleSystem::GetParticleGroupBuffer()
{
	return m_groupBuffer;
}

void b2ParticleSystem::SetParticleUserDataBuffer(void** buffer, int32 capacity)
{
	SetParticleBuffer(&m_userDataBuffer, buffer, capacity);
}

void b2ParticleSystem::QueryAABB(b2QueryCallback* callback, const b2AABB& aabb) const
{
	if (m_proxyCount == 0)
	{
		return;
	}
	Proxy* beginProxy = m_proxyBuffer;
	Proxy* endProxy = beginProxy + m_proxyCount;
	Proxy* firstProxy = std::lower_bound(
		beginProxy, endProxy,
		computeTag(
			m_inverseDiameter * aabb.lowerBound.x,
			m_inverseDiameter * aabb.lowerBound.y));
	Proxy* lastProxy = std::upper_bound(
		firstProxy, endProxy,
		computeTag(
			m_inverseDiameter * aabb.upperBound.x,
			m_inverseDiameter * aabb.upperBound.y));
	for (Proxy* proxy = firstProxy; proxy < lastProxy; ++proxy)
	{
		int32 i = proxy->index;
		const b2Vec2& p = m_positionBuffer.data[i];
		if (aabb.lowerBound.x < p.x && p.x < aabb.upperBound.x &&
			aabb.lowerBound.y < p.y && p.y < aabb.upperBound.y)
		{
			if (!callback->ReportParticle(i))
			{
				break;
			}
		}
	}
}

void b2ParticleSystem::RayCast(b2RayCastCallback* callback, const b2Vec2& point1, const b2Vec2& point2) const
{
	if (m_proxyCount == 0)
	{
		return;
	}
	Proxy* beginProxy = m_proxyBuffer;
	Proxy* endProxy = beginProxy + m_proxyCount;
	Proxy* firstProxy = std::lower_bound(
		beginProxy, endProxy,
		computeTag(
			m_inverseDiameter * b2Min(point1.x, point2.x) - 1,
			m_inverseDiameter * b2Min(point1.y, point2.y) - 1));
	Proxy* lastProxy = std::upper_bound(
		firstProxy, endProxy,
		computeTag(
			m_inverseDiameter * b2Max(point1.x, point2.x) + 1,
			m_inverseDiameter * b2Max(point1.y, point2.y) + 1));
	float32 fraction = 1;
	// solving the following equation:
	// ((1-t)*point1+t*point2-position)^2=diameter^2
	// where t is a potential fraction
	b2Vec2 v = point2 - point1;
	float32 v2 = b2Dot(v, v);
	for (Proxy* proxy = firstProxy; proxy < lastProxy; ++proxy)
	{
		int32 i = proxy->index;
		b2Vec2 p = point1 - m_positionBuffer.data[i];
		float32 pv = b2Dot(p, v);
		float32 p2 = b2Dot(p, p);
		float32 determinant = pv * pv - v2 * (p2 - m_squaredDiameter);
		if (determinant >= 0)
		{
			float32 sqrtDeterminant = b2Sqrt(determinant);
			// find a solution between 0 and fraction
			float32 t = (-pv - sqrtDeterminant) / v2;
			if (t > fraction)
			{
				continue;
			}
			if (t < 0)
			{
				t = (-pv + sqrtDeterminant) / v2;
				if (t < 0 || t > fraction)
				{
					continue;
				}
			}
			b2Vec2 n = p + t * v;
			n.Normalize();
			float32 f = callback->ReportParticle(i, point1 + t * v, n, t);
			fraction = b2Min(fraction, f);
			if (fraction <= 0)
			{
				break;
			}
		}
	}
}

float32 b2ParticleSystem::ComputeParticleCollisionEnergy() const
{
	float32 sum_v2 = 0;
	for (int32 k = 0; k < m_contactCount; k++)
	{
		const b2ParticleContact& contact = m_contactBuffer[k];
		int32 a = contact.indexA;
		int32 b = contact.indexB;
		b2Vec2 n = contact.normal;
		b2Vec2 v = m_velocityBuffer.data[b] - m_velocityBuffer.data[a];
		float32 vn = b2Dot(v, n);
		if (vn < 0)
		{
			sum_v2 += vn * vn;
		}
	}
	return 0.5f * GetParticleMass() * sum_v2;
}
