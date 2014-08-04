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

import com.badlogic.gdx.ai.steer.GroupBehavior;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback;
import com.badlogic.gdx.math.Vector;

/** {@code CollisionAvoidance} behavior steers the owner to avoid obstacles lying in its path. An obstacle is any object that can be
 * approximated by a circle (or sphere, if you are working in 3D).
 * <p>
 * This implementation uses collision prediction working out the closest approach of two agents and determining if their distance
 * at this point is less than the sum of their bounding radius. For avoiding groups of characters, averaging positions and
 * velocities do not work well with this approach. Instead, the algorithm needs to search for the character whose closest approach
 * will occur first and to react to this character only. Once this imminent collision is avoided, the steering behavior can then
 * react to more distant characters.
 * <p>
 * This algorithm works well with small and/or moving obstacles whose shape can be approximately represented by a center and a
 * radius.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class CollisionAvoidance<T extends Vector<T>> extends GroupBehavior<T> implements ProximityCallback<T> {

	/** The maximum acceleration that can be used to avoid collisions. */
	protected float maxLinearAcceleration;

	private float shortestTime;
	private Steerable<T> firstNeighbor;
	private float firstMinSeparation;
	private float firstDistance;
	private T firstRelativePosition;
	private T firstRelativeVelocity;
	private T relativePosition;
	private T relativeVelocity;

	/** Creates a {@code CollisionAvoidance} behavior for the specified owner, proximity and maximum linear acceleration.
	 * @param owner the owner of this behavior
	 * @param proximity the proximity of this behavior
	 * @param maxLinearAcceleration the maximum linear acceleration that can be used to avoid collisions. */
	public CollisionAvoidance (Steerable<T> owner, Proximity<T> proximity, float maxLinearAcceleration) {
		super(owner, proximity);
		this.maxLinearAcceleration = maxLinearAcceleration;

		this.firstRelativePosition = owner.newVector();
		this.firstRelativeVelocity = owner.newVector();

		this.relativeVelocity = owner.newVector();
	}

	@Override
	public SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		shortestTime = Float.POSITIVE_INFINITY;
		firstNeighbor = null;
		firstMinSeparation = 0;
		firstDistance = 0;
		relativePosition = steering.linear;

		// Take into consideration each neighbor to find the most imminent collision.
		int neighborCount = proximity.findNeighbors(this);

		// If we have no target, then return no steering acceleration
		if (neighborCount == 0 || firstNeighbor == null) return steering.setZero();

		// If we're going to hit exactly, or if we're already
		// colliding, then do the steering based on current position.
		if (firstMinSeparation <= 0 || firstDistance < owner.getBoundingRadius() + firstNeighbor.getBoundingRadius()) {
			relativePosition.set(firstNeighbor.getPosition()).sub(owner.getPosition());
		} else {
			// Otherwise calculate the future relative position
			relativePosition.set(firstRelativePosition).mulAdd(firstRelativeVelocity, shortestTime);
		}

		// Avoid the target
		// Notice that steerling.linear and relativePosition are the same vector
		relativePosition.nor().scl(-maxLinearAcceleration);

		// No angular acceleration
		steering.angular = 0f;

		// Output the steering
		return steering;
	}

	@Override
	public boolean reportNeighbor (Steerable<T> neighbor) {
		// Calculate the time to collision
		relativePosition.set(neighbor.getPosition()).sub(owner.getPosition());
		relativeVelocity.set(neighbor.getLinearVelocity()).sub(owner.getLinearVelocity());
		float relativeSpeed2 = relativeVelocity.len2();
		float timeToCollision = -relativePosition.dot(relativeVelocity) / relativeSpeed2;

		// If timeToCollision is negative, i.e. the owner is already moving away from the the neighbor,
		// or it's not the most imminent collision then no action needs to be taken.
		if (timeToCollision <= 0 || timeToCollision >= shortestTime) return false;

		// Check if it is going to be a collision at all
		float distance = relativePosition.len();
		float minSeparation = distance - (float)Math.sqrt(relativeSpeed2) * timeToCollision /* shortestTime */;
		if (minSeparation > owner.getBoundingRadius() + neighbor.getBoundingRadius()) return false;

		// Store most imminent collision data
		shortestTime = timeToCollision;
		firstNeighbor = neighbor;
		firstMinSeparation = minSeparation;
		firstDistance = distance;
		firstRelativePosition.set(relativePosition);
		firstRelativeVelocity.set(relativeVelocity);

		return true;
	}

	/** Returns the maximum linear acceleration that can be used to avoid collisions. */
	public float getMaxLinearAcceleration () {
		return maxLinearAcceleration;
	}

	/** Sets the maximum linear acceleration that can be used to avoid collisions. */
	public void setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
	}

}
