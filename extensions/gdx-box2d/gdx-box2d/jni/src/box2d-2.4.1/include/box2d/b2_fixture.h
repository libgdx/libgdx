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

#ifndef B2_FIXTURE_H
#define B2_FIXTURE_H

#include "b2_api.h"
#include "b2_body.h"
#include "b2_collision.h"
#include "b2_shape.h"

class b2BlockAllocator;
class b2Body;
class b2BroadPhase;
class b2Fixture;

/// This holds contact filtering data.
struct B2_API b2Filter
{
	b2Filter()
	{
		categoryBits = 0x0001;
		maskBits = 0xFFFF;
		groupIndex = 0;
	}

	/// The collision category bits. Normally you would just set one bit.
	uint16 categoryBits;

	/// The collision mask bits. This states the categories that this
	/// shape would accept for collision.
	uint16 maskBits;

	/// Collision groups allow a certain group of objects to never collide (negative)
	/// or always collide (positive). Zero means no collision group. Non-zero group
	/// filtering always wins against the mask bits.
	int16 groupIndex;
};

/// A fixture definition is used to create a fixture. This class defines an
/// abstract fixture definition. You can reuse fixture definitions safely.
struct B2_API b2FixtureDef
{
	/// The constructor sets the default fixture definition values.
	b2FixtureDef()
	{
		shape = nullptr;
		friction = 0.2f;
		restitution = 0.0f;
		restitutionThreshold = 1.0f * b2_lengthUnitsPerMeter;
		density = 0.0f;
		isSensor = false;
	}

	/// The shape, this must be set. The shape will be cloned, so you
	/// can create the shape on the stack.
	const b2Shape* shape;

	/// Use this to store application specific fixture data.
//	b2FixtureUserData userData;

	/// The friction coefficient, usually in the range [0,1].
	float friction;

	/// The restitution (elasticity) usually in the range [0,1].
	float restitution;

	/// Restitution velocity threshold, usually in m/s. Collisions above this
	/// speed have restitution applied (will bounce).
	float restitutionThreshold;

	/// The density, usually in kg/m^2.
	float density;

	/// A sensor shape collects contact information but never generates a collision
	/// response.
	bool isSensor;

	/// Contact filtering data.
	b2Filter filter;
};

/// This proxy is used internally to connect fixtures to the broad-phase.
struct B2_API b2FixtureProxy
{
	b2AABB aabb;
	b2Fixture* fixture;
	int32 childIndex;
	int32 proxyId;
};

/// A fixture is used to attach a shape to a body for collision detection. A fixture
/// inherits its transform from its parent. Fixtures hold additional non-geometric data
/// such as friction, collision filters, etc.
/// Fixtures are created via b2Body::CreateFixture.
/// @warning you cannot reuse fixtures.
class B2_API b2Fixture
{
public:
	/// Get the type of the child shape. You can use this to down cast to the concrete shape.
	/// @return the shape type.
	b2Shape::Type GetType() const;

	/// Get the child shape. You can modify the child shape, however you should not change the
	/// number of vertices because this will crash some collision caching mechanisms.
	/// Manipulating the shape may lead to non-physical behavior.
	b2Shape* GetShape();
	const b2Shape* GetShape() const;

	/// Set if this fixture is a sensor.
	void SetSensor(bool sensor);

	/// Is this fixture a sensor (non-solid)?
	/// @return the true if the shape is a sensor.
	bool IsSensor() const;

	/// Set the contact filtering data. This will not update contacts until the next time
	/// step when either parent body is active and awake.
	/// This automatically calls Refilter.
	void SetFilterData(const b2Filter& filter);

	/// Get the contact filtering data.
	const b2Filter& GetFilterData() const;

	/// Call this if you want to establish collision that was previously disabled by b2ContactFilter::ShouldCollide.
	void Refilter();

	/// Get the parent body of this fixture. This is nullptr if the fixture is not attached.
	/// @return the parent body.
	b2Body* GetBody();
	const b2Body* GetBody() const;

	/// Get the next fixture in the parent body's fixture list.
	/// @return the next shape.
	b2Fixture* GetNext();
	const b2Fixture* GetNext() const;

	/// Get the user data that was assigned in the fixture definition. Use this to
	/// store your application specific data.
//	b2FixtureUserData& GetUserData();
//	const b2FixtureUserData& GetUserData() const;

	/// Test a point for containment in this fixture.
	/// @param p a point in world coordinates.
	bool TestPoint(const b2Vec2& p) const;

	/// Cast a ray against this shape.
	/// @param output the ray-cast results.
	/// @param input the ray-cast input parameters.
	/// @param childIndex the child shape index (e.g. edge index)
	bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input, int32 childIndex) const;

	/// Get the mass data for this fixture. The mass data is based on the density and
	/// the shape. The rotational inertia is about the shape's origin. This operation
	/// may be expensive.
	void GetMassData(b2MassData* massData) const;

	/// Set the density of this fixture. This will _not_ automatically adjust the mass
	/// of the body. You must call b2Body::ResetMassData to update the body's mass.
	void SetDensity(float density);

	/// Get the density of this fixture.
	float GetDensity() const;

	/// Get the coefficient of friction.
	float GetFriction() const;

	/// Set the coefficient of friction. This will _not_ change the friction of
	/// existing contacts.
	void SetFriction(float friction);

	/// Get the coefficient of restitution.
	float GetRestitution() const;

	/// Set the coefficient of restitution. This will _not_ change the restitution of
	/// existing contacts.
	void SetRestitution(float restitution);

	/// Get the restitution velocity threshold.
	float GetRestitutionThreshold() const;

	/// Set the restitution threshold. This will _not_ change the restitution threshold of
	/// existing contacts.
	void SetRestitutionThreshold(float threshold);

	/// Get the fixture's AABB. This AABB may be enlarge and/or stale.
	/// If you need a more accurate AABB, compute it using the shape and
	/// the body transform.
	const b2AABB& GetAABB(int32 childIndex) const;

	/// Dump this fixture to the log file.
	void Dump(int32 bodyIndex);

protected:

	friend class b2Body;
	friend class b2World;
	friend class b2Contact;
	friend class b2ContactManager;

	b2Fixture();

	// We need separation create/destroy functions from the constructor/destructor because
	// the destructor cannot access the allocator (no destructor arguments allowed by C++).
	void Create(b2BlockAllocator* allocator, b2Body* body, const b2FixtureDef* def);
	void Destroy(b2BlockAllocator* allocator);

	// These support body activation/deactivation.
	void CreateProxies(b2BroadPhase* broadPhase, const b2Transform& xf);
	void DestroyProxies(b2BroadPhase* broadPhase);

	void Synchronize(b2BroadPhase* broadPhase, const b2Transform& xf1, const b2Transform& xf2);

	float m_density;

	b2Fixture* m_next;
	b2Body* m_body;

	b2Shape* m_shape;

	float m_friction;
	float m_restitution;
	float m_restitutionThreshold;

	b2FixtureProxy* m_proxies;
	int32 m_proxyCount;

	b2Filter m_filter;

	bool m_isSensor;

//	b2FixtureUserData m_userData;
};

inline b2Shape::Type b2Fixture::GetType() const
{
	return m_shape->GetType();
}

inline b2Shape* b2Fixture::GetShape()
{
	return m_shape;
}

inline const b2Shape* b2Fixture::GetShape() const
{
	return m_shape;
}

inline bool b2Fixture::IsSensor() const
{
	return m_isSensor;
}

inline const b2Filter& b2Fixture::GetFilterData() const
{
	return m_filter;
}

//inline b2FixtureUserData& b2Fixture::GetUserData()
//{
//	return m_userData;
//}
//
//inline const b2FixtureUserData& b2Fixture::GetUserData() const
//{
//	return m_userData;
//}

inline b2Body* b2Fixture::GetBody()
{
	return m_body;
}

inline const b2Body* b2Fixture::GetBody() const
{
	return m_body;
}

inline b2Fixture* b2Fixture::GetNext()
{
	return m_next;
}

inline const b2Fixture* b2Fixture::GetNext() const
{
	return m_next;
}

inline void b2Fixture::SetDensity(float density)
{
	b2Assert(b2IsValid(density) && density >= 0.0f);
	m_density = density;
}

inline float b2Fixture::GetDensity() const
{
	return m_density;
}

inline float b2Fixture::GetFriction() const
{
	return m_friction;
}

inline void b2Fixture::SetFriction(float friction)
{
	m_friction = friction;
}

inline float b2Fixture::GetRestitution() const
{
	return m_restitution;
}

inline void b2Fixture::SetRestitution(float restitution)
{
	m_restitution = restitution;
}

inline float b2Fixture::GetRestitutionThreshold() const
{
	return m_restitutionThreshold;
}

inline void b2Fixture::SetRestitutionThreshold(float threshold)
{
	m_restitutionThreshold = threshold;
}

inline bool b2Fixture::TestPoint(const b2Vec2& p) const
{
	return m_shape->TestPoint(m_body->GetTransform(), p);
}

inline bool b2Fixture::RayCast(b2RayCastOutput* output, const b2RayCastInput& input, int32 childIndex) const
{
	return m_shape->RayCast(output, input, m_body->GetTransform(), childIndex);
}

inline void b2Fixture::GetMassData(b2MassData* massData) const
{
	m_shape->ComputeMass(massData, m_density);
}

inline const b2AABB& b2Fixture::GetAABB(int32 childIndex) const
{
	b2Assert(0 <= childIndex && childIndex < m_proxyCount);
	return m_proxies[childIndex].aabb;
}

#endif
