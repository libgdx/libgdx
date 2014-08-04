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
import com.badlogic.gdx.utils.Array;

/** This combination behavior simply sums up all the active behaviors, applies their weights, and truncates the result before
 * returning.
 * <p>
 * With {@code WeightedBlender} you can combine multiple behaviors to get a more complex behavior. It can work fine, but the trade-off is that it comes with a few problems:
 * <ul>
 * <li>Since every active behavior is calculated every time step, it can be a costly method to process.</li>
 * <li>Behavior weights can be difficult to tweak.</li>
 * <li>It's problematic with conflicting forces. For instance, A common scenario is where a vehicle is backed up against a wall by
 * several other vehicles. In this example, the separating forces from the neighboring vehicles can be greater than the repulsive
 * force from the wall and the vehicle can end up being pushed through the wall boundary. This is almost certainly not going to be
 * favorable. Sure you can make the weights for the wall avoidance huge, but then your vehicle may behave strangely next time it
 * finds itself alone and next to a wall.</li>
 * </ul>
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class WeightedBlender<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The maximum linear acceleration that can be used. */
	protected float maxLinearAcceleration;

	/** The maximum angular acceleration that can be used. */
	protected float maxAngularAcceleration;

	/** The list of behaviors and their corresponding blending weights. */
	protected Array<BehaviorAndWeight<T>> weights;

	private SteeringAcceleration<T> steering;

	/** Creates a {@code WeightedBlender} for the specified {@code owner}, {@code maxLinearAcceleration} and
	 * {@code maxAngularAcceleration}.
	 * @param owner the owner of this behavior.
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used.
	 * @param maxAngularAcceleration the maximum angular acceleration that can be used. */
	public WeightedBlender (Steerable<T> owner, float maxLinearAcceleration, float maxAngularAcceleration) {
		super(owner);
		this.maxLinearAcceleration = maxLinearAcceleration;
		this.maxAngularAcceleration = maxAngularAcceleration;

		this.weights = new Array<BehaviorAndWeight<T>>();
		this.steering = new SteeringAcceleration<T>(owner.newVector());
	}

	public void add (SteeringBehavior<T> behavior, float weight) {
		add(new BehaviorAndWeight<T>(behavior, weight));
	}

	public void add (BehaviorAndWeight<T> weight) {
		weight.behavior.setOwner(owner);
		weights.add(weight);
	}

	public BehaviorAndWeight<T> getBehaviorAndWeight (int i) {
		return weights.get(i);
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> blendedSteering) {
		// Clear the output to start with
		blendedSteering.setZero();

		// Go through all the enabled behaviors
		int len = weights.size;
		for (int i = 0; i < len; i++) {
			BehaviorAndWeight<T> bw = weights.get(i);
			if (bw.behavior.isEnabled()) {
				// Calculate the behavior's steering of and add it to the accumulator
				bw.behavior.calculateSteering(steering);
				blendedSteering.mulAdd(steering, bw.weight);
			}
		}

		// Crop the result
		blendedSteering.linear.limit(maxLinearAcceleration);
		if (blendedSteering.angular > maxAngularAcceleration) blendedSteering.angular = maxAngularAcceleration;

		return blendedSteering;
	}

	/** Returns the maximum linear acceleration that can be used. */
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** Sets the maximum linear acceleration that can be used. */
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

	/** Returns the maximum angular acceleration that can be used. */
	public float getMaxAngularAcceleration () {
		return maxAngularAcceleration;
	}

	/** Sets the maximum angular acceleration that can be used. */
	public void setMaxAngularAcceleration (float maxAngularAcceleration) {
		this.maxAngularAcceleration = maxAngularAcceleration;
	}

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
