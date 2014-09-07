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
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector;

/** {@code Separation} is a group behavior producing a steering acceleration repelling from the other neighbors which are the agents
 * in the immediate area defined by the given {@link Proximity}. The acceleration is calculated by iterating through all the
 * neighbors, examining each one. The vector to each agent under consideration is normalized, multiplied by a strength decreasing
 * according to the inverse square law in relation to distance, and accumulated.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Separation<T extends Vector<T>> extends GroupBehavior<T> implements ProximityCallback<T> {

	/** The constant coefficient of decay for the inverse square law force. It controls how fast the separation strength decays with
	 * distance. */
	float decayCoefficient = 1f;

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
		float distanceSqr = toAgent.len2();
		float maxAcceleration = getActualLimiter().getMaxLinearAcceleration();

		// Calculate the strength of repulsion through inverse square law decay
		float strength = getDecayCoefficient() / distanceSqr;
		if (strength > maxAcceleration) strength = maxAcceleration;

		// Add the acceleration
		// Optimized code for linear.mulAdd(toAgent.nor(), strength);
		linear.mulAdd(toAgent, strength / (float)Math.sqrt(distanceSqr));

		return true;
	}

	/** Returns the coefficient of decay for the inverse square law force. */
	public float getDecayCoefficient () {
		return decayCoefficient;
	}

	/** Sets the coefficient of decay for the inverse square law force. It controls how fast the separation strength decays with
	 * distance.
	 * @param decayCoefficient the coefficient of decay to set */
	public Separation<T> setDecayCoefficient (float decayCoefficient) {
		this.decayCoefficient = decayCoefficient;
		return this;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Separation<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Separation<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
	 * @return this behavior for chaining. */
	@Override
	public Separation<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}
}
