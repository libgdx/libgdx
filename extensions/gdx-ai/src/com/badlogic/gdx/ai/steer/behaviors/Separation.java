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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** {@code Separation} is a group behavior producing a steering acceleration repelling from the other neighbors which are the agents
 * in the immediate area defined by the given {@link Proximity}. The acceleration is calculated by iterating through all the
 * neighbors, examining each one. The vector to each agent under consideration is normalized, divided by the distance to the
 * neighbor, and accumulated.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Separation<T extends Vector<T>> extends GroupBehavior<T> implements ProximityCallback<T> {

	private T toAgent;
	private T linear;

	/** Creates a {@code Separation} behavior for the specified owner and proximity.
	 * @param owner the owner of this behavior
	 * @param proximity the proximity to detect the owner's neighbors */
	public Separation (Steerable<T> owner, Proximity<T> proximity) {
		super(owner, proximity);

		this.toAgent = owner.newVector();
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		steering.setZero();

		linear = steering.linear;

		proximity.findNeighbors(this);

		return steering;
	}

	@Override
	public boolean reportNeighbor (Steerable<T> neighbor) {
		toAgent.set(owner.getPosition()).sub(neighbor.getPosition());

		// Scale the force inversely proportional to the agent's
		// distance from its neighbor.
		float toAgentLength2 = toAgent.len2();
		if (toAgentLength2 > MathUtils.FLOAT_ROUNDING_ERROR) {
			// Optimized code for
			// linear.add(toAgent.nor().scl(1f / toAgent.len()));
			linear.add(toAgent.scl(1f / toAgentLength2));
		}
		return true;
	}
}
