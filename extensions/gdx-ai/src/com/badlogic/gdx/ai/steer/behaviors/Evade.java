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

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector;

/** {@code Evade} behavior is almost the same as {@link Pursue} except that the agent flees from the estimated future position of
 * the pursuer. Indeed, reversing the acceleration is all we have to do.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Evade<T extends Vector<T>> extends Pursue<T> {

	/** Creates a {@code Evade} behavior for the specified owner and target. Maximum prediction time defaults to 1 second.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior, typically a pursuer. */
	public Evade (Steerable<T> owner, Steerable<T> target) {
		this(owner, target, 1);
	}

	/** Creates a {@code Evade} behavior for the specified owner and pursuer.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior, typically a pursuer
	 * @param maxPredictionTime the max time used to predict the pursuer's position assuming it continues to move with its current
	 *           velocity. */
	public Evade (Steerable<T> owner, Steerable<T> target, float maxPredictionTime) {
		super(owner, target, maxPredictionTime);
	}

	@Override
	protected float getActualMaxLinearAcceleration () {
		// Simply return the opposite of the max linear acceleration so to evade the target
		return -getActualLimiter().getMaxLinearAcceleration();
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Evade<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Evade<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
	 * @return this behavior for chaining. */
	@Override
	public Evade<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	@Override
	public Evade<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

}
