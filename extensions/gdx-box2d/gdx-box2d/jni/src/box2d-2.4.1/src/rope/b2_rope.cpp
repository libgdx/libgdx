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

#include "box2d/b2_draw.h"
#include "box2d/b2_rope.h"

#include <stdio.h>

struct b2RopeStretch
{
	int32 i1, i2;
	float invMass1, invMass2;
	float L;
	float lambda;
	float spring;
	float damper;
};

struct b2RopeBend
{
	int32 i1, i2, i3;
	float invMass1, invMass2, invMass3;
	float invEffectiveMass;
	float lambda;
	float L1, L2;
	float alpha1, alpha2;
	float spring;
	float damper;
};

b2Rope::b2Rope()
{
	m_position.SetZero();
	m_count = 0;
	m_stretchCount = 0;
	m_bendCount = 0;
	m_stretchConstraints = nullptr;
	m_bendConstraints = nullptr;
	m_bindPositions = nullptr;
	m_ps = nullptr;
	m_p0s = nullptr;
	m_vs = nullptr;
	m_invMasses = nullptr;
	m_gravity.SetZero();
}

b2Rope::~b2Rope()
{
	b2Free(m_stretchConstraints);
	b2Free(m_bendConstraints);
	b2Free(m_bindPositions);
	b2Free(m_ps);
	b2Free(m_p0s);
	b2Free(m_vs);
	b2Free(m_invMasses);
}

void b2Rope::Create(const b2RopeDef& def)
{
	b2Assert(def.count >= 3);
	m_position = def.position;
	m_count = def.count;
	m_bindPositions = (b2Vec2*)b2Alloc(m_count * sizeof(b2Vec2));
	m_ps = (b2Vec2*)b2Alloc(m_count * sizeof(b2Vec2));
	m_p0s = (b2Vec2*)b2Alloc(m_count * sizeof(b2Vec2));
	m_vs = (b2Vec2*)b2Alloc(m_count * sizeof(b2Vec2));
	m_invMasses = (float*)b2Alloc(m_count * sizeof(float));

	for (int32 i = 0; i < m_count; ++i)
	{
		m_bindPositions[i] = def.vertices[i];
		m_ps[i] = def.vertices[i] + m_position;
		m_p0s[i] = def.vertices[i] + m_position;
		m_vs[i].SetZero();

		float m = def.masses[i];
		if (m > 0.0f)
		{
			m_invMasses[i] = 1.0f / m;
		}
		else
		{
			m_invMasses[i] = 0.0f;
		}
	}

	m_stretchCount = m_count - 1;
	m_bendCount = m_count - 2;

	m_stretchConstraints = (b2RopeStretch*)b2Alloc(m_stretchCount * sizeof(b2RopeStretch));
	m_bendConstraints = (b2RopeBend*)b2Alloc(m_bendCount * sizeof(b2RopeBend));

	for (int32 i = 0; i < m_stretchCount; ++i)
	{
		b2RopeStretch& c = m_stretchConstraints[i];

		b2Vec2 p1 = m_ps[i];
		b2Vec2 p2 = m_ps[i+1];

		c.i1 = i;
		c.i2 = i + 1;
		c.L = b2Distance(p1, p2);
		c.invMass1 = m_invMasses[i];
		c.invMass2 = m_invMasses[i + 1];
		c.lambda = 0.0f;
		c.damper = 0.0f;
		c.spring = 0.0f;
	}

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		b2RopeBend& c = m_bendConstraints[i];

		b2Vec2 p1 = m_ps[i];
		b2Vec2 p2 = m_ps[i + 1];
		b2Vec2 p3 = m_ps[i + 2];

		c.i1 = i;
		c.i2 = i + 1;
		c.i3 = i + 2;
		c.invMass1 = m_invMasses[i];
		c.invMass2 = m_invMasses[i + 1];
		c.invMass3 = m_invMasses[i + 2];
		c.invEffectiveMass = 0.0f;
		c.L1 = b2Distance(p1, p2);
		c.L2 = b2Distance(p2, p3);
		c.lambda = 0.0f;

		// Pre-compute effective mass (TODO use flattened config)
		b2Vec2 e1 = p2 - p1;
		b2Vec2 e2 = p3 - p2;
		float L1sqr = e1.LengthSquared();
		float L2sqr = e2.LengthSquared();

		if (L1sqr * L2sqr == 0.0f)
		{
			continue;
		}

		b2Vec2 Jd1 = (-1.0f / L1sqr) * e1.Skew();
		b2Vec2 Jd2 = (1.0f / L2sqr) * e2.Skew();

		b2Vec2 J1 = -Jd1;
		b2Vec2 J2 = Jd1 - Jd2;
		b2Vec2 J3 = Jd2;

		c.invEffectiveMass = c.invMass1 * b2Dot(J1, J1) + c.invMass2 * b2Dot(J2, J2) + c.invMass3 * b2Dot(J3, J3);
	
		b2Vec2 r = p3 - p1;

		float rr = r.LengthSquared();
		if (rr == 0.0f)
		{
			continue;
		}

		// a1 = h2 / (h1 + h2)
		// a2 = h1 / (h1 + h2)
		c.alpha1 = b2Dot(e2, r) / rr;
		c.alpha2 = b2Dot(e1, r) / rr;
	}

	m_gravity = def.gravity;

	SetTuning(def.tuning);
}

void b2Rope::SetTuning(const b2RopeTuning& tuning)
{
	m_tuning = tuning;

	// Pre-compute spring and damper values based on tuning

	const float bendOmega = 2.0f * b2_pi * m_tuning.bendHertz;

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		b2RopeBend& c = m_bendConstraints[i];

		float L1sqr = c.L1 * c.L1;
		float L2sqr = c.L2 * c.L2;

		if (L1sqr * L2sqr == 0.0f)
		{
			c.spring = 0.0f;
			c.damper = 0.0f;
			continue;
		}

		// Flatten the triangle formed by the two edges
		float J2 = 1.0f / c.L1 + 1.0f / c.L2;
		float sum = c.invMass1 / L1sqr + c.invMass2 * J2 * J2 + c.invMass3 / L2sqr;
		if (sum == 0.0f)
		{
			c.spring = 0.0f;
			c.damper = 0.0f;
			continue;
		}

		float mass = 1.0f / sum;

		c.spring = mass * bendOmega * bendOmega;
		c.damper = 2.0f * mass * m_tuning.bendDamping * bendOmega;
	}
	
	const float stretchOmega = 2.0f * b2_pi * m_tuning.stretchHertz;

	for (int32 i = 0; i < m_stretchCount; ++i)
	{
		b2RopeStretch& c = m_stretchConstraints[i];

		float sum = c.invMass1 + c.invMass2;
		if (sum == 0.0f)
		{
			continue;
		}

		float mass = 1.0f / sum;

		c.spring = mass * stretchOmega * stretchOmega;
		c.damper = 2.0f * mass * m_tuning.stretchDamping * stretchOmega;
	}
}

void b2Rope::Step(float dt, int32 iterations, const b2Vec2& position)
{
	if (dt == 0.0f)
	{
		return;
	}

	const float inv_dt = 1.0f / dt;
	float d = expf(- dt * m_tuning.damping);

	// Apply gravity and damping
	for (int32 i = 0; i < m_count; ++i)
	{
		if (m_invMasses[i] > 0.0f)
		{
			m_vs[i] *= d;
			m_vs[i] += dt * m_gravity;
		}
		else
		{
			m_vs[i] = inv_dt * (m_bindPositions[i] + position - m_p0s[i]);
		}
	}

	// Apply bending spring
	if (m_tuning.bendingModel == b2_springAngleBendingModel)
	{
		ApplyBendForces(dt);
	}

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		m_bendConstraints[i].lambda = 0.0f;
	}

	for (int32 i = 0; i < m_stretchCount; ++i)
	{
		m_stretchConstraints[i].lambda = 0.0f;
	}

	// Update position
	for (int32 i = 0; i < m_count; ++i)
	{
		m_ps[i] += dt * m_vs[i];
	}

	// Solve constraints
	for (int32 i = 0; i < iterations; ++i)
	{
		if (m_tuning.bendingModel == b2_pbdAngleBendingModel)
		{
			SolveBend_PBD_Angle();
		}
		else if (m_tuning.bendingModel == b2_xpbdAngleBendingModel)
		{
			SolveBend_XPBD_Angle(dt);
		}
		else if (m_tuning.bendingModel == b2_pbdDistanceBendingModel)
		{
			SolveBend_PBD_Distance();
		}
		else if (m_tuning.bendingModel == b2_pbdHeightBendingModel)
		{
			SolveBend_PBD_Height();
		}
		else if (m_tuning.bendingModel == b2_pbdTriangleBendingModel)
		{
			SolveBend_PBD_Triangle();
		}

		if (m_tuning.stretchingModel == b2_pbdStretchingModel)
		{
			SolveStretch_PBD();
		}
		else if (m_tuning.stretchingModel == b2_xpbdStretchingModel)
		{
			SolveStretch_XPBD(dt);
		}
	}

	// Constrain velocity
	for (int32 i = 0; i < m_count; ++i)
	{
		m_vs[i] = inv_dt * (m_ps[i] - m_p0s[i]);
		m_p0s[i] = m_ps[i];
	}
}

void b2Rope::Reset(const b2Vec2& position)
{
	m_position = position;

	for (int32 i = 0; i < m_count; ++i)
	{
		m_ps[i] = m_bindPositions[i] + m_position;
		m_p0s[i] = m_bindPositions[i] + m_position;
		m_vs[i].SetZero();
	}

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		m_bendConstraints[i].lambda = 0.0f;
	}

	for (int32 i = 0; i < m_stretchCount; ++i)
	{
		m_stretchConstraints[i].lambda = 0.0f;
	}
}

void b2Rope::SolveStretch_PBD()
{
	const float stiffness = m_tuning.stretchStiffness;

	for (int32 i = 0; i < m_stretchCount; ++i)
	{
		const b2RopeStretch& c = m_stretchConstraints[i];

		b2Vec2 p1 = m_ps[c.i1];
		b2Vec2 p2 = m_ps[c.i2];

		b2Vec2 d = p2 - p1;
		float L = d.Normalize();

		float sum = c.invMass1 + c.invMass2;
		if (sum == 0.0f)
		{
			continue;
		}

		float s1 = c.invMass1 / sum;
		float s2 = c.invMass2 / sum;

		p1 -= stiffness * s1 * (c.L - L) * d;
		p2 += stiffness * s2 * (c.L - L) * d;

		m_ps[c.i1] = p1;
		m_ps[c.i2] = p2;
	}
}

void b2Rope::SolveStretch_XPBD(float dt)
{
	b2Assert(dt > 0.0f);

	for (int32 i = 0; i < m_stretchCount; ++i)
	{
		b2RopeStretch& c = m_stretchConstraints[i];

		b2Vec2 p1 = m_ps[c.i1];
		b2Vec2 p2 = m_ps[c.i2];

		b2Vec2 dp1 = p1 - m_p0s[c.i1];
		b2Vec2 dp2 = p2 - m_p0s[c.i2];

		b2Vec2 u = p2 - p1;
		float L = u.Normalize();

		b2Vec2 J1 = -u;
		b2Vec2 J2 = u;

		float sum = c.invMass1 + c.invMass2;
		if (sum == 0.0f)
		{
			continue;
		}

		const float alpha = 1.0f / (c.spring * dt * dt);	// 1 / kg
		const float beta = dt * dt * c.damper;				// kg * s
		const float sigma = alpha * beta / dt;				// non-dimensional
		float C = L - c.L;

		// This is using the initial velocities
		float Cdot = b2Dot(J1, dp1) + b2Dot(J2, dp2);

		float B = C + alpha * c.lambda + sigma * Cdot;
		float sum2 = (1.0f + sigma) * sum + alpha;

		float impulse = -B / sum2;

		p1 += (c.invMass1 * impulse) * J1;
		p2 += (c.invMass2 * impulse) * J2;

		m_ps[c.i1] = p1;
		m_ps[c.i2] = p2;
		c.lambda += impulse;
	}
}

void b2Rope::SolveBend_PBD_Angle()
{
	const float stiffness = m_tuning.bendStiffness;

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		const b2RopeBend& c = m_bendConstraints[i];

		b2Vec2 p1 = m_ps[c.i1];
		b2Vec2 p2 = m_ps[c.i2];
		b2Vec2 p3 = m_ps[c.i3];

		b2Vec2 d1 = p2 - p1;
		b2Vec2 d2 = p3 - p2;
		float a = b2Cross(d1, d2);
		float b = b2Dot(d1, d2);

		float angle = b2Atan2(a, b);

		float L1sqr, L2sqr;
		
		if (m_tuning.isometric)
		{
			L1sqr = c.L1 * c.L1;
			L2sqr = c.L2 * c.L2;
		}
		else
		{
			L1sqr = d1.LengthSquared();
			L2sqr = d2.LengthSquared();
		}

		if (L1sqr * L2sqr == 0.0f)
		{
			continue;
		}

		b2Vec2 Jd1 = (-1.0f / L1sqr) * d1.Skew();
		b2Vec2 Jd2 = (1.0f / L2sqr) * d2.Skew();

		b2Vec2 J1 = -Jd1;
		b2Vec2 J2 = Jd1 - Jd2;
		b2Vec2 J3 = Jd2;

		float sum;
		if (m_tuning.fixedEffectiveMass)
		{
			sum = c.invEffectiveMass;
		}
		else
		{
			sum = c.invMass1 * b2Dot(J1, J1) + c.invMass2 * b2Dot(J2, J2) + c.invMass3 * b2Dot(J3, J3);
		}

		if (sum == 0.0f)
		{
			sum = c.invEffectiveMass;
		}

		float impulse = -stiffness * angle / sum;

		p1 += (c.invMass1 * impulse) * J1;
		p2 += (c.invMass2 * impulse) * J2;
		p3 += (c.invMass3 * impulse) * J3;

		m_ps[c.i1] = p1;
		m_ps[c.i2] = p2;
		m_ps[c.i3] = p3;
	}
}

void b2Rope::SolveBend_XPBD_Angle(float dt)
{
	b2Assert(dt > 0.0f);

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		b2RopeBend& c = m_bendConstraints[i];

		b2Vec2 p1 = m_ps[c.i1];
		b2Vec2 p2 = m_ps[c.i2];
		b2Vec2 p3 = m_ps[c.i3];

		b2Vec2 dp1 = p1 - m_p0s[c.i1];
		b2Vec2 dp2 = p2 - m_p0s[c.i2];
		b2Vec2 dp3 = p3 - m_p0s[c.i3];

		b2Vec2 d1 = p2 - p1;
		b2Vec2 d2 = p3 - p2;

		float L1sqr, L2sqr;

		if (m_tuning.isometric)
		{
			L1sqr = c.L1 * c.L1;
			L2sqr = c.L2 * c.L2;
		}
		else
		{
			L1sqr = d1.LengthSquared();
			L2sqr = d2.LengthSquared();
		}

		if (L1sqr * L2sqr == 0.0f)
		{
			continue;
		}

		float a = b2Cross(d1, d2);
		float b = b2Dot(d1, d2);

		float angle = b2Atan2(a, b);

		b2Vec2 Jd1 = (-1.0f / L1sqr) * d1.Skew();
		b2Vec2 Jd2 = (1.0f / L2sqr) * d2.Skew();

		b2Vec2 J1 = -Jd1;
		b2Vec2 J2 = Jd1 - Jd2;
		b2Vec2 J3 = Jd2;

		float sum;
		if (m_tuning.fixedEffectiveMass)
		{
			sum = c.invEffectiveMass;
		}
		else
		{
			sum = c.invMass1 * b2Dot(J1, J1) + c.invMass2 * b2Dot(J2, J2) + c.invMass3 * b2Dot(J3, J3);
		}

		if (sum == 0.0f)
		{
			continue;
		}

		const float alpha = 1.0f / (c.spring * dt * dt);
		const float beta = dt * dt * c.damper;
		const float sigma = alpha * beta / dt;
		float C = angle;

		// This is using the initial velocities
		float Cdot = b2Dot(J1, dp1) + b2Dot(J2, dp2) + b2Dot(J3, dp3);

		float B = C + alpha * c.lambda + sigma * Cdot;
		float sum2 = (1.0f + sigma) * sum + alpha;

		float impulse = -B / sum2;

		p1 += (c.invMass1 * impulse) * J1;
		p2 += (c.invMass2 * impulse) * J2;
		p3 += (c.invMass3 * impulse) * J3;

		m_ps[c.i1] = p1;
		m_ps[c.i2] = p2;
		m_ps[c.i3] = p3;
		c.lambda += impulse;
	}
}

void b2Rope::ApplyBendForces(float dt)
{
	// omega = 2 * pi * hz
	const float omega = 2.0f * b2_pi * m_tuning.bendHertz;

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		const b2RopeBend& c = m_bendConstraints[i];

		b2Vec2 p1 = m_ps[c.i1];
		b2Vec2 p2 = m_ps[c.i2];
		b2Vec2 p3 = m_ps[c.i3];

		b2Vec2 v1 = m_vs[c.i1];
		b2Vec2 v2 = m_vs[c.i2];
		b2Vec2 v3 = m_vs[c.i3];

		b2Vec2 d1 = p2 - p1;
		b2Vec2 d2 = p3 - p2;

		float L1sqr, L2sqr;

		if (m_tuning.isometric)
		{
			L1sqr = c.L1 * c.L1;
			L2sqr = c.L2 * c.L2;
		}
		else
		{
			L1sqr = d1.LengthSquared();
			L2sqr = d2.LengthSquared();
		}

		if (L1sqr * L2sqr == 0.0f)
		{
			continue;
		}

		float a = b2Cross(d1, d2);
		float b = b2Dot(d1, d2);

		float angle = b2Atan2(a, b);

		b2Vec2 Jd1 = (-1.0f / L1sqr) * d1.Skew();
		b2Vec2 Jd2 = (1.0f / L2sqr) * d2.Skew();

		b2Vec2 J1 = -Jd1;
		b2Vec2 J2 = Jd1 - Jd2;
		b2Vec2 J3 = Jd2;

		float sum;
		if (m_tuning.fixedEffectiveMass)
		{
			sum = c.invEffectiveMass;
		}
		else
		{
			sum = c.invMass1 * b2Dot(J1, J1) + c.invMass2 * b2Dot(J2, J2) + c.invMass3 * b2Dot(J3, J3);
		}

		if (sum == 0.0f)
		{
			continue;
		}

		float mass = 1.0f / sum;

		const float spring = mass * omega * omega;
		const float damper = 2.0f * mass * m_tuning.bendDamping * omega;

		float C = angle;
		float Cdot = b2Dot(J1, v1) + b2Dot(J2, v2) + b2Dot(J3, v3);

		float impulse = -dt * (spring * C + damper * Cdot);

		m_vs[c.i1] += (c.invMass1 * impulse) * J1;
		m_vs[c.i2] += (c.invMass2 * impulse) * J2;
		m_vs[c.i3] += (c.invMass3 * impulse) * J3;
	}
}

void b2Rope::SolveBend_PBD_Distance()
{
	const float stiffness = m_tuning.bendStiffness;

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		const b2RopeBend& c = m_bendConstraints[i];

		int32 i1 = c.i1;
		int32 i2 = c.i3;

		b2Vec2 p1 = m_ps[i1];
		b2Vec2 p2 = m_ps[i2];

		b2Vec2 d = p2 - p1;
		float L = d.Normalize();

		float sum = c.invMass1 + c.invMass3;
		if (sum == 0.0f)
		{
			continue;
		}

		float s1 = c.invMass1 / sum;
		float s2 = c.invMass3 / sum;

		p1 -= stiffness * s1 * (c.L1 + c.L2 - L) * d;
		p2 += stiffness * s2 * (c.L1 + c.L2 - L) * d;

		m_ps[i1] = p1;
		m_ps[i2] = p2;
	}
}

// Constraint based implementation of:
// P. Volino: Simple Linear Bending Stiffness in Particle Systems
void b2Rope::SolveBend_PBD_Height()
{
	const float stiffness = m_tuning.bendStiffness;

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		const b2RopeBend& c = m_bendConstraints[i];

		b2Vec2 p1 = m_ps[c.i1];
		b2Vec2 p2 = m_ps[c.i2];
		b2Vec2 p3 = m_ps[c.i3];

		// Barycentric coordinates are held constant
		b2Vec2 d = c.alpha1 * p1 + c.alpha2 * p3 - p2;
		float dLen = d.Length();

		if (dLen == 0.0f)
		{
			continue;
		}

		b2Vec2 dHat = (1.0f / dLen) * d;

		b2Vec2 J1 = c.alpha1 * dHat;
		b2Vec2 J2 = -dHat;
		b2Vec2 J3 = c.alpha2 * dHat;

		float sum = c.invMass1 * c.alpha1 * c.alpha1 + c.invMass2 + c.invMass3 * c.alpha2 * c.alpha2;

		if (sum == 0.0f)
		{
			continue;
		}

		float C = dLen;
		float mass = 1.0f / sum;
		float impulse = -stiffness * mass * C;

		p1 += (c.invMass1 * impulse) * J1;
		p2 += (c.invMass2 * impulse) * J2;
		p3 += (c.invMass3 * impulse) * J3;

		m_ps[c.i1] = p1;
		m_ps[c.i2] = p2;
		m_ps[c.i3] = p3;
	}
}

// M. Kelager: A Triangle Bending Constraint Model for PBD
void b2Rope::SolveBend_PBD_Triangle()
{
	const float stiffness = m_tuning.bendStiffness;

	for (int32 i = 0; i < m_bendCount; ++i)
	{
		const b2RopeBend& c = m_bendConstraints[i];

		b2Vec2 b0 = m_ps[c.i1];
		b2Vec2 v = m_ps[c.i2];
		b2Vec2 b1 = m_ps[c.i3];

		float wb0 = c.invMass1;
		float wv = c.invMass2;
		float wb1 = c.invMass3;

		float W = wb0 + wb1 + 2.0f * wv;
		float invW = stiffness / W;

		b2Vec2 d = v - (1.0f / 3.0f) * (b0 + v + b1);

		b2Vec2 db0 = 2.0f * wb0 * invW * d;
		b2Vec2 dv = -4.0f * wv * invW * d;
		b2Vec2 db1 = 2.0f * wb1 * invW * d;

		b0 += db0;
		v += dv;
		b1 += db1;

		m_ps[c.i1] = b0;
		m_ps[c.i2] = v;
		m_ps[c.i3] = b1;
	}
}

void b2Rope::Draw(b2Draw* draw) const
{
	b2Color c(0.4f, 0.5f, 0.7f);
	b2Color pg(0.1f, 0.8f, 0.1f);
	b2Color pd(0.7f, 0.2f, 0.4f);

	for (int32 i = 0; i < m_count - 1; ++i)
	{
		draw->DrawSegment(m_ps[i], m_ps[i+1], c);

		const b2Color& pc = m_invMasses[i] > 0.0f ? pd : pg;
		draw->DrawPoint(m_ps[i], 5.0f, pc);
	}

	const b2Color& pc = m_invMasses[m_count - 1] > 0.0f ? pd : pg;
	draw->DrawPoint(m_ps[m_count - 1], 5.0f, pc);
}

int b2Rope::JavaGetCount() const {
    return m_count;
}

void b2Rope::JavaGetPS(float* buf) const {
    for (int i = 0; i < m_count; ++i) {
        buf[i * 2] = m_ps[i].x;
        buf[i * 2 + 1] = m_ps[i].y;
    }
}

void b2Rope::JavaGetInvMasses(float* buf) const {
    for (int i = 0; i < m_count; ++i) {
        buf[i] = m_invMasses[i];
    }
}
