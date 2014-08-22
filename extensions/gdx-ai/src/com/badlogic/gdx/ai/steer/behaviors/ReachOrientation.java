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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;

/** {@code ReachOrientation} tries to align the owner to the target. It pays no attention to the position or velocity of the owner
 * or target. This steering behavior does not produce any linear acceleration; it only responds by turning.
 * <p>
 * {@code ReachOrientation} behaves in a similar way to {@link Arrive} since it tries to reach the target orientation and tries to
 * have zero rotation when it gets there. Like arrive, it uses two radii: {@code decelerationRadius} for slowing down and
 * {@code alignTolerance} to make orientations near the target acceptable without letting small errors keep the owner swinging.
 * Because we are dealing with a single scalar value, rather than a 2D or 3D vector, the radius acts as an interval.
 * <p>
 * Similarly to {@code Arrive}, there is a {@code timeToTarget} that defaults to 0.1 seconds.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class ReachOrientation<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The target to align to. */
	protected Steerable<T> target;

	/** The maximum angular acceleration of the owner. */
	protected float maxAngularAcceleration;

	/** The maximum angular speed of the owner. */
	protected float maxAngularSpeed;

	/** The tolerance for aligning to the target without letting small errors keep the owner swinging. */
	protected float alignTolerance;

	/** The radius for beginning to slow down */
	protected float decelerationRadius;

	/** The time over which to achieve target rotation speed */
	protected float timeToTarget = 0.1f;

	/** Creates a {@code ReachOrientation} behavior for the specified owner.
	 * @param owner the owner of this behavior. */
	public ReachOrientation (Steerable<T> owner) {
		this(owner, null);
	}

	/** Creates a {@code ReachOrientation} behavior for the specified owner and target.
	 * @param owner the owner of this behavior
	 * @param target the target. */
	public ReachOrientation (Steerable<T> owner, Steerable<T> target) {
		super(owner);
		this.target = target;
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		return reachOrientation(steering, target.getOrientation());
	}

	/** Produces a steering that tries to align the owner to the target orientation. This method is called by subclasses that want
	 * to align to a certain orientation.
	 * @param steering the steering to be calculated.
	 * @param targetOrientation the target orientation you want to align to.
	 * @return the calculated steering for chaining. */
	protected SteeringAcceleration<T> reachOrientation (SteeringAcceleration<T> steering, float targetOrientation) {
		// Get the rotation direction to the target wrapped to the range [-PI, PI]
		float rotation = (targetOrientation - owner.getOrientation()) % MathUtils.PI2;
		if (rotation > MathUtils.PI) rotation -= MathUtils.PI2;

		// Absolute rotation
		float rotationSize = rotation < 0f ? -rotation : rotation;

		// Check if we are there, return no steering
		if (rotationSize < alignTolerance) return steering.setZero();

		// Use maximum rotation
		float targetRotation = getMaxAngularSpeed();

		// If we are inside the slow down radius, then calculate a scaled rotation
		if (rotationSize <= decelerationRadius) targetRotation *= rotationSize / decelerationRadius;

		// The final target rotation combines
		// speed (already in the variable) and direction
		targetRotation *= rotation / rotationSize;

		// Acceleration tries to get to the target rotation
		steering.angular = (targetRotation - owner.getAngularVelocity()) / timeToTarget;

		// Check if the absolute acceleration is too great
		float angularAcceleration = steering.angular < 0f ? -steering.angular : steering.angular;
		if (angularAcceleration > getMaxAngularAcceleration()) steering.angular *= getMaxAngularAcceleration() / angularAcceleration;

		// No linear acceleration
		steering.linear.setZero();

		// Output the steering
		return steering;
	}

	/** Returns the target to align to. */
	public Steerable<T> getTarget () {
		return target;
	}

	/** Sets the target to align to.
	 * @return this behavior for chaining. */
	public ReachOrientation<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	/** Returns the maximum angular acceleration of the owner. */
	public float getMaxAngularAcceleration () {
		return maxAngularAcceleration;
	}

	/** Sets the maximum angular acceleration of the owner.
	 * @return this behavior for chaining. */
	public ReachOrientation<T> setMaxAngularAcceleration (float maxAngularAcceleration) {
		this.maxAngularAcceleration = maxAngularAcceleration;
		return this;
	}

	/** Returns the maximum angular speed of the owner. */
	public float getMaxAngularSpeed () {
		return maxAngularSpeed;
	}

	/** Sets the maximum angular speed of the owner.
	 * @return this behavior for chaining. */
	public ReachOrientation<T> setMaxAngularSpeed (float maxAngularSpeed) {
		this.maxAngularSpeed = maxAngularSpeed;
		return this;
	}

	/** Returns the tolerance for aligning to the target without letting small errors keep the owner swinging. */
	public float getAlignTolerance () {
		return alignTolerance;
	}

	/** Sets the tolerance for aligning to the target without letting small errors keep the owner swinging.
	 * @return this behavior for chaining. */
	public ReachOrientation<T> setAlignTolerance (float alignTolerance) {
		this.alignTolerance = alignTolerance;
		return this;
	}

	/** Returns the radius for beginning to slow down */
	public float getDecelerationRadius () {
		return decelerationRadius;
	}

	/** Sets the radius for beginning to slow down
	 * @return this behavior for chaining. */
	public ReachOrientation<T> setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
		return this;
	}

	/** Returns the time over which to achieve target rotation speed */
	public float getTimeToTarget () {
		return timeToTarget;
	}

	/** Sets the time over which to achieve target rotation speed
	 * @return this behavior for chaining. */
	public ReachOrientation<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

}
