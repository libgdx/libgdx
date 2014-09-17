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

/** This steering behavior produces a linear acceleration trying to match target's velocity. It does not produce any angular
 * acceleration.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class MatchVelocity<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The target of this behavior */
	protected Steerable<T> target;

	/** The time over which to achieve target speed */
	protected float timeToTarget;

	/** Creates a {@code MatchVelocity} behavior for the given owner. No target is set. The maxLinearAcceleration is set to 100. The
	 * timeToTarget is set to 0.1 seconds.
	 * @param owner the owner of this behavior. */
	public MatchVelocity (Steerable<T> owner) {
		this(owner, null);
	}

	/** Creates a {@code MatchVelocity} behavior for the given owner and target. The timeToTarget is set to 0.1 seconds.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior. */
	public MatchVelocity (Steerable<T> owner, Steerable<T> target) {
		this(owner, target, 0.1f);
	}

	/** Creates a {@code MatchVelocity} behavior for the given owner, target and timeToTarget.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior
	 * @param timeToTarget the time over which to achieve target speed. */
	public MatchVelocity (Steerable<T> owner, Steerable<T> target, float timeToTarget) {
		super(owner);
		this.target = target;
		this.timeToTarget = timeToTarget;
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Acceleration tries to get to the target velocity without exceeding max acceleration
		steering.linear.set(target.getLinearVelocity()).sub(owner.getLinearVelocity()).scl(1f / timeToTarget)
			.limit(getActualLimiter().getMaxLinearAcceleration());

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	/** Returns the target whose velocity should be matched. */
	public Steerable<T> getTarget () {
		return target;
	}

	/** Sets the target whose velocity should be matched.
	 * @param target the target to set
	 * @return this behavior for chaining. */
	public MatchVelocity<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	/** Returns the time over which to achieve target speed. */
	public float getTimeToTarget () {
		return timeToTarget;
	}

	/** Sets the time over which to achieve target speed.
	 * @param timeToTarget the time to set
	 * @return this behavior for chaining. */
	public MatchVelocity<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public MatchVelocity<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public MatchVelocity<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
	 * @return this behavior for chaining. */
	@Override
	public MatchVelocity<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

}
