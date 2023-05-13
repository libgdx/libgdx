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

#include "box2d/b2_body.h"
#include "box2d/b2_draw.h"
#include "box2d/b2_prismatic_joint.h"
#include "box2d/b2_time_step.h"

// Linear constraint (point-to-line)
// d = p2 - p1 = x2 + r2 - x1 - r1
// C = dot(perp, d)
// Cdot = dot(d, cross(w1, perp)) + dot(perp, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//      = -dot(perp, v1) - dot(cross(d + r1, perp), w1) + dot(perp, v2) + dot(cross(r2, perp), v2)
// J = [-perp, -cross(d + r1, perp), perp, cross(r2,perp)]
//
// Angular constraint
// C = a2 - a1 + a_initial
// Cdot = w2 - w1
// J = [0 0 -1 0 0 1]
//
// K = J * invM * JT
//
// J = [-a -s1 a s2]
//     [0  -1  0  1]
// a = perp
// s1 = cross(d + r1, a) = cross(p2 - x1, a)
// s2 = cross(r2, a) = cross(p2 - x2, a)

// Motor/Limit linear constraint
// C = dot(ax1, d)
// Cdot = -dot(ax1, v1) - dot(cross(d + r1, ax1), w1) + dot(ax1, v2) + dot(cross(r2, ax1), v2)
// J = [-ax1 -cross(d+r1,ax1) ax1 cross(r2,ax1)]

// Predictive limit is applied even when the limit is not active.
// Prevents a constraint speed that can lead to a constraint error in one time step.
// Want C2 = C1 + h * Cdot >= 0
// Or:
// Cdot + C1/h >= 0
// I do not apply a negative constraint error because that is handled in position correction.
// So:
// Cdot + max(C1, 0)/h >= 0

// Block Solver
// We develop a block solver that includes the angular and linear constraints. This makes the limit stiffer.
//
// The Jacobian has 2 rows:
// J = [-uT -s1 uT s2] // linear
//     [0   -1   0  1] // angular
//
// u = perp
// s1 = cross(d + r1, u), s2 = cross(r2, u)
// a1 = cross(d + r1, v), a2 = cross(r2, v)

void b2PrismaticJointDef::Initialize(b2Body* bA, b2Body* bB, const b2Vec2& anchor, const b2Vec2& axis)
{
	bodyA = bA;
	bodyB = bB;
	localAnchorA = bodyA->GetLocalPoint(anchor);
	localAnchorB = bodyB->GetLocalPoint(anchor);
	localAxisA = bodyA->GetLocalVector(axis);
	referenceAngle = bodyB->GetAngle() - bodyA->GetAngle();
}

b2PrismaticJoint::b2PrismaticJoint(const b2PrismaticJointDef* def)
: b2Joint(def)
{
	m_localAnchorA = def->localAnchorA;
	m_localAnchorB = def->localAnchorB;
	m_localXAxisA = def->localAxisA;
	m_localXAxisA.Normalize();
	m_localYAxisA = b2Cross(1.0f, m_localXAxisA);
	m_referenceAngle = def->referenceAngle;

	m_impulse.SetZero();
	m_axialMass = 0.0f;
	m_motorImpulse = 0.0f;
	m_lowerImpulse = 0.0f;
	m_upperImpulse = 0.0f;

	m_lowerTranslation = def->lowerTranslation;
	m_upperTranslation = def->upperTranslation;

	b2Assert(m_lowerTranslation <= m_upperTranslation);

	m_maxMotorForce = def->maxMotorForce;
	m_motorSpeed = def->motorSpeed;
	m_enableLimit = def->enableLimit;
	m_enableMotor = def->enableMotor;

	m_translation = 0.0f;
	m_axis.SetZero();
	m_perp.SetZero();
}

void b2PrismaticJoint::InitVelocityConstraints(const b2SolverData& data)
{
	m_indexA = m_bodyA->m_islandIndex;
	m_indexB = m_bodyB->m_islandIndex;
	m_localCenterA = m_bodyA->m_sweep.localCenter;
	m_localCenterB = m_bodyB->m_sweep.localCenter;
	m_invMassA = m_bodyA->m_invMass;
	m_invMassB = m_bodyB->m_invMass;
	m_invIA = m_bodyA->m_invI;
	m_invIB = m_bodyB->m_invI;

	b2Vec2 cA = data.positions[m_indexA].c;
	float aA = data.positions[m_indexA].a;
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;

	b2Vec2 cB = data.positions[m_indexB].c;
	float aB = data.positions[m_indexB].a;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;

	b2Rot qA(aA), qB(aB);

	// Compute the effective masses.
	b2Vec2 rA = b2Mul(qA, m_localAnchorA - m_localCenterA);
	b2Vec2 rB = b2Mul(qB, m_localAnchorB - m_localCenterB);
	b2Vec2 d = (cB - cA) + rB - rA;

	float mA = m_invMassA, mB = m_invMassB;
	float iA = m_invIA, iB = m_invIB;

	// Compute motor Jacobian and effective mass.
	{
		m_axis = b2Mul(qA, m_localXAxisA);
		m_a1 = b2Cross(d + rA, m_axis);
		m_a2 = b2Cross(rB, m_axis);

		m_axialMass = mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2;
		if (m_axialMass > 0.0f)
		{
			m_axialMass = 1.0f / m_axialMass;
		}
	}

	// Prismatic constraint.
	{
		m_perp = b2Mul(qA, m_localYAxisA);

		m_s1 = b2Cross(d + rA, m_perp);
		m_s2 = b2Cross(rB, m_perp);

		float k11 = mA + mB + iA * m_s1 * m_s1 + iB * m_s2 * m_s2;
		float k12 = iA * m_s1 + iB * m_s2;
		float k22 = iA + iB;
		if (k22 == 0.0f)
		{
			// For bodies with fixed rotation.
			k22 = 1.0f;
		}

		m_K.ex.Set(k11, k12);
		m_K.ey.Set(k12, k22);
	}

	if (m_enableLimit)
	{
		m_translation = b2Dot(m_axis, d);
	}
	else
	{
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
	}

	if (m_enableMotor == false)
	{
		m_motorImpulse = 0.0f;
	}

	if (data.step.warmStarting)
	{
		// Account for variable time step.
		m_impulse *= data.step.dtRatio;
		m_motorImpulse *= data.step.dtRatio;
		m_lowerImpulse *= data.step.dtRatio;
		m_upperImpulse *= data.step.dtRatio;

		float axialImpulse = m_motorImpulse + m_lowerImpulse - m_upperImpulse;
		b2Vec2 P = m_impulse.x * m_perp + axialImpulse * m_axis;
		float LA = m_impulse.x * m_s1 + m_impulse.y + axialImpulse * m_a1;
		float LB = m_impulse.x * m_s2 + m_impulse.y + axialImpulse * m_a2;

		vA -= mA * P;
		wA -= iA * LA;

		vB += mB * P;
		wB += iB * LB;
	}
	else
	{
		m_impulse.SetZero();
		m_motorImpulse = 0.0f;
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
	}

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
}

void b2PrismaticJoint::SolveVelocityConstraints(const b2SolverData& data)
{
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;

	float mA = m_invMassA, mB = m_invMassB;
	float iA = m_invIA, iB = m_invIB;

	// Solve linear motor constraint
	if (m_enableMotor)
	{
		float Cdot = b2Dot(m_axis, vB - vA) + m_a2 * wB - m_a1 * wA;
		float impulse = m_axialMass * (m_motorSpeed - Cdot);
		float oldImpulse = m_motorImpulse;
		float maxImpulse = data.step.dt * m_maxMotorForce;
		m_motorImpulse = b2Clamp(m_motorImpulse + impulse, -maxImpulse, maxImpulse);
		impulse = m_motorImpulse - oldImpulse;

		b2Vec2 P = impulse * m_axis;
		float LA = impulse * m_a1;
		float LB = impulse * m_a2;

		vA -= mA * P;
		wA -= iA * LA;
		vB += mB * P;
		wB += iB * LB;
	}

	if (m_enableLimit)
	{
		// Lower limit
		{
			float C = m_translation - m_lowerTranslation;
			float Cdot = b2Dot(m_axis, vB - vA) + m_a2 * wB - m_a1 * wA;
			float impulse = -m_axialMass * (Cdot + b2Max(C, 0.0f) * data.step.inv_dt);
			float oldImpulse = m_lowerImpulse;
			m_lowerImpulse = b2Max(m_lowerImpulse + impulse, 0.0f);
			impulse = m_lowerImpulse - oldImpulse;

			b2Vec2 P = impulse * m_axis;
			float LA = impulse * m_a1;
			float LB = impulse * m_a2;

			vA -= mA * P;
			wA -= iA * LA;
			vB += mB * P;
			wB += iB * LB;
		}

		// Upper limit
		// Note: signs are flipped to keep C positive when the constraint is satisfied.
		// This also keeps the impulse positive when the limit is active.
		{
			float C = m_upperTranslation - m_translation;
			float Cdot = b2Dot(m_axis, vA - vB) + m_a1 * wA - m_a2 * wB;
			float impulse = -m_axialMass * (Cdot + b2Max(C, 0.0f) * data.step.inv_dt);
			float oldImpulse = m_upperImpulse;
			m_upperImpulse = b2Max(m_upperImpulse + impulse, 0.0f);
			impulse = m_upperImpulse - oldImpulse;

			b2Vec2 P = impulse * m_axis;
			float LA = impulse * m_a1;
			float LB = impulse * m_a2;

			vA += mA * P;
			wA += iA * LA;
			vB -= mB * P;
			wB -= iB * LB;
		}
	}

	// Solve the prismatic constraint in block form.
	{
		b2Vec2 Cdot;
		Cdot.x = b2Dot(m_perp, vB - vA) + m_s2 * wB - m_s1 * wA;
		Cdot.y = wB - wA;

		b2Vec2 df = m_K.Solve(-Cdot);
		m_impulse += df;

		b2Vec2 P = df.x * m_perp;
		float LA = df.x * m_s1 + df.y;
		float LB = df.x * m_s2 + df.y;

		vA -= mA * P;
		wA -= iA * LA;

		vB += mB * P;
		wB += iB * LB;
	}

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
}

// A velocity based solver computes reaction forces(impulses) using the velocity constraint solver.Under this context,
// the position solver is not there to resolve forces.It is only there to cope with integration error.
//
// Therefore, the pseudo impulses in the position solver do not have any physical meaning.Thus it is okay if they suck.
//
// We could take the active state from the velocity solver.However, the joint might push past the limit when the velocity
// solver indicates the limit is inactive.
bool b2PrismaticJoint::SolvePositionConstraints(const b2SolverData& data)
{
	b2Vec2 cA = data.positions[m_indexA].c;
	float aA = data.positions[m_indexA].a;
	b2Vec2 cB = data.positions[m_indexB].c;
	float aB = data.positions[m_indexB].a;

	b2Rot qA(aA), qB(aB);

	float mA = m_invMassA, mB = m_invMassB;
	float iA = m_invIA, iB = m_invIB;

	// Compute fresh Jacobians
	b2Vec2 rA = b2Mul(qA, m_localAnchorA - m_localCenterA);
	b2Vec2 rB = b2Mul(qB, m_localAnchorB - m_localCenterB);
	b2Vec2 d = cB + rB - cA - rA;

	b2Vec2 axis = b2Mul(qA, m_localXAxisA);
	float a1 = b2Cross(d + rA, axis);
	float a2 = b2Cross(rB, axis);
	b2Vec2 perp = b2Mul(qA, m_localYAxisA);

	float s1 = b2Cross(d + rA, perp);
	float s2 = b2Cross(rB, perp);

	b2Vec3 impulse;
	b2Vec2 C1;
	C1.x = b2Dot(perp, d);
	C1.y = aB - aA - m_referenceAngle;

	float linearError = b2Abs(C1.x);
	float angularError = b2Abs(C1.y);

	bool active = false;
	float C2 = 0.0f;
	if (m_enableLimit)
	{
		float translation = b2Dot(axis, d);
		if (b2Abs(m_upperTranslation - m_lowerTranslation) < 2.0f * b2_linearSlop)
		{
			C2 = translation;
			linearError = b2Max(linearError, b2Abs(translation));
			active = true;
		}
		else if (translation <= m_lowerTranslation)
		{
			C2 = b2Min(translation - m_lowerTranslation, 0.0f);
			linearError = b2Max(linearError, m_lowerTranslation - translation);
			active = true;
		}
		else if (translation >= m_upperTranslation)
		{
			C2 = b2Max(translation - m_upperTranslation, 0.0f);
			linearError = b2Max(linearError, translation - m_upperTranslation);
			active = true;
		}
	}

	if (active)
	{
		float k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2;
		float k12 = iA * s1 + iB * s2;
		float k13 = iA * s1 * a1 + iB * s2 * a2;
		float k22 = iA + iB;
		if (k22 == 0.0f)
		{
			// For fixed rotation
			k22 = 1.0f;
		}
		float k23 = iA * a1 + iB * a2;
		float k33 = mA + mB + iA * a1 * a1 + iB * a2 * a2;

		b2Mat33 K;
		K.ex.Set(k11, k12, k13);
		K.ey.Set(k12, k22, k23);
		K.ez.Set(k13, k23, k33);

		b2Vec3 C;
		C.x = C1.x;
		C.y = C1.y;
		C.z = C2;

		impulse = K.Solve33(-C);
	}
	else
	{
		float k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2;
		float k12 = iA * s1 + iB * s2;
		float k22 = iA + iB;
		if (k22 == 0.0f)
		{
			k22 = 1.0f;
		}

		b2Mat22 K;
		K.ex.Set(k11, k12);
		K.ey.Set(k12, k22);

		b2Vec2 impulse1 = K.Solve(-C1);
		impulse.x = impulse1.x;
		impulse.y = impulse1.y;
		impulse.z = 0.0f;
	}

	b2Vec2 P = impulse.x * perp + impulse.z * axis;
	float LA = impulse.x * s1 + impulse.y + impulse.z * a1;
	float LB = impulse.x * s2 + impulse.y + impulse.z * a2;

	cA -= mA * P;
	aA -= iA * LA;
	cB += mB * P;
	aB += iB * LB;

	data.positions[m_indexA].c = cA;
	data.positions[m_indexA].a = aA;
	data.positions[m_indexB].c = cB;
	data.positions[m_indexB].a = aB;

	return linearError <= b2_linearSlop && angularError <= b2_angularSlop;
}

b2Vec2 b2PrismaticJoint::GetAnchorA() const
{
	return m_bodyA->GetWorldPoint(m_localAnchorA);
}

b2Vec2 b2PrismaticJoint::GetAnchorB() const
{
	return m_bodyB->GetWorldPoint(m_localAnchorB);
}

b2Vec2 b2PrismaticJoint::GetReactionForce(float inv_dt) const
{
	return inv_dt * (m_impulse.x * m_perp + (m_motorImpulse + m_lowerImpulse - m_upperImpulse) * m_axis);
}

float b2PrismaticJoint::GetReactionTorque(float inv_dt) const
{
	return inv_dt * m_impulse.y;
}

float b2PrismaticJoint::GetJointTranslation() const
{
	b2Vec2 pA = m_bodyA->GetWorldPoint(m_localAnchorA);
	b2Vec2 pB = m_bodyB->GetWorldPoint(m_localAnchorB);
	b2Vec2 d = pB - pA;
	b2Vec2 axis = m_bodyA->GetWorldVector(m_localXAxisA);

	float translation = b2Dot(d, axis);
	return translation;
}

float b2PrismaticJoint::GetJointSpeed() const
{
	b2Body* bA = m_bodyA;
	b2Body* bB = m_bodyB;

	b2Vec2 rA = b2Mul(bA->m_xf.q, m_localAnchorA - bA->m_sweep.localCenter);
	b2Vec2 rB = b2Mul(bB->m_xf.q, m_localAnchorB - bB->m_sweep.localCenter);
	b2Vec2 p1 = bA->m_sweep.c + rA;
	b2Vec2 p2 = bB->m_sweep.c + rB;
	b2Vec2 d = p2 - p1;
	b2Vec2 axis = b2Mul(bA->m_xf.q, m_localXAxisA);

	b2Vec2 vA = bA->m_linearVelocity;
	b2Vec2 vB = bB->m_linearVelocity;
	float wA = bA->m_angularVelocity;
	float wB = bB->m_angularVelocity;

	float speed = b2Dot(d, b2Cross(wA, axis)) + b2Dot(axis, vB + b2Cross(wB, rB) - vA - b2Cross(wA, rA));
	return speed;
}

bool b2PrismaticJoint::IsLimitEnabled() const
{
	return m_enableLimit;
}

void b2PrismaticJoint::EnableLimit(bool flag)
{
	if (flag != m_enableLimit)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_enableLimit = flag;
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
	}
}

float b2PrismaticJoint::GetLowerLimit() const
{
	return m_lowerTranslation;
}

float b2PrismaticJoint::GetUpperLimit() const
{
	return m_upperTranslation;
}

void b2PrismaticJoint::SetLimits(float lower, float upper)
{
	b2Assert(lower <= upper);
	if (lower != m_lowerTranslation || upper != m_upperTranslation)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_lowerTranslation = lower;
		m_upperTranslation = upper;
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
	}
}

bool b2PrismaticJoint::IsMotorEnabled() const
{
	return m_enableMotor;
}

void b2PrismaticJoint::EnableMotor(bool flag)
{
	if (flag != m_enableMotor)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_enableMotor = flag;
	}
}

void b2PrismaticJoint::SetMotorSpeed(float speed)
{
	if (speed != m_motorSpeed)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_motorSpeed = speed;
	}
}

void b2PrismaticJoint::SetMaxMotorForce(float force)
{
	if (force != m_maxMotorForce)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_maxMotorForce = force;
	}
}

float b2PrismaticJoint::GetMotorForce(float inv_dt) const
{
	return inv_dt * m_motorImpulse;
}

void b2PrismaticJoint::Dump()
{
	// FLT_DECIMAL_DIG == 9

	int32 indexA = m_bodyA->m_islandIndex;
	int32 indexB = m_bodyB->m_islandIndex;

	b2Dump("  b2PrismaticJointDef jd;\n");
	b2Dump("  jd.bodyA = bodies[%d];\n", indexA);
	b2Dump("  jd.bodyB = bodies[%d];\n", indexB);
	b2Dump("  jd.collideConnected = bool(%d);\n", m_collideConnected);
	b2Dump("  jd.localAnchorA.Set(%.9g, %.9g);\n", m_localAnchorA.x, m_localAnchorA.y);
	b2Dump("  jd.localAnchorB.Set(%.9g, %.9g);\n", m_localAnchorB.x, m_localAnchorB.y);
	b2Dump("  jd.localAxisA.Set(%.9g, %.9g);\n", m_localXAxisA.x, m_localXAxisA.y);
	b2Dump("  jd.referenceAngle = %.9g;\n", m_referenceAngle);
	b2Dump("  jd.enableLimit = bool(%d);\n", m_enableLimit);
	b2Dump("  jd.lowerTranslation = %.9g;\n", m_lowerTranslation);
	b2Dump("  jd.upperTranslation = %.9g;\n", m_upperTranslation);
	b2Dump("  jd.enableMotor = bool(%d);\n", m_enableMotor);
	b2Dump("  jd.motorSpeed = %.9g;\n", m_motorSpeed);
	b2Dump("  jd.maxMotorForce = %.9g;\n", m_maxMotorForce);
	b2Dump("  joints[%d] = m_world->CreateJoint(&jd);\n", m_index);
}

void b2PrismaticJoint::Draw(b2Draw* draw) const
{
	const b2Transform& xfA = m_bodyA->GetTransform();
	const b2Transform& xfB = m_bodyB->GetTransform();
	b2Vec2 pA = b2Mul(xfA, m_localAnchorA);
	b2Vec2 pB = b2Mul(xfB, m_localAnchorB);

	b2Vec2 axis = b2Mul(xfA.q, m_localXAxisA);

	b2Color c1(0.7f, 0.7f, 0.7f);
	b2Color c2(0.3f, 0.9f, 0.3f);
	b2Color c3(0.9f, 0.3f, 0.3f);
	b2Color c4(0.3f, 0.3f, 0.9f);
	b2Color c5(0.4f, 0.4f, 0.4f);

	draw->DrawSegment(pA, pB, c5);

	if (m_enableLimit)
	{
		b2Vec2 lower = pA + m_lowerTranslation * axis;
		b2Vec2 upper = pA + m_upperTranslation * axis;
		b2Vec2 perp = b2Mul(xfA.q, m_localYAxisA);
		draw->DrawSegment(lower, upper, c1);
		draw->DrawSegment(lower - 0.5f * perp, lower + 0.5f * perp, c2);
		draw->DrawSegment(upper - 0.5f * perp, upper + 0.5f * perp, c3);
	}
	else
	{
		draw->DrawSegment(pA - 1.0f * axis, pA + 1.0f * axis, c1);
	}

	draw->DrawPoint(pA, 5.0f, c1);
	draw->DrawPoint(pB, 5.0f, c4);
}
