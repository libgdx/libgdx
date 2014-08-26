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
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector;

/** {@code Flee} behavior does the opposite of {@link Seek}. It produces a linear steering force that moves the agent away from a
 * target position.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Flee<T extends Vector<T>> extends Seek<T> {

	/** Creates a {@code Flee} behavior for the specified owner.
	 * @param owner the owner of this behavior. */
	public Flee (Steerable<T> owner) {
		this(owner, null);
	}

	/** Creates a {@code Flee} behavior for the specified owner and target.
	 * @param owner the owner of this behavior
	 * @param target the target agent of this behavior. */
	public Flee (Steerable<T> owner, Steerable<T> target) {
		super(owner, target);
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// We just do the opposite of seek, i.e. (owner.getPosition() - target.getPosition())
		// instead of (target.getPosition() - owner.getPosition())
		steering.linear.set(owner.getPosition()).sub(target.getPosition()).nor().scl(getActualLimiter().getMaxLinearAcceleration());

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Flee<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Flee<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
	 * @return this behavior for chaining. */
	@Override
	public Flee<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	@Override
	public Flee<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

}
