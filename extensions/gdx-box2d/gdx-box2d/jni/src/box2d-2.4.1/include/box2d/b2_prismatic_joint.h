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

#ifndef B2_PRISMATIC_JOINT_H
#define B2_PRISMATIC_JOINT_H

#include "b2_api.h"
#include "b2_joint.h"

/// Prismatic joint definition. This requires defining a line of
/// motion using an axis and an anchor point. The definition uses local
/// anchor points and a local axis so that the initial configuration
/// can violate the constraint slightly. The joint translation is zero
/// when the local anchor points coincide in world space. Using local
/// anchors and a local axis helps when saving and loading a game.
struct B2_API b2PrismaticJointDef : public b2JointDef
{
	b2PrismaticJointDef()
	{
		type = e_prismaticJoint;
		localAnchorA.SetZero();
		localAnchorB.SetZero();
		localAxisA.Set(1.0f, 0.0f);
		referenceAngle = 0.0f;
		enableLimit = false;
		lowerTranslation = 0.0f;
		upperTranslation = 0.0f;
		enableMotor = false;
		maxMotorForce = 0.0f;
		motorSpeed = 0.0f;
	}

	/// Initialize the bodies, anchors, axis, and reference angle using the world
	/// anchor and unit world axis.
	void Initialize(b2Body* bodyA, b2Body* bodyB, const b2Vec2& anchor, const b2Vec2& axis);

	/// The local anchor point relative to bodyA's origin.
	b2Vec2 localAnchorA;

	/// The local anchor point relative to bodyB's origin.
	b2Vec2 localAnchorB;

	/// The local translation unit axis in bodyA.
	b2Vec2 localAxisA;

	/// The constrained angle between the bodies: bodyB_angle - bodyA_angle.
	float referenceAngle;

	/// Enable/disable the joint limit.
	bool enableLimit;

	/// The lower translation limit, usually in meters.
	float lowerTranslation;

	/// The upper translation limit, usually in meters.
	float upperTranslation;

	/// Enable/disable the joint motor.
	bool enableMotor;

	/// The maximum motor torque, usually in N-m.
	float maxMotorForce;

	/// The desired motor speed in radians per second.
	float motorSpeed;
};

/// A prismatic joint. This joint provides one degree of freedom: translation
/// along an axis fixed in bodyA. Relative rotation is prevented. You can
/// use a joint limit to restrict the range of motion and a joint motor to
/// drive the motion or to model joint friction.
class B2_API b2PrismaticJoint : public b2Joint
{
public:
	b2Vec2 GetAnchorA() const override;
	b2Vec2 GetAnchorB() const override;

	b2Vec2 GetReactionForce(float inv_dt) const override;
	float GetReactionTorque(float inv_dt) const override;

	/// The local anchor point relative to bodyA's origin.
	const b2Vec2& GetLocalAnchorA() const { return m_localAnchorA; }

	/// The local anchor point relative to bodyB's origin.
	const b2Vec2& GetLocalAnchorB() const  { return m_localAnchorB; }

	/// The local joint axis relative to bodyA.
	const b2Vec2& GetLocalAxisA() const { return m_localXAxisA; }

	/// Get the reference angle.
	float GetReferenceAngle() const { return m_referenceAngle; }

	/// Get the current joint translation, usually in meters.
	float GetJointTranslation() const;

	/// Get the current joint translation speed, usually in meters per second.
	float GetJointSpeed() const;

	/// Is the joint limit enabled?
	bool IsLimitEnabled() const;

	/// Enable/disable the joint limit.
	void EnableLimit(bool flag);

	/// Get the lower joint limit, usually in meters.
	float GetLowerLimit() const;

	/// Get the upper joint limit, usually in meters.
	float GetUpperLimit() const;

	/// Set the joint limits, usually in meters.
	void SetLimits(float lower, float upper);

	/// Is the joint motor enabled?
	bool IsMotorEnabled() const;

	/// Enable/disable the joint motor.
	void EnableMotor(bool flag);

	/// Set the motor speed, usually in meters per second.
	void SetMotorSpeed(float speed);

	/// Get the motor speed, usually in meters per second.
	float GetMotorSpeed() const;

	/// Set the maximum motor force, usually in N.
	void SetMaxMotorForce(float force);
	float GetMaxMotorForce() const { return m_maxMotorForce; }

	/// Get the current motor force given the inverse time step, usually in N.
	float GetMotorForce(float inv_dt) const;

	/// Dump to b2Log
	void Dump() override;

	///
	void Draw(b2Draw* draw) const override;

protected:
	friend class b2Joint;
	friend class b2GearJoint;
	b2PrismaticJoint(const b2PrismaticJointDef* def);

	void InitVelocityConstraints(const b2SolverData& data) override;
	void SolveVelocityConstraints(const b2SolverData& data) override;
	bool SolvePositionConstraints(const b2SolverData& data) override;

	b2Vec2 m_localAnchorA;
	b2Vec2 m_localAnchorB;
	b2Vec2 m_localXAxisA;
	b2Vec2 m_localYAxisA;
	float m_referenceAngle;
	b2Vec2 m_impulse;
	float m_motorImpulse;
	float m_lowerImpulse;
	float m_upperImpulse;
	float m_lowerTranslation;
	float m_upperTranslation;
	float m_maxMotorForce;
	float m_motorSpeed;
	bool m_enableLimit;
	bool m_enableMotor;

	// Solver temp
	int32 m_indexA;
	int32 m_indexB;
	b2Vec2 m_localCenterA;
	b2Vec2 m_localCenterB;
	float m_invMassA;
	float m_invMassB;
	float m_invIA;
	float m_invIB;
	b2Vec2 m_axis, m_perp;
	float m_s1, m_s2;
	float m_a1, m_a2;
	b2Mat22 m_K;
	float m_translation;
	float m_axialMass;
};

inline float b2PrismaticJoint::GetMotorSpeed() const
{
	return m_motorSpeed;
}

#endif
