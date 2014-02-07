/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics.g3d.particles;

/** This class contains all the informations relative to a Particle
 * scale, position, rotation, velocities, color, etc...*/
public class Particle extends EmitObject 
{
	public static final int VEL_STRENGTH_INDEX = 0, 
							VEL_THETA_INDEX = 2,
							VEL_PHI_INDEX = 4;

	//Scale & Rotation
	protected float scale, scaleStart, scaleDiff;
	protected float rotation, rotationStart, rotationDiff; //Rotation around camera direction
	protected float dirX, dirY, dirZ; //Direction of travel
	
	//Velocities
	float[] velocity0Data = new float[6], velocity1Data = new float[6], velocity2Data = new float[6];
	
	//Color
	protected float tintR, tintG, tintB;
	protected float transparency, transparencyStart, transparencyDiff;
	
	//Transform
	protected float x, y, z; //Position
	protected float qx, qy, qz, qw; //Orientation
	protected float ox, oy, oz; //Position of the emitter when this particle has been emitted

}
