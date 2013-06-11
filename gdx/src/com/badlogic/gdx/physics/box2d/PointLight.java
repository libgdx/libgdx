/*
* Copyright (c) 2013 Thibault Lelore (based on http://box2dlights.googlecode.com)
*
* This part of software is provided 'as-is', without any express or implied
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

package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.utils.Disposable;

public class PointLight implements Disposable {
	// @off
	/*JNI
#include <Box2D/Collision/LightCalculus.h>
*/
	long pointAddr;
	final World world;
	
	final int nbRays;
	
	public PointLight(World world, int nbRays)
	{
		this.world = world;
		this.nbRays= nbRays;
		pointAddr  = world.createPointLight(nbRays);
	}

	public void releaseLight()
	{
		if( pointAddr!=0 )
			jniReleaseLight( pointAddr );
		pointAddr = 0;
	}
	
	
	private native void jniReleaseLight(long map); /*
	delete ((PointLight*) map);
*/

	public void update(float[] points, float x, float y, float distance)
	{
		jniComputeOcclusionMap(pointAddr, points, points.length, x, y, distance);
	}

	public void update_cone(float[] points, float x, float y, float distance,
		float direction, float coneSize)
	{
		jniComputeOcclusionMap(pointAddr, points, points.length, x, y, distance, direction, coneSize);
	}

	private native void jniComputeOcclusionMap(long map, float[] points, int nbPoints, float x, float y, float distance,
		float direction, float coneSize); /*
	((PointLight*) map)->computePoints(points, nbPoints, x, y, distance, direction, coneSize);
*/

	private native void jniComputeOcclusionMap(long map, float[] points, int nbPoints, float x, float y, float distance); /*
	((PointLight*) map)->computePoints(points, nbPoints, x, y, distance);
*/
	
	@Override
	public void dispose () {
		releaseLight();
	}

}
