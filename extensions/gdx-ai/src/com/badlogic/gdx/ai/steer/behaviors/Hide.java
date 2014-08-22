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

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity;
import com.badlogic.gdx.math.Vector;

/** This behavior attempts to position a owner so that an obstacle is always between itself and the agent (the hunter) it's trying
 * to hide from. First the distance to each of these obstacles is determined. Then the owner uses the arrive behavior to steer
 * toward the closest one. If no appropriate obstacles can be found, no steering is returned.
 * <p>
 * You can use this behavior not only for situations where you require a non-player character (NPC) to hide from the player, like
 * find cover when fired at, but also in situations where you would like an NPC to sneak up on a player. For example, you can
 * create an NPC capable of stalking a player through a gloomy forest, darting from tree to tree.
 * <p>
 * It's worth mentioning that since this behavior can produce no steering acceleration it is commonly used with
 * {@link PrioritySteering}. For instance, to make the owner go away from the target if there are no obstacles nearby to hide
 * behind, just use {@link Hide} and {@link Evade} behaviors with this priority order.
 * <p>
 * There are a few interesting modifications you might want to make to this behavior:
 * <ul>
 * <li>With {@link FieldOfViewProximity} you can allow the owner to hide only if the target is within its field of view. This
 * tends to produce unsatisfactory performance though, because the owner starts to act like a child hiding from monsters beneath
 * the bed sheets, something like "if you can't see it, then it can't see you" effect making the owner look dumb. This can be
 * countered slightly though by adding in a time effect so that the owner will hide if the target is visible or if it has seen the
 * target within the last {@code N} seconds. This gives it a sort of memory and produces reasonable-looking behavior.</li>
 * <li>The same as above, but this time the owner only tries to hide if the owner can see the target and the target can see the
 * owner.
 * <li>It might be desirable to produce a force that steers the owner so that it always favors hiding positions that are to the
 * side or rear of the pursuer. This can be achieved easily using the dot product to bias the distances returned from the method
 * {@link #getHidingPosition}.</li>
 * <li>At the beginning of any of the methods a check can be made to test if the target is within a "threat distance" before
 * proceeding with any further calculations. If the target is not a threat, then the method can return immediately with zero
 * steering.</li>
 * </ul>
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @autor davebaol */
public class Hide<T extends Vector<T>> extends Arrive<T> implements ProximityCallback<T> {

	/** The proximity to find nearby obstacles. */
	protected Proximity<T> proximity;

	/** The distance from the boundary of the obstacle behind which to hide. */
	protected float distanceFromBoundary;

	private T toObstacle;
	private T bestHidingSpot;
	private float distance2ToClosest;

	/** Creates an {@code Hide} behavior for the specified owner.
	 * @param owner the owner of this behavior */
	public Hide (Steerable<T> owner) {
		this(owner, null);
	}

	/** Creates a {@code Hide} behavior for the specified owner and target.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior */
	public Hide (Steerable<T> owner, Steerable<T> target) {
		this(owner, target, null);
	}

	/** Creates a {@code Hide} behavior for the specified owner, target and proximity.
	 * @param owner the owner of this behavior
	 * @param target the target of this behavior
	 * @param proximity the proximity to find nearby obstacles */
	public Hide (Steerable<T> owner, Steerable<T> target, Proximity<T> proximity) {
		super(owner, target);
		this.proximity = proximity;

		this.bestHidingSpot = owner.newVector();
		this.toObstacle = null; // Set to null since we'll reuse steering.linear for this vector
	}

	@Override
	protected SteeringAcceleration<T> calculateSteering (SteeringAcceleration<T> steering) {
		// Initialize member variables used by the callback
		this.distance2ToClosest = Float.POSITIVE_INFINITY;
		this.toObstacle = steering.linear;

		// Find neighbors (the obstacles) using this behavior as callback
		int neighborsCount = proximity.findNeighbors(this);

		// If no suitable obstacles found return no steering otherwise use Arrive on the hiding spot
		return neighborsCount == 0 ? steering.setZero() : arrive(steering, bestHidingSpot);
	}

	@Override
	public boolean reportNeighbor (Steerable<T> neighbor) {
		// Calculate the position of the hiding spot for this obstacle
		T hidingSpot = getHidingPosition(neighbor.getPosition(), neighbor.getBoundingRadius(), target.getPosition());

		// Work in distance-squared space to find the closest hiding
		// spot to the owner
		float distance2 = hidingSpot.dst2(owner.getPosition());
		if (distance2 < distance2ToClosest) {
			distance2ToClosest = distance2;
			bestHidingSpot.set(hidingSpot);
			return true;
		}

		return false;
	}

	/** Returns the proximity used to find nearby obstacles. */
	public Proximity<T> getProximity () {
		return proximity;
	}

	/** Sets the proximity used to find nearby obstacles.
	 * @param proximity the proximity to set
	 * @return this behavior for chaining. */
	public Hide<T> setProximity (Proximity<T> proximity) {
		this.proximity = proximity;
		return this;
	}

	/** Returns the distance from the boundary of the obstacle behind which to hide. */
	public float getDistanceFromBoundary () {
		return distanceFromBoundary;
	}

	/** Sets the distance from the boundary of the obstacle behind which to hide.
	 * @param distanceFromBoundary the distance to set
	 * @return this behavior for chaining. */
	public Hide<T> setDistanceFromBoundary (float distanceFromBoundary) {
		this.distanceFromBoundary = distanceFromBoundary;
		return this;
	}

	/** Given the position of a target and the position and radius of an obstacle, this method calculates a position
	 * {@code distanceFromBoundary} away from the object's bounding radius and directly opposite the target. It does this by scaling
	 * the normalized "to obstacle" vector by the required distance away from the center of the obstacle and then adding the result
	 * to the obstacle's position.
	 * @param obstaclePosition
	 * @param obstacleRadius
	 * @param targetPosition
	 * @return the hiding position behind the obstacle. */
	protected T getHidingPosition (T obstaclePosition, float obstacleRadius, T targetPosition) {
		// Calculate how far away the agent is to be from the chosen
		// obstacle's bounding radius
		float distanceAway = obstacleRadius + distanceFromBoundary;

		// Calculate the normalized vector toward the obstacle from the target
		toObstacle.set(obstaclePosition).sub(targetPosition).nor();

		// Scale it to size and add to the obstacle's position to get
		// the hiding spot.
		return toObstacle.scl(distanceAway).add(obstaclePosition);
	}

	//
	// Setters overridden in order to fix the correct return type for chaining
	//

	@Override
	public Hide<T> setTarget (Steerable<T> target) {
		this.target = target;
		return this;
	}

	@Override
	public Hide<T> setMaxLinearAcceleration (float maxLinearAcceleration) {
		this.maxLinearAcceleration = maxLinearAcceleration;
		return this;
	}

	@Override
	public Hide<T> setMaxLinearSpeed (float maxLinearSpeed) {
		this.maxLinearSpeed = maxLinearSpeed;
		return this;
	}

	@Override
	public Hide<T> setArrivalTolerance (float arrivalTolerance) {
		this.arrivalTolerance = arrivalTolerance;
		return this;
	}

	@Override
	public Hide<T> setDecelerationRadius (float decelerationRadius) {
		this.decelerationRadius = decelerationRadius;
		return this;
	}

	@Override
	public Hide<T> setTimeToTarget (float timeToTarget) {
		this.timeToTarget = timeToTarget;
		return this;
	}

}
