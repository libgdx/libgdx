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
import com.badlogic.gdx.ai.steer.behaviors.FollowPath.Path.Param;
import com.badlogic.gdx.math.Vector;

/** {@code FollowPath} behavior produces a linear acceleration that moves the agent along the given path. First it calculates the
 * agent location based on the specified prediction time. Then it calculates the position of the internal target based on that
 * agent location and the shape of the path. It finally seeks the internal target position.
 * <p>
 * For complex paths with sudden changes of direction the predictive behavior (i.e., with prediction time greater than 0) can
 * appear smoother than the non-predictive one (i.e., with no prediction time). However, predictive path following has the
 * downside of cutting corners when some sections of the path come close together. This cutting-corner attitude can make the
 * character miss a whole section of the path. This might not be what you want if, for example, the path represents a patrol
 * route.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @param <P> Type of path parameter implementing the {@link Path.Param} interface
 * 
 * @autor davebaol */
public class FollowPath<T extends Vector<T>, P extends Param> extends Arrive<T> {

	/** The path to follow */
	protected Path<T, P> path;

	/** The distance along the path to generate the target. Can be negative if the owner has to move along the reverse direction. */
	protected float pathOffset;

	/** The current position on the path */
	protected P pathParam;

	/** The time in the future to predict the owner's position. Set it to 0 for non-predictive path following. */
	protected float predictionTime;

	private T internalTargetPosition;

	/** Creates a non-predictive {@code FollowPath} behavior for the specified owner and path.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner. */
	public FollowPath (Steerable<T> owner, Path<T, P> path) {
		this(owner, path, 0);
	}

	/** Creates a non-predictive {@code FollowPath} behavior for the specified owner, path and path offset.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction. */
	public FollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset) {
		this(owner, path, pathOffset, 0);
	}

	/** Creates a non-predictive {@code FollowPath} behavior for the specified owner, path, path offset and maximum linear
	 * acceleration.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction.
	 * @param maxLinearAcceleration the maximum acceleration that can be used to reach the internal target. */
	public FollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset, float maxLinearAcceleration) {
		this(owner, path, pathOffset, maxLinearAcceleration, 0);
	}

	/** Creates a {@code FollowPath} behavior for the specified owner, path, path offset, maximum linear acceleration and prediction
	 * time.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction.
	 * @param maxLinearAcceleration the maximum acceleration that can be used to reach the internal target.
	 * @param predictionTime the time in the future to predict the owner's position. Can be 0 for non-predictive path following. */
	public FollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset, float maxLinearAcceleration, float predictionTime) {
		super(owner);
		this.path = path;
		this.pathParam = path.createParam();
		this.pathOffset = pathOffset;
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.predictionTime = predictionTime;

		this.internalTargetPosition = owner.newVector();
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {

		// Predictive or non-predictive behavior?
		T location = (predictionTime == 0) ?
		// Use the current position of the owner
		owner.getPosition()
			:
			// Calculate the predicted future position of the owner. We're reusing steering.linear here.
			steering.linear.set(owner.getPosition()).mulAdd(owner.getLinearVelocity(), predictionTime);

		// Find the distance from the start of the path
		float distance = path.calculateDistance(location, pathParam);

		// Offset it
		float targetDistance = distance + pathOffset;

		// Calculate the target position
		path.calculateTargetPosition(internalTargetPosition, pathParam, targetDistance);

		if (path.isOpen()) {
			if (pathOffset >= 0) {
				// Use Arrive to approach the last point of the path
				if (targetDistance > path.getLength() - decelerationRadius) return arrive(steering, internalTargetPosition);
			} else {
				// Use Arrive to approach the first point of the path
				if (targetDistance < decelerationRadius) return arrive(steering, internalTargetPosition);
			}
		}

		// Seek the target position
		steering.linear.set(internalTargetPosition).sub(owner.getPosition()).nor().scl(maxLinearAcceleration);

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	/** Returns the path to follow */
	public Path<T, P> getPath () {
		return path;
	}

	/** Sets the path followed by this behavior.
	 * @param path the path to set
	 * @return this behavior for chaining. */
	public FollowPath<T, P> setPath (Path<T, P> path) {
		this.path = path;
		return this;
	}

	/** Returns the path offset. */
	public float getPathOffset () {
		return pathOffset;
	}

	/** Sets the path offset to generate the target. Can be negative if the owner has to move along the reverse direction.
	 * @param pathOffset the pathOffset to set
	 * @return this behavior for chaining. */
	public FollowPath<T, P> setPathOffset (float pathOffset) {
		this.pathOffset = pathOffset;
		return this;
	}

	/** Returns the prediction time. */
	public float getPredictionTime () {
		return predictionTime;
	}

	/** Sets the prediction time. Set it to 0 for non-predictive path following.
	 * @param predictionTime the predictionTime to set
	 * @return this behavior for chaining. */
	public FollowPath<T, P> setPredictionTime (float predictionTime) {
		this.predictionTime = predictionTime;
		return this;
	}

	/** Returns the current position of the internal target. This method is useful for debug purpose. */
	public T getInternalTargetPosition () {
		return internalTargetPosition;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public FollowPath<T, P> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	@Override
	public FollowPath<T, P> setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
		return this;
	}

	@Override
	public FollowPath<T, P> setMaxSpeed (float maxSpeed) {
		this.maxSpeed = maxSpeed;
		return this;
	}

	@Override
	public FollowPath<T, P> setArrivalTolerance (float arrivalTolerance) {
		this.arrivalTolerance = arrivalTolerance;
		return this;
	}

	@Override
	public FollowPath<T, P> setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
		return this;
	}

	@Override
	public FollowPath<T, P> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

	/** The path for an agent having path following behavior. A path can be shared by multiple path following behaviors because its
	 * status is maintained in a {@link Path.Param} local to each behavior.
	 * <p>
	 * The most common type of path is made up of straight line segments, which usually gives reasonably good results while keeping
	 * the math simple. However, some driving games use splines to get smoother curved paths, which makes the math more complex.
	 * 
	 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
	 * @param <P> Type of path parameter implementing the {@link Path.Param} interface
	 * 
	 * @author davebaol */
	public interface Path<T extends Vector<T>, P extends Param> {

		/** Returns a new instance of the path parameter. */
		public P createParam ();

		/** Returns {@code true} if this path is open; {@code false} otherwise. */
		public boolean isOpen ();

		/** Returns the length of this path. */
		public float getLength ();

		/** Returns the first point of this path. */
		public T getStartPoint ();

		/** Returns the last point of this path. */
		public T getEndPoint ();

		/** Maps the given position to the nearest point along the path using the path parameter to ensure coherence and returns the
		 * distance of that nearest point from the start of the path.
		 * @param position a location in game space
		 * @param param the path parameter
		 * @return the distance of the nearest point along the path from the start of the path itself. */
		public float calculateDistance (T position, P param);

		/** Calculates the target position on the path based on its distance from the start and the path parameter.
		 * @param out the target position to calculate
		 * @param param the path parameter
		 * @param targetDistance the distance of the target position from the start of the path */
		public void calculateTargetPosition (T out, P param, float targetDistance);

		/** A path parameter used by path following behaviors to keep the path status.
		 * 
		 * @author davebaol */
		public interface Param {

			public float getDistance ();

			public void setDistance (float distance);
		}
	}
}
