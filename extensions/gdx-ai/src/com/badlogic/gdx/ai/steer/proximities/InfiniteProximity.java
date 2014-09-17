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

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** {@code InfiniteProximity} is likely the simplest type of Proximity one can imagine. All the agents contained in the specified
 * list are considered neighbors of the owner, excluded the owner itself (if it is part of the list).
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class InfiniteProximity<T extends Vector<T>> extends ProximityBase<T> {

	/** Creates a {@code InfiniteProximity} for the specified owner and list of agents.
	 * @param owner the owner of this proximity
	 * @param agents the list of agents */
	public InfiniteProximity (Steerable<T> owner, Array<? extends Steerable<T>> agents) {
		super(owner, agents);
	}

	@Override
	public int findNeighbors (ProximityCallback<T> callback) {
		int neighborCount = 0;
		int agentCount = agents.size;
		for (int i = 0; i < agentCount; i++) {
			Steerable<T> currentAgent = agents.get(i);

			// Make sure the agent being examined isn't the owner
			if (currentAgent != owner) {
				if (callback.reportNeighbor(currentAgent)) {
					neighborCount++;
				}
			}
		}

		return neighborCount;
	}

}
