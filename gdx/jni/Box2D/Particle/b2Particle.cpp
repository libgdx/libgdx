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
#include <Box2D/Particle/b2Particle.h>
#include <Box2D/Common/b2Draw.h>

b2ParticleColor b2ParticleColor_zero(0, 0, 0, 0);

b2ParticleColor::b2ParticleColor(const b2Color& color)
{
	r = (int8) (255 * color.r);
	g = (int8) (255 * color.g);
	b = (int8) (255 * color.b);
	a = (int8) 255;
}

b2Color b2ParticleColor::GetColor() const
{
	return b2Color(
		(float32) 1 / 255 * r,
		(float32) 1 / 255 * g,
		(float32) 1 / 255 * b);
}

void b2ParticleColor::Set(const b2Color& color)
{
	r = (int8) (255 * color.r);
	g = (int8) (255 * color.g);
	b = (int8) (255 * color.b);
	a = (int8) 255;
}
