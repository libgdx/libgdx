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

/** A {@code SteeringBehavior} calculates the linear and/or angular accelerations to be applied to its owner.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public abstract class SteeringBehavior<T extends Vector<T>> {

	/** The owner of this steering behavior */
	protected Steerable<T> owner;

	/** The limiter of this steering behavior */
	protected Limiter limiter;

	/** A flag indicating whether this steering behavior is enabled or not. */
	protected boolean enabled;

	/** Creates a {@code SteeringBehavior} for the specified owner. The behavior is enabled and has no explicit limiter, meaning
	 * that the owner is used instead.
	 * 
	 * @param owner the owner of this steering behavior */
	public SteeringBehavior (Steerable<T> owner) {
		this(owner, null, true);
	}

	/** Creates a {@code SteeringBehavior} for the specified owner and limiter. The behavior is enabled.
	 * 
	 * @param owner the owner of this steering behavior
	 * @param enabled a flag indicating whether this steering behavior is enabled or not */
	public SteeringBehavior (Steerable<T> owner, Limiter limiter) {
		this(owner, limiter, true);
	}

	/** Creates a {@code SteeringBehavior} for the specified owner and activation flag. The behavior has no explicit limiter,
	 * meaning that the owner is used instead.
	 * 
	 * @param owner the owner of this steering behavior
	 * @param enabled a flag indicating whether this steering behavior is enabled or not */
	public SteeringBehavior (Steerable<T> owner, boolean enabled) {
		this(owner, null, enabled);
	}

	/** Creates a {@code SteeringBehavior} for the specified owner, limiter and activation flag.
	 * 
	 * @param owner the owner of this steering behavior
	 * @param limiter the limiter of this steering behavior
	 * @param enabled a flag indicating whether this steering behavior is enabled or not */
	public SteeringBehavior (Steerable<T> owner, Limiter limiter, boolean enabled) {
		this.owner = owner;
		this.limiter = limiter;
		this.enabled = enabled;
	}

	/** If this behavior is enabled calculates the steering acceleration and writes it to the given steering output. If it is
	 * disabled the steering output is set to zero.
	 * @param steering the steering acceleration to be calculated.
	 * @return the calculated steering acceleration for chaining. */
	public SteeringAcceleration<T> steer (SteeringAcceleration<T> steering) {
		return isEnabled() ? calculateSteering(steering) : steering.setZero();
	}

	/** Calculates the steering acceleration produced by this behavior and writes it to the given steering output.
	 * @param steering the steering acceleration to be calculated.
	 * @return the calculated steering acceleration for chaining. */
	protected abstract SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering);

	/** Returns the owner of this steering behavior. */
	public Steerable<T> getOwner () {
		return owner;
	}

	/** Sets the owner of this steering behavior.
	 * @return this behavior for chaining. */
	public SteeringBehavior<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	/** Returns the limiter of this steering behavior. */
	public Limiter getLimiter () {
		return limiter;
	}

	/** Sets the limiter of this steering behavior.
	 * @return this behavior for chaining. */
	public SteeringBehavior<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	/** Returns true if this steering behavior is enabled; false otherwise. */
	public boolean isEnabled () {
		return enabled;
	}

	/** Sets this steering behavior on/off.
	 * @return this behavior for chaining. */
	public SteeringBehavior<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Returns the actual limiter of this steering behavior. */
	protected Limiter getActualLimiter () {
		return limiter == null ? owner : limiter;
	}
}
