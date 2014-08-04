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

package com.badlogic.gdx.ai.steer.behaviors;

import com.badlogic.gdx.ai.steer.GroupBehavior;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback;
import com.badlogic.gdx.math.Vector;

/** {@code Cohesion} is a group behavior producing a linear acceleration that attempts to move the agent towards the center of mass of
 * the agents in its immediate area defined by the given {@link Proximity}. The acceleration is calculated by first iterating
 * through all the neighbors and averaging their position vectors. This gives us the center of mass of the neighbors — the place
 * the agents wants to get to — so it seeks to that position.
 * <p>
 * A sheep running after its flock is demonstrating cohesive behavior. Use this behavior to keep a group of agents together.
 * <p>
 * Notice that this implementation always returns a normalized linear acceleration (or zero). This is not a problem since usually
 * you blend it with other group behaviors like {@link Separation} and {@link Alignment} so you can give it a proper weight, see
 * {@link WeightedBlender}.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Cohesion<T extends Vector<T>> extends GroupBehavior<T> implements ProximityCallback<T> {

	private T centerOfMass;

	/** Creates a Cohesion for the specified owner and proximity.
	 * @param owner the owner of this behavior.
	 * @param proximity the proximity to detect the owner's neighbors */
	public Cohesion (Steerable<T> owner, Proximity<T> proximity) {
		super(owner, proximity);
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {

		steering.setZero();

		centerOfMass = steering.linear;

		int neighborCount = proximity.findNeighbors(this);

		if (neighborCount > 0) {

			// The center of mass is the average of the sum of positions
			centerOfMass.scl(1f / neighborCount);

			// Now seek towards that position.
			// Note that the magnitude of cohesion is usually much larger than
			// separation or alignment so it usually helps to normalize it.
			centerOfMass.sub(owner.getPosition()).nor();
		}

		return steering;
	}

	@Override
	public boolean reportNeighbor (Steerable<T> neighbor) {
		centerOfMass.add(neighbor.getPosition());
		return true;
	}
}
