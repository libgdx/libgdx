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
#include "box2d/b2_distance_joint.h"
#include "box2d/b2_time_step.h"

// 1-D constrained system
// m (v2 - v1) = lambda
// v2 + (beta/h) * x1 + gamma * lambda = 0, gamma has units of inverse mass.
// x2 = x1 + h * v2

// 1-D mass-damper-spring system
// m (v2 - v1) + h * d * v2 + h * k * 

// C = norm(p2 - p1) - L
// u = (p2 - p1) / norm(p2 - p1)
// Cdot = dot(u, v2 + cross(w2, r2) - v1 - cross(w1, r1))
// J = [-u -cross(r1, u) u cross(r2, u)]
// K = J * invM * JT
//   = invMass1 + invI1 * cross(r1, u)^2 + invMass2 + invI2 * cross(r2, u)^2


void b2DistanceJointDef::Initialize(b2Body* b1, b2Body* b2,
									const b2Vec2& anchor1, const b2Vec2& anchor2)
{
	bodyA = b1;
	bodyB = b2;
	localAnchorA = bodyA->GetLocalPoint(anchor1);
	localAnchorB = bodyB->GetLocalPoint(anchor2);
	b2Vec2 d = anchor2 - anchor1;
	length = b2Max(d.Length(), b2_linearSlop);
	minLength = length;
	maxLength = length;
}

b2DistanceJoint::b2DistanceJoint(const b2DistanceJointDef* def)
: b2Joint(def)
{
	m_localAnchorA = def->localAnchorA;
	m_localAnchorB = def->localAnchorB;
	m_length = b2Max(def->length, b2_linearSlop);
	m_minLength = b2Max(def->minLength, b2_linearSlop);
	m_maxLength = b2Max(def->maxLength, m_minLength);
	m_stiffness = def->stiffness;
	m_damping = def->damping;

	m_gamma = 0.0f;
	m_bias = 0.0f;
	m_impulse = 0.0f;
	m_lowerImpulse = 0.0f;
	m_upperImpulse = 0.0f;
	m_currentLength = 0.0f;
}

void b2DistanceJoint::InitVelocityConstraints(const b2SolverData& data)
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

	m_rA = b2Mul(qA, m_localAnchorA - m_localCenterA);
	m_rB = b2Mul(qB, m_localAnchorB - m_localCenterB);
	m_u = cB + m_rB - cA - m_rA;

	// Handle singularity.
	m_currentLength = m_u.Length();
	if (m_currentLength > b2_linearSlop)
	{
		m_u *= 1.0f / m_currentLength;
	}
	else
	{
		m_u.Set(0.0f, 0.0f);
		m_mass = 0.0f;
		m_impulse = 0.0f;
		m_lowerImpulse = 0.0f;
		m_upperImpulse = 0.0f;
	}

	float crAu = b2Cross(m_rA, m_u);
	float crBu = b2Cross(m_rB, m_u);
	float invMass = m_invMassA + m_invIA * crAu * crAu + m_invMassB + m_invIB * crBu * crBu;
	m_mass = invMass != 0.0f ? 1.0f / invMass : 0.0f;

	if (m_stiffness > 0.0f && m_minLength < m_maxLength)
	{
		// soft
		float C = m_currentLength - m_length;

		float d = m_damping;
		float k = m_stiffness;

		// magic formulas
		float h = data.step.dt;

		// gamma = 1 / (h * (d + h * k))
		// the extra factor of h in the denominator is since the lambda is an impulse, not a force
		m_gamma = h * (d + h * k);
		m_gamma = m_gamma != 0.0f ? 1.0f / m_gamma : 0.0f;
		m_bias = C * h * k * m_gamma;

		invMass += m_gamma;
		m_softMass = invMass != 0.0f ? 1.0f / invMass : 0.0f;
	}
	else
	{
		// rigid
		m_gamma = 0.0f;
		m_bias = 0.0f;
		m_softMass = m_mass;
	}

	if (data.step.warmStarting)
	{
		// Scale the impulse to support a variable time step.
		m_impulse *= data.step.dtRatio;
		m_lowerImpulse *= data.step.dtRatio;
		m_upperImpulse *= data.step.dtRatio;

		b2Vec2 P = (m_impulse + m_lowerImpulse - m_upperImpulse) * m_u;
		vA -= m_invMassA * P;
		wA -= m_invIA * b2Cross(m_rA, P);
		vB += m_invMassB * P;
		wB += m_invIB * b2Cross(m_rB, P);
	}
	else
	{
		m_impulse = 0.0f;
	}

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
}

void b2DistanceJoint::SolveVelocityConstraints(const b2SolverData& data)
{
	b2Vec2 vA = data.velocities[m_indexA].v;
	float wA = data.velocities[m_indexA].w;
	b2Vec2 vB = data.velocities[m_indexB].v;
	float wB = data.velocities[m_indexB].w;

	if (m_minLength < m_maxLength)
	{
		if (m_stiffness > 0.0f)
		{
			// Cdot = dot(u, v + cross(w, r))
			b2Vec2 vpA = vA + b2Cross(wA, m_rA);
			b2Vec2 vpB = vB + b2Cross(wB, m_rB);
			float Cdot = b2Dot(m_u, vpB - vpA);

			float impulse = -m_softMass * (Cdot + m_bias + m_gamma * m_impulse);
			m_impulse += impulse;

			b2Vec2 P = impulse * m_u;
			vA -= m_invMassA * P;
			wA -= m_invIA * b2Cross(m_rA, P);
			vB += m_invMassB * P;
			wB += m_invIB * b2Cross(m_rB, P);
		}

		// lower
		{
			float C = m_currentLength - m_minLength;
			float bias = b2Max(0.0f, C) * data.step.inv_dt;

			b2Vec2 vpA = vA + b2Cross(wA, m_rA);
			b2Vec2 vpB = vB + b2Cross(wB, m_rB);
			float Cdot = b2Dot(m_u, vpB - vpA);

			float impulse = -m_mass * (Cdot + bias);
			float oldImpulse = m_lowerImpulse;
			m_lowerImpulse = b2Max(0.0f, m_lowerImpulse + impulse);
			impulse = m_lowerImpulse - oldImpulse;
			b2Vec2 P = impulse * m_u;

			vA -= m_invMassA * P;
			wA -= m_invIA * b2Cross(m_rA, P);
			vB += m_invMassB * P;
			wB += m_invIB * b2Cross(m_rB, P);
		}

		// upper
		{
			float C = m_maxLength - m_currentLength;
			float bias = b2Max(0.0f, C) * data.step.inv_dt;

			b2Vec2 vpA = vA + b2Cross(wA, m_rA);
			b2Vec2 vpB = vB + b2Cross(wB, m_rB);
			float Cdot = b2Dot(m_u, vpA - vpB);

			float impulse = -m_mass * (Cdot + bias);
			float oldImpulse = m_upperImpulse;
			m_upperImpulse = b2Max(0.0f, m_upperImpulse + impulse);
			impulse = m_upperImpulse - oldImpulse;
			b2Vec2 P = -impulse * m_u;

			vA -= m_invMassA * P;
			wA -= m_invIA * b2Cross(m_rA, P);
			vB += m_invMassB * P;
			wB += m_invIB * b2Cross(m_rB, P);
		}
	}
	else
	{
		// Equal limits

		// Cdot = dot(u, v + cross(w, r))
		b2Vec2 vpA = vA + b2Cross(wA, m_rA);
		b2Vec2 vpB = vB + b2Cross(wB, m_rB);
		float Cdot = b2Dot(m_u, vpB - vpA);

		float impulse = -m_mass * Cdot;
		m_impulse += impulse;

		b2Vec2 P = impulse * m_u;
		vA -= m_invMassA * P;
		wA -= m_invIA * b2Cross(m_rA, P);
		vB += m_invMassB * P;
		wB += m_invIB * b2Cross(m_rB, P);
	}

	data.velocities[m_indexA].v = vA;
	data.velocities[m_indexA].w = wA;
	data.velocities[m_indexB].v = vB;
	data.velocities[m_indexB].w = wB;
}

bool b2DistanceJoint::SolvePositionConstraints(const b2SolverData& data)
{
	b2Vec2 cA = data.positions[m_indexA].c;
	float aA = data.positions[m_indexA].a;
	b2Vec2 cB = data.positions[m_indexB].c;
	float aB = data.positions[m_indexB].a;

	b2Rot qA(aA), qB(aB);

	b2Vec2 rA = b2Mul(qA, m_localAnchorA - m_localCenterA);
	b2Vec2 rB = b2Mul(qB, m_localAnchorB - m_localCenterB);
	b2Vec2 u = cB + rB - cA - rA;

	float length = u.Normalize();
	float C;
	if (m_minLength == m_maxLength)
	{
		C = length - m_minLength;
	}
	else if (length < m_minLength)
	{
		C = length - m_minLength;
	}
	else if (m_maxLength < length)
	{
		C = length - m_maxLength;
	}
	else
	{
		return true;
	}

	float impulse = -m_mass * C;
	b2Vec2 P = impulse * u;

	cA -= m_invMassA * P;
	aA -= m_invIA * b2Cross(rA, P);
	cB += m_invMassB * P;
	aB += m_invIB * b2Cross(rB, P);

	data.positions[m_indexA].c = cA;
	data.positions[m_indexA].a = aA;
	data.positions[m_indexB].c = cB;
	data.positions[m_indexB].a = aB;

	return b2Abs(C) < b2_linearSlop;
}

b2Vec2 b2DistanceJoint::GetAnchorA() const
{
	return m_bodyA->GetWorldPoint(m_localAnchorA);
}

b2Vec2 b2DistanceJoint::GetAnchorB() const
{
	return m_bodyB->GetWorldPoint(m_localAnchorB);
}

b2Vec2 b2DistanceJoint::GetReactionForce(float inv_dt) const
{
	b2Vec2 F = inv_dt * (m_impulse + m_lowerImpulse - m_upperImpulse) * m_u;
	return F;
}

float b2DistanceJoint::GetReactionTorque(float inv_dt) const
{
	B2_NOT_USED(inv_dt);
	return 0.0f;
}

float b2DistanceJoint::SetLength(float length)
{
	m_impulse = 0.0f;
	m_length = b2Max(b2_linearSlop, length);
	return m_length;
}

float b2DistanceJoint::SetMinLength(float minLength)
{
	m_lowerImpulse = 0.0f;
	m_minLength = b2Clamp(minLength, b2_linearSlop, m_maxLength);
	return m_minLength;
}

float b2DistanceJoint::SetMaxLength(float maxLength)
{
	m_upperImpulse = 0.0f;
	m_maxLength = b2Max(maxLength, m_minLength);
	return m_maxLength;
}

float b2DistanceJoint::GetCurrentLength() const
{
	b2Vec2 pA = m_bodyA->GetWorldPoint(m_localAnchorA);
	b2Vec2 pB = m_bodyB->GetWorldPoint(m_localAnchorB);
	b2Vec2 d = pB - pA;
	float length = d.Length();
	return length;
}

void b2DistanceJoint::Dump()
{
	int32 indexA = m_bodyA->m_islandIndex;
	int32 indexB = m_bodyB->m_islandIndex;

	b2Dump("  b2DistanceJointDef jd;\n");
	b2Dump("  jd.bodyA = bodies[%d];\n", indexA);
	b2Dump("  jd.bodyB = bodies[%d];\n", indexB);
	b2Dump("  jd.collideConnected = bool(%d);\n", m_collideConnected);
	b2Dump("  jd.localAnchorA.Set(%.9g, %.9g);\n", m_localAnchorA.x, m_localAnchorA.y);
	b2Dump("  jd.localAnchorB.Set(%.9g, %.9g);\n", m_localAnchorB.x, m_localAnchorB.y);
	b2Dump("  jd.length = %.9g;\n", m_length);
	b2Dump("  jd.minLength = %.9g;\n", m_minLength);
	b2Dump("  jd.maxLength = %.9g;\n", m_maxLength);
	b2Dump("  jd.stiffness = %.9g;\n", m_stiffness);
	b2Dump("  jd.damping = %.9g;\n", m_damping);
	b2Dump("  joints[%d] = m_world->CreateJoint(&jd);\n", m_index);
}

void b2DistanceJoint::Draw(b2Draw* draw) const
{
	const b2Transform& xfA = m_bodyA->GetTransform();
	const b2Transform& xfB = m_bodyB->GetTransform();
	b2Vec2 pA = b2Mul(xfA, m_localAnchorA);
	b2Vec2 pB = b2Mul(xfB, m_localAnchorB);

	b2Vec2 axis = pB - pA;
	axis.Normalize();

	b2Color c1(0.7f, 0.7f, 0.7f);
	b2Color c2(0.3f, 0.9f, 0.3f);
	b2Color c3(0.9f, 0.3f, 0.3f);
	b2Color c4(0.4f, 0.4f, 0.4f);

	draw->DrawSegment(pA, pB, c4);
	
	b2Vec2 pRest = pA + m_length * axis;
	draw->DrawPoint(pRest, 8.0f, c1);

	if (m_minLength != m_maxLength)
	{
		if (m_minLength > b2_linearSlop)
		{
			b2Vec2 pMin = pA + m_minLength * axis;
			draw->DrawPoint(pMin, 4.0f, c2);
		}

		if (m_maxLength < FLT_MAX)
		{
			b2Vec2 pMax = pA + m_maxLength * axis;
			draw->DrawPoint(pMax, 4.0f, c3);
		}
	}
}
