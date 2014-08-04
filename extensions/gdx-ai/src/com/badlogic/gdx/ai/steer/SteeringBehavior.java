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

package com.badlogic.gdx.ai.steer;

import com.badlogic.gdx.math.Vector;

/** A steering behavior calculates the linear and/or angular accelerations to be applied to its owner.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public abstract class SteeringBehavior<T extends Vector<T>> {

	/** The owner of this steering behavior */
	protected Steerable<T> owner;

	/** A flag indicating whether this steering behavior is enabled or not. */
	protected boolean enabled;

	/** Creates a steering behavior for the specified agent. The behavior is enabled.
	 * 
	 * @param owner the owner of this steering behavior */
	public SteeringBehavior (Steerable<T> owner) {
		this(owner, true);
	}

	/** Creates a steering behavior for the specified agent.
	 * 
	 * @param owner the owner of this steering behavior
	 * @param enabled a flag indicating whether this steering behavior is enabled or not */
	public SteeringBehavior (Steerable<T> owner, boolean enabled) {
		this.owner = owner;
		this.enabled = enabled;
	}

	/** Calculates the steering acceleration produced by this behavior and writes it into the given steering output.
	 * @param steering the steering acceleration to be calculated.
	 * @return the calculated steering acceleration for chaining. */
	public abstract SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering);

	/** Returns the owner of this steering behavior. */
	public Steerable<T> getOwner () {
		return owner;
	}

	/** Sets the owner of this steering behavior. */
	public void setOwner (Steerable<T> owner) {
		this.owner = owner;
	}

	/** Returns true if this steering behavior is enabled; false otherwise. */
	public boolean isEnabled () {
		return enabled;
	}

	/** Enables and disables this steering behavior. */
	public void setEnabled (boolean enabled) {
		this.enabled = enabled;
	}

}
