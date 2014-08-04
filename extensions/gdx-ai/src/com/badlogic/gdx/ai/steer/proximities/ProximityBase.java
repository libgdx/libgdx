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

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** {@code ProximityBase} is the base class for any concrete proximity based on an {@link Array} of agents.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public abstract class ProximityBase<T extends Vector<T>> implements Proximity<T> {

	/** The ownerof  this proximity. */
	protected Steerable<T> owner;

	/** The array of the agents handled by this proximity. */
	protected Array<? extends Steerable<T>> agents;

	/** Creates a {@code ProximityBase} for the specified owner and list of agents.
	 * @param owner the owner of this proximity
	 * @param agents the list of agents */
	public ProximityBase (Steerable<T> owner, Array<? extends Steerable<T>> agents) {
		this.owner = owner;
		this.agents = agents;
	}

	@Override
	public Steerable<T> getOwner () {
		return owner;
	}

	@Override
	public void setOwner (Steerable<T> owner) {
		this.owner = owner;
	}

	/** Returns the array of the agents that represent potential neighbors. */
	public Array<? extends Steerable<T>> getAgents () {
		return agents;
	}

	/** Sets the array of the agents that represent potential neighbors. */
	public void setAgents (Array<Steerable<T>> agents) {
		this.agents = agents;
	}

}
