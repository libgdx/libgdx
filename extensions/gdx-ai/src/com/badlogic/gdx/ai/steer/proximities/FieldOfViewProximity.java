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

package com.badlogic.gdx.ai.steer.proximities;

import com.badlogic.gdx.ai.AIUtils;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** {@code FieldOfViewProximity} emulates the peripheral vision of the owner as if it had eyes. Any agents contained in the
 * specified list that are within the field of view of the owner are considered owner's neighbors. The field of view is determined
 * by a radius and an angle in degrees.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class FieldOfViewProximity<T extends Vector<T>> extends ProximityBase<T> {

	/** The radius of this proximity. */
	protected float radius;

	/** The angle in radians of this proximity. */
	protected float angle;

	private float coneThreshold;
	private int frameId;
	private T ownerOrientation;
	private T toAgent;

	/** Creates a {@code FieldOfViewProximity} for the specified owner, agents and cone area defined by the given radius and angle
	 * in radians.
	 * @param owner the owner of this proximity
	 * @param agents the agents
	 * @param radius the radius of the cone area
	 * @param angle the angle in radians of the cone area */
	public FieldOfViewProximity (Steerable<T> owner, Array<? extends Steerable<T>> agents, float radius, float angle) {
		super(owner, agents);
		this.radius = radius;
		setAngle(angle);
		this.frameId = 0;
		this.ownerOrientation = owner.newVector();
		this.toAgent = owner.newVector();
	}

	/** Returns the radius of this proximity. */
	public float getRadius () {
		return radius;
	}

	/** Sets the radius of this proximity. */
	public void setRadius (float radius) {
		this.radius = radius;
	}

	/** Returns the angle of this proximity in radians. */
	public float getAngle () {
		return angle;
	}

	/** Sets the angle of this proximity in radians. */
	public void setAngle (float angle) {
		this.angle = angle;
		this.coneThreshold = (float)Math.cos(angle * 0.5f);
	}

	@Override
	public int findNeighbors (ProximityCallback<T> callback) {
		int neighborCount = 0;
		int agentCount = agents.size;

		// Check current frame id to avoid repeating calculations
		// when this proximity is used by multiple group behaviors.
		if (this.frameId != AIUtils.getFrameId()) {
			// Save the frame id
			this.frameId = AIUtils.getFrameId();

			T ownerPosition = owner.getPosition();

			// Transform owner orientation to a Vector
			owner.angleToVector(ownerOrientation, owner.getOrientation());

			// Scan the agents searching for neighbors
			for (int i = 0; i < agentCount; i++) {
				Steerable<T> currentAgent = agents.get(i);

				// Make sure the agent being examined isn't the owner
				if (currentAgent != owner) {

					toAgent.set(currentAgent.getPosition()).sub(ownerPosition);

					// The bounding radius of the current agent is taken into account
					// by adding it to the radius proximity
					float range = radius + currentAgent.getBoundingRadius();

					float toAgentLen2 = toAgent.len2();

					// Make sure the current agent is within the range.
					// Notice we're working in distance-squared space to avoid square root.
					if (toAgentLen2 < range * range) {

						// If the current agent is within the field of view of the owner,
						// report it to the callback and tag it for further consideration.
						if (ownerOrientation.dot(toAgent) > coneThreshold) {
							if (callback.reportNeighbor(currentAgent)) {
								currentAgent.setTagged(true);
								neighborCount++;
								continue;
							}
						}
					}
				}

				// Clear the tag
				currentAgent.setTagged(false);
			}
		} else {
			// Scan the agents searching for tagged neighbors
			for (int i = 0; i < agentCount; i++) {
				Steerable<T> currentAgent = agents.get(i);

				// Make sure the agent being examined isn't the owner and that
				// it's tagged.
				if (currentAgent != owner && currentAgent.isTagged()) {

					if (callback.reportNeighbor(currentAgent)) {
						neighborCount++;
					}
				}
			}
		}

		return neighborCount;
	}

}
