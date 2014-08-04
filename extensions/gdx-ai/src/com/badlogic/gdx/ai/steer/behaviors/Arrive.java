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

/** {@code Arrive} behavior moves the agent towards a target position. It is similar to seek but it attempts to arrive at the target
 * position with a zero velocity.
 * <p>
 * {@code Arrive} behavior uses two radii. The {@code arrivalTolerance} lets the owner get near enough to the target without
 * letting small errors keep it in motion. The {@code decelerationRadius}, usually much larger than the previous one, specifies
 * when the incoming character will begin to slow down. The algorithm calculates an ideal speed for the owner. At the slowing-down
 * radius, this is equal to its maximum speed. At the target point, it is zero (we want to have zero speed when we arrive). In
 * between, the desired speed is an interpolated intermediate value, controlled by the distance from the target.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Arrive<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The target to arrive to. */
	protected Steerable<T> target;

	/** The maximum linear acceleration of the owner. */
	protected float maxLinearAcceleration;

	/** The maximum speed of the owner. */
	protected float maxSpeed;

	/** The tolerance for arriving at the target. It lets the owner get near enough to the target without letting small errors keep
	 * it in motion. */
	protected float arrivalTolerance;

	/** The radius for beginning to slow down */
	protected float decelerationRadius;

	/** The time over which to achieve target speed */
	protected float timeToTarget = 0.1f;

	/** Creates an {@code Arrive} behavior for the specified owner, target and acceleration.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to arrive at the target. */
	public Arrive (Steerable<T> owner, Steerable<T> target, float maxLinearAcceleration) {
		super(owner);
		this.target = target;
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		return arrive(steering, target.getPosition());
	}

	protected SteeringAcceleration<T> arrive (SteeringAcceleration<T> steering, T targetPosition) {
		// Get the direction and distance to the target
		T toTarget = steering.linear.set(targetPosition).sub(owner.getPosition());
		float distance = toTarget.len();

		// Check if we are there, return no steering
		if (distance <= arrivalTolerance) return steering.setZero();

		// Go max speed
		float targetSpeed = maxSpeed;

		// If we are inside the slow down radius calculate a scaled speed
		if (distance <= decelerationRadius) targetSpeed *= distance / decelerationRadius;

		// Target velocity combines speed and direction
		T targetVelocity = toTarget.scl(targetSpeed / distance); // Optimized code for: toTarget.nor().scl(targetSpeed)

		// Acceleration tries to get to the target velocity without exceeding max acceleration
		// Notice that steering.linear and targetVelocity are the same vector
		targetVelocity.sub(owner.getLinearVelocity()).scl(1f / timeToTarget).limit(maxLinearAcceleration);

		// No angular acceleration
		steering.angular = 0f;

		// Output the steering
		return steering;
	}

	/** Returns the target to arrive to. */
	public Steerable<T> getTarget () {
		return target;
	}

	/** Sets the target to arrive to. */
	public void setTarget (Steerable<T> target) {
		this.target = target;
	}

	/** Returns the maximum linear acceleration the owner can use. */
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** Sets the maximum linear acceleration the owner can use. */
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	/** Returns the maximum speed of the owner. */
	public float getMaxSpeed () {
		return maxSpeed;
	}

	/** Sets the maximum speed of the owner. */
	public void setMaxSpeed (float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/** Returns the tolerance for arriving at the target. It lets the owner get near enough to the target without letting small
	 * errors keep it in motion. */
	public float getArrivalTolerance () {
		return arrivalTolerance;
	}

	/** Sets the tolerance for arriving at the target. It lets the owner get near enough to the target without letting small errors
	 * keep it in motion. */
	public void setArrivalTolerance (float arrivalTolerance) {
		this.arrivalTolerance = arrivalTolerance;
	}

	/** Returns the radius for beginning to slow down. */
	public float getDecelerationRadius () {
		return decelerationRadius;
	}

	/** Sets the radius for beginning to slow down. */
	public void setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
	}

	/** Returns the time over which to achieve target speed. */
	public float getTimeToTarget () {
		return timeToTarget;
	}

	/** Sets the time over which to achieve target speed. */
	public void setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
	}

}
