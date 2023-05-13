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

#ifndef B2_ROPE_H
#define B2_ROPE_H

#include "b2_api.h"
#include "b2_math.h"

class b2Draw;
struct b2RopeStretch;
struct b2RopeBend;

enum b2StretchingModel
{
	b2_pbdStretchingModel,
	b2_xpbdStretchingModel
};

enum b2BendingModel
{
	b2_springAngleBendingModel = 0,
	b2_pbdAngleBendingModel,
	b2_xpbdAngleBendingModel,
	b2_pbdDistanceBendingModel,
	b2_pbdHeightBendingModel,
	b2_pbdTriangleBendingModel
};

///
struct B2_API b2RopeTuning
{
	b2RopeTuning()
	{
		stretchingModel = b2_pbdStretchingModel;
		bendingModel = b2_pbdAngleBendingModel;
		damping = 0.0f;
		stretchStiffness = 1.0f;
		stretchHertz = 1.0f;
		stretchDamping = 0.0f;
		bendStiffness = 0.5f;
		bendHertz = 1.0f;
		bendDamping = 0.0f;
		isometric = false;
		fixedEffectiveMass = false;
		warmStart = false;
	}

	b2StretchingModel stretchingModel;
	b2BendingModel bendingModel;
	float damping;
	float stretchStiffness;
	float stretchHertz;
	float stretchDamping;
	float bendStiffness;
	float bendHertz;
	float bendDamping;
	bool isometric;
	bool fixedEffectiveMass;
	bool warmStart;
};

///
struct B2_API b2RopeDef
{
	b2RopeDef()
	{
		position.SetZero();
		vertices = nullptr;
		count = 0;
		masses = nullptr;
		gravity.SetZero();
	}

	b2Vec2 position;
	b2Vec2* vertices;
	int32 count;
	float* masses;
	b2Vec2 gravity;
	b2RopeTuning tuning;
};

///
class B2_API b2Rope
{
public:
	b2Rope();
	~b2Rope();

	///
	void Create(const b2RopeDef& def);

	///
	void SetTuning(const b2RopeTuning& tuning);

	///
	void Step(float timeStep, int32 iterations, const b2Vec2& position);

	///
	void Reset(const b2Vec2& position);

	///
	void Draw(b2Draw* draw) const;

    /// ice1000: added for Java
    void JavaGetPS(float* buf) const;
    int JavaGetCount() const;
    void JavaGetInvMasses(float* buf) const;

private:

	void SolveStretch_PBD();
	void SolveStretch_XPBD(float dt);
	void SolveBend_PBD_Angle();
	void SolveBend_XPBD_Angle(float dt);
	void SolveBend_PBD_Distance();
	void SolveBend_PBD_Height();
	void SolveBend_PBD_Triangle();
	void ApplyBendForces(float dt);

	b2Vec2 m_position;

	int32 m_count;
	int32 m_stretchCount;
	int32 m_bendCount;

	b2RopeStretch* m_stretchConstraints;
	b2RopeBend* m_bendConstraints;

	b2Vec2* m_bindPositions;
	b2Vec2* m_ps;
	b2Vec2* m_p0s;
	b2Vec2* m_vs;

	float* m_invMasses;
	b2Vec2 m_gravity;

	b2RopeTuning m_tuning;
};

#endif
