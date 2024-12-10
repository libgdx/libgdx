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

#ifndef B2_CONTACT_SOLVER_H
#define B2_CONTACT_SOLVER_H

#include "box2d/b2_collision.h"
#include "box2d/b2_math.h"
#include "box2d/b2_time_step.h"

class b2Contact;
class b2Body;
class b2StackAllocator;
struct b2ContactPositionConstraint;

struct b2VelocityConstraintPoint
{
	b2Vec2 rA;
	b2Vec2 rB;
	float normalImpulse;
	float tangentImpulse;
	float normalMass;
	float tangentMass;
	float velocityBias;
};

struct b2ContactVelocityConstraint
{
	b2VelocityConstraintPoint points[b2_maxManifoldPoints];
	b2Vec2 normal;
	b2Mat22 normalMass;
	b2Mat22 K;
	int32 indexA;
	int32 indexB;
	float invMassA, invMassB;
	float invIA, invIB;
	float friction;
	float restitution;
	float threshold;
	float tangentSpeed;
	int32 pointCount;
	int32 contactIndex;
};

struct b2ContactSolverDef
{
	b2TimeStep step;
	b2Contact** contacts;
	int32 count;
	b2Position* positions;
	b2Velocity* velocities;
	b2StackAllocator* allocator;
};

class b2ContactSolver
{
public:
	b2ContactSolver(b2ContactSolverDef* def);
	~b2ContactSolver();

	void InitializeVelocityConstraints();

	void WarmStart();
	void SolveVelocityConstraints();
	void StoreImpulses();

	bool SolvePositionConstraints();
	bool SolveTOIPositionConstraints(int32 toiIndexA, int32 toiIndexB);

	b2TimeStep m_step;
	b2Position* m_positions;
	b2Velocity* m_velocities;
	b2StackAllocator* m_allocator;
	b2ContactPositionConstraint* m_positionConstraints;
	b2ContactVelocityConstraint* m_velocityConstraints;
	b2Contact** m_contacts;
	int m_count;
};

#endif

