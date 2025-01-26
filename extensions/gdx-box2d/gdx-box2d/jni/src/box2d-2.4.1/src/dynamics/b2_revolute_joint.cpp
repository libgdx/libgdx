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
#include "box2d/b2_revolute_joint.h"
#include "box2d/b2_time_step.h"

// Point-to-point constraint
// C = p2 - p1
// Cdot = v2 - v1
//      = v2 + cross(w2, r2) - v1 - cross(w1, r1)
// J = [-I -r1_skew I r2_skew ]
// Identity used:
// w k % (rx i + ry j) = w * (-ry i + rx j)

// Motor constraint
// Cdot = w2 - w1
// J = [0 0 -1 0 0 1]
// K = invI1 + invI2

void b2RevoluteJointDef::Initialize(b2Body* bA, b2Body* bB, const b2Vec2& anchor)
{
	bodyA = bA;
	bodyB = bB;
	localAnchorA = bodyA->GetLocalPoint(anchor);
	localAnchorB = bodyB->GetLocalPoint(anchor);
	referenceAngle = bodyB->GetAngle() - bodyA->GetAngle();
}

b2RevoluteJoint::b2RevoluteJoint(const b2RevoluteJointDef* def)
: b2Joint(def)
{
	m_localAnchorA = def->localAnchorA;
	m_localAnchorB = def->localAnchorB;
	m_referenceAngle = def->referenceAngle;

	m_impulse.SetZero();
	m_axialMass = 0.0f;
	m_motorImpulse = 0.0f;
	m_lowerImpulse = 0.0f;
	m_upperImpulse = 0.0f;

	m_lowerAngle = def->lowerAngle;
	m_upperAngle = def->upperAngle;
	m_maxMotorTorque = def->maxMotorTorque;
	m_motorSpeed = def->motorSpeed;
	m_enableLimit = def->enableLimit;
	m_enableMotor = def->enableMotor;

	m_angle = 0.0f;
}

void b2RevoluteJoint::InitVelocityConstraints(const b2SolverData& data)
{
	m_indexA = m_bodyA->m_islandIndex;
	m_indexB = m_bodyB->m_islandIndex;
	m_localCenterA = m_bodyA->m_sweep.localCenter;
	m_localCenterB = m_bodyB->m_sweep.localCenter;
	m_invMassA = m_bodyA->m_invMass;
	m_invMassB = m_bodyB->m_invMass;
	m_invIA = m_bodyA->m_invI;
	m_invIB = m_bodyB->m_invI;

	float aA = data.positions[m_indexA].a;
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;

	float aB = data.positions[m_indexB].a;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;

	b2Rot qA(aA), qB(aB);

	m_rA = b2Mul(qA, m_localAnchorA - m_localCenterA);
	m_rB = b2Mul(qB, m_localAnchorB - m_localCenterB);

	// J = [-I -r1_skew I r2_skew]
	// r_skew = [-ry; rx]

	// Matlab
	// K = [ mA+r1y^2*iA+mB+r2y^2*iB,  -r1y*iA*r1x-r2y*iB*r2x]
	//     [  -r1y*iA*r1x-r2y*iB*r2x, mA+r1x^2*iA+mB+r2x^2*iB]

	float mA = m_invMassA, mB = m_invMassB;
	float iA = m_invIA, iB = m_invIB;

	m_K.ex.x = mA + mB + m_rA.y * m_rA.y * iA + m_rB.y * m_rB.y * iB;
	m_K.ey.x = -m_rA.y * m_rA.x * iA - m_rB.y * m_rB.x * iB;
	m_K.ex.y = m_K.ey.x;
	m_K.ey.y = mA + mB + m_rA.x * m_rA.x * iA + m_rB.x * m_rB.x * iB;

	m_axialMass = iA + iB;
	bool fixedRotation;
	if (m_axialMass > 0.0f)
	{
		m_axialMass = 1.0f / m_axialMass;
		fixedRotation = false;
	}
	else
	{
		fixedRotation = true;
	}

	m_angle = aB - aA - m_referenceAngle;
	if (m_enableLimit == false || fixedRotation)
	{
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
	}

	if (m_enableMotor == false || fixedRotation)
	{
		m_motorImpulse = 0.0f;
	}

	if (data.step.warmStarting)
	{
		// Scale impulses to support a variable time step.
		m_impulse *= data.step.dtRatio;
		m_motorImpulse *= data.step.dtRatio;
		m_lowerImpulse *= data.step.dtRatio;
		m_upperImpulse *= data.step.dtRatio;

		float axialImpulse = m_motorImpulse + m_lowerImpulse - m_upperImpulse;
		b2Vec2 P(m_impulse.x, m_impulse.y);

		vA -= mA * P;
		wA -= iA * (b2Cross(m_rA, P) + axialImpulse);

		vB += mB * P;
		wB += iB * (b2Cross(m_rB, P) + axialImpulse);
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

void b2RevoluteJoint::SolveVelocityConstraints(const b2SolverData& data)
{
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;

	float mA = m_invMassA, mB = m_invMassB;
	float iA = m_invIA, iB = m_invIB;

	bool fixedRotation = (iA + iB == 0.0f);

	// Solve motor constraint.
	if (m_enableMotor && fixedRotation == false)
	{
		float Cdot = wB - wA - m_motorSpeed;
		float impulse = -m_axialMass * Cdot;
		float oldImpulse = m_motorImpulse;
		float maxImpulse = data.step.dt * m_maxMotorTorque;
		m_motorImpulse = b2Clamp(m_motorImpulse + impulse, -maxImpulse, maxImpulse);
		impulse = m_motorImpulse - oldImpulse;

		wA -= iA * impulse;
		wB += iB * impulse;
	}

	if (m_enableLimit && fixedRotation == false)
	{
		// Lower limit
		{
			float C = m_angle - m_lowerAngle;
			float Cdot = wB - wA;
			float impulse = -m_axialMass * (Cdot + b2Max(C, 0.0f) * data.step.inv_dt);
			float oldImpulse = m_lowerImpulse;
			m_lowerImpulse = b2Max(m_lowerImpulse + impulse, 0.0f);
			impulse = m_lowerImpulse - oldImpulse;

			wA -= iA * impulse;
			wB += iB * impulse;
		}

		// Upper limit
		// Note: signs are flipped to keep C positive when the constraint is satisfied.
		// This also keeps the impulse positive when the limit is active.
		{
			float C = m_upperAngle - m_angle;
			float Cdot = wA - wB;
			float impulse = -m_axialMass * (Cdot + b2Max(C, 0.0f) * data.step.inv_dt);
			float oldImpulse = m_upperImpulse;
			m_upperImpulse = b2Max(m_upperImpulse + impulse, 0.0f);
			impulse = m_upperImpulse - oldImpulse;

			wA += iA * impulse;
			wB -= iB * impulse;
		}
	}

	// Solve point-to-point constraint
	{
		b2Vec2 Cdot = vB + b2Cross(wB, m_rB) - vA - b2Cross(wA, m_rA);
		b2Vec2 impulse = m_K.Solve(-Cdot);

		m_impulse.x += impulse.x;
		m_impulse.y += impulse.y;

		vA -= mA * impulse;
		wA -= iA * b2Cross(m_rA, impulse);

		vB += mB * impulse;
		wB += iB * b2Cross(m_rB, impulse);
	}

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
}

bool b2RevoluteJoint::SolvePositionConstraints(const b2SolverData& data)
{
	b2Vec2 cA = data.positions[m_indexA].c;
	float aA = data.positions[m_indexA].a;
	b2Vec2 cB = data.positions[m_indexB].c;
	float aB = data.positions[m_indexB].a;

	b2Rot qA(aA), qB(aB);

	float angularError = 0.0f;
	float positionError = 0.0f;

	bool fixedRotation = (m_invIA + m_invIB == 0.0f);

	// Solve angular limit constraint
	if (m_enableLimit && fixedRotation == false)
	{
		float angle = aB - aA - m_referenceAngle;
		float C = 0.0f;

		if (b2Abs(m_upperAngle - m_lowerAngle) < 2.0f * b2_angularSlop)
		{
			// Prevent large angular corrections
			C = b2Clamp(angle - m_lowerAngle, -b2_maxAngularCorrection, b2_maxAngularCorrection);
		}
		else if (angle <= m_lowerAngle)
		{
			// Prevent large angular corrections and allow some slop.
			C = b2Clamp(angle - m_lowerAngle + b2_angularSlop, -b2_maxAngularCorrection, 0.0f);
		}
		else if (angle >= m_upperAngle)
		{
			// Prevent large angular corrections and allow some slop.
			C = b2Clamp(angle - m_upperAngle - b2_angularSlop, 0.0f, b2_maxAngularCorrection);
		}

		float limitImpulse = -m_axialMass * C;
		aA -= m_invIA * limitImpulse;
		aB += m_invIB * limitImpulse;
		angularError = b2Abs(C);
	}

	// Solve point-to-point constraint.
	{
		qA.Set(aA);
		qB.Set(aB);
		b2Vec2 rA = b2Mul(qA, m_localAnchorA - m_localCenterA);
		b2Vec2 rB = b2Mul(qB, m_localAnchorB - m_localCenterB);

		b2Vec2 C = cB + rB - cA - rA;
		positionError = C.Length();

		float mA = m_invMassA, mB = m_invMassB;
		float iA = m_invIA, iB = m_invIB;

		b2Mat22 K;
		K.ex.x = mA + mB + iA * rA.y * rA.y + iB * rB.y * rB.y;
		K.ex.y = -iA * rA.x * rA.y - iB * rB.x * rB.y;
		K.ey.x = K.ex.y;
		K.ey.y = mA + mB + iA * rA.x * rA.x + iB * rB.x * rB.x;

		b2Vec2 impulse = -K.Solve(C);

		cA -= mA * impulse;
		aA -= iA * b2Cross(rA, impulse);

		cB += mB * impulse;
		aB += iB * b2Cross(rB, impulse);
	}

	data.positions[m_indexA].c = cA;
	data.positions[m_indexA].a = aA;
	data.positions[m_indexB].c = cB;
	data.positions[m_indexB].a = aB;

	return positionError <= b2_linearSlop && angularError <= b2_angularSlop;
}

b2Vec2 b2RevoluteJoint::GetAnchorA() const
{
	return m_bodyA->GetWorldPoint(m_localAnchorA);
}

b2Vec2 b2RevoluteJoint::GetAnchorB() const
{
	return m_bodyB->GetWorldPoint(m_localAnchorB);
}

b2Vec2 b2RevoluteJoint::GetReactionForce(float inv_dt) const
{
	b2Vec2 P(m_impulse.x, m_impulse.y);
	return inv_dt * P;
}

float b2RevoluteJoint::GetReactionTorque(float inv_dt) const
{
	return inv_dt * (m_motorImpulse + m_lowerImpulse - m_upperImpulse);
}

float b2RevoluteJoint::GetJointAngle() const
{
	b2Body* bA = m_bodyA;
	b2Body* bB = m_bodyB;
	return bB->m_sweep.a - bA->m_sweep.a - m_referenceAngle;
}

float b2RevoluteJoint::GetJointSpeed() const
{
	b2Body* bA = m_bodyA;
	b2Body* bB = m_bodyB;
	return bB->m_angularVelocity - bA->m_angularVelocity;
}

bool b2RevoluteJoint::IsMotorEnabled() const
{
	return m_enableMotor;
}

void b2RevoluteJoint::EnableMotor(bool flag)
{
	if (flag != m_enableMotor)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_enableMotor = flag;
	}
}

float b2RevoluteJoint::GetMotorTorque(float inv_dt) const
{
	return inv_dt * m_motorImpulse;
}

void b2RevoluteJoint::SetMotorSpeed(float speed)
{
	if (speed != m_motorSpeed)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_motorSpeed = speed;
	}
}

void b2RevoluteJoint::SetMaxMotorTorque(float torque)
{
	if (torque != m_maxMotorTorque)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_maxMotorTorque = torque;
	}
}

bool b2RevoluteJoint::IsLimitEnabled() const
{
	return m_enableLimit;
}

void b2RevoluteJoint::EnableLimit(bool flag)
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

float b2RevoluteJoint::GetLowerLimit() const
{
	return m_lowerAngle;
}

float b2RevoluteJoint::GetUpperLimit() const
{
	return m_upperAngle;
}

void b2RevoluteJoint::SetLimits(float lower, float upper)
{
	b2Assert(lower <= upper);
	
	if (lower != m_lowerAngle || upper != m_upperAngle)
	{
		m_bodyA->SetAwake(true);
		m_bodyB->SetAwake(true);
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
		m_lowerAngle = lower;
		m_upperAngle = upper;
	}
}

void b2RevoluteJoint::Dump()
{
	int32 indexA = m_bodyA->m_islandIndex;
	int32 indexB = m_bodyB->m_islandIndex;

	b2Dump("  b2RevoluteJointDef jd;\n");
	b2Dump("  jd.bodyA = bodies[%d];\n", indexA);
	b2Dump("  jd.bodyB = bodies[%d];\n", indexB);
	b2Dump("  jd.collideConnected = bool(%d);\n", m_collideConnected);
	b2Dump("  jd.localAnchorA.Set(%.9g, %.9g);\n", m_localAnchorA.x, m_localAnchorA.y);
	b2Dump("  jd.localAnchorB.Set(%.9g, %.9g);\n", m_localAnchorB.x, m_localAnchorB.y);
	b2Dump("  jd.referenceAngle = %.9g;\n", m_referenceAngle);
	b2Dump("  jd.enableLimit = bool(%d);\n", m_enableLimit);
	b2Dump("  jd.lowerAngle = %.9g;\n", m_lowerAngle);
	b2Dump("  jd.upperAngle = %.9g;\n", m_upperAngle);
	b2Dump("  jd.enableMotor = bool(%d);\n", m_enableMotor);
	b2Dump("  jd.motorSpeed = %.9g;\n", m_motorSpeed);
	b2Dump("  jd.maxMotorTorque = %.9g;\n", m_maxMotorTorque);
	b2Dump("  joints[%d] = m_world->CreateJoint(&jd);\n", m_index);
}

///
void b2RevoluteJoint::Draw(b2Draw* draw) const
{
	const b2Transform& xfA = m_bodyA->GetTransform();
	const b2Transform& xfB = m_bodyB->GetTransform();
	b2Vec2 pA = b2Mul(xfA, m_localAnchorA);
	b2Vec2 pB = b2Mul(xfB, m_localAnchorB);

	b2Color c1(0.7f, 0.7f, 0.7f);
	b2Color c2(0.3f, 0.9f, 0.3f);
	b2Color c3(0.9f, 0.3f, 0.3f);
	b2Color c4(0.3f, 0.3f, 0.9f);
	b2Color c5(0.4f, 0.4f, 0.4f);

	draw->DrawPoint(pA, 5.0f, c4);
	draw->DrawPoint(pB, 5.0f, c5);

	float aA = m_bodyA->GetAngle();
	float aB = m_bodyB->GetAngle();
	float angle = aB - aA - m_referenceAngle;

	const float L = 0.5f;

	b2Vec2 r = L * b2Vec2(cosf(angle), sinf(angle));
	draw->DrawSegment(pB, pB + r, c1);
	draw->DrawCircle(pB, L, c1);

	if (m_enableLimit)
	{
		b2Vec2 rlo = L * b2Vec2(cosf(m_lowerAngle), sinf(m_lowerAngle));
		b2Vec2 rhi = L * b2Vec2(cosf(m_upperAngle), sinf(m_upperAngle));

		draw->DrawSegment(pB, pB + rlo, c2);
		draw->DrawSegment(pB, pB + rhi, c3);
	}

	b2Color color(0.5f, 0.8f, 0.8f);
	draw->DrawSegment(xfA.p, pA, color);
	draw->DrawSegment(pA, pB, color);
	draw->DrawSegment(xfB.p, pB, color);
}
