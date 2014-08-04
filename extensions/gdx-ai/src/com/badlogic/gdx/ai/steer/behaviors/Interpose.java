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
import com.badlogic.gdx.math.Vector;

/** Interpose behavior produces a steering force that moves the owner to a point along the imaginary line connecting two other
 * agents. A bodyguard taking a bullet for his employer or a soccer player intercepting a pass are examples of this type of
 * behavior. Like pursuit, the owner must estimate where the two agents are going to be located at a time {@code t} in the future.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Interpose<T extends Vector<T>> extends Arrive<T> {

	protected Steerable<T> agentA;
	protected Steerable<T> agentB;
	protected float interpositionRatio;

	private T targetPosition;

	/** Creates an {@code Interpose} behavior for the specified owner and agents using the given deceleration and the midpoint
	 * between agents as the target.
	 * @param owner the owner of this behavior
	 * @param agentA the first agent
	 * @param agentB the other agent
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to arrive at the target. */
	public Interpose (Steerable<T> owner, Steerable<T> agentA, Steerable<T> agentB, float maxLinearAcceleration) {
		this(owner, agentA, agentB, 0.5f, maxLinearAcceleration);
	}

	/** Creates an {@code Interpose} behavior for the specified owner and agents using the the given deceleration and interposing
	 * ratio.
	 * @param owner the owner of this behavior
	 * @param agentA the first agent
	 * @param agentB the other agent
	 * @param interpositionRatio a number between 0 and 1 indicating the percentage of the distance between the 2 agents that the
	 *           owner should reach.
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to arrive at the target. */
	public Interpose (Steerable<T> owner, Steerable<T> agentA, Steerable<T> agentB, float interposingRatio,
		float maxLinearAcceleration) {
		super(owner, null, maxLinearAcceleration);
		this.agentA = agentA;
		this.agentB = agentB;
		this.interpositionRatio = interposingRatio;

		this.targetPosition = owner.newVector();
	}

	/** Returns the first agent. */
	public Steerable<T> getAgentA () {
		return agentA;
	}

	/** Sets the first agent. */
	public void setAgentA (Steerable<T> agentA) {
		this.agentA = agentA;
	}

	/** Returns the second agent. */
	public Steerable<T> getAgentB () {
		return agentB;
	}

	/** Sets the second agent. */
	public void setAgentB (Steerable<T> agentB) {
		this.agentB = agentB;
	}

	/** Returns the interposition ratio. */
	public float getInterpositionRatio () {
		return interpositionRatio;
	}

	/** Sets the interposition ratio.
	 * @param interpositionRatio a number between 0 and 1 indicating the percentage of the distance between the 2 agents that the
	 *           owner should reach. Especially, 0 is the position of agentA and 1 is the position of agentB. */
	public void setInterpositionRatio (float interpositionRatio) {
		this.interpositionRatio = interpositionRatio;
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// First we need to figure out where the two agents are going to be at
		// time T in the future. This is approximated by determining the time
		// taken by the owner to reach the desired point between the 2 agents
		// at the current time at the max speed. This desired point P is given by
		// P = posA + interpositionRatio * (posB - posA)
		targetPosition.set(agentB.getPosition()).sub(agentA.getPosition()).scl(interpositionRatio).add(agentA.getPosition());

		float timeToTargetPosition = owner.getPosition().dst(targetPosition) / maxSpeed;

		// Now we have the time, we assume that agent A and agent B will continue on a
		// straight trajectory and extrapolate to get their future positions.
		// Note that here we are reusing steering.linear vector as agentA future position
		// and targetPosition as agentB future position.
		steering.linear.set(agentA.getPosition()).mulAdd(agentA.getLinearVelocity(), timeToTargetPosition);
		targetPosition.set(agentB.getPosition()).mulAdd(agentB.getLinearVelocity(), timeToTargetPosition);

		// Calculate the target position between these predicted positions
		targetPosition.sub(steering.linear).scl(interpositionRatio).add(steering.linear);

		// Finally delegate to Arrive
		return arrive(steering, targetPosition);
	}

	/** Returns the target position. This is intended for debug purpose. */
	public T getTargetPosition () {
		return targetPosition;
	}

}
