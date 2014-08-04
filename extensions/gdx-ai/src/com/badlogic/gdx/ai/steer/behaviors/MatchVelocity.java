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

	/** The kinematic data for the target */
	protected Steerable<T> target;

	/** The maximum acceleration of the character */
	protected float maxLinearAcceleration;

	/** The time over which to achieve target speed */
	protected float timeToTarget;

	/** Creates a MatchVelocity behavior for the given owner, target and maxLinearAcceleration. The timeToTarget is set to 0.1
	 * seconds.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to arrive at the target. */
	public MatchVelocity (Steerable<T> owner, Steerable<T> target, float maxLinearAcceleration) {
		this(owner, target, maxLinearAcceleration, 0.1f);
	}

	/** Creates a MatchVelocity behavior for the given owner, target, maxLinearAcceleration and timeToTarget.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to arrive at the target.
	 * @param timeToTarget the time over which to achieve target speed. */
	public MatchVelocity (Steerable<T> owner, Steerable<T> target, float maxLinearAcceleration, float timeToTarget) {
		super(owner);
		this.target = target;
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.timeToTarget = timeToTarget;
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Acceleration tries to get to the target velocity without exceeding max acceleration
		steering.linear.set(target.getLinearVelocity()).sub(owner.getLinearVelocity()).scl(1f / timeToTarget).limit(maxLinearAcceleration);

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	/** @return the target */
	public Steerable<T> getTarget () {
		return target;
	}

	/** @param target the target to set */
	public void setTarget (Steerable<T> target) {
		this.target = target;
	}

	/** @return the maxLinearAcceleration */
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** @param maxLinearAcceleration the maxLinearAcceleration to set */
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	/** @return the timeToTarget */
	public float getTimeToTarget () {
		return timeToTarget;
	}

	/** @param timeToTarget the timeToTarget to set */
	public void setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
	}

}
