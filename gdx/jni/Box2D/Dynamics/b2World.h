/*
* Copyright (c) 2006-2011 Erin Catto http://www.box2d.org
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

#ifndef B2_WORLD_H
#define B2_WORLD_H

#include <Box2D/Common/b2Math.h>
#include <Box2D/Common/b2BlockAllocator.h>
#include <Box2D/Common/b2StackAllocator.h>
#include <Box2D/Dynamics/b2ContactManager.h>
#include <Box2D/Dynamics/b2WorldCallbacks.h>
#include <Box2D/Dynamics/b2TimeStep.h>
#include <Box2D/Particle/b2ParticleSystem.h>

struct b2AABB;
struct b2BodyDef;
struct b2Color;
struct b2JointDef;
class b2Body;
class b2Draw;
class b2Fixture;
class b2Joint;
class b2ParticleGroup;

/// The world class manages all physics entities, dynamic simulation,
/// and asynchronous queries. The world also contains efficient memory
/// management facilities.
class b2World
{
public:
	/// Construct a world object.
	/// @param gravity the world gravity vector.
	b2World(const b2Vec2& gravity);

	/// Destruct the world. All physics entities are destroyed and all heap memory is released.
	~b2World();

	/// Register a destruction listener. The listener is owned by you and must
	/// remain in scope.
	void SetDestructionListener(b2DestructionListener* listener);

	/// Register a contact filter to provide specific control over collision.
	/// Otherwise the default filter is used (b2_defaultFilter). The listener is
	/// owned by you and must remain in scope. 
	void SetContactFilter(b2ContactFilter* filter);

	/// Register a contact event listener. The listener is owned by you and must
	/// remain in scope.
	void SetContactListener(b2ContactListener* listener);

	/// Register a routine for debug drawing. The debug draw functions are called
	/// inside with b2World::DrawDebugData method. The debug draw object is owned
	/// by you and must remain in scope.
	void SetDebugDraw(b2Draw* debugDraw);

	/// Create a rigid body given a definition. No reference to the definition
	/// is retained.
	/// @warning This function is locked during callbacks.
	b2Body* CreateBody(const b2BodyDef* def);

	/// Destroy a rigid body.
	/// This function is locked during callbacks.
	/// @warning This automatically deletes all associated shapes and joints.
	/// @warning This function is locked during callbacks.
	void DestroyBody(b2Body* body);

	/// Create a joint to constrain bodies together. No reference to the definition
	/// is retained. This may cause the connected bodies to cease colliding.
	/// @warning This function is locked during callbacks.
	b2Joint* CreateJoint(const b2JointDef* def);

	/// Destroy a joint. This may cause the connected bodies to begin colliding.
	/// @warning This function is locked during callbacks.
	void DestroyJoint(b2Joint* joint);

	/// Take a time step. This performs collision detection, integration,
	/// and constraint solution.
	/// @param timeStep the amount of time to simulate, this should not vary.
	/// @param velocityIterations for the velocity constraint solver.
	/// @param positionIterations for the position constraint solver.
	void Step(	float32 timeStep,
				int32 velocityIterations,
				int32 positionIterations);

	/// Manually clear the force buffer on all bodies. By default, forces are cleared automatically
	/// after each call to Step. The default behavior is modified by calling SetAutoClearForces.
	/// The purpose of this function is to support sub-stepping. Sub-stepping is often used to maintain
	/// a fixed sized time step under a variable frame-rate.
	/// When you perform sub-stepping you will disable auto clearing of forces and instead call
	/// ClearForces after all sub-steps are complete in one pass of your game loop.
	/// @see SetAutoClearForces
	void ClearForces();

	/// Call this to draw shapes and other debug draw data.
	void DrawDebugData();

	/// Query the world for all fixtures that potentially overlap the
	/// provided AABB.
	/// @param callback a user implemented callback class.
	/// @param aabb the query box.
	void QueryAABB(b2QueryCallback* callback, const b2AABB& aabb) const;

	/// Ray-cast the world for all fixtures in the path of the ray. Your callback
	/// controls whether you get the closest point, any point, or n-points.
	/// The ray-cast ignores shapes that contain the starting point.
	/// @param callback a user implemented callback class.
	/// @param point1 the ray starting point
	/// @param point2 the ray ending point
	void RayCast(b2RayCastCallback* callback, const b2Vec2& point1, const b2Vec2& point2) const;

	/// Get the world body list. With the returned body, use b2Body::GetNext to get
	/// the next body in the world list. A NULL body indicates the end of the list.
	/// @return the head of the world body list.
	b2Body* GetBodyList();
	const b2Body* GetBodyList() const;

	/// Get the world joint list. With the returned joint, use b2Joint::GetNext to get
	/// the next joint in the world list. A NULL joint indicates the end of the list.
	/// @return the head of the world joint list.
	b2Joint* GetJointList();
	const b2Joint* GetJointList() const;

	/// Get the world contact list. With the returned contact, use b2Contact::GetNext to get
	/// the next contact in the world list. A NULL contact indicates the end of the list.
	/// @return the head of the world contact list.
	/// @warning contacts are created and destroyed in the middle of a time step.
	/// Use b2ContactListener to avoid missing contacts.
	b2Contact* GetContactList();
	const b2Contact* GetContactList() const;

	/// Enable/disable sleep.
	void SetAllowSleeping(bool flag);
	bool GetAllowSleeping() const { return m_allowSleep; }

	/// Enable/disable warm starting. For testing.
	void SetWarmStarting(bool flag) { m_warmStarting = flag; }
	bool GetWarmStarting() const { return m_warmStarting; }

	/// Enable/disable continuous physics. For testing.
	void SetContinuousPhysics(bool flag) { m_continuousPhysics = flag; }
	bool GetContinuousPhysics() const { return m_continuousPhysics; }

	/// Enable/disable single stepped continuous physics. For testing.
	void SetSubStepping(bool flag) { m_subStepping = flag; }
	bool GetSubStepping() const { return m_subStepping; }

	/// Get the number of broad-phase proxies.
	int32 GetProxyCount() const;

	/// Get the number of bodies.
	int32 GetBodyCount() const;

	/// Get the number of joints.
	int32 GetJointCount() const;

	/// Get the number of contacts (each may have 0 or more contact points).
	int32 GetContactCount() const;

	/// Get the height of the dynamic tree.
	int32 GetTreeHeight() const;

	/// Get the balance of the dynamic tree.
	int32 GetTreeBalance() const;

	/// Get the quality metric of the dynamic tree. The smaller the better.
	/// The minimum is 1.
	float32 GetTreeQuality() const;

	/// Change the global gravity vector.
	void SetGravity(const b2Vec2& gravity);
	
	/// Get the global gravity vector.
	b2Vec2 GetGravity() const;

	/// Is the world locked (in the middle of a time step).
	bool IsLocked() const;

	/// Set flag to control automatic clearing of forces after each time step.
	void SetAutoClearForces(bool flag);

	/// Get the flag that controls automatic clearing of forces after each time step.
	bool GetAutoClearForces() const;

	/// Shift the world origin. Useful for large worlds.
	/// The body shift formula is: position -= newOrigin
	/// @param newOrigin the new origin with respect to the old origin
	void ShiftOrigin(const b2Vec2& newOrigin);

	/// Get the contact manager for testing.
	const b2ContactManager& GetContactManager() const;

	/// Get the current profile.
	const b2Profile& GetProfile() const;

	/// Dump the world into the log file.
	/// @warning this should be called outside of a time step.
	void Dump();

	/// Create a particle whose properties have been defined.
	/// No reference to the definition is retained.
	/// A simulation step must occur before it's possible to interact with a
	/// newly created particle.  For example, DestroyParticleInShape() will
	/// not destroy a particle until Step() has been called.
	/// @warning This function is locked during callbacks.
	/// @return the index of the particle.
	int32 CreateParticle(const b2ParticleDef& def);

	/// Destroy a particle.
	/// The particle is removed after the next step.
	void DestroyParticle(int32 index)
	{
		DestroyParticle(index, false);
	}

	/// Destroy a particle.
	/// The particle is removed after the next step.
	/// @param Index of the particle to destroy.
	/// @param Whether to call the destruction listener just before the
	/// particle is destroyed.
	void DestroyParticle(int32 index, bool callDestructionListener);

	/// Destroy particles inside a shape without enabling the destruction
	/// callback for destroyed particles.
	/// This function is locked during callbacks.
	/// For more information see
	/// DestroyParticleInShape(const b2Shape&, const b2Transform&,bool).
	/// @param Shape which encloses particles that should be destroyed.
	/// @param Transform applied to the shape.
	/// @warning This function is locked during callbacks.
	/// @return Number of particles destroyed.
	int32 DestroyParticlesInShape(const b2Shape& shape, const b2Transform& xf)
	{
		return DestroyParticlesInShape(shape, xf, false);
	}

	/// Destroy particles inside a shape.
	/// This function is locked during callbacks.
	/// In addition, this function immediately destroys particles in the shape
	/// in constrast to DestroyParticle() which defers the destruction until
	/// the next simulation step.
	/// @param Shape which encloses particles that should be destroyed.
	/// @param Transform applied to the shape.
	/// @param Whether to call the world b2DestructionListener for each
	/// particle destroyed.
	/// @warning This function is locked during callbacks.
	/// @return Number of particles destroyed.
	int32 DestroyParticlesInShape(const b2Shape& shape, const b2Transform& xf,
	                              bool callDestructionListener);


	/// Create a particle group whose properties have been defined. No reference
	/// to the definition is retained.
	/// @warning This function is locked during callbacks.
	b2ParticleGroup* CreateParticleGroup(const b2ParticleGroupDef& def);

	/// Join two particle groups.
	/// @param the first group. Expands to encompass the second group.
	/// @param the second group. It is destroyed.
	/// @warning This function is locked during callbacks.
	void JoinParticleGroups(b2ParticleGroup* groupA, b2ParticleGroup* groupB);

	/// Destroy particles in a group.
	/// This function is locked during callbacks.
	/// @param The particle group to destroy.
	/// @param Whether to call the world b2DestructionListener for each
	/// particle is destroyed.
	/// @warning This function is locked during callbacks.
	void DestroyParticlesInGroup(b2ParticleGroup* group, bool callDestructionListener);

	/// Destroy particles in a group without enabling the destruction
	/// callback for destroyed particles.
	/// This function is locked during callbacks.
	/// @param The particle group to destroy.
	/// @warning This function is locked during callbacks.
	void DestroyParticlesInGroup(b2ParticleGroup* group)
	{
		DestroyParticlesInGroup(group, false);
	}

	/// Get the world particle group list. With the returned group, use
	/// b2ParticleGroup::GetNext to get the next group in the world list.
	/// A NULL group indicates the end of the list.
	/// @return the head of the world particle group list.
	b2ParticleGroup* GetParticleGroupList();
	const b2ParticleGroup* GetParticleGroupList() const;

	/// Get the number of particle groups.
	int32 GetParticleGroupCount() const;

	/// Get the number of particles.
	int32 GetParticleCount() const;

	/// Get the maximum number of particles.
	int32 GetParticleMaxCount() const;

	/// Set the maximum number of particles.
	void SetParticleMaxCount(int32 count);

	/// Change the particle density.
	void SetParticleDensity(float32 density);

	/// Get the particle density.
	float32 GetParticleDensity() const;

	/// Change the particle gravity scale. Adjusts the effect of the global
	/// gravity vector on particles. Default value is 1.0f.
	void SetParticleGravityScale(float32 gravityScale);

	/// Get the particle gravity scale.
	float32 GetParticleGravityScale() const;

	/// Damping is used to reduce the velocity of particles. The damping
	/// parameter can be larger than 1.0f but the damping effect becomes
	/// sensitive to the time step when the damping parameter is large.
	void SetParticleDamping(float32 damping);

	/// Get damping for particles
	float32 GetParticleDamping() const;

	/// Change the particle radius.
	/// You should set this only once, on world start.
	/// If you change the radius during execution, existing particles may explode, shrink, or behave unexpectedly.
	void SetParticleRadius(float32 radius);

	/// Get the particle radius.
	float32 GetParticleRadius() const;

	/// Get the particle data.
	/// @return the pointer to the head of the particle data.
	uint32* GetParticleFlagsBuffer();
	b2Vec2* GetParticlePositionBuffer();
	b2Vec2* GetParticleVelocityBuffer();
	b2ParticleColor* GetParticleColorBuffer();
	b2ParticleGroup* const* GetParticleGroupBuffer();
	void** GetParticleUserDataBuffer();
	const uint32* GetParticleFlagsBuffer() const;
	const b2Vec2* GetParticlePositionBuffer() const;
	const b2Vec2* GetParticleVelocityBuffer() const;
	const b2ParticleColor* GetParticleColorBuffer() const;
	const b2ParticleGroup* const* GetParticleGroupBuffer() const;
	void* const* GetParticleUserDataBuffer() const;

	/// Set a buffer for particle data.
	/// @param buffer is a pointer to a block of memory.
	/// @param size is the number of values in the block.
	void SetParticleFlagsBuffer(uint32* buffer, int32 capacity);
	void SetParticlePositionBuffer(b2Vec2* buffer, int32 capacity);
	void SetParticleVelocityBuffer(b2Vec2* buffer, int32 capacity);
	void SetParticleColorBuffer(b2ParticleColor* buffer, int32 capacity);
	void SetParticleUserDataBuffer(void** buffer, int32 capacity);

	/// Get contacts between particles
	const b2ParticleContact* GetParticleContacts();
	int32 GetParticleContactCount();

	/// Get contacts between particles and bodies
	const b2ParticleBodyContact* GetParticleBodyContacts();
	int32 GetParticleBodyContactCount();

	/// Compute the kinetic energy that can be lost by damping force
	float32 ComputeParticleCollisionEnergy() const;

	/// Get API version.
	const b2Version* GetVersion() const {
		return m_liquidFunVersion;
	}

	/// Get API version string.
	const char* GetVersionString() const {
		return m_liquidFunVersionString;
	}

private:

	// m_flags
	enum
	{
		e_newFixture	= 0x0001,
		e_locked		= 0x0002,
		e_clearForces	= 0x0004
	};

	friend class b2Body;
	friend class b2Fixture;
	friend class b2ContactManager;
	friend class b2Controller;
	friend class b2ParticleSystem;

	void Solve(const b2TimeStep& step);
	void SolveTOI(const b2TimeStep& step);

	void DrawJoint(b2Joint* joint);
	void DrawShape(b2Fixture* shape, const b2Transform& xf, const b2Color& color);

	void DrawParticleSystem(const b2ParticleSystem& system);

	b2BlockAllocator m_blockAllocator;
	b2StackAllocator m_stackAllocator;

	int32 m_flags;

	b2ContactManager m_contactManager;

	b2Body* m_bodyList;
	b2Joint* m_jointList;

	int32 m_bodyCount;
	int32 m_jointCount;

	b2Vec2 m_gravity;
	bool m_allowSleep;

	b2DestructionListener* m_destructionListener;
	b2Draw* m_debugDraw;

	// This is used to compute the time step ratio to
	// support a variable time step.
	float32 m_inv_dt0;

	// These are for debugging the solver.
	bool m_warmStarting;
	bool m_continuousPhysics;
	bool m_subStepping;

	bool m_stepComplete;

	b2Profile m_profile;

	b2ParticleSystem m_particleSystem;

	/// Used to reference b2_LiquidFunVersion so that it's not stripped from
	/// the static library.
	const b2Version *m_liquidFunVersion;
	const char *m_liquidFunVersionString;
};

inline b2Body* b2World::GetBodyList()
{
	return m_bodyList;
}

inline const b2Body* b2World::GetBodyList() const
{
	return m_bodyList;
}

inline b2Joint* b2World::GetJointList()
{
	return m_jointList;
}

inline const b2Joint* b2World::GetJointList() const
{
	return m_jointList;
}

inline b2Contact* b2World::GetContactList()
{
	return m_contactManager.m_contactList;
}

inline const b2Contact* b2World::GetContactList() const
{
	return m_contactManager.m_contactList;
}

inline int32 b2World::GetBodyCount() const
{
	return m_bodyCount;
}

inline int32 b2World::GetJointCount() const
{
	return m_jointCount;
}

inline int32 b2World::GetContactCount() const
{
	return m_contactManager.m_contactCount;
}

inline void b2World::SetGravity(const b2Vec2& gravity)
{
	m_gravity = gravity;
}

inline b2Vec2 b2World::GetGravity() const
{
	return m_gravity;
}

inline bool b2World::IsLocked() const
{
	return (m_flags & e_locked) == e_locked;
}

inline void b2World::SetAutoClearForces(bool flag)
{
	if (flag)
	{
		m_flags |= e_clearForces;
	}
	else
	{
		m_flags &= ~e_clearForces;
	}
}

/// Get the flag that controls automatic clearing of forces after each time step.
inline bool b2World::GetAutoClearForces() const
{
	return (m_flags & e_clearForces) == e_clearForces;
}

inline const b2ContactManager& b2World::GetContactManager() const
{
	return m_contactManager;
}

inline const b2Profile& b2World::GetProfile() const
{
	return m_profile;
}

inline b2ParticleGroup* b2World::GetParticleGroupList()
{
	return m_particleSystem.GetParticleGroupList();
}

inline const b2ParticleGroup* b2World::GetParticleGroupList() const
{
	return m_particleSystem.GetParticleGroupList();
}

inline int32 b2World::GetParticleGroupCount() const
{
	return m_particleSystem.GetParticleGroupCount();
}

inline int32 b2World::GetParticleCount() const
{
	return m_particleSystem.GetParticleCount();
}

#endif
