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

/** A {@code RadiusProximity} elaborates any agents contained in the specified list that are within the radius of the owner.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class RadiusProximity<T extends Vector<T>> extends ProximityBase<T> {

	/** The radius of this proximity. */
	protected float radius;

	private int frameId;

	/** Creates a {@code RadiusProximity} for the specified owner, agents and radius.
	 * @param owner the owner of this proximity
	 * @param agents the agents
	 * @param radius the radius of the cone area */
	public RadiusProximity (Steerable<T> owner, Array<? extends Steerable<T>> agents, float radius) {
		super(owner, agents);
		this.radius = radius;
		this.frameId = 0;
	}

	/** Returns the radius of this proximity. */
	public float getRadius () {
		return radius;
	}

	/** Sets the radius of this proximity. */
	public void setRadius (float radius) {
		this.radius = radius;
	}

	@Override
	public int findNeighbors (ProximityCallback<T> callback) {
		int agentCount = agents.size;
		int neighborCount = 0;

		// Check current frame id to avoid repeating calculations
		// when this proximity is used by multiple group behaviors.
		if (this.frameId != AIUtils.getFrameId()) {
			// Save the frame id
			this.frameId = AIUtils.getFrameId();

			T ownerPosition = owner.getPosition();

			// Scan the agents searching for neighbors
			for (int i = 0; i < agentCount; i++) {
				Steerable<T> currentAgent = agents.get(i);

				// Make sure the agent being examined isn't the owner
				if (currentAgent != owner) {
					float squareDistance = ownerPosition.dst2(currentAgent.getPosition());

					// The bounding radius of the current agent is taken into account
					// by adding it to the range
					float range = radius + currentAgent.getBoundingRadius();

					// If the current agent is within the range, report it to the callback
					// and tag it for further consideration.
					if (squareDistance < range * range) {
						if (callback.reportNeighbor(currentAgent)) {
							currentAgent.setTagged(true);
							neighborCount++;
							continue;
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
