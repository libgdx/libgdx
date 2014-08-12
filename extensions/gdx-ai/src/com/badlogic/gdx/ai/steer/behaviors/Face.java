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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** {@code Face} behavior makes the owner look at its target. It delegates to the {@link ReachOrientation} behavior to perform the
 * rotation but calculates the target orientation first based on target and owner position.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Face<T extends Vector<T>> extends ReachOrientation<T> {

	/** Creates a {@code Face} behavior for the specified owner.
	 * @param owner the owner of this behavior. */
	public Face (Steerable<T> owner) {
		this(owner, null);
	}

	/** Creates a {@code Face} behavior for the specified owner and target.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior. */
	public Face (Steerable<T> owner, Steerable<T> target) {
		super(owner, target);
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		return face(steering, target.getPosition());
	}

	protected SteeringAcceleration<T> face (SteeringAcceleration<T> steering, T targetPosition) {
		// Get the direction to target
		T toTarget = steering.linear.set(targetPosition).sub(owner.getPosition());

		// Check for a zero direction, and return no steering if so
		if (toTarget.isZero(MathUtils.FLOAT_ROUNDING_ERROR)) return steering.setZero();

		// Calculate the orientation to face the target
		float orientation = owner.vectorToAngle(toTarget);

		// Delegate to ReachOrientation
		return reachOrientation(steering, orientation);
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Face<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	@Override
	public Face<T> setMaxAngularAcceleration (float maxAngularAcceleration) {
		this.maxAngularAcceleration = maxAngularAcceleration;
		return this;
	}

	@Override
	public Face<T> setMaxRotation (float maxRotation) {
		this.maxRotation = maxRotation;
		return this;
	}

	@Override
	public Face<T> setAlignTolerance (float alignTolerance) {
		this.alignTolerance = alignTolerance;
		return this;
	}

	@Override
	public Face<T> setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
		return this;
	}

	@Override
	public Face<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

}
