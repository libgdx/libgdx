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
#ifndef B2_TIME_STEP_H
#define B2_TIME_STEP_H

#include "b2_api.h"
#include "b2_math.h"

/// Profiling data. Times are in milliseconds.
struct B2_API b2Profile
{
	float step;
	float collide;
	float solve;
	float solveInit;
	float solveVelocity;
	float solvePosition;
	float broadphase;
	float solveTOI;
};

/// This is an internal structure.
struct B2_API b2TimeStep
{
	float dt;			// time step
	float inv_dt;		// inverse time step (0 if dt == 0).
	float dtRatio;	// dt * inv_dt0
	int32 velocityIterations;
	int32 positionIterations;
	bool warmStarting;
};

/// This is an internal structure.
struct B2_API b2Position
{
	b2Vec2 c;
	float a;
};

/// This is an internal structure.
struct B2_API b2Velocity
{
	b2Vec2 v;
	float w;
};

/// Solver Data
struct B2_API b2SolverData
{
	b2TimeStep step;
	b2Position* positions;
	b2Velocity* velocities;
};

#endif
