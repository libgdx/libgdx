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
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;

/** {@code Seek} behavior moves the owner towards the target position. Given a target, this behavior calculates the linear steering
 * acceleration which will direct the agent towards the target as fast as possible.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Seek<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The target to seek */
	protected Steerable<T> target;

	/** Creates a {@code Seek} behavior for the specified owner.
	 * @param owner the owner of this behavior. */
	public Seek (Steerable<T> owner) {
		this(owner, null);
	}

	/** Creates a {@code Seek} behavior for the specified owner and target.
	 * @param owner the owner of this behavior
	 * @param target the target agent of this behavior. */
	public Seek (Steerable<T> owner, Steerable<T> target) {
		super(owner);
		this.target = target;
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Try to match the position of the character with the position of the target by calculating
		// the direction to the target and by moving toward it as fast as possible.
		steering.linear.set(target.getPosition()).sub(owner.getPosition()).nor().scl(getActualLimiter().getMaxLinearAcceleration());

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	/** Returns the target to seek. */
	public Steerable<T> getTarget () {
		return target;
	}

	/** Sets the target to seek.
	 * @return this behavior for chaining. */
	public Seek<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Seek<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public Seek<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
	 * @return this behavior for chaining. */
	@Override
	public Seek<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

}
