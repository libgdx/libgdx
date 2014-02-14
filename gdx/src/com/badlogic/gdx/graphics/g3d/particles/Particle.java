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

import com.badlogic.gdx.math.Matrix4;

/** This class contains all the informations relative to a Particle
 * scale, position, rotation, velocities, color, etc...*/
public class Particle extends EmitObject {
	public static final int VEL_STRENGTH_INDEX = 0, 
							VEL_THETA_INDEX = 2,
							VEL_PHI_INDEX = 4;

	//Scale & Rotation
	public float scale, scaleStart, scaleDiff;
	public float rotation, rotationStart, rotationDiff; //Rotation around camera direction
	public float dirX, dirY, dirZ; //Direction of travel
	
	//Velocities
	public float[] velocity0Data = new float[6], velocity1Data = new float[6], velocity2Data = new float[6];
	
	//Color
	public float tintR, tintG, tintB;
	public float transparency, transparencyStart, transparencyDiff;
	
	//Transform
	public float x, y, z; //Position
	
	/** Emitter transform 
	 * This is a 4x3 column major matrix representing the translation, rotation, scale of the emitter when this
	 * particle was generated.
	 * It is needed because in this way it's possible to make the particle move with the emitter or not. */
	public float[] emitterTransform = new float[12];
}
