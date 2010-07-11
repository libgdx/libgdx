/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx.math.collision;


/**
 * A collision response is responsible for altering the 
 * position and velocity of an {@link EllipsoidCollider}
 * in case a collision occured. This can implement sliding
 * collision response or a bouncing collision response for
 * example.
 * 
 * @author mzechner
 *
 */
public interface CollisionResponse 
{
	/**
	 * Responds to a collision defined by the {@link CollisionPacket}, modifying
	 * the given position and velocity of the package in ellipsoid space accordingly. The displacementDistance is the
	 * distance a colliding object is displaced from the colliding plane. This value
	 * dependent on the scale of your world and is usually very small (e.g. 0.0001 for
	 * 1m per unit).
	 * 
	 * @param displacementDistance the displacement distance
	 */
	public void respond( CollisionPacket packet, float displacementDistance );
}
