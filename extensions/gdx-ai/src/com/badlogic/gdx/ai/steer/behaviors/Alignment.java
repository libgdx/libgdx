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

/** {@code Alignment} is a group behavior producing a linear acceleration that attempts to keep the owner aligned with the agents in
 * its immediate area defined by the given {@link Proximity}. The acceleration is calculated by first iterating through all the
 * neighbors and averaging their linear velocity vectors. This value is the desired direction, so we just subtract the owner's
 * linear velocity to get the steering output.
 * <p>
 * Cars moving along roads demonstrate {@code Alignment} type behavior. They also demonstrate {@link Separation} as they try to
 * keep a minimum distance from each other.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Alignment<T extends Vector<T>> extends GroupBehavior<T> implements ProximityCallback<T> {

	private T averageVelocity;

	/** Creates an {@code Alignment} behavior for the specified owner and proximity.
	 * @param owner the owner of this behavior
	 * @param proximity the proximity */
	public Alignment (Steerable<T> owner, Proximity<T> proximity) {
		super(owner, proximity);
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		steering.setZero();

		averageVelocity = steering.linear;

		int neighborCount = proximity.findNeighbors(this);

		if (neighborCount > 0) {
			// Average the accumulated velocities
			averageVelocity.scl(1f / neighborCount);

			// Match the average velocity.
			// Notice that steering.linear and averageVelocity are the same vector here.
			averageVelocity.sub(owner.getLinearVelocity()).limit(getActualLimiter().getMaxLinearAcceleration());
		}

		return steering;
	}

	@Override
	public boolean reportNeighbor (Steerable<T> neighbor) {
		// Accumulate neighbor velocity
		averageVelocity.add(neighbor.getLinearVelocity());
		return true;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Alignment<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Alignment<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
	 * @return this behavior for chaining. */
	@Override
	public Alignment<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

}
