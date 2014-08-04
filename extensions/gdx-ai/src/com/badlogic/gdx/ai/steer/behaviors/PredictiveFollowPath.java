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

/** {@code PredictiveFollowPath} behavior produces a linear acceleration that moves the agent along the given path. It calculates the
 * position of a target based on the predicted future location of the agent and the shape of the path. It then hands its target
 * off to seek.
 * <p>
 * For complex paths with sudden changes of direction this implementation can appear smoother than the non-predictive path
 * following, but has the downside of cutting corners when some sections of the path come close together. This cutting-corner
 * behavior can make the character miss a whole section of the path. This might not be what you want if, for example, the path
 * represents a patrol route.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class PredictiveFollowPath<T extends Vector<T>, P extends Param> extends FollowPathBase<T, P> {

	/** The time in the future to predict the owner's position */
	protected float predictionTime;

	private T futurePosition;

	/** Create a PredictiveFollowPath behavior. The prediction time is set to 0.1 seconds.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction.
	 * @param maxLinearAcceleration the maximum acceleration that can be used to reach the target. */
	public PredictiveFollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset, float maxLinearAcceleration) {
		this(owner, path, pathOffset, maxLinearAcceleration, 0.1f);
	}

	/** Create a PredictiveFollowPath behavior.
	 * @param owner the owner of this behavior
	 * @param path the path to be followed by the owner
	 * @param pathOffset the distance along the path to generate the target. Can be negative if the owner is to move along the
	 *           reverse direction.
	 * @param maxLinearAcceleration the maximum acceleration that can be used to reach the target.
	 * @param predictionTime the time in the future to predict the owner's position. */
	public PredictiveFollowPath (Steerable<T> owner, Path<T, P> path, float pathOffset, float maxLinearAcceleration,
		float predictionTime) {
		super(owner, path, pathOffset, maxLinearAcceleration);
		this.predictionTime = predictionTime;
		this.futurePosition = owner.newVector();
	}

	@Override
	protected T calculateSourcePosition () {
		// Find the predicted future position of the owner
		return futurePosition.set(owner.getPosition()).mulAdd(owner.getLinearVelocity(), predictionTime);
	}

	/** Returns the prediction time */
	public float getPredictionTime () {
		return predictionTime;
	}

	/** Sets the prediction time
	 * @param predictionTime the predictionTime to set */
	public void setPredictionTime (float predictionTime) {
		this.predictionTime = predictionTime;
	}
}
