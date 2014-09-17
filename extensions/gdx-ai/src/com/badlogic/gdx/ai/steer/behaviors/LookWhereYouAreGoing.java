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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** The entire steering framework assumes that the direction a character is facing does not have to be its direction of motion. In
 * many cases, however, you would like the character to face in the direction it is moving. To do this you can manually align the
 * orientation of the character to its linear velocity on each frame update or you can use the {@code LookWhereYouAreGoing}
 * behavior.
 * <p>
 * {@code LookWhereYouAreGoing} behavior gives the owner angular acceleration to make it face in the direction it is moving. In
 * this way the owner changes facing gradually, which can look more natural, especially for aerial vehicles such as helicopters or
 * for human characters that can move sideways.
 * <p>
 * This is a process similar to the {@code Face} behavior. The target orientation is calculated using the current velocity of the
 * owner. If there is no velocity, then the target orientation is set to the current orientation. We have no preference in this
 * situation for any orientation.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class LookWhereYouAreGoing<T extends Vector<T>> extends ReachOrientation<T> {

	/** Creates a {@code LookWhereYouAreGoing} behavior for the specified owner.
	 * @param owner the owner of this behavior. */
	public LookWhereYouAreGoing (Steerable<T> owner) {
		super(owner);
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Check for a zero direction, and return no steering if so
		if (owner.getLinearVelocity().isZero(MathUtils.FLOAT_ROUNDING_ERROR)) return steering.setZero();

		// Calculate the orientation based on the velocity of the owner
		float orientation = owner.vectorToAngle(owner.getLinearVelocity());

		// Delegate to ReachOrientation
		return reachOrientation(steering, orientation);
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public LookWhereYouAreGoing<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public LookWhereYouAreGoing<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum angular speed and
	 * acceleration.
	 * @return this behavior for chaining. */
	@Override
	public LookWhereYouAreGoing<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	/** Sets the target to align to. Notice that this method is inherited from {@link ReachOrientation}, but is completely useless
	 * for {@code LookWhereYouAreGoing} because the target orientation is determined by the velocity of the owner itself.
	 * @return this behavior for chaining. */
	@Override
	public LookWhereYouAreGoing<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	@Override
	public LookWhereYouAreGoing<T> setAlignTolerance (float alignTolerance) {
		this.alignTolerance = alignTolerance;
		return this;
	}

	@Override
	public LookWhereYouAreGoing<T> setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
		return this;
	}

	@Override
	public LookWhereYouAreGoing<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

}
