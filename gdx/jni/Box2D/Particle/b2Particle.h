/*
* Copyright (c) 2013 Google, Inc.
*
* This software is provided 'as-is', without any express or implied
* warranty.  In no event will the authors be held liable for any damages
* arising from the use of this software.
* Permission is granted to anyone to use this software for any purpose,
* including commercial applications, and to alter it and redistribute it
* freely, subject to the following restrictions:
* 1. The origin of this software must not be misrepresented; you must not
* claim that you wrote the original software. If you use this software
* in a product, an acknowledgment in the product documentation would be
* appreciated but is not required.
* 2. Altered source versions must be plainly marked as such, and must not be
* misrepresented as being the original software.
* 3. This notice may not be removed or altered from any source distribution.
*/
#ifndef B2_PARTICLE
#define B2_PARTICLE

#include <Box2D/Common/b2Math.h>

struct b2Color;

/// The particle type. Can be combined with | operator.
/// Zero means liquid.
enum b2ParticleFlag
{
	b2_waterParticle =       0,
	b2_zombieParticle =      1 << 1, // removed after next step
	b2_wallParticle =        1 << 2, // zero velocity
	b2_springParticle =      1 << 3, // with restitution from stretching
	b2_elasticParticle =     1 << 4, // with restitution from deformation
	b2_viscousParticle =     1 << 5, // with viscosity
	b2_powderParticle =      1 << 6, // without isotropic pressure
	b2_tensileParticle =     1 << 7, // with surface tension
	b2_colorMixingParticle = 1 << 8, // mixing color between contacting particles
	b2_destructionListener = 1 << 9, // call b2DestructionListener on destruction
};

/// Small color object for each particle
struct b2ParticleColor
{
	uint8 r,g,b,a;
	b2ParticleColor() {}
	/// Constructor with four elements: r (red), g (green), b (blue), and a (opacity).
	/// Each element can be specified 0 to 255.
	b2ParticleColor(int32 r, int32 g, int32 b, int32 a) : r(r), g(g), b(b), a(a)
	{}

	/// Constructor that initializes the above four elements with the value of the b2Color object
	///
	b2ParticleColor(const b2Color& color);

	/// True when all four color elements equal 0. When true, no memory is used for particle color.
	///
	bool IsZero() const
	{
		return !r && !g && !b && !a;
	}

	/// Used internally to convert the value of b2Color.
	///
	b2Color GetColor() const;

	/// Sets color for current object using the four elements described above.
	///
	void Set(int32 r_, int32 g_, int32 b_, int32 a_)
	{
		r = r_;
		g = g_;
		b = b_;
		a = a_;
	}

	/// Initializes the above four elements with the value of the b2Color object
	///
	void Set(const b2Color& color);
};

extern b2ParticleColor b2ParticleColor_zero;

/// A particle definition holds all the data needed to construct a particle.
/// You can safely re-use these definitions.
struct b2ParticleDef
{

	b2ParticleDef()
	{
		flags = 0;
		position = b2Vec2_zero;
		velocity = b2Vec2_zero;
		color = b2ParticleColor_zero;
		userData = NULL;
	}

	/// Specifies the type of particle. A particle may be more than one type.
	/// Multiple types are chained by logical sums, for example:
	/// pd.flags = b2_elasticParticle | b2_viscousParticle
	uint32 flags;

	/// The world position of the particle.
	b2Vec2 position;

	/// The linear velocity of the particle in world co-ordinates.
	b2Vec2 velocity;

	/// The color of the particle.
	b2ParticleColor color;

	/// Use this to store application-specific body data.
	void* userData;

};

#endif
