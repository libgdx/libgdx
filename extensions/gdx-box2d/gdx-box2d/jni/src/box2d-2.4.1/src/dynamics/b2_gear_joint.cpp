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

#include "box2d/b2_gear_joint.h"
#include "box2d/b2_revolute_joint.h"
#include "box2d/b2_prismatic_joint.h"
#include "box2d/b2_body.h"
#include "box2d/b2_time_step.h"

// Gear Joint:
// C0 = (coordinate1 + ratio * coordinate2)_initial
// C = (coordinate1 + ratio * coordinate2) - C0 = 0
// J = [J1 ratio * J2]
// K = J * invM * JT
//   = J1 * invM1 * J1T + ratio * ratio * J2 * invM2 * J2T
//
// Revolute:
// coordinate = rotation
// Cdot = angularVelocity
// J = [0 0 1]
// K = J * invM * JT = invI
//
// Prismatic:
// coordinate = dot(p - pg, ug)
// Cdot = dot(v + cross(w, r), ug)
// J = [ug cross(r, ug)]
// K = J * invM * JT = invMass + invI * cross(r, ug)^2

b2GearJoint::b2GearJoint(const b2GearJointDef* def)
: b2Joint(def)
{
	m_joint1 = def->joint1;
	m_joint2 = def->joint2;

	m_typeA = m_joint1->GetType();
	m_typeB = m_joint2->GetType();

	b2Assert(m_typeA == e_revoluteJoint || m_typeA == e_prismaticJoint);
	b2Assert(m_typeB == e_revoluteJoint || m_typeB == e_prismaticJoint);

	float coordinateA, coordinateB;

	// TODO_ERIN there might be some problem with the joint edges in b2Joint.

	m_bodyC = m_joint1->GetBodyA();
	m_bodyA = m_joint1->GetBodyB();

	// Body B on joint1 must be dynamic
	b2Assert(m_bodyA->m_type == b2_dynamicBody);

	// Get geometry of joint1
	b2Transform xfA = m_bodyA->m_xf;
	float aA = m_bodyA->m_sweep.a;
	b2Transform xfC = m_bodyC->m_xf;
	float aC = m_bodyC->m_sweep.a;

	if (m_typeA == e_revoluteJoint)
	{
		b2RevoluteJoint* revolute = (b2RevoluteJoint*)def->joint1;
		m_localAnchorC = revolute->m_localAnchorA;
		m_localAnchorA = revolute->m_localAnchorB;
		m_referenceAngleA = revolute->m_referenceAngle;
		m_localAxisC.SetZero();

		coordinateA = aA - aC - m_referenceAngleA;

		// position error is measured in radians
		m_tolerance = b2_angularSlop;
	}
	else
	{
		b2PrismaticJoint* prismatic = (b2PrismaticJoint*)def->joint1;
		m_localAnchorC = prismatic->m_localAnchorA;
		m_localAnchorA = prismatic->m_localAnchorB;
		m_referenceAngleA = prismatic->m_referenceAngle;
		m_localAxisC = prismatic->m_localXAxisA;

		b2Vec2 pC = m_localAnchorC;
		b2Vec2 pA = b2MulT(xfC.q, b2Mul(xfA.q, m_localAnchorA) + (xfA.p - xfC.p));
		coordinateA = b2Dot(pA - pC, m_localAxisC);

		// position error is measured in meters
		m_tolerance = b2_linearSlop;
	}

	m_bodyD = m_joint2->GetBodyA();
	m_bodyB = m_joint2->GetBodyB();

	// Body B on joint2 must be dynamic
	b2Assert(m_bodyB->m_type == b2_dynamicBody);

	// Get geometry of joint2
	b2Transform xfB = m_bodyB->m_xf;
	float aB = m_bodyB->m_sweep.a;
	b2Transform xfD = m_bodyD->m_xf;
	float aD = m_bodyD->m_sweep.a;

	if (m_typeB == e_revoluteJoint)
	{
		b2RevoluteJoint* revolute = (b2RevoluteJoint*)def->joint2;
		m_localAnchorD = revolute->m_localAnchorA;
		m_localAnchorB = revolute->m_localAnchorB;
		m_referenceAngleB = revolute->m_referenceAngle;
		m_localAxisD.SetZero();

		coordinateB = aB - aD - m_referenceAngleB;
	}
	else
	{
		b2PrismaticJoint* prismatic = (b2PrismaticJoint*)def->joint2;
		m_localAnchorD = prismatic->m_localAnchorA;
		m_localAnchorB = prismatic->m_localAnchorB;
		m_referenceAngleB = prismatic->m_referenceAngle;
		m_localAxisD = prismatic->m_localXAxisA;

		b2Vec2 pD = m_localAnchorD;
		b2Vec2 pB = b2MulT(xfD.q, b2Mul(xfB.q, m_localAnchorB) + (xfB.p - xfD.p));
		coordinateB = b2Dot(pB - pD, m_localAxisD);
	}

	m_ratio = def->ratio;

	m_constant = coordinateA + m_ratio * coordinateB;

	m_impulse = 0.0f;
}

void b2GearJoint::InitVelocityConstraints(const b2SolverData& data)
{
	m_indexA = m_bodyA->m_islandIndex;
	m_indexB = m_bodyB->m_islandIndex;
	m_indexC = m_bodyC->m_islandIndex;
	m_indexD = m_bodyD->m_islandIndex;
	m_lcA = m_bodyA->m_sweep.localCenter;
	m_lcB = m_bodyB->m_sweep.localCenter;
	m_lcC = m_bodyC->m_sweep.localCenter;
	m_lcD = m_bodyD->m_sweep.localCenter;
	m_mA = m_bodyA->m_invMass;
	m_mB = m_bodyB->m_invMass;
	m_mC = m_bodyC->m_invMass;
	m_mD = m_bodyD->m_invMass;
	m_iA = m_bodyA->m_invI;
	m_iB = m_bodyB->m_invI;
	m_iC = m_bodyC->m_invI;
	m_iD = m_bodyD->m_invI;

	float aA = data.positions[m_indexA].a;
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;

	float aB = data.positions[m_indexB].a;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;

	float aC = data.positions[m_indexC].a;
	b2Vec2 vC = data.velocities[m_indexC].v;
	float wC = data.velocities[m_indexC].w;

	float aD = data.positions[m_indexD].a;
	b2Vec2 vD = data.velocities[m_indexD].v;
	float wD = data.velocities[m_indexD].w;

	b2Rot qA(aA), qB(aB), qC(aC), qD(aD);

	m_mass = 0.0f;

	if (m_typeA == e_revoluteJoint)
	{
		m_JvAC.SetZero();
		m_JwA = 1.0f;
		m_JwC = 1.0f;
		m_mass += m_iA + m_iC;
	}
	else
	{
		b2Vec2 u = b2Mul(qC, m_localAxisC);
		b2Vec2 rC = b2Mul(qC, m_localAnchorC - m_lcC);
		b2Vec2 rA = b2Mul(qA, m_localAnchorA - m_lcA);
		m_JvAC = u;
		m_JwC = b2Cross(rC, u);
		m_JwA = b2Cross(rA, u);
		m_mass += m_mC + m_mA + m_iC * m_JwC * m_JwC + m_iA * m_JwA * m_JwA;
	}

	if (m_typeB == e_revoluteJoint)
	{
		m_JvBD.SetZero();
		m_JwB = m_ratio;
		m_JwD = m_ratio;
		m_mass += m_ratio * m_ratio * (m_iB + m_iD);
	}
	else
	{
		b2Vec2 u = b2Mul(qD, m_localAxisD);
		b2Vec2 rD = b2Mul(qD, m_localAnchorD - m_lcD);
		b2Vec2 rB = b2Mul(qB, m_localAnchorB - m_lcB);
		m_JvBD = m_ratio * u;
		m_JwD = m_ratio * b2Cross(rD, u);
		m_JwB = m_ratio * b2Cross(rB, u);
		m_mass += m_ratio * m_ratio * (m_mD + m_mB) + m_iD * m_JwD * m_JwD + m_iB * m_JwB * m_JwB;
	}

	// Compute effective mass.
	m_mass = m_mass > 0.0f ? 1.0f / m_mass : 0.0f;

	if (data.step.warmStarting)
	{
		vA += (m_mA * m_impulse) * m_JvAC;
		wA += m_iA * m_impulse * m_JwA;
		vB += (m_mB * m_impulse) * m_JvBD;
		wB += m_iB * m_impulse * m_JwB;
		vC -= (m_mC * m_impulse) * m_JvAC;
		wC -= m_iC * m_impulse * m_JwC;
		vD -= (m_mD * m_impulse) * m_JvBD;
		wD -= m_iD * m_impulse * m_JwD;
	}
	else
	{
		m_impulse = 0.0f;
	}

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
	data.velocities[m_indexC].v = vC;
	data.velocities[m_indexC].w = wC;
	data.velocities[m_indexD].v = vD;
	data.velocities[m_indexD].w = wD;
}

void b2GearJoint::SolveVelocityConstraints(const b2SolverData& data)
{
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;
	b2Vec2 vC = data.velocities[m_indexC].v;
	float wC = data.velocities[m_indexC].w;
	b2Vec2 vD = data.velocities[m_indexD].v;
	float wD = data.velocities[m_indexD].w;

	float Cdot = b2Dot(m_JvAC, vA - vC) + b2Dot(m_JvBD, vB - vD);
	Cdot += (m_JwA * wA - m_JwC * wC) + (m_JwB * wB - m_JwD * wD);

	float impulse = -m_mass * Cdot;
	m_impulse += impulse;

	vA += (m_mA * impulse) * m_JvAC;
	wA += m_iA * impulse * m_JwA;
	vB += (m_mB * impulse) * m_JvBD;
	wB += m_iB * impulse * m_JwB;
	vC -= (m_mC * impulse) * m_JvAC;
	wC -= m_iC * impulse * m_JwC;
	vD -= (m_mD * impulse) * m_JvBD;
	wD -= m_iD * impulse * m_JwD;

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
	data.velocities[m_indexC].v = vC;
	data.velocities[m_indexC].w = wC;
	data.velocities[m_indexD].v = vD;
	data.velocities[m_indexD].w = wD;
}

bool b2GearJoint::SolvePositionConstraints(const b2SolverData& data)
{
	b2Vec2 cA = data.positions[m_indexA].c;
	float aA = data.positions[m_indexA].a;
	b2Vec2 cB = data.positions[m_indexB].c;
	float aB = data.positions[m_indexB].a;
	b2Vec2 cC = data.positions[m_indexC].c;
	float aC = data.positions[m_indexC].a;
	b2Vec2 cD = data.positions[m_indexD].c;
	float aD = data.positions[m_indexD].a;

	b2Rot qA(aA), qB(aB), qC(aC), qD(aD);

	float coordinateA, coordinateB;

	b2Vec2 JvAC, JvBD;
	float JwA, JwB, JwC, JwD;
	float mass = 0.0f;

	if (m_typeA == e_revoluteJoint)
	{
		JvAC.SetZero();
		JwA = 1.0f;
		JwC = 1.0f;
		mass += m_iA + m_iC;

		coordinateA = aA - aC - m_referenceAngleA;
	}
	else
	{
		b2Vec2 u = b2Mul(qC, m_localAxisC);
		b2Vec2 rC = b2Mul(qC, m_localAnchorC - m_lcC);
		b2Vec2 rA = b2Mul(qA, m_localAnchorA - m_lcA);
		JvAC = u;
		JwC = b2Cross(rC, u);
		JwA = b2Cross(rA, u);
		mass += m_mC + m_mA + m_iC * JwC * JwC + m_iA * JwA * JwA;

		b2Vec2 pC = m_localAnchorC - m_lcC;
		b2Vec2 pA = b2MulT(qC, rA + (cA - cC));
		coordinateA = b2Dot(pA - pC, m_localAxisC);
	}

	if (m_typeB == e_revoluteJoint)
	{
		JvBD.SetZero();
		JwB = m_ratio;
		JwD = m_ratio;
		mass += m_ratio * m_ratio * (m_iB + m_iD);

		coordinateB = aB - aD - m_referenceAngleB;
	}
	else
	{
		b2Vec2 u = b2Mul(qD, m_localAxisD);
		b2Vec2 rD = b2Mul(qD, m_localAnchorD - m_lcD);
		b2Vec2 rB = b2Mul(qB, m_localAnchorB - m_lcB);
		JvBD = m_ratio * u;
		JwD = m_ratio * b2Cross(rD, u);
		JwB = m_ratio * b2Cross(rB, u);
		mass += m_ratio * m_ratio * (m_mD + m_mB) + m_iD * JwD * JwD + m_iB * JwB * JwB;

		b2Vec2 pD = m_localAnchorD - m_lcD;
		b2Vec2 pB = b2MulT(qD, rB + (cB - cD));
		coordinateB = b2Dot(pB - pD, m_localAxisD);
	}

	float C = (coordinateA + m_ratio * coordinateB) - m_constant;

	float impulse = 0.0f;
	if (mass > 0.0f)
	{
		impulse = -C / mass;
	}

	cA += m_mA * impulse * JvAC;
	aA += m_iA * impulse * JwA;
	cB += m_mB * impulse * JvBD;
	aB += m_iB * impulse * JwB;
	cC -= m_mC * impulse * JvAC;
	aC -= m_iC * impulse * JwC;
	cD -= m_mD * impulse * JvBD;
	aD -= m_iD * impulse * JwD;

	data.positions[m_indexA].c = cA;
	data.positions[m_indexA].a = aA;
	data.positions[m_indexB].c = cB;
	data.positions[m_indexB].a = aB;
	data.positions[m_indexC].c = cC;
	data.positions[m_indexC].a = aC;
	data.positions[m_indexD].c = cD;
	data.positions[m_indexD].a = aD;

	if (b2Abs(C) < m_tolerance)
	{
		return true;
	}

	return false;
}

b2Vec2 b2GearJoint::GetAnchorA() const
{
	return m_bodyA->GetWorldPoint(m_localAnchorA);
}

b2Vec2 b2GearJoint::GetAnchorB() const
{
	return m_bodyB->GetWorldPoint(m_localAnchorB);
}

b2Vec2 b2GearJoint::GetReactionForce(float inv_dt) const
{
	b2Vec2 P = m_impulse * m_JvAC;
	return inv_dt * P;
}

float b2GearJoint::GetReactionTorque(float inv_dt) const
{
	float L = m_impulse * m_JwA;
	return inv_dt * L;
}

void b2GearJoint::SetRatio(float ratio)
{
	b2Assert(b2IsValid(ratio));
	m_ratio = ratio;
}

float b2GearJoint::GetRatio() const
{
	return m_ratio;
}

void b2GearJoint::Dump()
{
	int32 indexA = m_bodyA->m_islandIndex;
	int32 indexB = m_bodyB->m_islandIndex;

	int32 index1 = m_joint1->m_index;
	int32 index2 = m_joint2->m_index;

	b2Dump("  b2GearJointDef jd;\n");
	b2Dump("  jd.bodyA = bodies[%d];\n", indexA);
	b2Dump("  jd.bodyB = bodies[%d];\n", indexB);
	b2Dump("  jd.collideConnected = bool(%d);\n", m_collideConnected);
	b2Dump("  jd.joint1 = joints[%d];\n", index1);
	b2Dump("  jd.joint2 = joints[%d];\n", index2);
	b2Dump("  jd.ratio = %.9g;\n", m_ratio);
	b2Dump("  joints[%d] = m_world->CreateJoint(&jd);\n", m_index);
}
