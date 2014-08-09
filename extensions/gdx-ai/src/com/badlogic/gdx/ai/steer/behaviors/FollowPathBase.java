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
import com.badlogic.gdx.ai.steer.behaviors.FollowPathBase.Path.Param;
import com.badlogic.gdx.math.Vector;

/** {@code FollowPathBase} is the base class for path following behaviors creating a linear acceleration that moves an agent along
 * the given path.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public abstract class FollowPathBase<T extends Vector<T>, P extends Param> extends SteeringBehavior<T> {

	/** The path to follow */
	protected Path<T, P> path;

	/** The distance along the path to generate the target. Can be negative if the owner is to move along the reverse direction. */
	protected float pathOffset;

	/** The current position on the path */
	protected P pathParam;

	/** The maximum acceleration that can be used to reach the target. */
	protected float maxLinearAcceleration;

	private T internalTargetPosition;

	/** Create a {@code FollowPathBase} behavior for the specified owner and path.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner. */
	public FollowPathBase (Steerable<T> owner, Path<T, P> path) {
		this(owner, path, 0);
	}

	/** Create a {@code FollowPathBase} behavior for the specified owner, path and path offset.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction. */
	public FollowPathBase (Steerable<T> owner, Path<T, P> path, float pathOffset) {
		this(owner, path, pathOffset, 0);
	}

	/** Create a {@code FollowPathBase} behavior for the specified owner, path, path offset and maximum linear acceleration.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction.
	 * @param maxLinearAcceleration the maximum acceleration that can be used to reach the internal target. */
	public FollowPathBase (Steerable<T> owner, Path<T, P> path, float pathOffset, float maxLinearAcceleration) {
		super(owner);
		this.path = path;
		this.pathParam = path.createParam();
		this.pathOffset = pathOffset;
		this.maxLinearAcceleration = maxLinearAcceleration;

		this.internalTargetPosition = owner.newVector();
	}

	/** Returns the owner position to be used as a source for farther calculations */
	protected abstract T calculateSourcePosition ();

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Find the current distance from the start of the path
		float distance = path.calculateDistance(calculateSourcePosition(), pathParam);

		// Offset it
		float targetDistance = distance + pathOffset;

		// Calculate the target position
		path.calculateTargetPosition(internalTargetPosition, pathParam, targetDistance);

		// Seek the target position
		steering.linear.set(internalTargetPosition).sub(owner.getPosition()).nor().scl(maxLinearAcceleration);

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	/** Returns the maximum linear acceleration */
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** Sets the maximum linear acceleration
	 * @param maxLinearAcceleration the maximum linear acceleration to set
	 * @return this behavior for chaining. */
	public FollowPathBase<T, P> setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
		return this;
	}

	/** Returns the path to follow */
	public Path<T, P> getPath () {
		return path;
	}

	/** @param path the path to set
	 * @return this behavior for chaining. */
	public FollowPathBase<T, P> setPath (Path<T, P> path) {
		this.path = path;
		return this;
	}

	/** Returns the pathOffset */
	public float getPathOffset () {
		return pathOffset;
	}

	/** @param pathOffset the pathOffset to set
	 * @return this behavior for chaining. */
	public FollowPathBase<T, P> setPathOffset (float pathOffset) {
		this.pathOffset = pathOffset;
		return this;
	}

	/** @return the pathParam */
	public P getPathParam () {
		return pathParam;
	}

	/** @param pathParam the pathParam to set
	 * @return this behavior for chaining. */
	public FollowPathBase<T, P> setPathParam (P pathParam) {
		this.pathParam = pathParam;
		return this;
	}

	/** Returns the current position of the internal target. This method is useful for debug purpose. */
	public T getInternalTargetPosition () {
		return internalTargetPosition;
	}

	/** The path for an agent having path following behavior.
	 * <p>
	 * The most common type of path is made up of straight line segments, which usually gives reasonably good results while keeping
	 * the math simple. However, some driving games use splines to get smoother curved paths, which makes the math more complex.
	 * 
	 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
	 * @param <P> Type of path parameter implementing the {@link Path.Param} interface
	 * 
	 * @author davebaol */
	public interface Path<T extends Vector<T>, P extends Param> {

		/** @return */
		public P createParam ();

		/** @return */
		public float calculateDistance (T position, P param);

		/**
		 * 
		 */
		public void calculateTargetPosition (T out, P param, float targetDistance);

		/** @author davebaol */
		public interface Param {
			/** @return */
			public float getDistance ();

			/** @return */
			public void setDistance (float distance);
		}
	}
}
