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
import com.badlogic.gdx.ai.steer.behaviors.FollowPathBase.Path.Param;
import com.badlogic.gdx.math.Vector;

/** {@code FollowPath behavior produces a linear acceleration that moves the agent along the given path. It calculates the position of a
 * target based on the current agent location and the shape of the path. It then hands its target off to seek.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class FollowPath<T extends Vector<T>, P extends Param> extends FollowPathBase<T, P> {

	/** Create a {@code FollowPath} behavior for the specified owner and path.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner. */
	public FollowPath (Steerable<T> owner, Path<T, P> path) {
		this(owner, path, 0);
	}

	/** Create a {@code FollowPath} behavior for the specified owner, path and path offset.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction. */
	public FollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset) {
		this(owner, path, pathOffset, 0);
	}

	/** Create a {@code FollowPath} behavior for the specified owner, path, path offset and maximum linear acceleration.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction.
	 * @param maxLinearAcceleration the maximum acceleration that can be used to reach the target. */
	public FollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset, float maxLinearAcceleration) {
		super(owner, path, pathOffset, maxLinearAcceleration);
	}

	@Override
	protected T calculateSourcePosition () {
		// Simply return the current position of the owner
		return owner.getPosition();
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public FollowPath<T, P> setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
		return this;
	}

	@Override
	public FollowPath<T, P> setPath (Path<T, P> path) {
		this.path = path;
		return this;
	}

	@Override
	public FollowPath<T, P> setPathOffset (float pathOffset) {
		this.pathOffset = pathOffset;
		return this;
	}

	@Override
	public FollowPath<T, P> setPathParam (P pathParam) {
		this.pathParam = pathParam;
		return this;
	}
}
