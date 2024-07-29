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

#ifndef B2_DRAW_H
#define B2_DRAW_H

#include "b2_api.h"
#include "b2_math.h"

/// Color for debug drawing. Each value has the range [0,1].
struct B2_API b2Color
{
	b2Color() {}
	b2Color(float rIn, float gIn, float bIn, float aIn = 1.0f)
	{
		r = rIn; g = gIn; b = bIn; a = aIn;
	}

	void Set(float rIn, float gIn, float bIn, float aIn = 1.0f)
	{
		r = rIn; g = gIn; b = bIn; a = aIn;
	}

	float r, g, b, a;
};

/// Implement and register this class with a b2World to provide debug drawing of physics
/// entities in your game.
class B2_API b2Draw
{
public:
	b2Draw();

	virtual ~b2Draw() {}

	enum
	{
		e_shapeBit				= 0x0001,	///< draw shapes
		e_jointBit				= 0x0002,	///< draw joint connections
		e_aabbBit				= 0x0004,	///< draw axis aligned bounding boxes
		e_pairBit				= 0x0008,	///< draw broad-phase pairs
		e_centerOfMassBit		= 0x0010	///< draw center of mass frame
	};

	/// Set the drawing flags.
	void SetFlags(uint32 flags);

	/// Get the drawing flags.
	uint32 GetFlags() const;

	/// Append flags to the current flags.
	void AppendFlags(uint32 flags);

	/// Clear flags from the current flags.
	void ClearFlags(uint32 flags);

	/// Draw a closed polygon provided in CCW order.
	virtual void DrawPolygon(const b2Vec2* vertices, int32 vertexCount, const b2Color& color) = 0;

	/// Draw a solid closed polygon provided in CCW order.
	virtual void DrawSolidPolygon(const b2Vec2* vertices, int32 vertexCount, const b2Color& color) = 0;

	/// Draw a circle.
	virtual void DrawCircle(const b2Vec2& center, float radius, const b2Color& color) = 0;

	/// Draw a solid circle.
	virtual void DrawSolidCircle(const b2Vec2& center, float radius, const b2Vec2& axis, const b2Color& color) = 0;

	/// Draw a line segment.
	virtual void DrawSegment(const b2Vec2& p1, const b2Vec2& p2, const b2Color& color) = 0;

	/// Draw a transform. Choose your own length scale.
	/// @param xf a transform.
	virtual void DrawTransform(const b2Transform& xf) = 0;

	/// Draw a point.
	virtual void DrawPoint(const b2Vec2& p, float size, const b2Color& color) = 0;

protected:
	uint32 m_drawFlags;
};

#endif
