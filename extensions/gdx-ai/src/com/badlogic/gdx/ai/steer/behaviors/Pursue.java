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

/** {@code Pursue} behavior produces a force that steers the agent towards the evader (the target). Actually it predicts where an
 * agent will be in time @{code t} and seeks towards that point to intercept it. We did this naturally playing tag as children,
 * which is why the most difficult tag players to catch were those who kept switching direction, foiling our predictions.
 * <p>
 * This implementation performs the prediction by assuming the target will continue moving with the same velocity it currently
 * has. This is a reasonable assumption over short distances, and even over longer distances it doesn't appear too stupid. The
 * algorithm works out the distance between character and target and works out how long it would take to get there, at maximum
 * speed. It uses this time interval as its prediction lookahead. It calculates the position of the target if it continues to move
 * with its current velocity. This new position is then used as the target of a standard seek behavior.
 * <p>
 * If the character is moving slowly, or the target is a long way away, the prediction time could be very large. The target is
 * less likely to follow the same path forever, so we'd like to set a limit on how far ahead we aim. The algorithm has a
 * {@code maxPredictionTime} for this reason. If the prediction time is beyond this, then the maximum time is used.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Pursue<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The target */
	protected Steerable<T> target;

	/** The maximum acceleration that can be used. */
	protected float maxLinearAcceleration;

	/** The maximum prediction time */
	protected float maxPredictionTime;

	/** Creates a {@code Pursue} behavior for the specified owner and target.
	 * @param owner the owner of this behavior
	 * @param target the target
	 * @param maxLinearAcceleration the maximum linear acceleration of the owner
	 * @param maxPredictionTime the max time used to predict the target's position assuming it continues to move with its current
	 *           velocity. */
	public Pursue (Steerable<T> owner, Steerable<T> target, float maxLinearAcceleration, float maxPredictionTime) {
		super(owner);
		this.target = target;
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.maxPredictionTime = maxPredictionTime;
	}

	/** Returns the actual linear acceleration to be applied. This method is overridden by the {@link Evade} behavior to invert the
	 * maximum linear acceleration in order to evade the target. */
	protected float getActualLinearAcceleration () {
		return maxLinearAcceleration;
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		T targetPosition = target.getPosition();

		// Get the square distance to the evader (the target)
		float squareDistance = steering.linear.set(targetPosition).sub(owner.getPosition()).len2();

		// Work out our current square speed
		float squareSpeed = owner.getLinearVelocity().len2();

		float predictionTime = maxPredictionTime;

		if (squareSpeed > 0) {
			// Calculate prediction time if speed is not too small to give a reasonable value
			float squarePredictionTime = squareDistance / squareSpeed;
			if (squarePredictionTime < maxPredictionTime * maxPredictionTime)
				predictionTime = (float)Math.sqrt(squarePredictionTime);
		}

		// Calculate and seek/flee the predicted position of the target
		steering.linear.set(targetPosition).mulAdd(target.getLinearVelocity(), predictionTime).sub(owner.getPosition()).nor()
			.scl(getActualLinearAcceleration());

		// No angular acceleration
		steering.angular = 0;

		// Output steering acceleration
		return steering;
	}

	/** Returns the target. */
	public Steerable<T> getTarget () {
		return target;
	}

	/** Sets the target. */
	public void setTarget (Steerable<T> target) {
		this.target = target;
	}

	/** Returns the maximum linear acceleration that can be used. */
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** Sets the maximum linear acceleration that can be used. */
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	/** Returns the maximum prediction time. */
	public float getMaxPredictionTime () {
		return maxPredictionTime;
	}

	/** Sets the maximum prediction time. */
	public void setMaxPredictionTime (float maxPredictionTime) {
		this.maxPredictionTime = maxPredictionTime;
	}

}
