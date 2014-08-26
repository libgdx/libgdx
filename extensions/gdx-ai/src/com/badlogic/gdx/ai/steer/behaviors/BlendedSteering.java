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
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.limiters.NeutralConstantLimiter;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** This combination behavior simply sums up all the behaviors, applies their weights, and truncates the result before returning.
 * There are no constraints on the blending weights; they don't have to sum to one, for example, and rarely do. Don't think of
 * {@code BlendedSteering} as a weighted mean, because it's not.
 * <p>
 * With {@code BlendedSteering} you can combine multiple behaviors to get a more complex behavior. It can work fine, but the
 * trade-off is that it comes with a few problems:
 * <ul>
 * <li>Since every active behavior is calculated every time step, it can be a costly method to process.</li>
 * <li>Behavior weights can be difficult to tweak. There have been research projects that have tried to evolve the steering
 * weights using genetic algorithms or neural networks. Results have not been encouraging, however, and manual experimentation
 * still seems to be the most sensible approach.</li>
 * <li>It's problematic with conflicting forces. For instance, a common scenario is where an agent is backed up against a wall by
 * several other agents. In this example, the separating forces from the neighboring agents can be greater than the repulsive
 * force from the wall and the agent can end up being pushed through the wall boundary. This is almost certainly not going to be
 * favorable. Sure you can make the weights for the wall avoidance huge, but then your agent may behave strangely next time it
 * finds itself alone and next to a wall.</li>
 * </ul>
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class BlendedSteering<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The list of behaviors and their corresponding blending weights. */
	protected Array<BehaviorAndWeight<T>> list;

	private SteeringAcceleration<T> steering;

	/** Creates a {@code BlendedSteering} for the specified {@code owner}, {@code maxLinearAcceleration} and
	 * {@code maxAngularAcceleration}.
	 * @param owner the owner of this behavior.
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used.
	 * @param maxAngularAcceleration the maximum angular acceleration that can be used. */
	public BlendedSteering (Steerable<T> owner) {
		super(owner);

		this.list = new Array<BehaviorAndWeight<T>>();
		this.steering = new SteeringAcceleration<T>(owner.newVector());
	}

	/** Adds a steering behavior and its weight to the list.
	 * @param behavior the steering behavior to add
	 * @param weight the weight of the behavior
	 * @return this behavior for chaining. */
	public BlendedSteering<T> add (SteeringBehavior<T> behavior, float weight) {
		return add(new BehaviorAndWeight<T>(behavior, weight));
	}

	/** Adds a steering behavior and its weight to the list.
	 * @param item the steering behavior and its weight
	 * @return this behavior for chaining. */
	public BlendedSteering<T> add (BehaviorAndWeight<T> item) {
		item.behavior.setOwner(owner);
		list.add(item);
		return this;
	}

	/** Returns the weighted behavior at the specified index.
	 * @param index the index of the weighted behavior to return */
	public BehaviorAndWeight<T> get (int index) {
		return list.get(index);
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> blendedSteering) {
		// Clear the output to start with
		blendedSteering.setZero();

		// Go through all the behaviors
		int len = list.size;
		for (int i = 0; i < len; i++) {
			BehaviorAndWeight<T> bw = list.get(i);

			// Calculate the behavior's steering
			bw.behavior.steer(steering);

			// Scale and add the steering to the accumulator
			blendedSteering.mulAdd(steering, bw.weight);
		}

		Limiter actualLimiter = getActualLimiter();

		// Crop the result
		blendedSteering.linear.limit(actualLimiter.getMaxLinearAcceleration());
		if (blendedSteering.angular > actualLimiter.getMaxAngularAcceleration())
			blendedSteering.angular = actualLimiter.getMaxAngularAcceleration();

		return blendedSteering;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public BlendedSteering<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public BlendedSteering<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear and angular
	 * accelerations. You can use {@link NeutralConstantLimiter#LIMITER} to permanently avoid all truncations.
	 * @return this behavior for chaining. */
	@Override
	public BlendedSteering<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}

	//
	// Nested classes
	//

	public static class BehaviorAndWeight<T extends Vector<T>> {

		protected SteeringBehavior<T> behavior;
		protected float weight;

		public BehaviorAndWeight (SteeringBehavior<T> behavior, float weight) {
			this.behavior = behavior;
			this.weight = weight;
		}

		public SteeringBehavior<T> getBehavior () {
			return behavior;
		}

		public void setBehavior (SteeringBehavior<T> behavior) {
			this.behavior = behavior;
		}

		public float getWeight () {
			return weight;
		}

		public void setWeight (float weight) {
			this.weight = weight;
		}
	}

}
