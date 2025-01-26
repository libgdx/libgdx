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

#ifndef B2_MOUSE_JOINT_H
#define B2_MOUSE_JOINT_H

#include "b2_api.h"
#include "b2_joint.h"

/// Mouse joint definition. This requires a world target point,
/// tuning parameters, and the time step.
struct B2_API b2MouseJointDef : public b2JointDef
{
	b2MouseJointDef()
	{
		type = e_mouseJoint;
		target.Set(0.0f, 0.0f);
		maxForce = 0.0f;
		stiffness = 0.0f;
		damping = 0.0f;
	}

	/// The initial world target point. This is assumed
	/// to coincide with the body anchor initially.
	b2Vec2 target;

	/// The maximum constraint force that can be exerted
	/// to move the candidate body. Usually you will express
	/// as some multiple of the weight (multiplier * mass * gravity).
	float maxForce;

	/// The linear stiffness in N/m
	float stiffness;

	/// The linear damping in N*s/m
	float damping;
};

/// A mouse joint is used to make a point on a body track a
/// specified world point. This a soft constraint with a maximum
/// force. This allows the constraint to stretch and without
/// applying huge forces.
/// NOTE: this joint is not documented in the manual because it was
/// developed to be used in the testbed. If you want to learn how to
/// use the mouse joint, look at the testbed.
class B2_API b2MouseJoint : public b2Joint
{
public:

	/// Implements b2Joint.
	b2Vec2 GetAnchorA() const override;

	/// Implements b2Joint.
	b2Vec2 GetAnchorB() const override;

	/// Implements b2Joint.
	b2Vec2 GetReactionForce(float inv_dt) const override;

	/// Implements b2Joint.
	float GetReactionTorque(float inv_dt) const override;

	/// Use this to update the target point.
	void SetTarget(const b2Vec2& target);
	const b2Vec2& GetTarget() const;

	/// Set/get the maximum force in Newtons.
	void SetMaxForce(float force);
	float GetMaxForce() const;

	/// Set/get the linear stiffness in N/m
	void SetStiffness(float stiffness) { m_stiffness = stiffness; }
	float GetStiffness() const { return m_stiffness; }

	/// Set/get linear damping in N*s/m
	void SetDamping(float damping) { m_damping = damping; }
	float GetDamping() const { return m_damping; }

	/// The mouse joint does not support dumping.
	void Dump() override { b2Log("Mouse joint dumping is not supported.\n"); }

	/// Implement b2Joint::ShiftOrigin
	void ShiftOrigin(const b2Vec2& newOrigin) override;

protected:
	friend class b2Joint;

	b2MouseJoint(const b2MouseJointDef* def);

	void InitVelocityConstraints(const b2SolverData& data) override;
	void SolveVelocityConstraints(const b2SolverData& data) override;
	bool SolvePositionConstraints(const b2SolverData& data) override;

	b2Vec2 m_localAnchorB;
	b2Vec2 m_targetA;
	float m_stiffness;
	float m_damping;
	float m_beta;

	// Solver shared
	b2Vec2 m_impulse;
	float m_maxForce;
	float m_gamma;

	// Solver temp
	int32 m_indexA;
	int32 m_indexB;
	b2Vec2 m_rB;
	b2Vec2 m_localCenterB;
	float m_invMassB;
	float m_invIB;
	b2Mat22 m_mass;
	b2Vec2 m_C;
};

#endif
