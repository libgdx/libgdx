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
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** The {@code PrioritySteering} behavior iterates through the behaviors and returns the first non zero steering. It makes sense
 * since certain steering behaviors only request an acceleration in particular conditions. Unlike {@link Seek} or {@link Evade},
 * which always produce an acceleration, {@link RaycastObstacleAvoidance}, {@link CollisionAvoidance}, {@link Separation},
 * {@link Hide} and {@link Arrive} will suggest no acceleration in many cases. But when these behaviors do suggest an
 * acceleration, it is unwise to ignore it. An obstacle avoidance behavior, for example, should be honored immediately to avoid
 * the crash.
 * <p>
 * Typically the behaviors of a {@code PrioritySteering} are arranged in groups with regular blending weights, see
 * {@link BlendedSteering}. These groups are then placed in priority order to let the steering system consider each group in turn.
 * It blends the steering behaviors in the current group together. If the total result is very small (less than some small, but
 * adjustable, parameter), then it is ignored and the next group is considered. It is best not to check against zero directly,
 * because numerical instability in calculations can mean that a zero value is never reached for some steering behaviors. Using a
 * small constant value (conventionally called {@code epsilon}) avoids this problem. When a group is found with a result that isn't
 * small, its result is used to steer the agent.
 * <p>
 * For instance, a pursuing agent working in a team may have three priorities:
 * <ul>
 * <li>a collision avoidance group that contains behaviors for obstacle avoidance, wall avoidance, and avoiding other characters.</li>
 * <li>a separation behavior used to avoid getting too close to other members of the chasing pack.</li>
 * <li>a pursuit behavior to chase the target.</li>
 * </ul>
 * If the character is far from any interference, the collision avoidance group will return with no desired acceleration. The
 * separation behavior will then be considered but will also return with no action. Finally, the pursuit behavior will be
 * considered, and the acceleration needed to continue the chase will be used. If the current motion of the character is perfect
 * for the pursuit, this behavior may also return with no acceleration. In this case, there are no more behaviors to consider, so
 * the character will have no acceleration, just as if they'd been exclusively controlled by the pursuit behavior.
 * <p>
 * In a different scenario, if the character is about to crash into a wall, the first group will return an acceleration that will
 * help avoid the crash. The character will carry out this acceleration immediately, and the steering behaviors in the other
 * groups won't be considered.
 * <p>
 * Usually {@code PrioritySteering} gives you a good compromise between speed and accuracy.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class PrioritySteering<T extends Vector<T>> extends SteeringBehavior<T> {

	/** The threshold of the steering acceleration magnitude below which a steering behavior is considered to have given no output. */
	protected float epsilon;

	/** The list of steering behaviors in priority order. The first item in the list is tried first, the subsequent entries are only
	 * considered if the first one does not return a result. */
	protected Array<SteeringBehavior<T>> behaviors = new Array<SteeringBehavior<T>>();

	/** Creates a {@code PrioritySteering} behavior for the specified owner. The threshold is set to 0.001.
	 * @param owner the owner of this behavior */
	public PrioritySteering (Steerable<T> owner) {
		this(owner, 0.001f);
	}

	/** Creates a {@code PrioritySteering} behavior for the specified owner and threshold.
	 * @param owner the owner of this behavior
	 * @param epsilon the threshold of the steering acceleration magnitude below which a steering behavior is considered to have
	 *           given no output */
	public PrioritySteering (Steerable<T> owner, float epsilon) {
		super(owner);
		this.epsilon = epsilon;
	}

	/** Adds the specified behavior to the priority list.
	 * @param behavior the behavior to add
	 * @return this behavior for chaining. */
	public PrioritySteering<T> add (SteeringBehavior<T> behavior) {
		behaviors.add(behavior);
		return this;
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// We'll need epsilon squared later.
		float epsilonSquared = epsilon * epsilon;

		// Go through the behaviors until one has a large enough acceleration
		int n = behaviors.size;
		for (int i = 0; i < n; i++) {
			SteeringBehavior<T> behavior = behaviors.get(i);

			// Calculate the behavior's steering
			behavior.steer(steering);

			// If we're above the threshold return the current steering
			if (steering.calculateSquareMagnitude() > epsilonSquared) return steering;
		}

		// If we get here, it means that no behavior had a large enough acceleration,
		// so return the small acceleration from the final behavior or zero if there are
		// no behaviors in the list.
		return n > 0 ? steering : steering.setZero();
	}

	/** Returns the threshold of the steering acceleration magnitude below which a steering behavior is considered to have given no
	 * output. */
	public float getEpsilon () {
		return epsilon;
	}

	/** Sets the threshold of the steering acceleration magnitude below which a steering behavior is considered to have given no
	 * output.
	 * @param epsilon the epsilon to set
	 * @return this behavior for chaining. */
	public PrioritySteering<T> setEpsilon (float epsilon) {
		this.epsilon = epsilon;
		return this;
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public PrioritySteering<T> setOwner (Steerable<T> owner) {
		this.owner = owner;
		return this;
	}

	@Override
	public PrioritySteering<T> setEnabled (boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/** Sets the limiter of this steering behavior. However, {@code PrioritySteering} needs no limiter at all as it simply returns
	 * the first non zero steering acceleration.
	 * @return this behavior for chaining. */
	@Override
	public PrioritySteering<T> setLimiter (Limiter limiter) {
		this.limiter = limiter;
		return this;
	}
}
