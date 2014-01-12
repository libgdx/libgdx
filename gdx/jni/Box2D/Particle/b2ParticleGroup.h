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
#ifndef B2_PARTICLE_GROUP
#define B2_PARTICLE_GROUP

#include <Box2D/Particle/b2Particle.h>

class b2Shape;
class b2World;
class b2ParticleSystem;
class b2ParticleGroup;
struct b2ParticleColor;

enum b2ParticleGroupFlag
{
	b2_solidParticleGroup =   1 << 0, // resists penetration
	b2_rigidParticleGroup =   1 << 1, // keeps its shape
};

/// A particle group definition holds all the data needed to construct a particle group.
/// You can safely re-use these definitions.
struct b2ParticleGroupDef
{

	b2ParticleGroupDef()
	{
		flags = 0;
		groupFlags = 0;
		position = b2Vec2_zero;
		angle = 0;
		linearVelocity = b2Vec2_zero;
		angularVelocity = 0;
		color = b2ParticleColor_zero;
		strength = 1;
		shape = NULL;
		destroyAutomatically = true;
		userData = NULL;
	}

	/// The particle-behavior flags.
	uint32 flags;

	/// The group-construction flags.
	uint32 groupFlags;

	/// The world position of the group.
	/// Moves the group's shape a distance equal to the value of position.
	b2Vec2 position;

	/// The world angle of the group in radians.
	/// Rotates the shape by an angle equal to the value of angle.
	float32 angle;

	/// The linear velocity of the group's origin in world co-ordinates.
	b2Vec2 linearVelocity;

	/// The angular velocity of the group.
	float32 angularVelocity;

	/// The color of all particles in the group.
	b2ParticleColor color;

	/// The strength of cohesion among the particles in a group with flag b2_elasticParticle or b2_springParticle.
	float32 strength;

	/// Shape containing the particle group.
	const b2Shape* shape;

	/// If true, destroy the group automatically after its last particle has been destroyed.
	bool destroyAutomatically;

	/// Use this to store application-specific group data.
	void* userData;

};

/// A group of particles. These are created via b2World::CreateParticleGroup.
class b2ParticleGroup
{

public:

	/// Get the next particle group from the list in b2_World.
	b2ParticleGroup* GetNext();
	const b2ParticleGroup* GetNext() const;

	/// Get the number of particles.
	int32 GetParticleCount() const;

	/// Get the offset of this group in the global particle buffer
	int32 GetBufferIndex() const;

	/// Get the construction flags for the group.
	int32 GetGroupFlags() const;

	/// Set the construction flags for the group.
	void SetGroupFlags(int32 flags);

	/// Get the total mass of the group: the sum of all particles in it.
	float32 GetMass() const;

	/// Get the moment of inertia for the group.
	float32 GetInertia() const;

	/// Get the center of gravity for the group.
	b2Vec2 GetCenter() const;

	/// Get the linear velocity of the group.
	b2Vec2 GetLinearVelocity() const;

	/// Get the angular velocity of the group.
	float32 GetAngularVelocity() const;

	/// Get the position of the group's origin and rotation.
	/// Used only with groups of rigid particles.
	const b2Transform& GetTransform() const;

	/// Get position of the particle group as a whole.
	/// Used only with groups of rigid particles.
	const b2Vec2& GetPosition() const;

	/// Get the rotational angle of the particle group as a whole.
	/// Used only with groups of rigid particles.
	float32 GetAngle() const;

	/// Get the user data pointer that was provided in the group definition.
	void* GetUserData() const;

	/// Set the user data. Use this to store your application specific data.
	void SetUserData(void* data);

private:

	friend class b2ParticleSystem;

	b2ParticleSystem* m_system;
	int32 m_firstIndex, m_lastIndex;
	uint32 m_groupFlags;
	float32 m_strength;
	b2ParticleGroup* m_prev;
	b2ParticleGroup* m_next;

	mutable int32 m_timestamp;
	mutable float32 m_mass;
	mutable float32 m_inertia;
	mutable b2Vec2 m_center;
	mutable b2Vec2 m_linearVelocity;
	mutable float32 m_angularVelocity;
	mutable b2Transform m_transform;

	unsigned m_destroyAutomatically:1;
	unsigned m_toBeDestroyed:1;
	unsigned m_toBeSplit:1;

	void* m_userData;

	b2ParticleGroup();
	~b2ParticleGroup();
	void UpdateStatistics() const;

};

inline b2ParticleGroup* b2ParticleGroup::GetNext()
{
	return m_next;
}

inline const b2ParticleGroup* b2ParticleGroup::GetNext() const
{
	return m_next;
}

inline int32 b2ParticleGroup::GetParticleCount() const
{
	return m_lastIndex - m_firstIndex;
}

#endif
