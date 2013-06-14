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

package com.box2dLight.box2dLight;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

public class Box2dLight implements Disposable {
	// @off
	/*JNI
#include <Box2D/Collision/LightCalculus.h>
*/
	long pointAddr;
	final World world;
	
	final int nbRays;
	
	public Box2dLight(World world, int nbRays, boolean isPoint)
	{
		this.world = world;
		this.nbRays= nbRays;
		if( isPoint ){
			pointAddr = world.createPointLight(nbRays);
		}else{
			pointAddr = world.createDirectionalLight(nbRays);
		}
	}

	public void releaseLight()
	{
		if( pointAddr!=0 )
			jniReleaseLight( pointAddr );
		pointAddr = 0;
	}

	@Override
	public void dispose () {
		releaseLight();
	}

	public void update(float x, float y, float distance)
	{
		jniComputeOcclusion(pointAddr, x, y, distance);
	}

	public void update_cone(float x, float y, float distance,
		float direction, float coneSize)
	{
		if( pointAddr==0 ){
			jniComputeOcclusion(pointAddr, x, y, distance, direction, coneSize);
		}else{
			jniComputeOcclusion(pointAddr, x, y, distance, direction, coneSize);
		}
	}

	public void setLightMesh( float[] segments, float colorF, boolean isGL20 )
	{
		jniSetLightMesh(pointAddr, segments, colorF, isGL20);
	}

	public void setShadowMesh( float[] segments, float colorF, float softShadowLenght, boolean isGL20 )
	{
		jniSetShadowMesh(pointAddr, segments, colorF, softShadowLenght, isGL20);
	}

	private native void jniSetLightMesh( long addr, float[] segments, float colorF, boolean isGL20 );/*
	((PointLight*) addr)->setLightMesh(segments,colorF,isGL20);
	*/

	private native void jniSetShadowMesh( long addr, float[] segments, float colorF, float softShadowLenght, boolean isGL20 );/*
	((PointLight*) addr)->setShadowMesh(segments,colorF,softShadowLenght,isGL20);
	*/
	
	/**
	 * set given contact filter for ALL LIGHTS
	 * 
	 * @param filter
	 */
	static public void setContactFilter(Filter filter) {
		jniSetContactFilter(filter.categoryBits, filter.groupIndex, filter.maskBits);
	}

	/**
	 * create new contact filter for ALL LIGHTS with give parameters
	 * 
	 * @param categoryBits
	 * @param groupIndex
	 * @param maskBits
	 */
	static public void setContactFilter(short categoryBits, short groupIndex,
			short maskBits) {
		jniSetContactFilter(categoryBits, groupIndex, maskBits);
	}

	/**
	 * Used to set sensor transparent or opaque for this light
	 * @param shouldCollide if true, sensor stop ray of light
	 */
	
	public void setSensorFilter(boolean shouldCollide) {
		jniSetSensorFilter(pointAddr, shouldCollide);
	}

	private native void jniSetSensorFilter(long addr, boolean shouldCollide);/*
	((PointLight*) addr)->setSensorFilter(shouldCollide);
*/

	static private native void jniSetContactFilter(short categoryBits, short groupIndex,
		short maskBits);/*
	PointLight::setContactFilter((short)categoryBits,(short)groupIndex,(short)maskBits);
*/

	private native void jniComputeOcclusion(long addr, float x, float y, float distance,
		float direction, float coneSize); /*
	((PointLight*) addr)->computePoints(x, y, distance, direction, coneSize);
*/

	private native void jniComputeOcclusion(long addr, float x, float y, float distance); /*
	((PointLight*) addr)->computePoints(x, y, distance);
*/
	
	private native void jniReleaseLight(long addr); /*
	delete ((PointLight*) addr);
*/

}
