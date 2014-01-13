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
#include <Box2D/Particle/b2ParticleGroup.h>
#include <Box2D/Particle/b2ParticleSystem.h>

b2ParticleGroup::b2ParticleGroup()
{

	m_system = NULL;
	m_firstIndex = 0;
	m_lastIndex = 0;
	m_groupFlags = 0;
	m_strength = 1.0f;
	m_prev = NULL;
	m_next = NULL;

	m_timestamp = -1;
	m_mass = 0;
	m_inertia = 0;
	m_center = b2Vec2_zero;
	m_linearVelocity = b2Vec2_zero;
	m_angularVelocity = 0;
	m_transform.SetIdentity();

	m_destroyAutomatically = true;
	m_toBeDestroyed = false;
	m_toBeSplit = false;

	m_userData = NULL;

}

b2ParticleGroup::~b2ParticleGroup()
{
}

int32 b2ParticleGroup::GetBufferIndex() const
{
  return m_firstIndex;
}

int32 b2ParticleGroup::GetGroupFlags() const
{
	return m_groupFlags;
}

void b2ParticleGroup::SetGroupFlags(int32 flags)
{
	m_groupFlags = flags;
}

float32 b2ParticleGroup::GetMass() const
{
	UpdateStatistics();
	return m_mass;
}

float32 b2ParticleGroup::GetInertia() const
{
	UpdateStatistics();
	return m_inertia;
}

b2Vec2 b2ParticleGroup::GetCenter() const
{
	UpdateStatistics();
	return m_center;
}

b2Vec2 b2ParticleGroup::GetLinearVelocity() const
{
	UpdateStatistics();
	return m_linearVelocity;
}

float32 b2ParticleGroup::GetAngularVelocity() const
{
	UpdateStatistics();
	return m_angularVelocity;
}

const b2Transform& b2ParticleGroup::GetTransform() const
{
	return m_transform;
}

const b2Vec2& b2ParticleGroup::GetPosition() const
{
	return m_transform.p;
}

float32 b2ParticleGroup::GetAngle() const
{
	return m_transform.q.GetAngle();
}

void* b2ParticleGroup::GetUserData() const
{
	return m_userData;
}

void b2ParticleGroup::SetUserData(void* data)
{
	m_userData = data;
}

void b2ParticleGroup::UpdateStatistics() const
{
	if (m_timestamp != m_system->m_timestamp)
	{
		float32 m = m_system->GetParticleMass();
		m_mass = 0;
		m_center.SetZero();
		m_linearVelocity.SetZero();
		for (int32 i = m_firstIndex; i < m_lastIndex; i++)
		{
			m_mass += m;
			m_center += m * m_system->m_positionBuffer.data[i];
			m_linearVelocity += m * m_system->m_velocityBuffer.data[i];
		}
		if (m_mass > 0)
		{
			m_center *= 1 / m_mass;
			m_linearVelocity *= 1 / m_mass;
		}
		m_inertia = 0;
		m_angularVelocity = 0;
		for (int32 i = m_firstIndex; i < m_lastIndex; i++)
		{
			b2Vec2 p = m_system->m_positionBuffer.data[i] - m_center;
			b2Vec2 v = m_system->m_velocityBuffer.data[i] - m_linearVelocity;
			m_inertia += m * b2Dot(p, p);
			m_angularVelocity += m * b2Cross(p, v);
		}
		if (m_inertia > 0)
		{
			m_angularVelocity *= 1 / m_inertia;
		}
		m_timestamp = m_system->m_timestamp;
	}
}
